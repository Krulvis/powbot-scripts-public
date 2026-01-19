package org.powbot.krulvis.daeyalt

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement.Companion.ids


class WearingEquipment(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "WearingEquipment?") {
	override val failedComponent: TreeComponent<DaeyaltMiner> = OpenedBank(script)
	override val successComponent: TreeComponent<DaeyaltMiner> = AtMine(script)

	override fun validate(): Boolean {
		return script.equipment.all { it.meets() }
	}

}


class OpenedBank(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "OpenedBank?") {
	override val failedComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "OpeningBank") {
		Bank.open()
	}
	override val successComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "HandlingBank") {
		val ids = script.equipment.ids()
		if (!Bank.depositAllExcept(*ids)) {
			return@SimpleLeaf
		}

		script.equipment.all { it.withdrawAndEquip(true) }
	}

	override fun validate(): Boolean = Bank.opened()

}

