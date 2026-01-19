package org.powbot.krulvis.dagannothkings.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.dagannothkings.DagannothKings

class AtKings(script: DagannothKings) : Branch<DagannothKings>(script, "AtKings?") {
	override val failedComponent: TreeComponent<DagannothKings> = AtKingsLadder(script)
	override val successComponent: TreeComponent<DagannothKings> = ShouldLoot(script)

	override fun validate(): Boolean {
		return true
	}
}

class AtKingsLadder(script: DagannothKings) : Branch<DagannothKings>(script, "AtKingsLadder?") {
	override val failedComponent: TreeComponent<DagannothKings>
		get() = TODO("Not yet implemented")
	override val successComponent: TreeComponent<DagannothKings> = SimpleLeaf<DagannothKings>(script, "Enter Kings Lair") {

	}

	override fun validate(): Boolean {
		return false
	}
}