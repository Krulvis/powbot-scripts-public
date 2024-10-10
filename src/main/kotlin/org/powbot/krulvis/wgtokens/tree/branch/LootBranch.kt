package org.powbot.krulvis.wgtokens.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.wgtokens.WGTokens
import org.powbot.krulvis.wgtokens.tree.leaf.Loot


class CanLoot(script: WGTokens) : Branch<WGTokens>(script, "ShouldLoot?") {
    override val failedComponent: TreeComponent<WGTokens> = IsKilling(script)
    override val successComponent: TreeComponent<WGTokens> = Loot(script)

    override fun validate(): Boolean {
        return script.loot().isNotEmpty()
    }

}