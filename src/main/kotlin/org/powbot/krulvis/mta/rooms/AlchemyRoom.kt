package org.powbot.krulvis.mta.rooms

import org.powbot.api.Color.GREEN
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.tree.branches.CanCastHA
import org.powbot.mobile.drawing.Rendering

object AlchemyRoom : MTARoom {
	override val WIDGET_ID = 194
	override val portalName: String = "Alchemists"

	override fun rootComponent(mta: MTA): TreeComponent<MTA> = CanCastHA(mta)

	override fun points(): Int {
		return super.points() + Inventory.stream().name("Coins").count(true).toInt() / 100
	}

	const val ITEM_VALUE_START_ID = 12

	var bestItem: Alchable = Alchable.NONE
	var order: List<Alchable> = emptyList()
	var cupboards: List<GameObject> = emptyList()

	fun paint(g: Rendering) {
		cupboards.forEachIndexed { i, it ->
			val item = order.getOrNull(i)
			it.tile.drawOnScreen("[${it.id}]: $item", outlineColor = GREEN)
		}
	}

	fun getCupboard(): GameObject = getAllCupboards()[order.indexOf(bestItem)]

	fun getAllCupboards(): List<GameObject> {
		cupboards = Objects.stream().name("Cupboard").action("Take-5").sortedBy { it.id }.toList()
		return cupboards
	}

	fun getItemsWorth(): List<Pair<Alchable, Int>> {
		val comps = Components.stream(WIDGET_ID).toList()
		val worth = comps.subList(ITEM_VALUE_START_ID, ITEM_VALUE_START_ID + 5).map { it.text().toInt() }
		return Alchable.values().zip(worth)
	}

	fun getBest(): Alchable = getItemsWorth().maxByOrNull { it.second }?.first ?: Alchable.NONE

	fun getDroppables(): List<Item> =
		Inventory.stream().name(*(Alchable.names - bestItem.itemName).toTypedArray()).toList()

	enum class Alchable(val itemName: String) {
		LEATHER_BOOTS("Leather boots"),
		ADAMANT_KITESHIELD("Adamant kiteshield"),
		ADAMANT_HELM("Adamant med helm"),
		EMERALD("Emerald"),
		RUNE_LONGSWORD("Rune longsword"),
		NONE("None")
		;

		fun inventoryItem(): Item = Inventory.stream().name(itemName).first()
		fun groundItem(): GroundItem = GroundItems.stream().name(itemName).nearest().first()

		companion object {
			val names = values().map { it.itemName }

			fun forName(itemName: String): Alchable = values().first { it.itemName == itemName }
		}
	}

}