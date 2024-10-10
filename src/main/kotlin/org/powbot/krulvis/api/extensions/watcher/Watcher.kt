package org.powbot.krulvis.api.extensions.watcher

import org.powbot.api.Events

abstract class Watcher {

    init {
        Events.register(this)
    }

    fun unregister() {
        try {
            Events.unregister(this)
        } catch (e: IllegalArgumentException) {
            //nothing, but it can throw this IllegalArgumentException if it wasn't registered
        }
    }
}