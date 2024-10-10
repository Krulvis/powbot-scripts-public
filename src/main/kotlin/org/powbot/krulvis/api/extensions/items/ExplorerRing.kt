package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Equipment

object ExplorerRing : IEquipmentItem {
	override val ids: IntArray = intArrayOf(13125, 13126, 13127, 13128)
	override val itemName: String = "Explorer's ring"
	override val stackable: Boolean = false

	private const val ALCHEMY_WIDGET = 483
	override val slot: Equipment.Slot = Equipment.Slot.RING

	fun alchemyWidgetOpen() = Components.stream(ALCHEMY_WIDGET).text("High Alchemy").first().visible()


	override fun hasWith(): Boolean {
		return getInventoryCount() >= 1 || getEquipmentCount() >= 1
	}

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount() + getEquipmentCount()
	}

	fun alchemyComponent() = Components.stream(ALCHEMY_WIDGET)
	fun isAlchemyOpen() = false

	fun alchemize(item: org.powbot.api.rt4.Item): Boolean {
		if (!hasWith()) return false

		if (!isAlchemyOpen()) {
			if (equip(true)) {

			}
		}
		if (isAlchemyOpen()) {
			val itemComponent = alchemyComponent().itemId(item.id).first()
			return itemComponent.interact("High-level alchemize")
		}
		return false
	}
}