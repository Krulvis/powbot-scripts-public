package org.powbot.krulvis.darkcrabs.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.Data.DESERTED_KEEP
import org.powbot.krulvis.darkcrabs.Data.KEEP_LEVER
import org.powbot.krulvis.darkcrabs.Data.RESOURCE_AREA
import org.powbot.krulvis.darkcrabs.Data.cutWeb
import org.powbot.krulvis.darkcrabs.Data.resourceGate

class OpenBank(script: DarkCrabs) : Leaf<DarkCrabs>(script, "Open Bank") {
    override fun execute() {
        val bank = Bank.getBank()
        if (bank.valid()) {
            Bank.openNearest()
        } else if (RESOURCE_AREA.contains(me)) {
            Worlds.open()
            exitResourceArea()
        } else if (Combat.wildernessLevel() > 0) {
            Worlds.open()
            if (!DESERTED_KEEP.contains(me)) {
                enterDesertedKeep()
            } else {
                exitWilderness()
            }
        }
    }

    private fun exitResourceArea(): Boolean {
        val gate = resourceGate()
        if (walkAndInteract(gate, "Open")) {
            return waitForDistance(gate) { !RESOURCE_AREA.contains(me) }
        }
        return false
    }

    private fun enterDesertedKeep(): Boolean {
        if (!cutWeb()) {
            return false
        } else {
            Movement.step(KEEP_LEVER)
        }
        return DESERTED_KEEP.contains(me)
    }

    private fun exitWilderness(): Boolean {
        val lever = Objects.stream(KEEP_LEVER, GameObject.Type.WALL_DECORATION).name("Lever").first()
        if (walkAndInteract(lever, "Edgeville")) {
            return waitForDistance(lever) { !DESERTED_KEEP.contains(me) }
        }
        return false
    }
}
