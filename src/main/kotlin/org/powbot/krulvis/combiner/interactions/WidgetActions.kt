package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.WidgetActionEvent

object WidgetActions {

	fun WidgetActionEvent.isHomeTeleport(): Boolean {
		return name == "Teleport to House" && interaction == "Cast"
	}
}