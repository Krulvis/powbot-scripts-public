package org.powbot.krulvis.giantsfoundry

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.extensions.items.Bar

const val CRUCIBLE_BAR_COUNT_VARP = 3431
const val JOB_VARP = 3429
const val HEAT_VARP = 3433
const val ROOT = 754

const val MOULD_WIDGET_ROOT = 718

val METAL_ITEMS = mapOf(
	"bar" to 1,
	"scimitar" to 1,
	"longsword" to 1,
	"full helm" to 1,
	"sq shield" to 1,
	"claws" to 1,
	"warhammer" to 1,
	"battleaxe" to 1,
	"chainbody" to 2,
	"kiteshield" to 2,
	"2h sword" to 2,
	"platelegs" to 2,
	"plateskirt" to 2,
	"platebody" to 4
)

val METAL_ITEM_NAMES = METAL_ITEMS.keys.toTypedArray()

fun getCrucibleValueForItem(item: Item): Int {
	val key = METAL_ITEMS.keys.firstOrNull { item.name().contains(it, true) } ?: return 0
	return METAL_ITEMS[key]!!
}

fun Bar.crucibleInventoryCount(): Int {
	val invItems =
		Inventory.stream().nameContains(craftedBarItemPrefix(), itemName).nameContains(*METAL_ITEM_NAMES).toList()
	return invItems.sumOf { getCrucibleValueForItem(it) }
}

fun Bar.crucibleCount() = Varpbits.varpbit(CRUCIBLE_BAR_COUNT_VARP, 5 * BARS.indexOf(this), 31)

val BARS = arrayOf(Bar.BRONZE, Bar.IRON, Bar.STEEL, Bar.MITHRIL, Bar.ADAMANTITE, Bar.RUNITE)

fun crucibleBars(): Map<Bar, Int> {
	return BARS.associateWith { it.crucibleCount() }
}

fun currentTemp(): Int = Varpbits.varpbit(HEAT_VARP, 1023)

enum class BonusType {
	Broad,
	Flat,
	Heavy,
	Light,
	Narrow,
	Spiked;

	companion object {
		fun isBonus(component: Component) = values().any { it.name == component.text() }
		fun forComp(component: Component) = values().firstOrNull { it.name == component.text() }
		fun forText(text: String) = values().firstOrNull { it.name == text }
	}
}


enum class Action(
	val textureId: Int,
	val interactable: String,
	val tile: Tile,
	var min: Int,
	var max: Int,
	val activeBarComponentId: Int,
	val heats: Boolean = false
) {
	HAMMER(4442, "Trip hammer", Tile(3367, 11497), -1, -1, 21),
	GRIND(4443, "Grindstone", Tile(3364, 11492), -1, -1, 20, true),
	POLISH(4444, "Polishing wheel", Tile(3365, 11485), -1, -1, 19);

	fun canPerform() = currentTemp() in min + 4..max

	fun calculateMinMax() {
		val totalWidth = Widgets.component(ROOT, 8).width()
		val barComp = Widgets.component(ROOT, activeBarComponentId)
		min = (1000.0 / totalWidth * barComp.x()).toInt()
		max = (1000.0 / totalWidth * (barComp.x() + barComp.width())).toInt()
		debug("Calculated min=$min, max=$max for $name with totalWidth=$totalWidth, barX=${barComp.x()}, barWidth=${barComp.width()}")
	}

	fun getObj() = Objects.stream(30).type(GameObject.Type.INTERACTIVE).name(interactable).firstOrNull()

	companion object {
		fun forTexture(texture: Int) = values().firstOrNull { it.textureId == texture }
		fun calculateMinMax() = values().forEach { it.calculateMinMax() }
	}

}