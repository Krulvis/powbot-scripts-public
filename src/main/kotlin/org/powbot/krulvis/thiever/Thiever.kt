package org.powbot.krulvis.thiever

import com.google.common.eventbus.Subscribe
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.EquipmentItem
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.thiever.tree.branch.ShouldEat
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager
import org.powbot.mobile.service.ScriptUploader

@ScriptManifest(
	name = "krul Thiever",
	description = "Pickpockets any NPC",
	author = "Krulvis",
	version = "1.1.6",
	markdownFileName = "Thiever.md",
	scriptId = "e6043ead-e607-4385-b67a-a86dcf699204",
	category = ScriptCategory.Thieving
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Targets",
			description = "What NPC to pickpocket?",
			optionType = OptionType.NPC_ACTIONS,
			defaultValue = "{\"id\":0,\"interaction\":\"Pickpocket\",\"mouseX\":449,\"mouseY\":262,\"rawEntityName\":\"<col=ffff00>Master Farmer\",\"rawOpcode\":11,\"var0\":51,\"widgetId\":48,\"index\":79,\"level\":-1,\"name\":\"Master Farmer\",\"strippedName\":\"Master Farmer\"}"
		),
		ScriptConfiguration(
			name = "CenterTile",
			description = "Where to walk to after banking?",
			optionType = OptionType.TILE,
			defaultValue = "{\"floor\":0,\"x\":1249,\"y\":3750,\"rendered\":true}"
		),
		ScriptConfiguration(
			name = "MaxDistance",
			description = "How far can the npc be from center tile?",
			defaultValue = "15",
			optionType = OptionType.INTEGER,
		),
		ScriptConfiguration(
			name = "Food",
			description = "What food to use?",
			defaultValue = "SALMON",
			allowedValues = ["SHRIMP", "CAKES", "TROUT", "SALMON", "PEACH", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "POTATO_CHEESE", "MONKFISH", "SHARK", "KARAMBWAN"]
		),
		ScriptConfiguration(
			name = "Food amount",
			description = "How much food to take from bank?",
			defaultValue = "5",
			optionType = OptionType.INTEGER
		),
		ScriptConfiguration(
			name = "Droppables",
			description = "What loot should be dropped (split with ,)",
			defaultValue = "potato, onion, cabbage, tomato, marigold, nasturtium, rosemary, redberry, cadavaberry, dwellberry, guam, marrentill, tarromin, harralander",
			optionType = OptionType.STRING
		),
		ScriptConfiguration(
			name = "MinFreeSpace",
			description = "Bank at X free inventory slots left",
			defaultValue = "3",
			optionType = OptionType.INTEGER
		),
		ScriptConfiguration(
			name = "Left-click",
			description = "Force left-click on pickpocket?",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = "Prepare menu",
			description = "Open menu right after pickpocketing?",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = "Dodgy necklace",
			description = "Equip dodgy necklace?",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = "Stop on wandering",
			description = "Stop script when the NPC is further than max distance from center tile?",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = "Rogues Outfit",
			description = "Make sure Rogue's outfit is equipped",
			defaultValue = "true",
			optionType = OptionType.BOOLEAN
		),

	]
)
class Thiever : KrulScript() {
	override fun createPainter(): ATPaint<*> = ThieverPainter(this)

	override val rootComponent: TreeComponent<*> = ShouldEat(this)

	override fun onStart() {
		super.onStart()
//        NpcActionEvent()
		dodgyNeck = getOption("Dodgy necklace")
		logger.info("Droppables=[${droppableNames.joinToString()}] empty=${droppableNames.isEmpty()}")
		logger.info("FreeSlotCount=$freeSlots")
	}

	val centerTile by lazy { getOption<Tile>("CenterTile") }
	val food by lazy { Food.valueOf(getOption("Food")) }
	val freeSlots by lazy { getOption<Int>("MinFreeSpace") }
	val stopOnWander by lazy { getOption<Boolean>("Stop on wandering") }
	val maxDistance by lazy { getOption<Int>("MaxDistance") }
	val target by lazy { getOption<List<NpcActionEvent>>("Targets") }
	val roguesOutfit by lazy { getOption<Boolean>("Rogues Outfit") }
	val foodAmount by lazy { (getOption<Int>("Food amount")) }
	val prepare by lazy { (getOption<Boolean>("Prepare menu")) }
	val useMenu by lazy { !getOption<Boolean>("Left-click") }
	val droppableNames by lazy { getOption<String>("Droppables").split(",").map { it.trim() }.filterNot { it.isBlank() } }

	//Allow setting dodgyNeck to false after we're out of necklaces
	var dodgyNeck = false

	val dodgy = EquipmentItem(21143, Equipment.Slot.NECK)
	var nextPouchOpening = Random.nextInt(1, 28)
	var droppables: List<Item> = emptyList()
	val stunTimer = Timer(3000)

	fun getTarget(): Npc? {
		return Npcs.stream().within(centerTile, maxDistance).name(*target.map { it.name }.toTypedArray())
			.nearest(centerTile).firstOrNull()
	}

	val ROGUE_GEAR = listOf("Rogue mask", "Rogue top", "Rogue trousers", "Rogue gloves", "Rogue boots")
	fun getMissingRoguesPieces(): List<String> {
		val equipment = Equipment.get()
		return ROGUE_GEAR.filter { r -> equipment.none { e -> e.name() == r } }
	}

	fun stunned() = !stunTimer.isFinished()

	fun isDroppable(name: String) = droppableNames.any { name.contains(it, true) }

	@Subscribe
	fun onGameActionEvent(evt: GameActionEvent) {
		if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
			if (ScriptManager.state() == ScriptState.Running && prepare && Game.singleTapEnabled())
				getTarget()?.click()
		}
	}

	@Subscribe
	fun onGameActionEvent(evt: PlayerAnimationChangedEvent) {
		if (evt.player == me) {
			if (evt.animation == 420) {
				stunTimer.reset(Random.nextInt(3000, 4000))
			}
		}
	}

	@ValueChanged("Left-click")
	fun onLeftClick(leftClick: Boolean) {
		if (leftClick) {
			updateOption("Prepare menu", false, OptionType.BOOLEAN)
		}
		updateVisibility("Prepare menu", !leftClick)
	}

	private fun getTrackedPaintItem(id: Int): InventoryItemPaintItem? {
		return painter.paintBuilder.items.flatten().filterIsInstance<InventoryItemPaintItem>().firstOrNull { it.itemId == id }
	}

	@Subscribe
	fun onInventoryChange(evt: InventoryChangeEvent) {
		val id = evt.itemId
		val name = evt.itemName ?: return
		if (evt.quantityChange > 0 && getTrackedPaintItem(id) == null && !isDroppable(name)) {
			painter.paintBuilder.trackInventoryItems(id)
			painter.paintBuilder.items
			val tracked = getTrackedPaintItem(id)
			logger.info("Now tracking: ${ItemLoader.lookup(id)?.name()}, tracked=${tracked}, adding ${evt.quantityChange} as start")
			if (tracked != null) tracked.diff += evt.quantityChange
		}
	}

	fun coinPouch(): Item? = Inventory.stream().name("Coin pouch").firstOrNull()

	fun coinPouchCount() = coinPouch()?.stack ?: 0
}

fun main() {
	ScriptUploader().uploadAndStart("krul Thiever", "GIM", "emulator-5554", true, false)
//    Thiever().startScript("127.0.0.1", "GIM", false)
}