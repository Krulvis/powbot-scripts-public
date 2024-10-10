package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.demonicgorilla.DemonicGorilla
import org.powbot.krulvis.demonicgorilla.tree.branch.IsKilling

class Attack(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Attacking") {
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