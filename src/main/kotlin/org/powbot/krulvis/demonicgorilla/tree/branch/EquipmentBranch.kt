package org.powbot.krulvis.demonicgorilla.tree.branch

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class ShouldEquipAmmo(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Should equip ammo?") {
	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "Equip ammo") {
		invAmmo.interact("Equip")
		Utils.waitFor { !validate() }
	}
	override val failedComponent: TreeComponent<DemonicGorilla> = ShouldHighAlch(script, ShouldEquipGear(script))

	var invAmmo = Item.Nil
	override fun validate(): Boolean {
		val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER && it.item.stackable }
			?: return false
		invAmmo = ammo.item.getInvItem() ?: Item.Nil
		return invAmmo.valid() && (
			Inventory.isFull()
				|| invAmmo.stack > 5
				|| !ammo.item.inEquipment()
			)
	}
}

class ShouldEquipGear(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Should equip gear?") {
	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "Equip gear") {
		script.equipment.forEach { it.item.equip(false) }
		equipTimer.reset()
	}
	override val failedComponent: TreeComponent<DemonicGorilla> = ShouldScatterAshes(script)

	val equipTimer = Timer(600)
	override fun validate(): Boolean {
		return equipTimer.isFinished() && script.currentTarget.valid() && script.equipment.any { !it.meets() }
	}
}