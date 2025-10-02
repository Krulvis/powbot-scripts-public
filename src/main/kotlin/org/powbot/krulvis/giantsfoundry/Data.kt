package org.powbot.krulvis.giantsfoundry

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.paint.PaintFormatters.round
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.extensions.items.Bar
import org.slf4j.LoggerFactory
import kotlin.math.ceil
import kotlin.math.roundToInt

const val CRUCIBLE_BAR_COUNT_VARP = 3431
const val JOB_VARP = 3429
const val FOUNDRY_VARP = 3433
const val ROOT = 754
const val ACTIVITIES_CHILD_INDEX = 75
const val ACTIVE_ACTIVITY_INDEX = 76

const val MOULD_WIDGET_ROOT = 718

val METAL_ITEMS = mapOf(
	"scimitar" to 1,
	"longsword" to 1,
	"full helm" to 1,
	"sq shield" to 1,
	"claws" to 1,
	"warhammer" to 2,
	"battleaxe" to 2,
	"chainbody" to 2,
	"kiteshield" to 2,
	"2h sword" to 2,
	"platelegs" to 2,
	"plateskirt" to 2,
	"platebody" to 4,
	"bar" to 1,
)

val METAL_ITEM_NAMES = METAL_ITEMS.keys.toTypedArray()

fun getCrucibleBarsForItem(item: Item): Int {
	val key = METAL_ITEMS.keys.firstOrNull { item.name().contains(it, true) } ?: return 0
	return METAL_ITEMS[key]!!
}

fun Bar.crucibleInventoryCount(): Int {
	val invItems =
		Inventory.stream().nameContains(craftedBarItemPrefix(), itemName).nameContains(*METAL_ITEM_NAMES).toList()
	return invItems.sumOf { getCrucibleBarsForItem(it) }
}

fun Bar.crucibleCount() = Varpbits.varpbit(CRUCIBLE_BAR_COUNT_VARP, 5 * BARS.indexOf(this), 31)

val BARS = arrayOf(Bar.BRONZE, Bar.IRON, Bar.STEEL, Bar.MITHRIL, Bar.ADAMANTITE, Bar.RUNITE)

fun crucibleBars(): Map<Bar, Int> {
	return BARS.associateWith { it.crucibleCount() }
}

fun currentHeat(): Int = Varpbits.varpbit(FOUNDRY_VARP, 1023)
fun currentProgress(): Int = Varpbits.varpbit(FOUNDRY_VARP, 10, 1023)

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

data class Stage(val action: Action, val start: Int, val end: Int) {

	fun isActive(progress: Int) = progress in start..end
	fun isActive() = isActive(currentProgress())

	companion object {
		private val logger = LoggerFactory.getLogger(Stage::class.java.simpleName)
		val Nil = Stage(Action.HAMMER, -1, -1)

		fun parseStages(): Array<Stage> {
			val stagesBar = Widgets.component(ROOT, ACTIVITIES_CHILD_INDEX)
			val children = stagesBar.components()
			val stageIdentifiers = children.filter { Action.forTexture(it.textureId()) != null }
			val stagesCount = stageIdentifiers.size
			val stageSize = 1000.0 / stagesCount
			logger.info("Parsing stages: stageCount=${stagesCount}, stageSize=${stageSize.round(2)}")
			val stages = stageIdentifiers.mapIndexed { i, stage ->
				//The texture is one child above the boundingComp
				Stage(
					Action.forTexture(stage.textureId())!!,
					ceil(stageSize * i).toInt(),
					(stageSize * i + stageSize).toInt()
				)
			}.toTypedArray()
			stages.forEachIndexed { i, s ->
				logger.info("Stage[$i]=${s.action}, start=${s.start}, end=${s.end}")
			}

			return stages
		}
	}
}

enum class Action(
	val textureId: Int,
	val interactable: String,
	val tile: Tile,
	var minHeat: Int,
	var maxHeat: Int,
	val activeBarComponentId: Int,
	val heats: Boolean = false
) {
	HAMMER(4442, "Trip hammer", Tile(3365, 11497, 0), -1, -1, 21),
	GRIND(4443, "Grindstone", Tile(3362, 11492, 0), -1, -1, 20, true),
	POLISH(4444, "Polishing wheel", Tile(3363, 11485, 0), -1, -1, 19);

	fun canPerform() = currentHeat() in minHeat + 4..maxHeat

	fun reset() {
		minHeat = -1
		maxHeat = -1
	}

	fun barComponent() = Widgets.component(ROOT, activeBarComponentId)

	fun calculateMinMax() {
		val totalWidth = Widgets.component(ROOT, 8).width()
		val barComp = barComponent()
		minHeat = (1000.0 / totalWidth * barComp.x()).toInt()
		maxHeat = (1000.0 / totalWidth * (barComp.x() + barComp.width())).toInt()
		debug("Calculated min=$minHeat, max=$maxHeat for $name with totalWidth=$totalWidth, barX=${barComp.x()}, barWidth=${barComp.width()}")
	}

	fun getObj(): GameObject {
		val obj = Objects.stream(tile, GameObject.Type.INTERACTIVE).name(interactable).first()
		if (!obj.valid()) return obj
		when (obj.name) {
			HAMMER.interactable -> obj.bounds(-112, 112, -114, 0, -32, 32)
			GRIND.interactable -> obj.bounds(-112, 112, -114, 0, -32, 32)
			POLISH.interactable -> obj.bounds(-72, 72, -164, 20, -72, 72)
		}
		return obj
	}

	companion object {
		fun forTexture(texture: Int) = values().firstOrNull { it.textureId == texture }
		fun calculateMinMax() = values().forEach { it.calculateMinMax() }
		fun reset() = values().forEach { it.reset() }
	}

}