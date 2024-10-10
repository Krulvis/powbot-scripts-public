package org.powbot.krulvis.gwd.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.gwd.GWDScript

class Kill<S>(script: S, val allowMovement: Boolean) :
	Leaf<S>(script, "Killing") where S : GWDScript<S> {

	var target: Npc = Npc.Nil
	override fun execute() {
		target.bounds(-32, 32, -192, 0, -32, 32)
		val tile = me.trueTile()
		if (me.interacting() != target) {
			if (if (allowMovement) walkAndInteract(target, "Attack") else target.interact("Attack")) {
				waitFor { me.interacting() == target }
			} else {
				Movement.step(tile)
			}
		}
	}
}