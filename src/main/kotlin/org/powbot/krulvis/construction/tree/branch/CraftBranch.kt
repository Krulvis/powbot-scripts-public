package org.powbot.krulvis.construction.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.construction.Construction
import org.powbot.krulvis.construction.tree.leaf.Build
import org.powbot.krulvis.construction.tree.leaf.Remove

class CanBuild(script: Construction) : Branch<Construction>(script, "Can build?") {
    override val failedComponent: TreeComponent<Construction> = Remove(script)
    override val successComponent: TreeComponent<Construction> = Build(script)

    override fun validate(): Boolean {
        return script.buildSpace() != null && script.hasMats()
    }
}