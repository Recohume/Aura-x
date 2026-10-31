package com.auramusic.app

import android.app.Application
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.core.media.AuraPlayerManager

class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SimpMusicExtractorEngine.initExtractors()
        AuraPlayerManager.initialize(this)
    }
}
