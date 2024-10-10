package org.powbot.krulvis.construction.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.construction.Construction
import org.powbot.krulvis.construction.tree.leaf.SendDemon

class ShouldSendDemon(script: Construction) : Branch<Construction>(script, "Should send demon") {
    override val failedComponent: TreeComponent<Construction> = CanBuild(script)
    override val successComponent: TreeComponent<Construction> = SendDemon(script)

    override fun validate(): Boolean {
        return script.plankCount() < 3 && script.hasCape()
    }
}