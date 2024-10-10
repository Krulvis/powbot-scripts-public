package org.powbot.krulvis.grotesqueguardians.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.grotesqueguardians.GrotesqueGuardians
import org.powbot.krulvis.grotesqueguardians.tree.leaf.HandleBank

class ShouldBank(script: GrotesqueGuardians) : Branch<GrotesqueGuardians>(script, "ShouldBank?") {
	override val failedComponent: TreeComponent<GrotesqueGuardians>
		get() = TODO("Not yet implemented")
	override val successComponent: TreeComponent<GrotesqueGuardians> = OpenedBank(script)

	override fun validate(): Boolean {
		return script.banking
	}
}

class OpenedBank(script: GrotesqueGuardians) : Branch<GrotesqueGuardians>(script, "ShouldBank?") {
	override val failedComponent: TreeComponent<GrotesqueGuardians> = SimpleLeaf(script, "OpenBank") {
		Bank.openNearest(script.bankTeleport)
	}
	override val successComponent: TreeComponent<GrotesqueGuardians> = HandleBank(script)

	override fun validate(): Boolean {
		return Bank.opened()
	}
}
