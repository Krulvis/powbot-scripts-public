package org.powbot.krulvis.api.extensions.items

import org.powbot.mobile.rscache.loader.ItemLoader

class InventoryItem(override val ids: IntArray) : Item {

	constructor(id: Int) : this(intArrayOf(id))

	override val itemName: String by lazy { ItemLoader.lookup(id)!!.name() }
	override val stackable: Boolean by lazy { ItemLoader.lookup(id)!!.stackable() }

	override fun hasWith(): Boolean = inInventory()

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(countNoted)
	}

}