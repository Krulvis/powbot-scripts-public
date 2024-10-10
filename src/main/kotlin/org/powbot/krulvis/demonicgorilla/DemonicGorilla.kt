package org.powbot.krulvis.demonicgorilla

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.PaintCheckboxChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.extensions.*
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.*
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FEROX_ENCLAVE_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_DEATH_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_MAGE_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_MELEE_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_RANGE_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DemonicPrayer
import org.powbot.krulvis.demonicgorilla.tree.branch.ShouldStop
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.BURY_BONES_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.mobile.script.ScriptManager


//<editor-fold desc="ScriptManifest">
@ScriptManifest(
	name = "krul DemonicGorilla",
	description = "Fights Demonic Gorilla.",
	author = "Krulvis",
	version = "1.0.0",
	category = ScriptCategory.Combat,
	scriptId = "d5f40929-ce2e-44e2-ae99-307dff28984d",
	priv = true
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			INVENTORY_OPTION, "What should your inventory look like?",
			optionType = OptionType.INVENTORY
		),
		ScriptConfiguration(
			USE_MELEE_OPTION, "Use melee gear?",
			optionType = OptionType.BOOLEAN, defaultValue = "true", visible = true
		),
		ScriptConfiguration(
			MELEE_EQUIPMENT_OPTION, "What melee gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = true
		),
		ScriptConfiguration(
			MELEE_PRAYER_OPTION,
			"What melee offensive prayer to use?",
			optionType = OptionType.STRING,
			visible = true,
			defaultValue = "PIETY",
			allowedValues = ["NONE", "CHIVALRY", "PIETY"]
		),
		ScriptConfiguration(
			USE_RANGE_OPTION, "Use ranged gear?",
			optionType = OptionType.BOOLEAN, visible = true, defaultValue = "true"
		),
		ScriptConfiguration(
			RANGE_EQUIPMENT_OPTION, "What ranged gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = true
		),
		ScriptConfiguration(
			RANGE_PRAYER_OPTION,
			"What range offensive prayer to use?",
			optionType = OptionType.STRING,
			visible = true,
			defaultValue = "EAGLE_EYE",
			allowedValues = ["NONE", "SHARP_EYE", "HAWK_EYE", "EAGLE_EYE", "RIGOUR"]
		),
		ScriptConfiguration(
			USE_MAGE_OPTION, "Use mage gear?",
			optionType = OptionType.BOOLEAN, visible = true, defaultValue = "false"
		),
		ScriptConfiguration(
			MAGE_EQUIPMENT_OPTION, "What mage gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = false
		),
		ScriptConfiguration(
			MAGE_PRAYER_OPTION,
			"What mage offensive prayer to use?",
			optionType = OptionType.STRING,
			visible = false,
			defaultValue = "MYSTIC_MIGHT",
			allowedValues = ["NONE", "MYSTIC_WILL", "MYSTIC_LORE", "MYSTIC_MIGHT", "AUGURY"]
		),
		ScriptConfiguration(
			SPECIAL_WEAPON_OPTION, "Special Attack Weapon?",
			optionType = OptionType.STRING, defaultValue = ARCLIGHT, allowedValues = ["NONE", DDS, ARCLIGHT]
		),
		ScriptConfiguration(
			RESURRECT_OPTION,
			"Resurrect spell?",
			optionType = OptionType.STRING,
			defaultValue = GREATER_GHOST,
			allowedValues = ["NONE", GREATER_GHOST, GREATER_ZOMBIE, GREATER_SKELETON]
		),
		ScriptConfiguration(
			BURY_BONES_OPTION, "Scatter ashes?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION,
			"Teleport to bank",
			optionType = OptionType.STRING,
			defaultValue = CASTLE_WARS_JEWELLERY_BOX,
			allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX]
		),
	]
)
//</editor-fold>
class DemonicGorilla : KillerScript() {

	override fun createPainter(): ATPaint<*> = DGPainter(this)

	override val rootComponent: TreeComponent<*> = ShouldStop(this)


	override fun onStart() {
		super.onStart()
		equipment = meleeEquipment
		if (buryBones) lootNames.add("malicious ashes")
		resurrectedTimer.reset(0.6 * Skills.level(Skill.Magic))
		resurrectedTimer.stop()
	}

	//<editor-fold desc="UISubscribers">
	@ValueChanged(USE_MELEE_OPTION)
	fun onUseMelee(useMelee: Boolean) {
		updateVisibility(MELEE_EQUIPMENT_OPTION, useMelee)
		updateVisibility(MELEE_PRAYER_OPTION, useMelee)
	}

	@ValueChanged(USE_RANGE_OPTION)
	fun onUseRange(useRange: Boolean) {
		updateVisibility(RANGE_EQUIPMENT_OPTION, useRange)
		updateVisibility(RANGE_PRAYER_OPTION, useRange)
	}

	@ValueChanged(USE_MAGE_OPTION)
	fun onUseMage(useMage: Boolean) {
		updateVisibility(MAGE_EQUIPMENT_OPTION, useMage)
		updateVisibility(MAGE_PRAYER_OPTION, useMage)
	}
	//</editor-fold desc="UISubscribers">


	//Inventory
	private val inventoryOptions by lazy { getOption<Map<Int, Int>>(INVENTORY_OPTION) }
	val requiredInventory by lazy {
		val equipmentIds = allEquipmentItems.map { it.id }
		inventoryOptions.map { InventoryRequirement(it.key, it.value) }.filterNot { it.item.id in equipmentIds }
	}

	//Equipment
	fun getEquipment(optionKey: String): List<EquipmentRequirement> {
		val option = getOption<Map<Int, Int>>(optionKey)
		return option.map {
			EquipmentRequirement(
				it.key,
				Slot.forIndex(it.value)!!,
			)
		}
	}

	val specialWeapon by lazy { Weapon.values().firstOrNull { it.itemName == getOption(SPECIAL_WEAPON_OPTION) } }
	val meleeEquipment by lazy { getEquipment(MELEE_EQUIPMENT_OPTION) }
	val rangeEquipment by lazy { getEquipment(RANGE_EQUIPMENT_OPTION) }
	val mageEquipment by lazy { getEquipment(MAGE_EQUIPMENT_OPTION) }

	val allEquipmentItems by lazy { (meleeEquipment.map { it.item } + rangeEquipment.map { it.item } + mageEquipment.map { it.item }).distinct() }
	val ammos: List<Item> by lazy {
		val mutableAmmoList = mutableListOf<IEquipmentItem>()
		val meleeAmmo = meleeEquipment.firstOrNull { it.slot == Slot.QUIVER }
		val rangeAmmo = rangeEquipment.firstOrNull { it.slot == Slot.QUIVER }
		val mageAmmo = mageEquipment.firstOrNull { it.slot == Slot.QUIVER }
		if (meleeAmmo != null)
			mutableAmmoList.add(meleeAmmo.item)
		if (rangeAmmo != null)
			mutableAmmoList.add(rangeAmmo.item)
		if (mageAmmo != null)
			mutableAmmoList.add(mageAmmo.item)
		mutableAmmoList.distinct()
	}
	override val ammoIds by lazy { ammos.map { it.id }.toIntArray() }

	val teleportEquipments by lazy {
		allEquipmentItems.mapNotNull { TeleportEquipment.getTeleportEquipment(it.id) }
	}
	var equipment: List<EquipmentRequirement> = emptyList()

	override fun GroundItem.isLoot() = isLoot(stackSize())

	private fun GenericItem.isLoot(amount: Int): Boolean {
		val nameLC = name().lowercase()
		return lootNames.any { ln -> nameLC.contains(ln) } || getPrice() * amount >= if (stackable()) 500 else 2000
	}

	var npcDeathWatchers: MutableList<NpcDeathWatcher> = mutableListOf()

	//Banking option
	var forcedBanking = false
	var lastTrip = false
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val seedPodTeleport = TeleportMethod(ItemTeleport.ROYAL_SEED_POD)
	val aggressionTimer = Timer(15 * 60 * 1000)

	fun nearbyMonsters(): List<Npc> =
		Npcs.stream().within(centerTile, 20).name(DEMONIC_GORILLA).nearest().list()

	private fun Npc.attackingOtherPlayer(): Boolean {
		val interacting = interacting()
		return interacting is Player && interacting != Players.local()
	}

	fun target(): Npc {
		val local = Players.local()
		val nearbyMonsters =
			nearbyMonsters().filterNot { it.healthBarVisible() && (it.attackingOtherPlayer() || it.healthPercent() == 0) }
				.sortedBy { it.distance() }
		val attackingMe =
			nearbyMonsters.firstOrNull { it.interacting() is Npc || it.interacting() == local && it.reachable() }
		return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() } ?: Npc.Nil
	}

	//Safespot options
	val centerTile = Tile(2104, 5653, 0)
	val buryBones by lazy { getOption<Boolean>(BURY_BONES_OPTION) }

	//Prayer options
	val protectionPrayerSwitchTimer = Timer(3000)
	var protectionPrayer = Prayer.Effect.PROTECT_FROM_MISSILES
	val meleeOffensivePrayer by lazy {
		Prayer.Effect.values().firstOrNull { it.name == getOption<String>(MELEE_PRAYER_OPTION) }
	}
	val rangeOffensivePrayer by lazy {
		Prayer.Effect.values().firstOrNull { it.name == getOption<String>(RANGE_PRAYER_OPTION) }
	}
	val mageOffensivePrayer by lazy {
		Prayer.Effect.values().firstOrNull { it.name == getOption<String>(MAGE_PRAYER_OPTION) }
	}
	var offensivePrayer: Prayer.Effect? = null

	//Resurrection options
	val resurrectSpell by lazy { ResurrectSpell.values().firstOrNull { it.name == getOption(RESURRECT_OPTION) } }
	var resurrectedTimer = Timer(0.6 * 99)

	//Custom slayer options
	var lastTask = false
	var superiorAppeared = false

	var demonicPrayer: DemonicPrayer = DemonicPrayer.NONE
	fun switchStyle(prayer: DemonicPrayer) {
		demonicPrayer = prayer
		when (prayer) {
			DemonicPrayer.RANGE -> {
				if (meleeEquipment.isNotEmpty()) {
					equipment = meleeEquipment
					offensivePrayer = meleeOffensivePrayer
				} else {
					equipment = mageEquipment
					offensivePrayer = mageOffensivePrayer
				}
			}

			DemonicPrayer.MELEE -> {
				if (rangeEquipment.isNotEmpty()) {
					equipment = rangeEquipment
					offensivePrayer = rangeOffensivePrayer
				} else {
					equipment = meleeEquipment
					offensivePrayer = meleeOffensivePrayer
				}
			}

			DemonicPrayer.MAGE, DemonicPrayer.NONE -> {
				if (meleeEquipment.isNotEmpty()) {
					equipment = meleeEquipment
					offensivePrayer = meleeOffensivePrayer
				} else {
					equipment = rangeEquipment
					offensivePrayer = rangeOffensivePrayer
				}
			}
		}
	}

	@Subscribe
	fun onTickEvent(_e: TickEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		if (currentTarget.valid() && currentTarget.name == DEMONIC_GORILLA) {
			if (currentTarget.overheadMessage()
					?.contains("Rhaaa") == true && protectionPrayerSwitchTimer.isFinished()
			) {
				logger.info("Switching attack style RHAAAA")
				protectionPrayerSwitchTimer.reset()
				if (currentTarget.inMotion()) {
					logger.info("In motion so melee attacking")
					protectionPrayer = Prayer.Effect.PROTECT_FROM_MELEE
				} else {
					logger.info("Not in motion so other prayer")
					val prots = arrayOf(
						Prayer.Effect.PROTECT_FROM_MISSILES,
						Prayer.Effect.PROTECT_FROM_MAGIC,
						Prayer.Effect.PROTECT_FROM_MELEE
					)
					protectionPrayer = prots.filterNot { it == protectionPrayer }.random()
				}
			}
			val prayId = currentTarget.prayerHeadIconId()
			val prayer = DemonicPrayer.forOverheadId(prayId)
			logger.info("Fighting demonic gorilla with overheadIcon=${prayId}, prayer=${prayer}")
			switchStyle(prayer)
		}
	}

	@Subscribe
	fun onInventoryChange(evt: InventoryChangeEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val id = evt.itemId
		val isTeleport = TeleportEquipment.isTeleportEquipment(id)
		if (evt.quantityChange > 0 && id != VIAL
			&& requiredInventory.none { id in it.item.ids }
			&& allEquipmentItems.none { it.ids.contains(id) }
			&& !isTeleport
		) {
			painter.trackItem(id, evt.quantityChange)
		}
	}

	@Subscribe
	fun onAnimationChangeEvent(e: NpcAnimationChangedEvent) {
		if (e.npc != currentTarget) return
		val anim = e.animation
		logger.info("Gorilla animation=${anim}")
		if (anim == DEMONIC_GORILLA_DEATH_ANIM) {
			logger.info("We found death animation")
			lootWachter = LootWatcher(e.npc.tile(), ammoIds, isLoot = { it.isLoot() }, lootList = ironmanLoot)
		}
		when (e.animation) {
			DEMONIC_GORILLA_MELEE_ANIM -> protectionPrayer = Prayer.Effect.PROTECT_FROM_MELEE
			DEMONIC_GORILLA_RANGE_ANIM -> protectionPrayer = Prayer.Effect.PROTECT_FROM_MISSILES
			DEMONIC_GORILLA_MAGE_ANIM -> protectionPrayer = Prayer.Effect.PROTECT_FROM_MAGIC
		}
		logger.info("ProtectionPrayer=${protectionPrayer}")
	}

	@Subscribe
	fun onPaintCheckbox(pcce: PaintCheckboxChangedEvent) {
		if (pcce.checkboxId == "stopAfterTask") {
			lastTask = pcce.checked
			val painter = painter as DGPainter
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
}


fun main() {
	DemonicGorilla().startScript("127.0.0.1", "GIM", false)
}