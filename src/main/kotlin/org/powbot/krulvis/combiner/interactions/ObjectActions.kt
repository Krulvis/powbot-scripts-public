package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Poh

object ObjectActions {

	fun GameObjectActionEvent.getObject(): GameObject? {
		return if (interaction == "Use") {
			if (Poh.inside()) {
				Objects.stream(25).name(name).nearest().firstOrNull()
			} else {
				Objects.stream(tile, 5).name(name).nearest().firstOrNull()
			}
		} else if (Poh.inside()) {
			Objects.stream(25).name(name).action(interaction).nearest().firstOrNull()
		} else {
			Objects.stream(tile, 5).name(name).action(interaction).nearest().firstOrNull()
		}
	}

	fun GameActionEvent.isClimb() = this is GameObjectActionEvent && interaction.contains("Climb", true)

}