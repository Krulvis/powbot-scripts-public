package org.powbot.krulvis.darkcrabs.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.Data.FISHING_ANIMATION
import org.powbot.krulvis.darkcrabs.Data.FISHING_SPOT

class Fish(script: DarkCrabs) : Leaf<DarkCrabs>(script, "Fish") {
    override fun execute() {
        Container.FISH_BARREL.emptied = false
        val spot = Npcs.stream().name("Fishing spot").first()
        if (walkAndInteract(spot, "Cage")) {
            waitForDistance(spot) { me.animation() == FISHING_ANIMATION }
        } else {
            Movement.step(FISHING_SPOT)
            waitFor { Npcs.stream().name("Fishing spot").first().valid() }
        }
    }

}
