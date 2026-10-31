package com.auramusic.app.core.lyrics

import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.domain.model.LyricLine
import com.auramusic.app.domain.model.LyricWord
import com.auramusic.app.domain.model.LyricsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

object SimpMusicLyricsRepository {
    private val lyricsCache = ConcurrentHashMap<String, LyricsData>()

    suspend fun getLyrics(videoId: String, title: String, artist: String, durationSeconds: Int): LyricsData =
        withContext(Dispatchers.IO) {
            lyricsCache[videoId]?.let { return@withContext it }

            // 1. Primary: Official SimpMusic Lyrics API (https://api-lyrics.simpmusic.org/v1/{videoId})
            fetchFromSimpMusicApi(videoId)?.let { data ->
                lyricsCache[videoId] = data
                return@withContext data
            }

            // 2. Fallback: Direct LRCLIB API (https://lrclib.net/api/search)
            fetchFromLrcLib(videoId, title, artist, durationSeconds)?.let { data ->
                lyricsCache[videoId] = data
                return@withContext data
            }

            LyricsData(
                videoId = videoId,
                plainLyrics = "Instrumental or lyrics not yet indexed for $title — $artist.",
                syncedLines = emptyList(),
                source = "None"
            )
        }

    private fun fetchFromSimpMusicApi(videoId: String): LyricsData? {
        return try {
            val req = Request.Builder()
                .url("https://api-lyrics.simpmusic.org/v1/$videoId")
                .header("Accept", "application/json")
                .header("User-Agent", "SimpMusicLyrics/1.0.0")
                .build()

            SimpMusicExtractorEngine.httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val root = JSONObject(resp.body?.string() ?: return null)
                if (!root.optBoolean("success", false)) return null
                val dataArr = root.optJSONArray("data") ?: return null
                if (dataArr.length() == 0) return null

                // Pick item with richSyncLyrics first, otherwise syncedLyrics
                var bestObj: JSONObject = dataArr.getJSONObject(0)
                for (i in 0 until dataArr.length()) {
                    val item = dataArr.getJSONObject(i)
                    if (item.optString("richSyncLyrics").isNotBlank()) {
                        bestObj = item
                        break
                    }
                }

                val richLrc = bestObj.optString("richSyncLyrics")
                val syncedLrc = bestObj.optString("syncedLyrics")
                val plain = bestObj.optString("plainLyric")

                val rawToParse = if (richLrc.isNotBlank()) richLrc else syncedLrc
                val lines = if (rawToParse.isNotBlank()) parseLrcWithRichWords(rawToParse) else emptyList()
                val hasRomanized = lines.any { !it.romanizedText.isNullOrBlank() }

                LyricsData(
                    videoId = videoId,
                    plainLyrics = plain.ifBlank { lines.joinToString("\n") { it.text } },
                    syncedLines = lines,
                    source = if (richLrc.isNotBlank()) "SimpMusic RichSync (Word-by-Word)" else "SimpMusic Synced LRC",
                    hasRomanization = hasRomanized
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchFromLrcLib(videoId: String, title: String, artist: String, durationSeconds: Int): LyricsData? {
        return try {
            val cleanTitle = title.replace(Regex("\\(.*?\\)|\\[.*?]"), "").trim()
            val cleanArtist = artist.split(",", "ft.", "feat.").firstOrNull()?.trim() ?: artist
            val query = URLEncoder.encode("$cleanTitle $cleanArtist", "UTF-8")
            val req = Request.Builder()
                .url("https://lrclib.net/api/search?q=$query")
                .header("User-Agent", "AuraMusicKotlin/1.0.0")
                .build()

            SimpMusicExtractorEngine.httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val arr = JSONArray(resp.body?.string() ?: return null)
                if (arr.length() == 0) return null

                var chosen = arr.getJSONObject(0)
                for (i in 0 until arr.length()) {
                    val candidate = arr.getJSONObject(i)
                    if (candidate.optString("syncedLyrics").isNotBlank()) {
                        chosen = candidate
                        break
                    }
                }

                val synced = chosen.optString("syncedLyrics")
                val plain = chosen.optString("plainLyrics")
                val lines = if (synced.isNotBlank()) parseLrcWithRichWords(synced) else emptyList()
                if (lines.isEmpty() && plain.isBlank()) return null

                LyricsData(
                    videoId = videoId,
                    plainLyrics = plain.ifBlank { lines.joinToString("\n") { it.text } },
                    syncedLines = lines,
                    source = "LRCLIB Direct",
                    hasRomanization = lines.any { !it.romanizedText.isNullOrBlank() }
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLrcWithRichWords(lrcText: String): List<LyricLine> {
        val lineRegex = Regex("^\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)$")
        val wordTagRegex = Regex("<(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?>\\s*([^<]+)")

        data class RawLine(
            val startSec: Float,
            val rawContent: String
        )

        val rawLines = lrcText.lines().mapNotNull { line ->
            val match = lineRegex.find(line.trim()) ?: return@mapNotNull null
            val min = match.groupValues[1].toIntOrNull() ?: 0
            val sec = match.groupValues[2].toIntOrNull() ?: 0
            val msStr = match.groupValues[3].padEnd(3, '0')
            val ms = msStr.toIntOrNull() ?: 0
            val totalSec = min * 60f + sec + (ms / 1000f)
            RawLine(totalSec, match.groupValues[4].trim())
        }.sortedBy { it.startSec }

        if (rawLines.isEmpty()) return emptyList()

        return rawLines.mapIndexedNotNull { index, raw ->
            val nextLineSec = rawLines.getOrNull(index + 1)?.startSec ?: (raw.startSec + 4.5f)
            val lineEndSec = nextLineSec.coerceAtLeast(raw.startSec + 0.6f)

            val wordMatches = wordTagRegex.findAll(raw.rawContent).toList()
            val words = mutableListOf<LyricWord>()
            val cleanText: String

            if (wordMatches.isNotEmpty()) {
                val parsedWords = wordMatches.map { wm ->
                    val wMin = wm.groupValues[1].toIntOrNull() ?: 0
                    val wSec = wm.groupValues[2].toIntOrNull() ?: 0
                    val wMs = wm.groupValues[3].padEnd(3, '0').toIntOrNull() ?: 0
                    val wStart = wMin * 60f + wSec + (wMs / 1000f)
                    wStart to wm.groupValues[4].trim()
                }.filter { it.second.isNotEmpty() }

                for (wIdx in parsedWords.indices) {
                    val (wStart, wText) = parsedWords[wIdx]
                    val wEnd = parsedWords.getOrNull(wIdx + 1)?.first ?: lineEndSec
                    words.add(
                        LyricWord(
                            word = wText,
                            startTimeSec = wStart,
                            endTimeSec = wEnd.coerceAtLeast(wStart + 0.12f)
                        )
                    )
                }
                cleanText = parsedWords.joinToString(" ") { it.second }
            } else {
                cleanText = raw.rawContent.replace(Regex("<[^>]+>"), "").trim()
                if (cleanText.isNotEmpty()) {
                    val splitTokens = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }
                    val durationSpan = (lineEndSec - raw.startSec).coerceAtLeast(0.5f)
                    val step = durationSpan / splitTokens.size.coerceAtLeast(1)
                    splitTokens.forEachIndexed { tIdx, token ->
                        val wStart = raw.startSec + tIdx * step
                        words.add(
                            LyricWord(
                                word = token,
                                startTimeSec = wStart,
                                endTimeSec = wStart + step
                            )
                        )
                    }
                }
            }

            if (cleanText.isEmpty()) return@mapIndexedNotNull null

            LyricLine(
                timeSec = raw.startSec,
                endTimeSec = lineEndSec,
                text = cleanText,
                words = words,
                romanizedText = romanizeIfNonLatin(cleanText)
            )
        }
    }

    /**
     * Lightweight Romanizer for Korean Hangul, Japanese Kana, and Cyrillic lyrics.
     */
    private fun romanizeIfNonLatin(text: String): String? {
        var hasNonLatin = false
        val sb = StringBuilder()

        val initials = arrayOf(
            "g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s",
            "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
        )
        val medials = arrayOf(
            "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa",
            "wae", "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i"
        )
        val finals = arrayOf(
            "", "k", "k", "ks", "n", "nj", "nh", "t", "l", "lg", "lm",
            "lb", "ls", "lt", "lp", "lh", "m", "p", "bs", "t", "t", "ng",
            "t", "t", "k", "t", "p", "t"
        )

        for (ch in text) {
            val code = ch.code
            when {
                // Korean Hangul Syllables (AC00..D7A3)
                code in 0xAC00..0xD7A3 -> {
                    hasNonLatin = true
                    val index = code - 0xAC00
                    val initial = index / (21 * 28)
                    val medial = (index % (21 * 28)) / 28
                    val finalIdx = index % 28
                    sb.append(initials[initial]).append(medials[medial]).append(finals[finalIdx])
                }
                // Hiragana (3040..309F) or Katakana (30A0..30FF)
                code in 0x3040..0x30FF -> {
                    hasNonLatin = true
                    sb.append(KANA_MAP[ch] ?: ch)
                }
                else -> sb.append(ch)
            }
        }
        return if (hasNonLatin) sb.toString() else null
    }

    private val KANA_MAP = mapOf(
        'あ' to "a", 'い' to "i", 'う' to "u", 'え' to "e", 'お' to "o",
        'か' to "ka", 'き' to "ki", 'く' to "ku", 'け' to "ke", 'こ' to "ko",
        'さ' to "sa", 'し' to "shi", 'す' to "su", 'せ' to "se", 'そ' to "so",
        'た' to "ta", 'ち' to "chi", 'つ' to "tsu", 'て' to "te", 'と' to "to",
        'な' to "na", 'に' to "ni", 'ぬ' to "nu", 'ね' to "ne", 'の' to "no",
        'は' to "ha", 'ひ' to "hi", 'ふ' to "fu", 'へ' to "he", 'ほ' to "ho",
        'ま' to "ma", 'み' to "mi", 'む' to "mu", 'め' to "me", 'も' to "mo",
        'や' to "ya", 'ゆ' to "yu", 'よ' to "yo",
        'ら' to "ra", 'り' to "ri", 'る' to "ru", 'れ' to "re", 'ろ' to "ro",
        'わ' to "wa", 'を' to "wo", 'ん' to "n",
        'が' to "ga", 'ぎ' to "gi", 'ぐ' to "gu", 'げ' to "ge", 'ご' to "go",
        'ざ' to "za", 'じ' to "ji", 'ず' to "zu", 'ぜ' to "ze", 'ぞ' to "zo",
        'だ' to "da", 'ぢ' to "ji", 'づ' to "zu", 'で' to "de", 'ど' to "do",
        'ば' to "ba", 'び' to "bi", 'ぶ' to "bu", 'べ' to "be", 'ぼ' to "bo",
        'ぱ' to "pa", 'ぴ' to "pi", 'ぷ' to "pu", 'ぺ' to "pe", 'ぽ' to "po"
    )
}
