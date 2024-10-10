package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tithe.TitheFarmer

class Leave(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Leaving") {

    override fun execute() {
        Objects.stream(25).name("Farm door").nearest().findFirst().ifPresent {
            if (walkAndInteract(it, "Open", useMenu = false)) {
                waitFor(long()) { script.getPoints() == -1 }
            }
        }
    }
}