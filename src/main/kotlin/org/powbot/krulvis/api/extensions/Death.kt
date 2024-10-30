package org.powbot.krulvis.api.extensions

import org.powbot.api.Tile
import org.powbot.api.rt4.Varpbits

object Death {

	const val GRAVESTONE_TILE_VARP = 3916

	//	const val GRAVESTONE_ACTIVE_VARP = 843
	const val GRAVESTONE_TIMER_VARP = 1697

	fun timeRemaining() = Varpbits.varpbit(GRAVESTONE_TIMER_VARP) / 427.844
	fun gravestoneActive() = Varpbits.varpbit(GRAVESTONE_TIMER_VARP) > 0

	fun gravestoneTile(): Tile {
		val v = Varpbits.varpbit(GRAVESTONE_TILE_VARP)
		val x = (v.shr(14) and 0x3FFF)
		val y = (v and 0x3FFF)
		val z = v.shr(28) and 0x3
		return Tile(x, y, z)
	}
}