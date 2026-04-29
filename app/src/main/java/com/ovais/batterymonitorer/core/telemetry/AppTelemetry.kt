package com.ovais.batterymonitorer.core.telemetry

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AppTelemetry {
    fun breadcrumb(name: String, params: Map<String, String> = emptyMap()) {
        val analytics = runCatching { FirebaseAnalytics.getInstance(AppContextHolder.appContext) }.getOrNull()
        val bundle = Bundle().apply {
            params.forEach { (key, value) -> putString(key.take(40), value.take(100)) }
        }
        analytics?.logEvent(name.take(40), bundle)

        val crashlytics = FirebaseCrashlytics.getInstance()
        val kv = if (params.isEmpty()) "" else params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        crashlytics.log("breadcrumb:$name $kv")
    }

    fun recordException(throwable: Throwable, reason: String) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("exception_reason:$reason")
        crashlytics.recordException(throwable)
    }
}

