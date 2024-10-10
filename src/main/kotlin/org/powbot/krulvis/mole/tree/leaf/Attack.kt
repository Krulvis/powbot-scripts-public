package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mole.GiantMole

class Attack(script: GiantMole) : Leaf<GiantMole>(script, "Attack") {
	override fun execute() {
		val mole = script.findMole()
		script.logger.info("Attacking mole with animation=${mole.animation()}")
		if (mole.animation() !in script.moleBurrowAnimations && !mole.died() && walkAndInteract(mole, "Attack")) {
			Prayer.prayer(Prayer.Effect.PROTECT_FROM_MELEE, true)
			waitForDistance(mole) { ATContext.me.interacting() == mole }
		}
	}

	private fun Npc.died() = healthBarVisible() && healthPercent() == 0
}