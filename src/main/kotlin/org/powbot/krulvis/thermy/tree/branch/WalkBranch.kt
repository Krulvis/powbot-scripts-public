package org.powbot.krulvis.thermy.tree.branch

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.thermy.Thermy

class AtSmokeDevils(script: Thermy): Branch<Thermy>(script, "AtSmokeDevils?") {
    override val failedComponent: TreeComponent<Thermy>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Thermy>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        return Npcs.stream().name("Smoke devil").isNotEmpty()
    }
}
