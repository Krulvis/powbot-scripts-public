package org.powbot.krulvis.construction.tree.leaf

import org.powbot.api.rt4.Components
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.construction.Construction

class Build(script: Construction) : Leaf<Construction>(script, "Build cape") {

    fun craftComp() = Components.stream(458).name("<col=ff9040>Mythical cape</col>").firstOrNull()


    override fun execute() {
        var craftComp = craftComp()
        if (craftComp == null || !craftComp.visible()) {
            val post = script.buildSpace() ?: return
            if (ATContext.walkAndInteract(post,"Build")) {
                waitFor { craftComp()?.visible() == true }
            }
        }
        craftComp = craftComp()
        if (craftComp?.visible() == true) {
            craftComp.click()
            val waitForBuild = waitFor { script.builtObj() != null }
            script.logger.info("Wait for build completed=$waitForBuild")
        }
    }
}