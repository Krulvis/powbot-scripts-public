package org.powbot.krulvis.darkcrabs.tree.branch

import org.powbot.api.rt4.Worlds
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.Data.FISHING_ANIMATION
import org.powbot.krulvis.darkcrabs.Data.RESOURCE_AREA
import org.powbot.krulvis.darkcrabs.tree.leaf.Fish
import org.powbot.krulvis.darkcrabs.tree.leaf.WalkToResourceArea

class Fishing(script: DarkCrabs) : Branch<DarkCrabs>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<DarkCrabs> = InResourceArea(script)
    override val successComponent: TreeComponent<DarkCrabs> = SimpleLeaf(script, "Waiting") {
        Worlds.open()
        sleep(100)
    }

    override fun validate(): Boolean {
        return me.animation() == FISHING_ANIMATION
    }
}

class InResourceArea(script: DarkCrabs) : Branch<DarkCrabs>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<DarkCrabs> = WalkToResourceArea(script)
    override val successComponent: TreeComponent<DarkCrabs> = Fish(script)

    override fun validate(): Boolean {
        return RESOURCE_AREA.contains(me)
    }
}