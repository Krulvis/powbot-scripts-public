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
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.dagannothkings.Data.EQUIPMENT_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.HEAL_GEAR_OPTION
import org.powbot.krulvis.dagannothkings.Data.HEAL_ON_SPINOLYP_OPTION
import org.powbot.krulvis.dagannothkings.Data.INVENTORY_OPTION
import org.powbot.krulvis.dagannothkings.Data.KILL_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king
import org.powbot.krulvis.dagannothkings.Data.OFFENSIVE_PRAY_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.SAFESPOT_REX
import org.powbot.krulvis.dagannothkings.Data.getKingsLadderUp
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
			"Equipment for Prime",
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
		ScriptConfiguration(
			HEAL_ON_SPINOLYP_OPTION,
			"Heal on Spinolyps in downtime?",
			OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			HEAL_GEAR_OPTION,
			"Equipment when killing Spinolyps",
			OptionType.EQUIPMENT,
			visible = false
		),
		ScriptConfiguration(
			INVENTORY_OPTION, "Inventory setup", OptionType.INVENTORY
		),
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

	@ValueChanged(HEAL_ON_SPINOLYP_OPTION)
	fun onSpinnops(spinnops: Boolean) {
		updateVisibility(HEAL_GEAR_OPTION, spinnops)
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


	val healOnSpinolyp by lazy { getOption<Boolean>(HEAL_ON_SPINOLYP_OPTION) }
	val spinolypEquipment by lazy { EquipmentRequirement.forOption(getOption(HEAL_GEAR_OPTION)) }
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val allEquipment by lazy { (Data.King.values().flatMap { it.equipment } + spinolypEquipment).distinct() }
	val allEquipmentIds by lazy { allEquipment.flatMap { e -> e.item.ids.toList() }.distinct() }
	val inventory by lazy {
		InventoryRequirement.forOption(getOption(INVENTORY_OPTION)).filterNot { it.item.id in allEquipmentIds }
	}
	var kills = 0
	var target = Npc.Nil
	val animMap: MutableMap<Data.King, Int> = mutableMapOf()
	var forcedProtectionPrayer: Prayer.Effect? = null
	var forcedBanking = false
	var prayerBlock = false //Set to true while setting prayer from tickEvent

	//Rex settings
	var lureTile: Tile = Tile.Nil //Tile to stand on to lure rex before safespotting
	var rexSafeTile: Tile = Tile.Nil //Tile on which to stand when safespotting rex
	var rexTile: Tile = Tile.Nil //Tile that rex should be standing on before safespotting him
	var rexSpawnTile: Tile = Tile.Nil
	var evadeRexTile = Tile.Nil
	var centerTileEvadeSupreme = Tile.Nil

	val safeSpotRex: Boolean by lazy { getOption(SAFESPOT_REX) }
	var aliveKings: List<Data.King> = emptyList()
	var offensiveKings = emptyList<Data.King>()
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
			evadeRexTile = ladderTile.derive(4, 9)
			lureTile = ladderTile.derive(29, 1)
			rexSafeTile = ladderTile.derive(28, -8)
			rexTile = ladderTile.derive(28, -4)
			rexSpawnTile = ladderTile.derive(15, -3)
			centerTileEvadeSupreme = ladderTile.derive(22, 0)

			Data.King.Rex.killTile = rexTile
			Data.King.Prime.killTile =
				ladderTile.derive(22, 11) // Do not go further than this tile or Supreme might attack
		} else if (lureTile.distance() < 50) {
			if (getKingsLadderUp().valid()) {
				lureTile = Tile.Nil
				ladderTile = Tile.Nil
				logger.info("Moved up? Escaped?")
				return
			}
			val mt = me.tile()
			logger.info("mytile dx=${mt.x - ladderTile.x}, dy=${mt.y - ladderTile.y}")
			//Inside Kings lair and set basic stuff
			val newKings = Npcs.stream().nameContains("Dagannoth").toList()
			newKings.associateWith { it.king() }.forEach {
				it.value?.npc = it.key
			}
			aliveKings = Data.King.values().filter { !it.npc.dead() }
			offensiveKings =
				aliveKings.filter { it.npc.interacting() == me }
					.filter { it != Data.King.Rex || !safeSpotRex || me.tile() != rexSafeTile }
			setForcedProtection(offensiveKings)
		}
		watchForLoot()
	}

	private fun setForcedProtectionOnNext(activeKings: List<Data.King>) {
		val sortedAttacking =
			activeKings.associateWith { animMap.getOrDefault(it, Int.MAX_VALUE) }.toList().sortedBy { it.second }
		if (sortedAttacking.isEmpty()) return
		val first = sortedAttacking.first()
		val prime = animMap.getOrDefault(Data.King.Prime, Int.MAX_VALUE)
		forcedProtectionPrayer = if (first.second == prime) {
			//Always prioritize prime if two attack at the same time
			Data.King.Prime.protectionPrayer
		} else {
			first.first.protectionPrayer
		}

		logger.info("Setting prayer=${forcedProtectionPrayer} because offensiveKings.isNotEmpty()")
		setPrayer(forcedProtectionPrayer!!)
	}

	private fun setPrayer(effect: Prayer.Effect) {
		prayerBlock = true
		if (!Prayer.prayerActive(effect)) {
			Prayer.prayer(effect, true)
		}
		prayerBlock = false
	}

	private fun setForcedProtection(kings: List<Data.King>) {
		val targetKing = target.king()
		val firstRespawn =
			Data.King.values().minByOrNull { it.respawnTimer.getRemainder() }
		if (offensiveKings.isNotEmpty()) {
			setForcedProtectionOnNext(kings)
		} else if (targetKing != null && targetKing != Data.King.Rex) {
			logger.info("Setting prayer according to king we're fighting")
			forcedProtectionPrayer = targetKing.protectionPrayer
			setPrayer(forcedProtectionPrayer!!)
		} else if ((firstRespawn?.respawnTimer?.getRemainder() ?: Long.MAX_VALUE) in -5000..2000) {
			forcedProtectionPrayer = firstRespawn!!.protectionPrayer
			setPrayer(forcedProtectionPrayer!!)
		} else {
			logger.info("Don't need prayer")
			forcedProtectionPrayer = null
			Data.King.values().forEach {
				Prayer.prayer(it.protectionPrayer, false)
				val offensive = it.offensivePrayer
				if (offensive != null) Prayer.prayer(offensive, false)
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
			val lastAnimTick = animMap.getOrDefault(king, ticks)
			animMap[king] = ticks
			logger.info("Attack from king=$king, took=${ticks - lastAnimTick}")
			setForcedProtectionOnNext(offensiveKings)
		}
		if (king.kill && npc.dead()) {
			if (king.respawnTimer.isFinished()) {
				kills++
			}
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
		return aliveKings.sortedBy { it.ordinal }.firstOrNull { it.kill }?.npc
	}


	override fun createPainter(): ATPaint<*> = DKPaint(this)

	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}
