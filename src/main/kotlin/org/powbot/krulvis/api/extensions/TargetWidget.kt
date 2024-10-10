package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets

object TargetWidget {

	private val ROOT_WIDGET = 303
	fun widget() = Widget(ROOT_WIDGET)

	fun nameComponent(): Component = Widgets.component(ROOT_WIDGET, 9)

	fun name(): String = nameComponent().text()

	fun health(): Int {
		val text = widget().components().firstOrNull { it.visible() && it.text().contains("/") }?.text() ?: return -1
		return text.substring(0, text.indexOf("/") - 1).toInt()
	}
}