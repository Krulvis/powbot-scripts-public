package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class DodgeProjectile(script: Fighter) : Leaf<Fighter>(script, "DodgeProjectile") {

	override fun execute() {
		Movement.step(script.projectileSafespot, 0)
	}

}