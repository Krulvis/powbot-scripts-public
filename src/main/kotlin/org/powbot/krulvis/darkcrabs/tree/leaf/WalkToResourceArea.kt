package org.powbot.krulvis.darkcrabs.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.Data.DESERTED_KEEP
import org.powbot.krulvis.darkcrabs.Data.EDGE_LEVER
import org.powbot.krulvis.darkcrabs.Data.RESOURCE_AREA
import org.powbot.krulvis.darkcrabs.Data.RESOURCE_AREA_DOOR
import org.powbot.krulvis.darkcrabs.Data.cutWeb
import org.powbot.krulvis.darkcrabs.Data.resourceGate

class WalkToResourceArea(script: DarkCrabs) : Leaf<DarkCrabs>(script, "WalkToResourceArea") {
    override fun execute() {
        Worlds.open()
        if (DESERTED_KEEP.contains(me)) {
            exitDesertedKeep()
        } else if (Combat.wildernessLevel() > 0) {
            val gate = resourceGate()
            if (gate.valid()) {
                if (walkAndInteract(gate, "Open")) {
                    waitForDistance(gate) { RESOURCE_AREA.contains(me) }
                }
            }
        } else {
            val lever = Objects.stream(EDGE_LEVER, GameObject.Type.WALL_DECORATION).name("Lever").first()
            if (walkAndInteract(lever, "Pull")) {
                waitForDistance(lever) { DESERTED_KEEP.contains(me) }
            }
        }
    }

    private fun exitDesertedKeep(): Boolean {
        if (!cutWeb()) {
            return false
        } else {
            Movement.step(RESOURCE_AREA_DOOR)
        }
        return !DESERTED_KEEP.contains(me)
    }
}