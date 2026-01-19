package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Widgets

object Warning {
	val ROOT_WIDGET = 475

	fun visible() = Widgets.component(ROOT_WIDGET, 1).visible()
	fun dontAskAgain() = Components.stream(ROOT_WIDGET).text("Don't ask me this again").first()
	fun enterButton() = Components.stream(ROOT_WIDGET).action("Enter Wilderness").first()

}