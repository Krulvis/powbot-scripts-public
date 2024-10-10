package org.powbot.krulvis.miner

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.extensions.items.Ore.Companion.hasOre
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Data.TOP_POLY
import org.powbot.krulvis.miner.tree.branch.ShouldFixStrut
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
	name = "krul Miner",
	description = "Mines & banks anything, anywhere (supports motherlode)",
	author = "Krulvis",
	version = "1.4.9",
	scriptId = "04f61d39-3abc-420d-84f6-f39243cdf584",
	markdownFileName = "Miner.md",
	category = ScriptCategory.Mining
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			"Rocks",
			"Which rocks do you want to mine?\nMake sure the delete the pre-set by clicking the red trash bin if you want to mine other rocks.",
			optionType = OptionType.GAMEOBJECT_ACTIONS,
			//Motherlode < 72
			defaultValue = "[{\"id\":26663,\"interaction\":\"Mine\",\"mouseX\":615,\"mouseY\":282,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":49,\"var6\":-1,\"widgetId\":38,\"tile\":{\"floor\":0,\"x\":3729,\"y\":5678,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":545,\"mouseY\":224,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":49,\"var6\":-1,\"widgetId\":39,\"tile\":{\"floor\":0,\"x\":3729,\"y\":5679,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":600,\"mouseY\":231,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":49,\"var6\":-1,\"widgetId\":37,\"tile\":{\"floor\":0,\"x\":3729,\"y\":5677,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":648,\"mouseY\":271,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":48,\"var6\":-1,\"widgetId\":34,\"tile\":{\"floor\":0,\"x\":3728,\"y\":5674,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":602,\"mouseY\":246,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":48,\"var6\":-1,\"widgetId\":35,\"tile\":{\"floor\":0,\"x\":3728,\"y\":5675,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26661,\"interaction\":\"Mine\",\"mouseX\":636,\"mouseY\":177,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":46,\"var6\":-1,\"widgetId\":41,\"tile\":{\"floor\":0,\"x\":3726,\"y\":5681,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":522,\"mouseY\":235,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":45,\"var6\":-1,\"widgetId\":35,\"tile\":{\"floor\":0,\"x\":3725,\"y\":5675,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":478,\"mouseY\":262,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":45,\"var6\":-1,\"widgetId\":34,\"tile\":{\"floor\":0,\"x\":3725,\"y\":5674,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":394,\"mouseY\":355,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":44,\"var6\":-1,\"widgetId\":30,\"tile\":{\"floor\":0,\"x\":3724,\"y\":5670,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":412,\"mouseY\":344,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":44,\"var6\":-1,\"widgetId\":29,\"tile\":{\"floor\":0,\"x\":3724,\"y\":5669,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":600,\"mouseY\":270,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":30,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5670,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":498,\"mouseY\":219,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":32,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5672,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":544,\"mouseY\":218,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":30,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5670,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26663,\"interaction\":\"Mine\",\"mouseX\":541,\"mouseY\":203,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":31,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5671,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":621,\"mouseY\":227,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":28,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5668,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":661,\"mouseY\":226,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":26,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5666,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":629,\"mouseY\":217,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":28,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5668,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26663,\"interaction\":\"Mine\",\"mouseX\":573,\"mouseY\":230,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":27,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5667,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":516,\"mouseY\":221,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":43,\"var6\":-1,\"widgetId\":27,\"tile\":{\"floor\":0,\"x\":3723,\"y\":5667,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":486,\"mouseY\":241,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":43,\"var6\":-1,\"widgetId\":26,\"tile\":{\"floor\":0,\"x\":3723,\"y\":5666,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":486,\"mouseY\":264,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":43,\"var6\":-1,\"widgetId\":24,\"tile\":{\"floor\":0,\"x\":3723,\"y\":5664,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26663,\"interaction\":\"Mine\",\"mouseX\":491,\"mouseY\":248,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":43,\"var6\":-1,\"widgetId\":23,\"tile\":{\"floor\":0,\"x\":3723,\"y\":5663,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":477,\"mouseY\":266,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":43,\"var6\":-1,\"widgetId\":22,\"tile\":{\"floor\":0,\"x\":3723,\"y\":5662,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26661,\"interaction\":\"Mine\",\"mouseX\":487,\"mouseY\":280,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":44,\"var6\":-1,\"widgetId\":20,\"tile\":{\"floor\":0,\"x\":3724,\"y\":5660,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":529,\"mouseY\":180,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":48,\"var6\":-1,\"widgetId\":24,\"tile\":{\"floor\":0,\"x\":3728,\"y\":5664,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":557,\"mouseY\":178,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":48,\"var6\":-1,\"widgetId\":23,\"tile\":{\"floor\":0,\"x\":3728,\"y\":5663,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26661,\"interaction\":\"Mine\",\"mouseX\":588,\"mouseY\":205,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":47,\"var6\":-1,\"widgetId\":21,\"tile\":{\"floor\":0,\"x\":3727,\"y\":5661,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26662,\"interaction\":\"Mine\",\"mouseX\":641,\"mouseY\":225,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":48,\"var6\":-1,\"widgetId\":19,\"tile\":{\"floor\":0,\"x\":3728,\"y\":5659,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":26664,\"interaction\":\"Mine\",\"mouseX\":665,\"mouseY\":221,\"rawEntityName\":\"<col=ffff>Ore vein\",\"rawOpcode\":3,\"var0\":48,\"var6\":-1,\"widgetId\":18,\"tile\":{\"floor\":0,\"x\":3728,\"y\":5658,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"}]"
			//Motherlode >= 72
//            defaultValue = "[{\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5678,\"z\":0},\"x\":3756,\"y\":5678}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5679,\"z\":0},\"x\":3756,\"y\":5679}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3758,\"y\":5680,\"z\":0},\"x\":3758,\"y\":5680}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3758,\"y\":5681,\"z\":0},\"x\":3758,\"y\":5681}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3755,\"y\":5681,\"z\":0},\"x\":3755,\"y\":5681}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3755,\"y\":5682,\"z\":0},\"x\":3755,\"y\":5682}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3755,\"y\":5683,\"z\":0},\"x\":3755,\"y\":5683}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5684,\"z\":0},\"x\":3756,\"y\":5684}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3757,\"y\":5684,\"z\":0},\"x\":3757,\"y\":5684}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3758,\"y\":5685,\"z\":0},\"x\":3758,\"y\":5685}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3759,\"y\":5682,\"z\":0},\"x\":3759,\"y\":5682}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3761,\"y\":5681,\"z\":0},\"x\":3761,\"y\":5681}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3762,\"y\":5682,\"z\":0},\"x\":3762,\"y\":5682}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3762,\"y\":5683,\"z\":0},\"x\":3762,\"y\":5683}}]",
//            defaultValue = "[{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3756,\"y\":5678,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3756,\"y\":5679,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3758,\"y\":5680,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3758,\"y\":5681,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Examine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Depleted vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3755,\"y\":5681,\"rendered\":true},\"name\":\"Depleted vein\",\"strippedName\":\"Depleted vein\"},{\"id\":-1,\"interaction\":\"Examine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Depleted vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3755,\"y\":5682,\"rendered\":true},\"name\":\"Depleted vein\",\"strippedName\":\"Depleted vein\"},{\"id\":-1,\"interaction\":\"Examine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Depleted vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3755,\"y\":5683,\"rendered\":true},\"name\":\"Depleted vein\",\"strippedName\":\"Depleted vein\"},{\"id\":-1,\"interaction\":\"Examine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Depleted vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3756,\"y\":5684,\"rendered\":true},\"name\":\"Depleted vein\",\"strippedName\":\"Depleted vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3757,\"y\":5684,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3758,\"y\":5685,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3759,\"y\":5682,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3761,\"y\":5681,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3762,\"y\":5682,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"},{\"id\":-1,\"interaction\":\"Mine\",\"mouseX\":-1,\"mouseY\":-1,\"rawEntityName\":\"Ore vein\",\"rawOpcode\":3,\"var0\":-1,\"widgetId\":-1,\"tile\":{\"floor\":0,\"x\":3762,\"y\":5683,\"rendered\":true},\"name\":\"Ore vein\",\"strippedName\":\"Ore vein\"}]"
			//Alkharid 2 ores
//            defaultValue = "[{\"interaction\":\"Mine\",\"name\":\"Rocks\",\"tile\":{\"floor\":0,\"p\":{\"x\":3303,\"y\":3284,\"z\":0},\"x\":3303,\"y\":3284}}, {\"interaction\":\"Mine\",\"name\":\"Rocks\",\"tile\":{\"floor\":0,\"p\":{\"x\":3302,\"y\":3285,\"z\":0},\"x\":3302,\"y\":3285}}]"
		),
		ScriptConfiguration(
			"Bank ores",
			"Bank the ores you mine?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		),
		ScriptConfiguration(
			"Top level Hopper",
			"Use top level Hopper in MLM",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			"Deposit box",
			"Use depositbox",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			"Hop",
			"Hop from players?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			"Fast mine",
			"Don't sleep",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		),
	]
)
class Miner : KrulScript() {

	@ValueChanged("Bank ores")
	fun onBankOresValue(bank: Boolean) {
		if (!bank) updateOption("Deposit box", false, OptionType.BOOLEAN)
		updateVisibility("Deposit box", bank)
	}

	val rockLocations by lazy {
		getOption<List<GameObjectActionEvent>>("Rocks").map { it.tile }
	}

	val bankOres by lazy { getOption<Boolean>("Bank ores") }
	val useDepositBox by lazy { getOption<Boolean>("Deposit box") }
	val fastMine by lazy { getOption<Boolean>("Fast mine") }
	val hopFromPlayers by lazy { getOption<Boolean>("Hop") }
	val topLevelHopper by lazy { getOption<Boolean>("Top level Hopper") }
	val mineDelay = DelayHandler(2000, oddsModifier, "MineDelay")
	var lastPayDirtDrop = 0L
	var waterskins = false

	override fun createPainter(): ATPaint<*> = MinerPainter(this)

	override val rootComponent: TreeComponent<*> = ShouldFixStrut(this)

	/**
	 * Used to get the amount of loot in the motherload sack
	 */
	fun getMotherlodeCount(): Int = (Varpbits.varpbit(375) and 65535) / 256

	fun getMotherlodeCapacity() = (if (Varpbits.varpbit(5556) == 1) 189 else 108) - 27

	var shouldEmptySack = false

	/**
	 * Get the sack in the motherload mine
	 */
	fun getSack() = Objects.stream(50).type(GameObject.Type.FLOOR_DECORATION).name("Sack").action("Search").findFirst()

	fun getBrokenStrut() =
		Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Broken strut").nearest().firstOrNull()

	fun inTopFloorAreas(): Boolean {
		return inTopFloorAreas(Players.local().tile())
	}

	fun inTopFloorAreas(t: Tile): Boolean {
		return TOP_POLY.contains(t)
	}

	fun getBestRock(): GameObject? {
		val rocks = Objects.stream(50).filtered {
			it.tile() in rockLocations && it.hasOre()
		}.nearest().toList()

		val calcifiedRocks =
			rocks.filter { it.name == "Calcified rocks" }.map { Pair(it, Objects.stream(it.tile, 0).count()) }
				.firstOrNull { it.second == 3L }
		return calcifiedRocks?.first ?: rocks.firstOrNull()
	}

	val nearHopper = Tile(3748, 5673, 0)
	val northOfLadder = Tile(3755, 5675, 0)
	val ladderTile = Tile(3755, 5674, 0)
	fun escapeTopFloor(): Boolean {
		val pos = Players.local().tile()
		if (inTopFloorAreas() && LocalPathFinder.findPath(pos, nearHopper, true).isEmpty()) {
			if (northOfLadder.distance() <= 5 && LocalPathFinder.findPath(pos, northOfLadder, true).isNotEmpty()) {
				if (ladderTile.matrix().interact("Climb")) {
					return waitFor {
						LocalPathFinder.findPath(Players.local().tile(), nearHopper, true).isNotEmpty()
					}
				}
			} else {
				logger.info("Walking to top of ladder first...")
				Movement.walkTo(northOfLadder)
			}
			return false
		}
		return true
	}

	//Cam Torum stuff
	fun inCamTorum() = Tile(1460, 9545, 1).distance() < 150

	val BANK_TILE_CAM_TORUM = Tile(1454, 9566, 1)
	val tilesToCamTorumMine = listOf(
		BANK_TILE_CAM_TORUM, Tile(1448, 9567, 1), Tile(1449, 9562, 1),
		Tile(1456, 9562, 1), Tile(1462, 9562, 1), Tile(1467, 9562, 1), Tile(1473, 9562, 1),
		Tile(1478, 9562, 1), Tile(1483, 9562, 1), Tile(1488, 9562, 1), Tile(1493, 9559, 1),
		Tile(1497, 9554, 1), Tile(1501, 9551, 1), Tile(1503, 9545, 1), Tile(1507, 9540, 1)
	)

	//
	@com.google.common.eventbus.Subscribe
	fun actionListener(gae: GameActionEvent) {
		if (gae.interaction == "Deposit worn items") {
			if (waitFor(5000) { !Inventory.containsOneOf(*Data.TOOLS) && !Equipment.containsOneOf(*Data.TOOLS) }) {
				Notifications.showNotification("Accidentally deposited equipment, stopping script")
				ScriptManager.stop()
			}
		}
	}
}

fun main() {
	Miner().startScript("127.0.0.1", "GIM", false)
}