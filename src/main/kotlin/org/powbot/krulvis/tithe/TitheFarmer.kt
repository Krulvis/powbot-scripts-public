package org.powbot.krulvis.tithe

import com.google.common.eventbus.Subscribe
import org.powbot.api.*
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.PaintCheckboxChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.tithe.Data.NAMES
import org.powbot.krulvis.tithe.tree.branch.ShouldStart
import org.powbot.krulvis.tithe.tree.leaf.Refill
import org.powbot.krulvis.tithe.tree.leaf.Start
import kotlin.math.min

@ScriptManifest(
	name = "krul Tithe",
	description = "Tithe farming mini-game",
	author = "Krulvis",
	version = "1.1.5",
	scriptId = "97078671-3780-4a44-b488-36ef241686dd",
	markdownFileName = "Tithe.md",
	category = ScriptCategory.Farming,
	singleTapRequired = false
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Patches",
			description = "How many patches do you want to use? Max 20",
			optionType = OptionType.INTEGER,
			defaultValue = "16"
		),
	]
)
class TitheFarmer : KrulScript() {

	override val rootComponent: TreeComponent<*> = ShouldStart(this)

	val patchCount by lazy { min(getOption<Int>("Patches").toInt(), 20) }
	var lastPatch: Patch? = null
	var nextPatch: Patch? = null
	var lastRound = false
	var startPoints = -1
	var gainedPoints = 0
	var patches = listOf<Patch>()
	val chillTimer = Timer(5000)
	var planting = false

	override fun createPainter(): ATPaint<*> {
		return TithePainter(this)
	}


	override fun onStart() {
		super.onStart()
		val production = Production.ProductionTracker(Data.WATER_CAN_FULL, true)
		Production.trackers.add(production)
		Production.trackers.first { it.id == production.id }.lastChangeTime = System.currentTimeMillis() - 5000
	}

	val cornerPatchTile by lazy {
		val allPatches = Objects.stream(25, GameObject.Type.INTERACTIVE)
			.nameContains("Logavano", "Bologano", "Golovanova", "Tithe patch").list()
		val maxX = allPatches.minOf { it.tile().x() }
		val maxY = allPatches.maxOf { it.tile().y() }
		Tile(maxX, maxY, 0)
	}


	fun getPatchTiles(): List<Tile> {
		val tiles = mutableListOf(cornerPatchTile)
		val columns = mutableListOf<Tile>()
		tiles.add(Tile(tiles[0].x(), tiles[0].y() - 15))
		tiles.forEach {
			for (y in 0..9 step 3) {
				columns.add(Tile(it.x(), it.y() - y))
				columns.add(Tile(it.x() + 5, it.y() - y))
			}
		}

		val lastPatch = Tile(tiles[0].x() + 10, tiles[0].y() - 9)
		for (y in 0..9 step 3) {
			columns.add(Tile(lastPatch.x(), lastPatch.y() + y))
		}

		//Do 17 for the first couple rounds to skip the round where you do just 4 plants
		return columns.toList().subList(0, patchCount)
	}

	fun refreshPatches() {
		val startTimer = System.currentTimeMillis()
		val tiles = getPatchTiles()
		val onorderedPatches = tiles.map { tile ->
			Objects.stream(tile, GameObject.Type.INTERACTIVE).filtered { it.name().isNotEmpty() && it.name() != "null" }
				.first()
		}
		patches = tiles.mapIndexed { index, tile ->
			Patch(onorderedPatches.firstOrNull { it.tile() == tile } ?: GameObject.Nil, tile, index)
		}
		logger.info("Refreshed patches in =${System.currentTimeMillis() - startTimer}")
	}

	fun hasSeeds(): Boolean = getSeed() != Item.Nil

	override fun canBreak(): Boolean {
		return lastLeaf is Start || lastLeaf is Refill
	}

	fun getSeed() = Inventory.stream().id(*Data.SEEDS).first()

	fun seedCount(): Int {
		return Inventory.stream().id(*Data.SEEDS).count(true).toInt()
	}

	fun getPoints(): Int {
		val widget =
			Widgets.widget(241).components().firstOrNull { it.text().contains("Points:") } ?: return -1
		return widget.text().substring(8).toInt()
	}

	fun getWaterCount(): Int = Inventory.stream().id(*Data.WATER_CANS).list().sumOf { it.id() - 5332 }

	fun hasEnoughWater(): Boolean = getWaterCount() >= patchCount * 3

	@Subscribe
	fun onGameActionEvent(evt: GameActionEvent) {
		if (evt.rawOpcode == 4 || evt.opcode() == GameActionOpcode.InteractObject) {
			val tile = Tile(evt.var0 + 1, evt.widgetId + 1, 0).globalTile()
			if (NAMES.any { evt.rawEntityName.contains(it, true) }) {
				lastPatch = patches.first { it.tile == tile }
				logger.info("Interacted ${evt.interaction} on $lastPatch")
			}
		}
	}

	@ValueChanged("Smart Patches")
	fun onValueChange(smart: Boolean) {
		updateEnabled("Patches", !smart)
		if (smart) {
			updateOption("Patches", 16, OptionType.INTEGER)
		}
	}

	@Subscribe
	fun onCheckBoxEvent(e: PaintCheckboxChangedEvent) {
		if (e.checkboxId == "lastRound") {
			lastRound = e.checked
		}
	}

	fun interact(
		target: Interactive?,
		action: String,
		selectItem: Int = -1,
		useMenu: Boolean = true
	): Boolean {
		val t = target ?: return false
		val name = (t as Nameable).name()
		val pos = (t as Locatable).tile()
		val destination = Movement.destination()
		turnRunOn()
		if (!t.inViewport()
			|| (destination != pos && pos.distanceTo(if (destination == Tile.Nil) Players.local() else destination) > 12)
		) {
			logger.info("Walking before interacting... in viewport: ${t.inViewport()}")
			if (pos.matrix().onMap()) {
				Movement.step(pos)
			} else {
				LocalPathFinder.findWalkablePath(pos).traverse()
			}
		}
		val selectedId = Inventory.selectedItem().id()
		if (selectedId != selectItem) {
			Game.tab(Game.Tab.INVENTORY)
			if (selectItem > -1) {
				Inventory.stream().id(selectItem).firstOrNull()?.interact("Use")
			} else {
				Inventory.stream().id(selectedId).firstOrNull()?.click()
			}

		}
		val interactBool =
			if (name == null || name == "null" || name.isEmpty()) t.interact(action, useMenu) else t.interact(
				action,
				name,
				useMenu
			)
		return Condition.wait {
			Inventory.selectedItemIndex() == -1 || Inventory.selectedItem().id() == selectItem
		} && interactBool
	}

	fun turnRunOn(): Boolean {
		if (Movement.running()) {
			return true
		}
		if (Movement.energyLevel() >= Random.nextInt(1, 5)) {
			return Widgets.widget(Constants.MOVEMENT_MAP).component(Constants.MOVEMENT_RUN_ENERGY - 1).click()
		}
		return false
	}


}

fun main() {
	TitheFarmer().startScript(false)
}