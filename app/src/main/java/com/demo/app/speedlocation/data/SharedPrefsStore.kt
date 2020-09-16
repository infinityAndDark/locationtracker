package com.demo.app.speedlocation.data

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.demo.app.speedlocation.MyApplication
import com.google.gson.Gson

object SharedPrefsHelper {
    fun save(key: String, data: String?) {
        var mData = data
        if (mData == null) mData = ""
        prefs.edit().putString(key, mData).apply()
    }

    fun get(key: String): String? {
        return prefs.getString(key, null)
    }

    private val prefs: SharedPreferences by lazy {
        MyApplication.GLOBAL_CONTEXT.getSharedPreferences(
            "my_prefs",
            MODE_PRIVATE
        )
    }
}

object SharedPrefsStore {
    private const val KEY_RECORD_HISTORY = "KEY_RECORD_HISTORY"
    private const val KEY_RECORD_SESSION_TIME = "KEY_RECORD_SESSION_TIME"
    fun getRunningSession(time: Long): RunningSession? {
        val data = SharedPrefsHelper.get("${KEY_RECORD_SESSION_TIME}_$time")
        if (data.isNullOrEmpty()) return null
        return Gson().fromJson(data, RunningSession::class.java)
    }

    fun saveRunningSession(data: RunningSession) {
        SharedPrefsHelper.save(
            "${KEY_RECORD_SESSION_TIME}_${data.time}",
            Gson().toJson(data, RunningSession::class.java)
        )
    }

    fun getRecordHistory(): RecordHistory {
        val data = SharedPrefsHelper.get(KEY_RECORD_HISTORY)
        if (data.isNullOrEmpty()) return RecordHistory(ArrayList())
        return Gson().fromJson(data, RecordHistory::class.java)
    }

    fun saveRecordHistory(history: RecordHistory) {
        SharedPrefsHelper.save(
            KEY_RECORD_HISTORY,
            Gson().toJson(history, RecordHistory::class.java)
        )
    }
}