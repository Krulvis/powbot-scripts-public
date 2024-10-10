package org.powbot.krulvis.lizardshamans

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FAIRY_RING_BLS
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FAIRY_RING_DJR
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.UniqueLootTracker
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.lizardshamans.Data.LOOT
import org.powbot.krulvis.lizardshamans.event.JumpEvent
import org.powbot.krulvis.lizardshamans.tree.branch.ShouldBank

@ScriptManifest(
	"krul LizardmanShamans", "Kills lizardman shamans for Dragon Warhammer",
	category = ScriptCategory.Combat, version = "1.0.1", priv = true,
	scriptId = "08bda146-7aba-4fb3-90e9-68b4bdeb2d19"
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration("Equipment", "What to wear?", optionType = OptionType.EQUIPMENT),
		ScriptConfiguration("Inventory", "What to take with?", optionType = OptionType.INVENTORY),
		ScriptConfiguration("Slayer", "Kill for slayer task?", optionType = OptionType.BOOLEAN),
		ScriptConfiguration(
			"BankTeleport",
			"How to get to bank?",
			optionType = OptionType.STRING,
			allowedValues = [CASTLE_WARS_JEWELLERY_BOX, EDGEVILLE_MOUNTED_GLORY],
			defaultValue = CASTLE_WARS_JEWELLERY_BOX
		),
		ScriptConfiguration(
			"ShamanTeleport", "How to get to Shamans?", OptionType.STRING,
			allowedValues = [FAIRY_RING_BLS, FAIRY_RING_DJR], defaultValue = FAIRY_RING_DJR
		),
	]
)
class LizardShamans : KillerScript(false), UniqueLootTracker {
	override fun createPainter(): ATPaint<*> = LizardShamanPainter(this)

	var banking = false
	val lootList = mutableListOf<GroundItem>()
	var lastAnimation = -1
	var jumpEvent = JumpEvent(Tile.Nil)
	val escapeTiles = mutableListOf<Pair<Tile, Double>>()
	var escapeTile = Tile.Nil
	val spawns = mutableListOf<Npc>()
	val skippedLoot = mutableListOf<GroundItem>()

	@Subscribe
	fun onTick(tickEvent: TickEvent) {
		watchForLoot()
		findSpawns()
		escapeTiles.clear()
		if (!currentTarget.valid() && spawns.isEmpty()) return

		findEscapeTile()
	}

	private fun findSpawns() {
		spawns.clear()
		spawns.addAll(Npcs.stream().name("Spawn").within(10).toList())
	}

	private fun watchForLoot() {
		GroundItems.stream().within(15)
			.filtered { !skippedLoot.contains(it) && !lootList.contains(it) && it.isLoot() }
			.forEach { gi ->
				lootList.add(gi)
			}
	}

	private fun findEscapeTile() {
		val myTile = me.tile()
		val collisionMap = Movement.collisionMap(myTile.floor).flags()
		val tiles = Data.furthestReachableTiles(myTile, collisionMap)
		escapeTiles.addAll(tiles.filterNot { it.distance() < 3 }.map {
			val (a, b, c) = Data.lineEquation(myTile, it)
			it to Data.averagePerpendicularDistance(spawns + currentTarget, a, b, c)
		})
		escapeTile = escapeTiles.maxByOrNull { it.second }?.first ?: Tile.Nil
	}

	@Subscribe
	fun onNpcAnimation(nae: NpcAnimationChangedEvent) {
		val npc = nae.npc
		val anim = nae.animation
		if (npc.name == "Lizardman shaman") {
			val attack = Data.AttackAnimation.forAnimation(anim)
			logger.info("Shaman Animation=$anim, attack=${attack}")
			if (npc == currentTarget) {
				lastAnimation = anim
				if (attack == Data.AttackAnimation.Jump) {
					jumpEvent = JumpEvent(me.tile())
				} else if (attack == Data.AttackAnimation.Falling) {
					jumpEvent.timer.stop()
				}
			}
			if (npc.dead()) {
				kills++
//				LootWatcher(npc.tile(), -1, lootList = lootList) { it.isLoot() }
			}
		}
	}

	override val requiredIds: IntArray by lazy {
		inventory.flatMap { it.item.ids.toList() }.toIntArray() +
			equipment.flatMap { it.item.ids.toList() }.toIntArray()
	}

	@Subscribe
	fun onMessageEvent(me: MessageEvent) {
		if (me.message.contains("so you can't take ")) {
			logger.info("Ironman message CANT TAKE type=${me.messageType}")
			skippedLoot.addAll(lootList)
			lootList.clear()
		}
	}

	override val ammoIds: IntArray by lazy {
		equipment.firstOrNull { it.slot == Slot.QUIVER }?.item?.ids ?: intArrayOf()
	}


	override fun GroundItem.isLoot(): Boolean {
		val name = name()
		return name in LOOT
			|| (name == "Coins" && stackSize() > 1000)
			|| (name == "Chilli potato" && Inventory.emptySlotCount() > (if (Food.CHILI_POTATO.canEat()) 1 else 2))
	}


	val equipment by lazy { EquipmentRequirement.forOption(getOption("Equipment")) }
	val inventory by lazy { InventoryRequirement.forOption(getOption("Inventory")) }
	val slayerTask by lazy { getOption<Boolean>("Slayer") }
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption("BankTeleport"))) }
	val shamanTeleport by lazy { TeleportMethod(Teleport.forName(getOption("ShamanTeleport"))) }

	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}

fun main() {
	LizardShamans().startScript()
}