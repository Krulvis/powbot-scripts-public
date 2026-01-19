package org.powbot.krulvis.thermy.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.thermy.Thermy

class Attack(script: Thermy) : Leaf<Thermy>(script, "Attack") {
    override fun execute() {
        val weapon = Inventory.stream().name(if (currentHP() >= 80) "Trident of the Swamp" else "Master wand").first()
        if (weapon.valid()) weapon.interact("Wield")
        if (!targetingThermy() && walkAndInteract(script.thermy, "Attack", allowWalk = false, maxDistance = 20)) {
            script.logger.warn("Attacked")
            waitFor { targetingThermy() }
        }
    }

    fun targetingThermy() = me.interacting() == script.thermy
}