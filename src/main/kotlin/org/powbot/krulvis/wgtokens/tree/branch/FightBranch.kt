package org.powbot.krulvis.wgtokens.tree.branch

import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.wgtokens.WGTokens
import org.powbot.krulvis.wgtokens.tree.leaf.Animate


class IsKilling(script: WGTokens) : Branch<WGTokens>(script, "IsKilling?") {
    override val failedComponent: TreeComponent<WGTokens> = Animate(script)
    override val successComponent: TreeComponent<WGTokens> = SimpleLeaf(script, "In Combat") {
        waitFor(long()) { currentHP() / maxHP().toDouble() < .4 || script.loot().isNotEmpty() }
    }

    override fun validate(): Boolean {
        return !script.armour.inInventory() || (script.armour.npc() != null && Players.local()
            .healthBarVisible())
    }
}