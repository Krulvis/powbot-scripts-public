package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.rooms.GraveyardRoom.getEdible
import org.powbot.krulvis.mta.rooms.GraveyardRoom.healing
import org.powbot.krulvis.mta.MTA

class DepositBones(script: MTA) : Leaf<MTA>(script, "Deposit bones") {
    override fun execute() {
        val edible = getEdible()
        val missingHealth = Combat.maxHealth() - Combat.health()
        script.logger.info("Depositing bones, have edible=${edible}, heals=${edible.healing()}, missingHealth=${missingHealth}")
        if (edible.valid() && missingHealth >= edible.healing()) {
            edible.interact("Eat")
        }

        val hole = Objects.stream().name("Food chute").action("Deposit").firstOrNull() ?: return
        if (walkAndInteract(hole, "Deposit")) {
            waitForDistance(hole) { !getEdible().valid() }
        }
    }
}