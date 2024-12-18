package org.powbot.krulvis.api.extensions.randoms

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widgets

class EquipmentScreen : RandomHandler() {

	private var comp = Component.Nil
	override fun validate(): Boolean {
		comp = findComp()
		return comp.visible()
	}

	private fun findComp(): Component =
		Widgets.component(84, 3)

	override fun execute(): Boolean {
		val close = comp.firstOrNull { it != null && it.visible() && it.actions().contains("Close") }
		return close?.click() == true
	}
}
