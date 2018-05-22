package com.memory4963.fileselector.Utils

import android.os.FileObserver

/**
 * Created by memory4963 on 2018/5/17.
 */
class FileListener(path: String, private val callback: ListenerCallback) : FileObserver(path, FileObserver.CREATE) {
    
    override fun onEvent(event: Int, path: String?) {
        if (path != null) {
            callback.handleMessage(path)
        }
    }
}

interface ListenerCallback {
    fun handleMessage(path: String)
}
