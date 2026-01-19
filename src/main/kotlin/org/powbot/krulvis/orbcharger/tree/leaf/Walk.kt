package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.orbcharger.OrbCrafter

class Walk(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Walking") {
    override fun execute() {
        Movement.walkTo(script.orb.obeliskTile)
    }
}