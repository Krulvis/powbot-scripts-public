package org.powbot.krulvis.cerberus.tree.leaf

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.cerberus.Cerberus

class Attack(script: Cerberus) : Leaf<Cerberus>(script, "Attack") {
    override fun execute() {
        val tick = script.ticks + if (script.centerTile == me.tile()) 2 else 1
        val cerb = script.cerberus
        cerb.bounds(-49, 52, -282, -220, -92, 112)
        if (!Prayer.quickPrayer()) {
            Prayer.quickPrayer(true)
        }
        if (script.specWeapon.canSpecial() && script.specWeapon.inEquipment()) {
            Combat.specialAttack(true)
        }
        if (cerb.interact("Attack")) {
            waitFor { script.ticks >= tick }
        } else {
            script.logger.info("Failed to attack cerberus")
        }
    }
}