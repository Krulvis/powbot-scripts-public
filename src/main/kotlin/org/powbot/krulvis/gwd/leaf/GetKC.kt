package org.powbot.krulvis.gwd.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.gwd.GWDScript
import kotlin.math.min

class GetKC<S>(script: S) : Leaf<S>(script, "Getting KC") where S : GWDScript<S> {
	private val ignoreTargets = arrayOf("Imp", "Icefiend")
	override fun execute() {
		val interacting = me.interacting()
		val attackingMe = Npcs.stream().interactingWithMe().firstOrNull()
		if (attackingMe != null && interacting != attackingMe) {
			script.logger.info("Someone attacking me! $interacting")
			attackingMe.attack()
		} else if (interacting is Npc && interacting.valid()) {
			if (interacting.canAttack()) {
				script.logger.info("Already attacking: $interacting")
				sleep(150)
			} else {
				script.logger.info("Stepping to avoid keeping attacking: $interacting")
				Movement.step(me.tile())
			}
		} else {
			val targets = Npcs.stream().name(*script.god.kcNames).filtered { it.canAttack() }
			val hpVisible = targets.firstOrNull { it.healthBarVisible() }
			val target = attackingMe ?: hpVisible ?: (targets.minByOrNull { it.healthPercent() } ?: return)
			target.attack()
		}
	}

	private fun Npc.attack(): Boolean {
		script.logger.info("Going to attack: $this")
		if (walkAndInteract(this, "Attack")) {
			return waitFor(long()) { me.interacting() == this && healthBarVisible() }
		}
		return false
	}

	private fun Npc.canAttack(): Boolean {
		val t = interacting()
		val hp = t.healthPercent()
		val minHp = if (t.name in ignoreTargets) 20 else 0
		return t.valid() && hp > minHp && trueTile() == tile()
	}

}