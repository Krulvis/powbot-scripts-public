package org.powbot.krulvis.tormenteddemon.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.tormenteddemon.TormentedDemon
import org.powbot.krulvis.tormenteddemon.tree.branch.IsKilling

class Attack(script: TormentedDemon) : Leaf<TormentedDemon>(script, "Attacking") {
	override fun execute() {
		val target = script.target()
		target.bounds(-32, 32, -192, 0, -32, 32)
		if (walkAndInteract(target, "Attack")) {
			script.currentTarget = target
			Condition.wait({
				IsKilling.killing()
			}, 250, 10)
		}
	}

}