package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.mole.GiantMole

class WaitForLoot(script: GiantMole) : Leaf<GiantMole>(script, "Waiting...") {
    override fun execute() {
        script.disablePrayers()

    }
}