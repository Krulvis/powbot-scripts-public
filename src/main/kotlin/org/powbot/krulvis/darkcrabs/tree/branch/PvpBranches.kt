package org.powbot.krulvis.darkcrabs.tree.branch

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.tree.leaf.Hop
import org.powbot.mobile.script.ScriptManager

class ShouldHop(script: DarkCrabs) : Branch<DarkCrabs>(script, "ShouldHop?") {
    override val failedComponent: TreeComponent<DarkCrabs> = ShouldStop(script)
    override val successComponent: TreeComponent<DarkCrabs> = Hop(script)

    override fun validate(): Boolean {
        val wildernessLevel = Combat.wildernessLevel()
        val myLevel = me.combatLevel
        return Players.stream().notLocalPlayer()
            .filtered { it.combatLevel >= myLevel - wildernessLevel && it.combatLevel <= myLevel + wildernessLevel }
            .isNotEmpty() && !me.healthBarVisible()
    }
}

class ShouldStop(script: DarkCrabs) : Branch<DarkCrabs>(script, "ShouldStop?") {
    override val failedComponent: TreeComponent<DarkCrabs> = ShouldBank(script)
    override val successComponent: TreeComponent<DarkCrabs> = SimpleLeaf(script, "Died") { ScriptManager.stop() }

    override fun validate(): Boolean {
        return script.died
    }
}
