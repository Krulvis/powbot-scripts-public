package org.powbot.krulvis.dagannothkings

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.dagannothkings.Data.EQUIPMENT_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.INVENTORY_OPTION
import org.powbot.krulvis.dagannothkings.Data.KILL_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.KING_DEATH_ANIM
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king
import org.powbot.krulvis.dagannothkings.Data.OFFENSIVE_PRAY_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.SAFESPOT_REX
import org.powbot.krulvis.dagannothkings.tree.branch.ShouldBank
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.mobile.script.ScriptManager

fun main() {
	DagannothKings().startScript()
}

@ScriptManifest(
	"krul DagannothKings",
	"Kills Dagannoth Kings",
	version = "1.0.0",
	scriptId = "f6ac533c-0aee-4992-aea7-10460ed56c8c",
	category = ScriptCategory.Combat,
	priv = true
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(KILL_PREFIX_OPTION + "Rex", "Kill Rex", OptionType.BOOLEAN, defaultValue = "true"),
		ScriptConfiguration(SAFESPOT_REX, "Lure rex to safespot?", OptionType.BOOLEAN, defaultValue = "true"),
		ScriptConfiguration(EQUIPMENT_PREFIX_OPTION + "Rex", "Equipment for Rex", OptionType.EQUIPMENT),
		ScriptConfiguration(
			OFFENSIVE_PRAY_PREFIX_OPTION + "Rex", "Offensive Rex Prayer", OptionType.STRING,
			allowedValues = ["NONE", "MYSTIC_WILL", "MYSTIC_MIGHT", "AUGURY"], defaultValue = "NONE"
		),
		ScriptConfiguration(KILL_PREFIX_OPTION + "Prime", "Kill Prime", OptionType.BOOLEAN, defaultValue = "false"),
		ScriptConfiguration(
			EQUIPMENT_PREFIX_OPTION + "Prime",
			"Equipment for Supreme",
			OptionType.EQUIPMENT,
			visible = false
		),
		ScriptConfiguration(
			OFFENSIVE_PRAY_PREFIX_OPTION + "Prime",
			"Offensive Prime Prayer",
			OptionType.STRING,
			visible = false,
			allowedValues = ["NONE", "HAWK_EYE", "EAGLE_EYE", "RIGOUR"],
			defaultValue = "NONE"
		),
		ScriptConfiguration(KILL_PREFIX_OPTION + "Supreme", "Kill Supreme", OptionType.BOOLEAN, defaultValue = "false"),
		ScriptConfiguration(
			EQUIPMENT_PREFIX_OPTION + "Supreme",
			"Equipment for Supreme",
			OptionType.EQUIPMENT,
			visible = false
		),
		ScriptConfiguration(
			OFFENSIVE_PRAY_PREFIX_OPTION + "Supreme",
			"Offensive Supreme Prayer",
			OptionType.STRING,
			visible = false,
			allowedValues = ["NONE", "ULTIMATE_STRENGTH", "CHIVALRY", "PIETY"],
			defaultValue = "NONE"
		),
		ScriptConfiguration(INVENTORY_OPTION, "Inventory setup", OptionType.INVENTORY),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION, "Which teleport to go to bank?", OptionType.STRING,
			defaultValue = CASTLE_WARS_JEWELLERY_BOX, allowedValues = [CASTLE_WARS_JEWELLERY_BOX]
		)
	]
)
class DagannothKings : KrulScript() {

	val lootList = mutableListOf<GroundItem>()
	val skippedLoot = mutableListOf<GroundItem>()

	@ValueChanged(KILL_PREFIX_OPTION + "Rex")
	fun onRex(rex: Boolean) {
		updateVisibility(SAFESPOT_REX, rex)
		updateVisibility(OFFENSIVE_PRAY_PREFIX_OPTION + "Rex", rex)
		updateVisibility(EQUIPMENT_PREFIX_OPTION + "Rex", rex)
	}

	@ValueChanged(KILL_PREFIX_OPTION + "Supreme")
	fun onSupreme(supreme: Boolean) {
		updateVisibility(OFFENSIVE_PRAY_PREFIX_OPTION + "Supreme", supreme)
		updateVisibility(EQUIPMENT_PREFIX_OPTION + "Supreme", supreme)
	}

	@ValueChanged(KILL_PREFIX_OPTION + "Prime")
	fun onPrime(prime: Boolean) {
		updateVisibility(OFFENSIVE_PRAY_PREFIX_OPTION + "Prime", prime)
		updateVisibility(EQUIPMENT_PREFIX_OPTION + "Prime", prime)
	}

	override fun onStart() {
		super.onStart()
		Data.King.values().forEach {
			it.offensivePrayer = Prayer.Effect.values()
				.firstOrNull { pray -> pray.name == getOption(OFFENSIVE_PRAY_PREFIX_OPTION + it.name) }
			it.equipment = EquipmentRequirement.forOption(getOption(EQUIPMENT_PREFIX_OPTION + it.name))
			it.kill = getOption(KILL_PREFIX_OPTION + it.name)
			it.respawnTimer.stop()
		}
	}

	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val allEquipment by lazy { Data.King.values().flatMap { it.equipment }.distinct() }
	val allEquipmentIds by lazy { allEquipment.flatMap { e -> e.item.ids.toList() }.distinct() }
	val inventory by lazy {
		InventoryRequirement.forOption(getOption(INVENTORY_OPTION)).filterNot { it.item.id in allEquipmentIds }
	}
	var kills = 0
	var target = Npc.Nil
	val animMap: MutableMap<Data.King, Int> = mutableMapOf()
	var forcedProtectionPrayer: Prayer.Effect? = null
	var forcedBanking = false

	//Rex settings
	var lureTile: Tile = Tile.Nil
	var safeTile: Tile = Tile.Nil
	var rexTile: Tile = Tile.Nil
	var rexSpawnTile: Tile = Tile.Nil

	val safeSpotRex: Boolean by lazy { getOption(SAFESPOT_REX) }
	var activeKings: List<Npc> = emptyList()
	var aliveKings: List<Npc> = emptyList()
	var ladderTile = Tile.Nil

	@Subscribe
	fun onGameTick(e: TickEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val me = me
		val targ = me.interacting()
		if (targ is Npc && targ.king() != null) {
			target = targ
		}

		if (!ladderTile.valid()) {
			//Do everything
			val ladder = Data.getKingsLadderDown()
			if (!ladder.valid()) return

			ladderTile = ladder.tile
		} else if (lureTile == Tile.Nil) {
			logger.info("Setting all tiles")
			lureTile = ladderTile.derive(29, 1)
			safeTile = ladderTile.derive(28, -8)
			rexTile = ladderTile.derive(28, -4) //Tile that rex should be standing on when safeSpotting him
			rexSpawnTile = ladderTile.derive(15, -3)

			Data.King.Rex.killTile = rexTile
			Data.King.Prime.killTile = ladderTile.derive(22, 11) // Do not go further than this tile or Supreme might attack
		} else if (lureTile.distance() < 50) {
			val mt = me.tile()
			logger.info("mytile dx=${mt.x - ladderTile.x}, dy=${mt.y - ladderTile.y}")
			//Inside Kings lair and set basic stuff
			val newKings = Npcs.stream().nameContains("Dagannoth").toList()
			newKings.forEach { k ->
				if (!activeKings.contains(k)) {
					val kingTile = k.tile()
					logger.info("${k.name} spawned at tile=${k.tile()}, dx=${kingTile.x - ladderTile.x}, dy=${kingTile.y - ladderTile.y}")
				}
			}
			activeKings = newKings
			aliveKings = newKings.filter { !it.dead() }
		}
		watchForLoot()
	}

	fun setForcedProtection(kings: List<Npc>) {
		val offensiveKings = kings
			.filter { it.interacting() == me }
			.mapNotNull { it.king() }
			.filter { it != Data.King.Rex || !safeSpotRex }
		val targetKing = target.king()
		if (offensiveKings.isNotEmpty()) {
			val cycle = Game.cycle()
			forcedProtectionPrayer =
				offensiveKings.maxByOrNull { cycle - animMap.getOrDefault(it, -1) }?.protectionPrayer ?: return
			if (!Prayer.prayerActive(forcedProtectionPrayer!!)) {
				Prayer.prayer(forcedProtectionPrayer!!, true)
			}
		} else if (targetKing != null && targetKing != Data.King.Rex) {
			forcedProtectionPrayer = targetKing.protectionPrayer
			if (!Prayer.prayerActive(forcedProtectionPrayer!!)) {
				Prayer.prayer(forcedProtectionPrayer!!, true)
			}
		} else {
			forcedProtectionPrayer = null
			Data.King.values().map { it.protectionPrayer }.forEach {
				if (Prayer.prayerActive(it)) {
					Prayer.prayer(it, false)
				}
			}
		}
	}

	private fun watchForLoot() {
		GroundItems.stream().within(15)
			.filtered { !skippedLoot.contains(it) && !lootList.contains(it) && it.isLoot() }
			.forEach { gi ->
				lootList.add(gi)
			}
	}

	private fun GroundItem.isLoot(): Boolean {
		val name = name()
		return name in Data.LOOT
			|| (name == "Coins" && stackSize() > 1000)
			|| (Food.forName(name) != null)
	}


	@Subscribe
	fun onNpcAnimation(e: NpcAnimationChangedEvent) {
		val npc = e.npc
		val king = npc.king() ?: return
		val anim = e.animation
		val isOffensive = anim == king.offensiveAnim
		logger.info("NpcAnimationEvent(npc=${npc.name}, animation=${anim}, offensive=${isOffensive})")
		if (isOffensive) {
			val lastCycle = animMap.getOrDefault(king, Game.cycle())
			animMap[king] = Game.cycle()
			logger.info("Attack from king=$king, took=${Game.cycle() - lastCycle}")
		}
		if (anim == KING_DEATH_ANIM && npc == target && npc.dead()) {
			kills++
			king.respawnTimer.reset()
		}
	}


	@Subscribe
	fun onInventoryChange(i: InventoryChangeEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val name = i.itemName
		if (name in Data.LOOT || name == "Coins") {
			painter.trackItem(i.itemId, i.quantityChange)
		}
	}

	@Subscribe
	fun onMessageEvent(me: MessageEvent) {
		if (me.message.contains("so you can't take ")) {
			logger.info("Ironman message CANT TAKE type=${me.messageType}")
			skippedLoot.addAll(lootList)
			lootList.clear()
		}
	}

	fun getNewTarget(): Npc? {
		val aliveKings = activeKings.filter { !it.dead() }.associateBy { it.king() }
		return aliveKings.getOrDefault(Data.King.values().firstOrNull { king -> aliveKings.any { it.key == king } }, null)
	}


	override fun createPainter(): ATPaint<*> = DKPaint(this)

	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}
