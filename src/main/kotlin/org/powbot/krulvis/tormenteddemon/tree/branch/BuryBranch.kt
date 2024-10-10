package org.powbot.krulvis.tormenteddemon.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class ShouldScatterAshes(script: TormentedDemon) : Branch<TormentedDemon>(script, "ShouldScatterAshes?") {

	var remains = emptyList<Item>()
	var ashes = emptyList<Item>()

	val demonicSacrifice = Magic.ArceuusSpell.DEMONIC_OFFERING

	override val successComponent: TreeComponent<TormentedDemon> = SimpleLeaf(script, "Scatter Ashes") {
		if (demonicSacrifice.canCast()) {
			if (demonicSacrifice.cast()) {
				Utils.waitFor { findAshes().isEmpty() }
			}
		} else {
			remains.forEach { ashes ->
				if (ashes.interact("Scatter")) {
					Utils.waitFor { findAshes().isEmpty() }
				}
			}
		}
	}
	override val failedComponent: TreeComponent<TormentedDemon> = ShouldBank(script)
	private fun findAshes() = Inventory.stream().name("Malicious ashes").toList()

	override fun validate(): Boolean {
		if (!script.buryBones) return false
		ashes = findAshes()
		return if (demonicSacrifice.canCast()) {
			ashes.size >= 3
		} else {
			ashes.isNotEmpty()
		}
	}
}