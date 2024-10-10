package org.powbot.krulvis.darkcrabs.tree.leaf

import org.powbot.api.rt4.World
import org.powbot.api.rt4.Worlds
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.darkcrabs.DarkCrabs

class Hop(script: DarkCrabs) : Leaf<DarkCrabs>(script, "Hop") {
    override fun execute() {
        val currentWorld = Worlds.current()
        val type = if (Worlds.isCurrentWorldMembers()) World.Type.MEMBERS else World.Type.FREE
        val world = Worlds.stream().filtered { it != currentWorld && it.type() == type && it.inViewport() }.first()
        world.hop()
    }

    companion object{
        fun openWorldHopper(){
        }
    }

}
