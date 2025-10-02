package org.powbot.krulvis.araxxor.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data.ARAXXOR

class KillTarget(script: Araxxor) : Leaf<Araxxor>(script, "KillTarget") {
    override fun execute() {
        val targ = script.target
        targ.bounds(-32, 32, -64, 0, -32, 32)

        if (targ.name == ARAXXOR && script.specWeapon.canSpecial()
            && !Combat.specialAttack() && script.specWeapon.inEquipment()
        ) {
            Combat.specialAttack(true)
        }

        if (me.interacting() == targ || script.ticks < script.myNextAttackTick) {
            sleep(20)
            return
        }

        if (!script.drippingTimer.isFinished()) {
            script.logger.info("Dripping, waiting for timer to finish before attacking again...")
            sleep(20)
            return
        } else if (script.enrage) {
            val dest = Movement.destination()
            if (dest != Tile.Nil && me.trueTile() != dest) {
                script.logger.info("Still running to destination=${dest}, distance=${dest.distance()}")
//                waitFor(dest.distance().toInt() * 300) { me.trueTile() == dest || Movement.destination() == Tile.Nil }
            }
            if (!script.enrageWalkTimeout.isFinished()) {
                waitFor { script.enrageWalkTimeout.isFinished() }
            }
        }

        val dead = targ.dead()
        val interact =
            walkAndInteract(targ, "Attack", allowWalk = targ.name == ARAXXOR, maxDistance = 17, perfectInteract = true)
        script.logger.info("Target=${targ.name}, interact=${interact}, dead=${dead}")
        if (!dead && interact)
            waitFor(650) { me.interacting() == targ }
    }


}