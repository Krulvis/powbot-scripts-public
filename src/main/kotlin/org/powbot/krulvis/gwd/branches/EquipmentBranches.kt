package org.powbot.krulvis.gwd.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.gwd.GWDScript

class ShouldEquipGear<S>(
	script: S,
	private val gearset: MutableList<EquipmentRequirement>,
	override val failedComponent: TreeComponent<S>
) :
	Branch<S>(script, "ShouldEquipGear?") where S : GWDScript<S> {
	override val successComponent = SimpleLeaf(script, "EquipGear") {
		missing.forEach { it.item.equip(false) }
		equipTimer.reset()
	}

	private var missing = emptyList<EquipmentRequirement>()
	private val equipTimer = Timer(600)
	override fun validate(): Boolean {
		if (!equipTimer.isFinished()) return false
		missing = gearset.filter { !it.meets() }
		script.logger.info("Checking if needs to equip gear=[${gearset.joinToString { it.item.itemName }}], missing=[${missing.joinToString { it.item.itemName }}]")
		return missing.isNotEmpty()
	}
}