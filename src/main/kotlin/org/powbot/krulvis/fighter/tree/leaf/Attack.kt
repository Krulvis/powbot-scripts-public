package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.branch.IsKilling

class Attack(script: Fighter) : Leaf<Fighter>(script, "Attacking") {
	override fun execute() {
		val target = script.target()
		if (script.canActivateQuickPrayer()) {
			Prayer.quickPrayer(true)
		}
		if (script.useCannon) {
			Combat.autoRetaliate(true)
			//Stand on good position
			return
		}

		target.bounds(-32, 32, -192, 0, 0 - 32, 32)
		if (attack(target)) {
			script.currentTarget = target
			Condition.wait({
				IsKilling.killing(script.superiorActive) || script.shouldReturnToSafespot()
			}, 250, 10)
			if (script.shouldReturnToSafespot()) {
				Movement.step(script.centerTile, 0)
			}
		}
	}

	fun attack(target: Npc?): Boolean {
		val t = target ?: return false
		return if (script.useSafespot) {
			target.interact("Attack")
		} else if (!t.reachable()) {
			LocalPathFinder.findWalkablePath(t.tile()).traverse()
		} else {
			ATContext.walkAndInteract(t, "Attack")
		}
	}
}