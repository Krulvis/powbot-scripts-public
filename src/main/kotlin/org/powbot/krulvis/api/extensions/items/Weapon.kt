package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.mobile.rscache.loader.ItemLoader

const val DDS = "DDS"
const val ARCLIGHT = "ARCLIGHT"

enum class Weapon(val specialPercentage: Int, override val ids: IntArray, val statReducer: Boolean = false) :
	IEquipmentItem {
	NIL(101, intArrayOf(-1)),
	DDS(25, intArrayOf(1215, 1231, 5680, 5698)),
	ARCLIGHT(50, intArrayOf(19675), true),
	EMBERLIGHT(50, intArrayOf(29589), true),
	DRAGON_WARHAMMER(50, intArrayOf(13576), true)
	;

	override val itemName: String by lazy { ItemLoader.lookup(id)!!.name() }

	override val slot: Equipment.Slot = Equipment.Slot.MAIN_HAND
	override val stackable: Boolean = false

	fun canSpecial(): Boolean = Combat.specialPercentage() >= specialPercentage

	companion object {
		fun forId(id: Int): Weapon = values().firstOrNull { id in it.ids } ?: NIL

		fun List<EquipmentRequirement>.weapon() =
			forId(firstOrNull { it.slot == Equipment.Slot.MAIN_HAND }?.item?.id ?: -1)
	}
}