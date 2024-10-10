package org.powbot.krulvis.gwd

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Npc
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.fighter.MONSTER_TELEPORT_OPTION
import org.powbot.krulvis.gwd.GWD.STACK_BODYGUARDS_OPTION


abstract class GWDScript<S : GWDScript<S>>(val god: GWD.God) : KillerScript(false) {

	val equipmentGeneral = mutableListOf<EquipmentRequirement>()
	val equipmentKC = mutableListOf<EquipmentRequirement>()
	val equipmentMelee = mutableListOf<EquipmentRequirement>()
	val equipmentRange = mutableListOf<EquipmentRequirement>()
	val equipmentMage = mutableListOf<EquipmentRequirement>()

	var lastAttackTick = -4

	fun canAttack() = ticks > lastAttackTick + 2

	override fun onStart() {
		super.onStart()
		logger.info("Starting script ${javaClass.simpleName}")
		equipmentGeneral.addAll(generalEquipment())
		equipmentMelee.addAll(meleeEquipment())
		equipmentRange.addAll(rangeEquipment())
		equipmentMage.addAll(mageEquipment())
		equipmentKC.addAll(kcEquipment())
	}

	val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val gwdTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }
	val stackGuards by lazy { getOption<Boolean>(STACK_BODYGUARDS_OPTION) }

	var general: Npc = Npc.Nil
	var mageBG: Npc = Npc.Nil
	var rangeBG: Npc = Npc.Nil
	var meleeBG: Npc = Npc.Nil

	abstract fun generalAliveBranch(): TreeComponent<S>
	abstract fun generalEquipment(): List<EquipmentRequirement>
	abstract fun meleeEquipment(): List<EquipmentRequirement>
	abstract fun rangeEquipment(): List<EquipmentRequirement>
	abstract fun mageEquipment(): List<EquipmentRequirement>
	abstract fun kcEquipment(): List<EquipmentRequirement>

	override val ammoIds: IntArray by lazy { intArrayOf(0) }

	override fun GroundItem.isLoot(): Boolean {
		val name = name().lowercase()
		return GWD.lootNames.any { it in name }
	}

	@Subscribe
	fun onTick(e: TickEvent) {
		general = god.getGeneral()
		meleeBG = god.getMelee()
		rangeBG = god.getRange()
		mageBG = god.getMage()
	}

	val attackTimers: Array<Timer> = arrayOf(Timer(), Timer(), Timer(), Timer())


	@Subscribe
	fun onAnimation(e: NpcAnimationChangedEvent) {

	}

}