package org.powbot.krulvis.smither.tree.branch

import org.powbot.api.Production
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.smither.Smither
import org.powbot.krulvis.smither.tree.leaf.ClickWidget

class ShouldSmith(script: Smither) : Branch<Smither>(script, "Should Smith") {
    override val failedComponent: TreeComponent<Smither> = SimpleLeaf(script, "Chilling") {}
    override val successComponent: TreeComponent<Smither> = IsWidgetOpen(script)

    override fun validate(): Boolean {
        return Production.stoppedUsing(script.bar.id)
    }
}

class IsWidgetOpen(script: Smither) : Branch<Smither>(script, "IsWidgetOpen") {

    override val failedComponent: TreeComponent<Smither> = SimpleLeaf(script, "Smith Anvil") {
        val anvil = Objects.stream().name("Anvil").action("Smith").nearest().firstOrNull()
        if (anvil != null && Utils.walkAndInteract(anvil, "Smith")) {
            waitFor(long()) { widgetOpen() }
        }
    }

    override val successComponent: TreeComponent<Smither> = ClickWidget(script)

    override fun validate(): Boolean {
        return widgetOpen()
    }

    companion object {
        fun widgetOpen(): Boolean = Components.stream().text("What would you like to make?").isNotEmpty()
    }
}
