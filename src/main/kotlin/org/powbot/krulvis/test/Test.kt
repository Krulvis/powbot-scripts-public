package org.powbot.krulvis.test

import org.powbot.api.Tile


fun main() {
	val varp = 1040189392

	val c = varp
	val x = (c.shr(14) and 0x3FFF) + 1
	val y = (c and 0x3FFF) + 1
	val z = c.shr(28) and 0x3
	println("X=${x}, Y=${y}, Z=${z}")
}
