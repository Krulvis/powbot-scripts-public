package org.powbot.krulvis.construction.tree.leaf

import org.powbot.api.rt4.Components
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.construction.Construction

class Remove(script: Construction) : Leaf<Construction>(script, "Remove cape") {

    fun removeComp() = Components.stream(219).text("Yes").firstOrNull()


    override fun execute() {
        var removeComp = removeComp()
        if (removeComp == null || !removeComp.visible()) {
            val post = script.builtObj() ?: return
            if (walkAndInteract(post,"Remove")) {
                waitFor { removeComp()?.visible() == true }
            }
        }
        removeComp = removeComp()
        if (removeComp?.visible() == true) {
            removeComp.click()
            waitFor { script.buildSpace() != null }
        }
    }
}