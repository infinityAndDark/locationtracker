package com.demo.app.speedlocation

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

abstract class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adapterJob: Job? = null
    fun cancelLaunch() {
        adapterJob?.cancel()
    }

    protected fun backgroundLaunch(block: suspend CoroutineScope.() -> Unit) {
        adapterJob?.cancel()
        adapterJob = CoroutineScope(Dispatchers.Default).launch { block() }
    }

    protected suspend fun <T> inMain(block: suspend CoroutineScope.() -> T) =
        withContext(Dispatchers.Main) { block() }

}