package org.powbot.krulvis.api.extensions.randoms

abstract class RandomHandler {
    abstract fun validate(): Boolean

    abstract fun execute(): Boolean
}