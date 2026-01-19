package org.powbot.krulvis.thermy.tree.leaf

import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Projectiles
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.thermy.Thermy

class Freeze(script: Thermy) : Leaf<Thermy>(script, "Freeze") {
    val ib = Magic.AncientSpell.ICE_BARRAGE
    override fun execute() {
        if (!ib.casting() && ib.cast()) {
            script.logger.warn("Casting Ice Barrage")
            waitFor(600) { ib.casting() }
        }
        if (ib.casting()) {
            val magicXp = Skills.experience(Skill.Magic)
            if (walkAndInteract(script.thermy, "Cast", allowWalk = false, maxDistance = 20)) {
                script.logger.warn("Clicked Ice Barrage")
                if (script.thermy.distance() >= script.minDistance) {
                    script.logger.warn("Walking to best suitable tile after freezing")
                }
                if (waitFor(long()) {
                        Projectiles.stream()
                            .any { it.target() == script.thermy && it.id in script.freezeIds } && Skills.experience(
                            Skill.Magic
                        ) > magicXp + 52
                    }) {
                    Movement.stepNoConfirm(script.closestMaxDistTile)
                    script.freezeTimer.reset()
                }
            } else {
                script.logger.warn("Couldn't cast Ice Barrage...")
            }
        }
    }
}