package org.powbot.krulvis.smelter.tree.branch

import org.powbot.api.Production
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Item.Companion.CANNONBALL
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.smelter.Smelter

class ShouldSmelt(script: Smelter) : Branch<Smelter>(script, "Should Smelt") {
    override val failedComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Chilling") {}
    override val successComponent: TreeComponent<Smelter> = IsWidgetOpen(script)

    override fun validate(): Boolean {
        return if (script.cannonballs) {
            val stoppedMakingBalls = Production.stoppedMaking(
                CANNONBALL, 10000
            )
            script.logger.info("Stopped making balls = $stoppedMakingBalls")
            stoppedMakingBalls
        } else Production.stoppedUsing(script.bar.primary.id)
    }
}

class IsWidgetOpen(script: Smelter) : Branch<Smelter>(script, "IsWidgetOpen") {

    override val failedComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Smelt Furnace") {
        val anvil = Objects.stream().name("Furnace").action("Smelt").nearest().firstOrNull()
        script.logger.info("Going to interact with anvil")
        if (anvil != null && Utils.walkAndInteract(anvil, "Smelt")) {
            script.logger.info("Interacted with anvil")
            waitFor(long()) { getComponent() != null }
        }
    }

    override val successComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Clicking Widget") {
        if (comp?.interact(if (script.cannonballs) "Make sets:" else "Smelt", false) == true) {
            waitFor(long()) { !Production.stoppedUsing(script.bar.primary.id) }
        }
    }

    var comp: Component? = null

    override fun validate(): Boolean {
        script.logger.info("Getting component...")
        comp = getComponent()
        script.logger.info("Component is open: ${comp?.visible()}")
        return comp?.visible() == true
    }

    fun getComponent(): Component? = if (script.cannonballs) Components.stream(270).action("Make sets:")
        .firstOrNull() else script.bar.getSmeltComponent()

}
