package org.powbot.krulvis.api.script

import org.powbot.api.rt4.GroundItem

interface Looting {

	val ironmanLoot: MutableList<GroundItem>

	fun getGroundLoot(): List<GroundItem> = ironmanLoot
}