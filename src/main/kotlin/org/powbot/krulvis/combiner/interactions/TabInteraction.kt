package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.WidgetActionEvent
import org.powbot.api.rt4.Game

object TabInteraction {

	fun WidgetActionEvent.isTabOpening(): Boolean {
		return getTabOpening() != null
	}

	fun WidgetActionEvent.getTabOpening(): Game.Tab? {
		if (name.isNotEmpty()) {
			return null
		}
		return Game.Tab.values().firstOrNull { it.name.equals(interaction, true) }
	}
}