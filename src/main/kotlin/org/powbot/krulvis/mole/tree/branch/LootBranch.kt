package org.powbot.krulvis.mole.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.mole.GiantMole
import org.powbot.krulvis.mole.tree.leaf.Loot


class CanLoot(script: GiantMole) : Branch<GiantMole>(script, "CanLoot?") {
    override val failedComponent: TreeComponent<GiantMole> = ShouldHighAlch(script, AtMole(script))
    override val successComponent: TreeComponent<GiantMole> = Loot(script)

    override fun validate(): Boolean {
        return script.lootList.isNotEmpty()
    }
}

