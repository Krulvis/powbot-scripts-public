package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.Random
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Weapon
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class WaitWhileKilling(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Wait for kill confirm...") {
    override fun execute() {

        val offensivePrayer = script.offensivePrayer
        script.logger.info("defensivePray=${script.protectionPrayer}, offensivePray=${script.offensivePrayer}")
        if (!Prayer.prayerActive(script.protectionPrayer)) {
            Prayer.prayer(script.protectionPrayer, true)
        }
        if (offensivePrayer != null && !Prayer.prayerActive(offensivePrayer)) {
            Prayer.prayer(offensivePrayer, true)
        }

        val specialWeapon = script.specialWeapon
        if (script.equipment != script.meleeEquipment && script.currentTarget.distance() <= 1) {
            script.logger.info("Walking step back because ranging")
//			Movement.step()
        }

        if (script.resurrectSpell != null && script.resurrectedTimer.isFinished()) {
            val spell = script.resurrectSpell!!.spell
            if (spell.cast()) {
                script.resurrectedTimer.reset(0.6 * Skills.level(Skill.Magic) * 1000 + Random.nextInt(1000, 10000))
            }
        }

        val specEquipped = script.equipment.any { it.item.id == specialWeapon?.id }
        script.logger.info("Special Weapon = ${specialWeapon}, can Spec=${specialWeapon?.canSpecial()}, inEquipment=${specEquipped}")
        if (specialWeapon != null && specialWeapon.canSpecial() && specEquipped) {
            val makesSense =
                !specialWeapon.statReducer || (!script.reducedStats && script.currentTarget.healthPercent() >= 75)
            script.logger.info("Special Attack makeSense=$makesSense because reducedStats=${specialWeapon.statReducer}, reducedStats=${script.reducedStats}, hp=${script.currentTarget.healthPercent()}")
            if (makesSense) {
                Combat.specialAttack(true)
            } else {

            }
        }
    }
}