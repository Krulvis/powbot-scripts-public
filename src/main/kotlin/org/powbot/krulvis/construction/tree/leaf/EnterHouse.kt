package org.powbot.krulvis.construction.tree.leaf

import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.construction.Construction

class EnterHouse(script: Construction) : Leaf<Construction>(script, "Sending demon") {
    override fun execute() {
        val portal = Objects.stream().name("Portal").action("Enter").firstOrNull()
        if (portal?.interact("Enter") == true) {
            waitFor { House.isInside() }
        }
    }
}