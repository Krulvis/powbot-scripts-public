package org.powbot.krulvis.api.extensions.items.container

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.Item

interface Openable : Item {

	val CLOSED_ID: Int
	val OPEN_ID: Int

	fun open(): Boolean {
		val invItem = Inventory.stream().id(CLOSED_ID).firstOrNull() ?: return true
		return invItem.interact("Open")
	}
}