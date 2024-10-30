package org.powbot.krulvis.cerberus.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.cerberus.Cerberus

class Attack(script: Cerberus) : Leaf<Cerberus>(script, "Attack") {
	override fun execute() {
		if (script.flinch) {

		}
	}
}