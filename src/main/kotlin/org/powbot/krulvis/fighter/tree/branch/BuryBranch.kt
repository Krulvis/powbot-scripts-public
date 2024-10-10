package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.fighter.Fighter

class ShouldBuryBones(script: Fighter) : Branch<Fighter>(script, "Should Bury bones?") {

	var remains = emptyList<Item>()
	var ashes = emptyList<Item>()

	val demonicSacrifice = Magic.ArceuusSpell.DEMONIC_OFFERING

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Bury bones") {
		if (demonicSacrifice.canCast()) {
			if (ashes.size >= 3 && demonicSacrifice.cast()) {
				Utils.waitFor { filterRemains().size < remains.size }
			}
		} else {
			remains.forEachIndexed { i, bone ->
				val count = Inventory.getCount(bone.id)
				val action = bone.buryAction()
				script.logger.info("$action on ${bone.name()}")
				if (bone.interact(action)) {
					Utils.waitFor { count > Inventory.getCount(bone.id) }
					if (i < this.remains.size - 1)
						Utils.sleep(1500)
				}
			}
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldBank(script)


	private val possibleBuryActions = listOf("Bury", "Scatter")
	private fun Item.buryAction(): String? = actions().firstOrNull { it in possibleBuryActions }
	private fun filterRemains() = Inventory.stream().filtered { item ->
		item.buryAction() != null
	}.toList()

	override fun validate(): Boolean {
		if (!script.buryBones) return false
		remains = filterRemains()
		ashes = remains.filter { it.name().lowercase().contains("ashes") }
		return if (demonicSacrifice.canCast()) {
			ashes.size >= 3
		} else {
			remains.isNotEmpty()
		}
	}
}