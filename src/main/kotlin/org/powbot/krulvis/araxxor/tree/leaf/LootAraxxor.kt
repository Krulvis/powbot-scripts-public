package org.powbot.krulvis.araxxor.tree.leaf

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data.ARAXXOR

class LootAraxxor(script: Araxxor) : Leaf<Araxxor>(script, "Harvest") {
    override fun execute() {
        script.enrage = false

        if (!script.harvestTimer.isFinished()) {
            if (waitFor(long()) { script.araxxor.actions.contains("Harvest") }) {
                script.harvestTimer.stop()
            }
        }
        val lootable = script.araxxor
        lootable.bounds(-32, 32, -64, 0, -32, 32)
        if (lootable.valid()) {
            if (walkAndInteract(lootable, "Harvest")) {
                if (waitForDistance(lootable, extraWait = 4000) {
                        Npcs.stream().name(ARAXXOR).isEmpty()
                    }) {
                    script.logger.info("Harvested successfully")
                }
            }
        } else {
            script.logger.info("Araxxor not found, waiting for it to appear...")
        }
    }


}