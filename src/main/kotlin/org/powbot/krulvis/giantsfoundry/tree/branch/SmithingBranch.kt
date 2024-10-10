package org.powbot.krulvis.giantsfoundry.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.giantsfoundry.Action
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.tree.leaf.FixTemperature
import org.powbot.krulvis.giantsfoundry.tree.leaf.HandIn
import org.powbot.krulvis.giantsfoundry.tree.leaf.SmithAndWait

class IsSmithing(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Is Smithing") {
    override val failedComponent: TreeComponent<GiantsFoundry> = HasAssignment(script)
    override val successComponent: TreeComponent<GiantsFoundry> = CanHandIn(script)

    override fun validate(): Boolean {
        return script.isSmithing()
    }
}

class CanHandIn(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Can hand in") {
    override val failedComponent: TreeComponent<GiantsFoundry> = CanPerform(script)
    override val successComponent: TreeComponent<GiantsFoundry> = HandIn(script)

    override fun validate(): Boolean {
        return script.currentAction == null
    }
}

class CanPerform(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Can perform") {
    override val failedComponent: TreeComponent<GiantsFoundry> = FixTemperature(script)
    override val successComponent: TreeComponent<GiantsFoundry> = SmithAndWait(script)

    override fun validate(): Boolean {
        val action = script.currentAction
        if (action != null && action.min == -1) {
            Action.calculateMinMax()
        }
        return action?.canPerform() == true
    }
}