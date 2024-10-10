package org.powbot.krulvis.api.antiban

import org.powbot.api.Random

class OddsModifier {

    var minModifier: Double = 0.0
        private set
    var maxModifier: Double = 0.0
        private set

    init {
        reset()
    }

    fun reset() {
        minModifier = Random.nextInt(75, 115) / 100.0
        maxModifier = Random.nextInt(85, 125) / 100.0
    }

    fun gaussian() = Random.nextDouble(minModifier, maxModifier)
}