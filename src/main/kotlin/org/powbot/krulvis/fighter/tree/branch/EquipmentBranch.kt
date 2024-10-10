package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.fighter.Fighter

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
		invAmmo.interact("Equip")
		Utils.waitFor { !validate() }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldEquipGear(script)

	var invAmmo = Item.Nil
	override fun validate(): Boolean {
		val ammo = script.ammo ?: return false
		invAmmo = ammo.item.getInvItem() ?: return false
		return invAmmo.valid() && (
			Inventory.isFull()
				|| invAmmo.stack > 5
				|| !ammo.item.inEquipment()
			)
	}
}


class ShouldEquipGear(script: Fighter) : Branch<Fighter>(script, "Should equip gear?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip gear") {
		missingGear.forEach { it.item.equip(false) }
		equipTimer.reset()
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldDropTrash(script)
	var missingGear: List<EquipmentRequirement> = listOf()

	val equipTimer = Timer(600)
	override fun validate(): Boolean {
		if (!equipTimer.isFinished()) return false
		missingGear = script.equipment.filter { !it.meets() }
		return missingGear.isNotEmpty()
	}
}