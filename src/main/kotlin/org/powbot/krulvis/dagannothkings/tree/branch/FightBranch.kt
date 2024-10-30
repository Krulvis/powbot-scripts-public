package org.powbot.krulvis.dagannothkings.tree.branch

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king
import org.powbot.krulvis.dagannothkings.tree.leaf.Attack
import org.powbot.krulvis.dagannothkings.tree.leaf.Fight
import org.powbot.krulvis.dagannothkings.tree.leaf.Loot
import org.powbot.krulvis.dagannothkings.tree.leaf.LureRex

class ShouldLoot(script: DagannothKings) : Branch<DagannothKings>(script, "ShouldLoot?") {
	override val successComponent: TreeComponent<DagannothKings> = Loot(script)
	override val failedComponent: TreeComponent<DagannothKings> = ShouldConsume(script, HasTarget(script))

	override fun validate(): Boolean {
		if (script.target.dead()) {
			if (script.target.valid() && script.offensiveKings.filterNot { it == script.target.king() }.isEmpty()) {
				waitFor(1200) { script.lootList.isNotEmpty() }
			}
			script.target = script.getNewTarget() ?: Npc.Nil
		} else if (script.target.king() == Data.King.Rex && script.offensiveKings.size > 1) {
			script.target = script.offensiveKings.first { it != Data.King.Rex }.npc
		}
		return script.lootList.isNotEmpty() && script.lootList.any { Food.forName(it.name()) == null || Inventory.emptySlotCount() > 0 }
	}
}

class HasTarget(script: DagannothKings) : Branch<DagannothKings>(script, "HasNewTarget?") {
	override val failedComponent: TreeComponent<DagannothKings> = EvadeRex(script)
	override val successComponent: TreeComponent<DagannothKings> = ShouldEquipGear(script)

	override fun validate(): Boolean {
		return script.target.valid() && !script.target.dead()
	}
}

class ShouldEquipGear(script: DagannothKings) : Branch<DagannothKings>(script, "ShouldEquipGear?") {
	override val successComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "EquippingGear") {
		script.logger.info("Equipping = ${missingEquipment.joinToString { it.item.itemName }}")
		if (missingEquipment.any { it.slot == Equipment.Slot.MAIN_HAND } && script.target.king()!!.equipment.none { it.slot == Equipment.Slot.OFF_HAND }) {
			if (Inventory.isFull() && Equipment.itemAt(Equipment.Slot.OFF_HAND).valid()) {
				script.logger.info("Need to eat something before we can equip bow")
				Food.getFirstFood()?.eat()
			}
		}
		missingEquipment.forEach { it.item.equip(false) }
		equipTimer.reset()
	}
	override val failedComponent: TreeComponent<DagannothKings> = NeedsToWalkToPrimeKillTile(script)

	private val equipTimer = Timer(600)
	private var missingEquipment = emptyList<EquipmentRequirement>()

	override fun validate(): Boolean {
		missingEquipment = script.target.king()?.equipment?.filter { !it.meets() } ?: return false
		return missingEquipment.isNotEmpty() && equipTimer.isFinished()
	}
}

class NeedsToWalkToPrimeKillTile(script: DagannothKings) : Branch<DagannothKings>(script, "WalkToPrimeKillTile?") {
	override val successComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "WalkToPrimeKillTile") {
		Movement.step(king.killTile)
	}
	override val failedComponent: TreeComponent<DagannothKings> = FightingKing(script)

	private var king: Data.King = Data.King.Prime
	override fun validate(): Boolean {
		king = script.target.king() ?: return false
		return king == Data.King.Prime && !Data.King.Supreme.kill && king.killTile.distance() > 3
	}
}

class FightingKing(script: DagannothKings) : Branch<DagannothKings>(script, "FightingKing?") {
	override val successComponent: TreeComponent<DagannothKings> = ShouldLureRex(script)
	override val failedComponent: TreeComponent<DagannothKings> = Attack(script)

	override fun validate(): Boolean {
		return script.target.valid() && !script.target.dead() && me.interacting() == script.target
	}
}

class ShouldLureRex(script: DagannothKings) : Branch<DagannothKings>(script, "LureRex?") {
	override val successComponent: TreeComponent<DagannothKings> = LureRex(script)
	override val failedComponent: TreeComponent<DagannothKings> = Fight(script)

	override fun validate(): Boolean {
		if (script.target.king() == Data.King.Rex && script.safeSpotRex) {
			if (script.target.distanceTo(script.rexTile) > 0 || script.rexSafeTile.distance() > 0) {
				return true
			}
		}
		return false
	}
}


