package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects

object ObjectActions {

    fun GameObjectActionEvent.getObject(): GameObject? {
        return if (interaction == "Use") {
            Objects.stream(tile, 5).name(name).nearest().firstOrNull()
        } else {
            Objects.stream(tile, 5).name(name).action(interaction).nearest().firstOrNull()
        }
    }

    fun GameActionEvent.isClimb() = this is GameObjectActionEvent && interaction.contains("Climb", true)

}