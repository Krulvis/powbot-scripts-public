package org.powbot.krulvis.fighter

import com.google.common.eventbus.Subscribe
import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.paint.TextPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.items.ITeleportItem
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.requirements.ItemRequirement
import org.powbot.krulvis.api.extensions.teleports.*
import org.powbot.krulvis.api.extensions.teleports.Teleport.Companion.DISABLE_TELEPORTS_OPTION
import org.powbot.krulvis.api.extensions.teleports.Teleport.Companion.USE_WEB_OPTION
import org.powbot.krulvis.api.extensions.teleports.poh.LUNAR_ISLE_HOUSE_PORTAL
import org.powbot.krulvis.api.extensions.teleports.poh.openable.*
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.UniqueLootTracker
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.fighter.Defender.currentDefenderIndex
import org.powbot.krulvis.fighter.Superior.Companion.superior
import org.powbot.krulvis.fighter.tree.branch.ShouldStop
import kotlin.math.floor
import kotlin.random.Random


//<editor-fold desc="ScriptManifest">
@ScriptManifest(
	name = "krul Fighter",
	description = "Fights anything, anywhere. Supports defender collecting.",
	author = "Krulvis",
	version = "1.5.8",
	markdownFileName = "Fighter.md",
	scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
	category = ScriptCategory.Combat,
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			WARRIOR_GUILD_OPTION, "Collect defenders in the warrior guild",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			INVENTORY_OPTION, "What should your inventory look like?",
			optionType = OptionType.INVENTORY
		),
		ScriptConfiguration(
			PRAY_AT_ALTAR_OPTION, "Pray at nearby altar.",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			EQUIPMENT_OPTION, "What gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = true
		),
		ScriptConfiguration(
			MONSTERS_OPTION,
			"Click the NPC's you want to kill",
			optionType = OptionType.NPC_ACTIONS
		),
		ScriptConfiguration(
			MONSTER_AUTO_DESTROY_OPTION, "Select if monster is finished off (Gargoyles)",
			OptionType.BOOLEAN
		),
		ScriptConfiguration(
			RADIUS_OPTION, "Kill radius", optionType = OptionType.INTEGER, defaultValue = "10"
		),
		ScriptConfiguration(
			HOP_FROM_PLAYERS_OPTION, "Do you want to hop from players?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			PLAYER_HOP_COUNT_OPTION, "At how many players in radius do you want to hop?",
			optionType = OptionType.INTEGER, defaultValue = "1", visible = false
		),
		ScriptConfiguration(
			USE_SAFESPOT_OPTION, "Do you want to force a safespot?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			SAFESPOT_RADIUS, "Safespot allowed radius",
			optionType = OptionType.INTEGER, defaultValue = "0", visible = false
		),
		ScriptConfiguration(
			WALK_BACK_TO_SAFESPOT_OPTION,
			"Walk to safespot after attacking?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false",
			visible = false
		),
		ScriptConfiguration(
			CENTER_TILE_OPTION, "Get safespot/center tile",
			optionType = OptionType.TILE
		),
		ScriptConfiguration(
			WAIT_FOR_LOOT_OPTION, "Wait for loot after kill?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			IRONMAN_DROPS_OPTION, description = "Only pick up your own drops.",
			optionType = OptionType.BOOLEAN, defaultValue = "true"
		),
		ScriptConfiguration(
			LOOT_PRICE_OPTION, "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
		),
		ScriptConfiguration(
			LOOT_OVERRIDES_OPTION,
			"Separate items with \",\" Start with \"!\" to never loot",
			optionType = OptionType.STRING,
			defaultValue = "Long bone, curved bone, clue, totem, !blue dragon scale, Scaly blue dragonhide, toadflax, irit, avantoe, kwuarm, snapdragon, cadantine, lantadyme, dwarf weed, torstol"
		),
		ScriptConfiguration(
			BURY_BONES_OPTION, "Bury, Scatter or Offer bones&ashes.",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),

		ScriptConfiguration(USE_CANNON_OPTION, "Use a cannon?", OptionType.BOOLEAN, "false"),
		ScriptConfiguration(CANNON_TILE_OPTION, "Where to place cannon?", OptionType.TILE, visible = false),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION,
			"Teleport to bank",
			optionType = OptionType.STRING,
			defaultValue = EDGEVILLE_MOUNTED_GLORY,
			allowedValues = [USE_WEB_OPTION, DISABLE_TELEPORTS_OPTION, ROYAL_SEED_POD, EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD,
				FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX, LUNAR_ISLE_HOUSE_PORTAL]
		),
		ScriptConfiguration(
			MONSTER_TELEPORT_OPTION,
			"Teleport to Monsters",
			optionType = OptionType.STRING,
			defaultValue = USE_WEB_OPTION,
			allowedValues = [USE_WEB_OPTION, DISABLE_TELEPORTS_OPTION, EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX,
				CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX, LUNAR_ISLE_HOUSE_PORTAL, POISON_WASTE_SPIRIT_TREE_POH,
				STRONGHOLD_SLAYER, FREMENNIK_SLAYER, MORYTANIA_SLAYER, MOUNT_KARUULM_BLESSING, FAIRY_RING_CKS, LAVA_MAZE_BURNING, BANDIT_CAMP_BURNING]
		)
	]
)
//</editor-fold>
class Fighter : KillerScript(), UniqueLootTracker {

	override fun createPainter(): ATPaint<*> = FighterPainter(this)

	val useTeleportsToBank by lazy { getOption<String>(BANK_TELEPORT_OPTION) != DISABLE_TELEPORTS_OPTION }
	val useTeleportsToMonsters by lazy { getOption<String>(MONSTER_TELEPORT_OPTION) != DISABLE_TELEPORTS_OPTION }

	override val rootComponent: TreeComponent<*> = ShouldConsume(this, ShouldStop(this))
	override fun onStart() {
		super.onStart()
		Defender.lastDefenderIndex = currentDefenderIndex()
		if (prayAtNearbyAltar) {
			ShouldSipPotion.skippingPotions.addAll(listOf(Potion.PRAYER, Potion.SUPER_RESTORE))
		}

		val bankTeleportItemReqs =
			bankTeleport.teleport?.requirements?.filterIsInstance<ItemRequirement>() ?: emptyList()
		val monsterTeleportItemReqs =
			monsterTeleport.teleport?.requirements?.filterIsInstance<ItemRequirement>() ?: emptyList()
		val teleportItemReqs = (bankTeleportItemReqs + monsterTeleportItemReqs).groupingBy { it.item }.eachCount()
		requiredInventory.filter { it.item is ITeleportItem }.forEach {
			if (it.item in teleportItemReqs.keys) {
				it.amount = teleportItemReqs[it.item]!!
			}
		}
	}

	//<editor-fold desc="UISubscribers">
	@ValueChanged(WARRIOR_GUILD_OPTION)
	fun onWGChange(inWG: Boolean) {
		if (inWG) {
			updateOption(USE_SAFESPOT_OPTION, false, OptionType.BOOLEAN)
			updateOption(CENTER_TILE_OPTION, Defender.killSpot(), OptionType.TILE)
			val npcAction = NpcActionEvent(
				0, 0, 10, 13729, 0,
				"Attack", "<col=ffff00>Cyclops<col=40ff00>  (level-106)",
				447, 447, -1
			)
			updateOption(MONSTERS_OPTION, listOf(npcAction), OptionType.NPC_ACTIONS)
			updateOption(RADIUS_OPTION, 25, OptionType.INTEGER)
		}
	}

	@ValueChanged(USE_CANNON_OPTION)
	fun onUseCannon(useCannon: Boolean) {
		updateVisibility(CANNON_TILE_OPTION, useCannon)
	}


	@ValueChanged(USE_SAFESPOT_OPTION)
	fun onSafeSpotChange(useSafespot: Boolean) {
		updateVisibility(WALK_BACK_TO_SAFESPOT_OPTION, useSafespot)
		updateVisibility(SAFESPOT_RADIUS, useSafespot)
	}

	@ValueChanged(HOP_FROM_PLAYERS_OPTION)
	fun onHopFromPlayersChange(hopFromPlayers: Boolean) {
		updateVisibility(PLAYER_HOP_COUNT_OPTION, hopFromPlayers)
	}


	//</editor-fold desc="UISubscribers">


	//Warrior guild
	val warriorTokens = 8851
	val warriorGuild by lazy { getOption<Boolean>(WARRIOR_GUILD_OPTION) }

	//Inventory
	private val inventoryOptions by lazy { getOption<Map<Int, Int>>(INVENTORY_OPTION) }
	val requiredInventory by lazy { inventoryOptions.map { InventoryRequirement(it.key, it.value) } }

	//Equipment
	fun getEquipment(optionKey: String): List<EquipmentRequirement> {
		val option = getOption<Map<Int, Int>>(optionKey)
		return option.map {
			EquipmentRequirement(
				it.key,
				it.value,
				1
			)
		}
	}

	val equipment by lazy { getEquipment(EQUIPMENT_OPTION) }

	val ammo by lazy {
		equipment.firstOrNull { it.slot == Slot.QUIVER }
	}
	override val ammoIds by lazy { intArrayOf(ammo?.item?.id ?: -1) }

	val teleportEquipments by lazy {
		equipment.mapNotNull { TeleportEquipment.getTeleportEquipment(it.item.id) }
	}

	//Loot
	var superiorsSpawned = 0
	var superiorsKilled = 0
	val ironman by lazy { getOption<Boolean>(IRONMAN_DROPS_OPTION) }
	val waitForLootAfterKill by lazy { getOption<Boolean>(WAIT_FOR_LOOT_OPTION) }
	val minLootPrice by lazy { getOption<Int>(LOOT_PRICE_OPTION) }
	val lootNameOptions by lazy {
		val names = getOption<String>(LOOT_OVERRIDES_OPTION).split(",")
		val trimmed = mutableListOf<String>()
		names.forEach { trimmed.add(it.trim().lowercase()) }
		trimmed
	}
	val lootNames by lazy {
		val names = lootNameOptions.filterNot { it.startsWith("!") }.toMutableList()
		if (ammo != null) {
			names.add(ammo!!.item.itemName)
		}
		names.add("imbued heart")
		names.add("hydra's")
		names.add("visage")
		names.add("brimstone key")
		names.add("ancient shard")
		names.add("basilisk jaw")
		logger.info("Looting: [${names.joinToString()}]")
		names.toList()
	}
	val neverLoot by lazy {
		val trimmed = mutableListOf<String>()
		lootNameOptions
			.filter { it.startsWith("!") }
			.forEach { trimmed.add(it.replace("!", "")) }
		logger.info("Not looting: [${trimmed.joinToString()}]")
		trimmed
	}


	override fun GroundItem.isLoot() = isLoot(stackSize())

	private fun GenericItem.isLoot(amount: Int): Boolean {
		if (warriorGuild && id() in Defender.defenders) return true
		val nameLC = name().lowercase()
		return !neverLoot.contains(nameLC) &&
			(lootNames.any { ln -> nameLC.contains(ln) } || getPrice() * amount >= minLootPrice)
	}


	override fun getGroundLoot(): List<GroundItem> =
		if (ironman) ironmanLoot else GroundItems.stream().within(centerTile, killRadius).filtered { it.isLoot() }
			.toList()

	//Banking option
	var forcedBanking = false
	var lastTrip = false
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }


	//monsters Killing spot
	private val monsters by lazy {
		getOption<List<NpcActionEvent>>(MONSTERS_OPTION).map { it.name }
	}
	val monsterTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }
	val aggressionTimer = Timer(15 * 60 * 1000)

	private val monsterNames: List<String> get() = if (superiorActive) SUPERIORS else monsters
	fun nearbyMonsters(): List<Npc> =
		Npcs.stream().within(centerTile, killRadius.toDouble()).name(*monsterNames.toTypedArray()).nearest().list()

	private fun Npc.attackingOtherPlayer(): Boolean {
		val interacting = interacting()
		return interacting is Player && interacting != Players.local()
	}

	fun target(): Npc {
		val local = Players.local()
		val nearbyMonsters =
			nearbyMonsters().filterNot {
				it.healthBarVisible() && (it.attackingOtherPlayer() || it.healthPercent() == 0)
			}.sortedBy { it.distance() }
		val attackingMe =
			nearbyMonsters.firstOrNull { it.interacting() is Npc || it.interacting() == local }
		return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() } ?: Npc.Nil
	}

	//Safespot options
	val killRadius by lazy { getOption<Int>(RADIUS_OPTION) }
	val safespotRadius by lazy { getOption<Int>(SAFESPOT_RADIUS) }
	val useSafespot by lazy { getOption<Boolean>(USE_SAFESPOT_OPTION) }
	val walkBack by lazy { getOption<Boolean>(WALK_BACK_TO_SAFESPOT_OPTION) }
	val centerTile by lazy {
		val tile = getOption<Tile>(CENTER_TILE_OPTION)
		if (tile == Tile.Nil) {
			Notifications.showNotification("Using current tile as you forgot to set a tile")
			return@lazy Players.local().tile()
		}
		return@lazy tile
	}
	val buryBones by lazy { getOption<Boolean>(BURY_BONES_OPTION) }
	fun shouldReturnToSafespot() = useSafespot
		&& centerTile.distance() > safespotRadius
		&& (walkBack || Players.local().healthBarVisible())
		&& projectiles.none { it.destination() == centerTile }

	//Hop from players options
	val hopFromPlayers by lazy { getOption<Boolean>(HOP_FROM_PLAYERS_OPTION) }
	val playerHopAmount by lazy { getOption<Int>(PLAYER_HOP_COUNT_OPTION) }

	//Prayer options
	val prayAtNearbyAltar by lazy { getOption<Boolean>(PRAY_AT_ALTAR_OPTION) }
	var nextAltarPrayRestore = Random.nextInt(5, 15)

	private val prayerItems = arrayOf("Bonecrusher", "Bonecrusher necklace", "Ash sanctifier")
	private val usingPrayer by lazy {
		prayAtNearbyAltar
			|| requiredInventory.filter { it.item is Potion }
			.any { (it.item as Potion).skill == Constants.SKILLS_PRAYER }
			|| (CATACOMBS_AREA.contains(centerTile) && requiredInventory.any { it.item.itemName in prayerItems })
			|| (CATACOMBS_AREA.contains(centerTile) && buryBones)
	}

	fun canActivateQuickPrayer() = usingPrayer && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0
	fun canDeactivateQuickPrayer() = Prayer.quickPrayer() &&
		aggressionTimer.isFinished() && useSafespot && Npcs.stream().interactingWithMe().none { !it.dead() }

	//Cannon option
	val useCannon by lazy { getOption<Boolean>(USE_CANNON_OPTION) }
	val cannonTile by lazy { getOption<Tile>(CANNON_TILE_OPTION) }
	fun getCannon() =
		Objects.stream(cannonTile, GameObject.Type.INTERACTIVE).name("Broken multicannon", "Dwarf multicannon").first()

	//Custom slayer options
	var lastTask = false
	var superiorActive = false

	@Subscribe
	fun experienceEvent(xpEvent: SkillExpGainedEvent) {
		if (xpEvent.skill == Skill.Slayer) {
			logger.info("Slayer xp at: ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
		} else if (xpEvent.skill == Skill.Hitpoints) {
			val dmg = floor(xpEvent.expGained / 1.33).toInt()
			if (TargetWidget.health() - dmg <= 0 && hasSlayerBracelet && !wearingSlayerBracelet()) {
				val slayBracelet = getSlayerBracelet()
				if (slayBracelet.valid()) {
					logger.info("Wearing bracelet on xp drop ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
					slayBracelet.fclick()
				}
			}
		}
	}

	override fun onDeath(npc: Npc) {
		if (npc.superior() != null) {
			superiorsKilled++
			superiorActive = false
		}
		super.onDeath(npc)
	}

	@Subscribe
	fun messageReceived(msg: MessageEvent) {
		if (msg.messageType != MessageType.Game) return
		if (msg.message.contains("so you can't take ")) {
			logger.info("Ironman message CANT TAKE type=${msg.messageType}")
			ironmanLoot.clear()
		}
		if (msg.message.contains("A superior foe has appeared")) {
			logger.info("Superior appeared message received: type=${msg.messageType}")
			superiorActive = true
			superiorsSpawned++
			if (!painter.containsLabel("Superior (K/S)")) {
				val indexForKills = painter.paintRowIndexForLabel("Kills")
				val superiorRow = listOf(
					TextPaintItem { "Superior (K/S)" },
					TextPaintItem { "(${superiorsKilled}/${superiorsSpawned})" }
				)
				painter.paintBuilder.items.add(indexForKills, superiorRow)
			}
		}
	}


	@Subscribe
	fun onPaintCheckbox(pcce: PaintCheckboxChangedEvent) {
		if (pcce.checkboxId == "stopAfterTask") {
			lastTask = pcce.checked
			val painter = painter as FighterPainter
			if (pcce.checked && !painter.paintBuilder.items.contains(painter.slayerTracker)) {
				val index =
					painter.paintBuilder.items.indexOfFirst { row -> row.any { it is CheckboxPaintItem && it.id == "stopAfterTask" } }
				painter.paintBuilder.items.add(index, painter.slayerTracker)
			} else if (!pcce.checked && painter.paintBuilder.items.contains(painter.slayerTracker)) {
				painter.paintBuilder.items.remove(painter.slayerTracker)
			}
		} else if (pcce.checkboxId == "stopAtBank") {
			lastTrip = pcce.checked
		}
	}

	override val requiredIds: IntArray by lazy {
		intArrayOf(
			*Defender.defenders,
			*requiredInventory.flatMap { it.item.ids.toList() }.toIntArray(),
			*equipment.flatMap { it.item.ids.toList() }.toIntArray()
		)
	}
}


fun main() {
	Fighter().startScript("127.0.0.1", "GIM", false)
}