package com.demo.app.speedlocation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.speedlocation.helper.registerEventBus
import com.demo.app.speedlocation.helper.unRegisterEventBus
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class MyFragment : Fragment() {
    private var newJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerEventBus(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unRegisterEventBus(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(message: Any?) {
        onReceivedEvent(message)
    }

    protected open fun onReceivedEvent(event: Any?) {

    }

    protected fun mainLaunch(block: suspend CoroutineScope.() -> Unit) {
        newJob?.cancel()
        newJob = CoroutineScope(Dispatchers.Main)
            .launch { block() }
    }

    protected suspend fun <T> inBackground(block: suspend CoroutineScope.() -> T) =
        withContext(Dispatchers.Default) { block() }

    protected fun cancelLaunch() {
        newJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelLaunch()
    }
}