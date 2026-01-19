package org.powbot.krulvis.spices.tree.branches

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Components
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.spices.Spices
import org.powbot.krulvis.spices.tree.leafs.Enter
import org.powbot.krulvis.spices.tree.leafs.Feed

class Killing(script: Spices) : Branch<Spices>(script, "Killing Rat?") {
    override val failedComponent: TreeComponent<Spices> = Entering(script)

    override val successComponent: TreeComponent<Spices> = ShouldFeed(script)

    override fun validate(): Boolean {
        return script.fighting()
    }
}


class ShouldFeed(script: Spices) : Branch<Spices>(script, "Should feed?") {
    override val failedComponent: TreeComponent<Spices> = SimpleLeaf(script, "Chilling") {}
    override val successComponent: TreeComponent<Spices> = Feed(script)

    override fun validate(): Boolean {
        val cat = script.cat() ?: return false
        return cat.healthPercent() <= 60.0
    }
}


class Entering(script: Spices) : Branch<Spices>(script, "Entering?") {
    override val failedComponent: TreeComponent<Spices> = Enter(script)
    override val successComponent: TreeComponent<Spices> = SimpleLeaf(script, "Clicking component") {
        val widget = Components.stream(219, 1).id(1)
                .firstOrNull()
        if (widget?.click() == true || Chat.clickContinue()) {
            waitFor(5000) { script.fighting() || Chat.clickContinue() }
        }
    }

    override fun validate(): Boolean {
        return Chat.canContinue() || Components.stream(219, 1).firstOrNull()?.visible() == true
    }
}