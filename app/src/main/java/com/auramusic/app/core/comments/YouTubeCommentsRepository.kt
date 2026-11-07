package com.auramusic.app.core.comments

import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.domain.model.AuraCommentsPage
import com.auramusic.app.domain.model.AuraYouTubeComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject

/** Read-only InnerTube comments flow: watch-page comments tab token, then comments continuation. */
object YouTubeCommentsRepository {
    private const val ENDPOINT = "https://music.youtube.com/youtubei/v1/next?prettyPrint=false"
    private const val CLIENT_VERSION = "1.20250312.01.00"

    suspend fun firstPage(videoId: String): AuraCommentsPage = withContext(Dispatchers.IO) {
        require(videoId.isNotBlank()) { "A video id is required to load comments" }
        val watch = nextRequest(JSONObject().put("videoId", videoId))
        val token = findCommentsTabToken(watch)
            ?: return@withContext AuraCommentsPage(null, emptyList(), commentsDisabled = true)
        parsePage(nextRequest(JSONObject().put("continuation", token)))
    }

    suspend fun nextPage(continuation: String): AuraCommentsPage = withContext(Dispatchers.IO) {
        parsePage(nextRequest(JSONObject().put("continuation", continuation)))
    }

    private fun nextRequest(extra: JSONObject): JSONObject {
        val payload = JSONObject().put("context", JSONObject().put("client", JSONObject().apply {
            put("clientName", "WEB_REMIX")
            put("clientVersion", CLIENT_VERSION)
            put("hl", "en")
            put("gl", "US")
        }))
        extra.keys().forEach { key -> payload.put(key, extra.get(key)) }
        val request = Request.Builder().url(ENDPOINT)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36")
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com/")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()
        return SimpMusicExtractorEngine.httpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "YouTube comments request failed (${response.code})" }
            JSONObject(response.body?.string() ?: "{}")
        }
    }

    private fun findCommentsTabToken(root: JSONObject): String? {
        val watchRenderer = findObjects(root, "watchNextTabbedResultsRenderer").firstOrNull() ?: return null
        val tabs = watchRenderer.optJSONArray("tabs") ?: return null
        for (index in 0 until tabs.length()) {
            val tab = tabs.optJSONObject(index)?.optJSONObject("tabRenderer") ?: continue
            val section = tab.optJSONObject("content")?.optJSONObject("sectionListRenderer") ?: continue
            val continuations = section.optJSONArray("continuations") ?: continue
            for (i in 0 until continuations.length()) {
                val reload = continuations.optJSONObject(i)?.optJSONObject("reloadContinuationData") ?: continue
                reload.optString("continuation").takeIf(String::isNotBlank)?.let { return it }
            }
        }
        return null
    }

    private fun parsePage(root: JSONObject): AuraCommentsPage {
        val entities = mutableMapOf<String, JSONObject>()
        findObjects(root, "entityBatchUpdate").forEach { batch ->
            val mutations = batch.optJSONArray("mutations") ?: return@forEach
            for (i in 0 until mutations.length()) {
                val mutation = mutations.optJSONObject(i) ?: continue
                val key = mutation.optString("entityKey")
                val payload = mutation.optJSONObject("payload")?.optJSONObject("commentEntityPayload")
                if (key.isNotBlank() && payload != null) entities[key] = payload
            }
        }

        val comments = mutableListOf<AuraYouTubeComment>()
        findObjects(root, "commentThreadRenderer").forEach { thread ->
            val viewModel = thread.optJSONObject("commentViewModel")?.optJSONObject("commentViewModel")
            val commentKey = viewModel?.optString("commentKey").orEmpty()
            val payload = entities[commentKey]
            if (payload != null) {
                val properties = payload.optJSONObject("properties")
                val author = payload.optJSONObject("author")
                val toolbar = payload.optJSONObject("toolbar")
                val id = viewModel?.optString("commentId").orEmpty().ifBlank { properties?.optString("commentId").orEmpty() }
                if (id.isNotBlank()) comments += AuraYouTubeComment(
                    id = id,
                    author = author?.optString("displayName").orEmpty().ifBlank { "YouTube user" },
                    avatarUrl = author?.optString("avatarThumbnailUrl")?.takeIf(String::isNotBlank),
                    text = properties?.optJSONObject("content")?.optString("content").orEmpty(),
                    publishedTime = properties?.optString("publishedTime").orEmpty(),
                    likeCount = toolbar?.optString("likeCountNotliked").orEmpty()
                )
            } else {
                // Older InnerTube renderer is still used for some tracks and client responses.
                thread.optJSONObject("comment")?.optJSONObject("commentRenderer")?.toAuraComment()?.let(comments::add)
            }
        }
        if (comments.isEmpty()) findObjects(root, "commentRenderer").mapNotNullTo(comments) { it.toAuraComment() }

        val header = findObjects(root, "commentsHeaderRenderer").firstOrNull()
        val count = header?.optJSONObject("commentsCount")?.readText()
            ?: header?.optJSONObject("countText")?.readText()
        val continuation = findObjects(root, "continuationItemRenderer").firstNotNullOfOrNull { renderer ->
            renderer.optJSONObject("continuationEndpoint")?.optJSONObject("continuationCommand")?.optString("token")
                ?.takeIf(String::isNotBlank)
                ?: renderer.optJSONObject("button")?.optJSONObject("buttonRenderer")
                    ?.optJSONObject("command")?.optJSONObject("continuationCommand")?.optString("token")
                    ?.takeIf(String::isNotBlank)
        }
        return AuraCommentsPage(count, comments.distinctBy { it.id }, continuation)
    }

    private fun JSONObject.toAuraComment(): AuraYouTubeComment? {
        val id = optString("commentId").ifBlank { return null }
        val author = optJSONObject("authorText")?.readText().orEmpty().ifBlank { "YouTube user" }
        val text = optJSONObject("contentText")?.readText().orEmpty()
        val avatar = optJSONObject("authorThumbnail")?.optJSONArray("thumbnails")?.lastOrNullObject()?.optString("url")
        return AuraYouTubeComment(
            id = id,
            author = author,
            avatarUrl = avatar?.takeIf(String::isNotBlank),
            text = text,
            publishedTime = optJSONObject("publishedTimeText")?.readText().orEmpty(),
            likeCount = optJSONObject("voteCount")?.readText().orEmpty()
        )
    }

    private fun JSONObject?.readText(): String? {
        if (this == null) return null
        optString("simpleText").takeIf(String::isNotBlank)?.let { return it }
        return optJSONArray("runs")?.let { runs ->
            (0 until runs.length()).joinToString("") { runs.optJSONObject(it)?.optString("text").orEmpty() }
        }
    }

    private fun JSONArray.lastOrNullObject(): JSONObject? = optJSONObject(length() - 1)

    private fun findObjects(node: JSONObject, key: String): List<JSONObject> = buildList {
        node.keys().forEach { name ->
            val value = node.opt(name)
            when (value) {
                is JSONObject -> {
                    if (name == key) add(value)
                    addAll(findObjects(value, key))
                }
                is JSONArray -> for (i in 0 until value.length()) value.optJSONObject(i)?.let { addAll(findObjects(it, key)) }
            }
        }
    }
}
