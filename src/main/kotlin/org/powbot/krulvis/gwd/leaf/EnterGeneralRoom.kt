package org.powbot.krulvis.gwd.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.gwd.GWDScript

class EnterGeneralRoom<S>(script: S) : Leaf<S>(script, "Enter General Room") where S : GWDScript<S> {
	override fun execute() {
		return
		val door = Objects.stream(script.god.doorTile, GameObject.Type.INTERACTIVE).name("Big door").first()
		if (walkAndInteract(door, "Open")) {
			waitFor(10000) {
				script.god.calculateGeneralArea()
				script.god.inside()
			}
		}
	}
}