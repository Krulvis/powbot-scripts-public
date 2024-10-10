package org.powbot.krulvis.spices

import org.powbot.api.Tile

sealed class SpiceSealed(val name: String, val tile: Tile) {

    companion object {
        const val BROWN_STR = "BROWN"

        object BROWN : SpiceSealed("BROWN", Tile(0, 0, 0))
    }
}

const val BROWN_STR = "Brown"
const val YELLOW_STR = "Yellow"

enum class Spice(val spiceName: String, val tile: Tile) {
    BROWN(BROWN_STR, Tile(0, 0)),
    YELLOW(YELLOW_STR, Tile(0, 0)),
    ;

    companion object {
        fun forConst(name: String): Spice? {
            return values().firstOrNull { it.spiceName == name }
        }
    }
}
