package org.powbot.krulvis.test

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest

@ScriptManifest("testWidgets", "", priv = true)
class TestWidgets : AbstractScript() {

	val widgets = mutableListOf<Widget>()
	val components = mutableListOf<Component>()


	override fun poll() {
		val newWidgets = Widgets.get().filterNotNull()
		if (widgets.isEmpty()) {
			widgets.addAll(newWidgets)
		} else {
			widgets.filter { !newWidgets.contains(it) }.forEach {
				logger.info("Widget=${it} is gone")
			}
			newWidgets.forEach { nw ->
				val index = nw.index
				val old = widgets.firstOrNull { it.index == index }
				if (old == null) logger.info("newWidget=$nw")
				else {
					val oldComponents = old.components()
					val newComponents = nw.components()
					newComponents.forEach { nc ->
						val cIndex = nc.index()
						val oldC = oldComponents[cIndex]
						if (nc.visible() && !oldC.visible()) logger.info("$nc is visible")
						nc.compareBounds(oldC)
					}
				}
			}
			widgets.clear()
			widgets.addAll(newWidgets)
		}
	}

	fun Component.compareBounds(old: Component): Boolean {
		val rect = boundingRect()
		val oldRect = old.boundingRect()

		return if (rect.x != oldRect.x) {
			logger.info("x changed $this=${rect}, $old=${oldRect}")
			true
		} else if (rect.y != oldRect.y) {
			logger.info("y changed $this=${rect}, $old=${oldRect}")
			true
		} else if (rect.height != oldRect.height) {
			logger.info("height changed $this=${rect}, $old=${oldRect}")
			true
		} else if (rect.width != oldRect.width) {
			logger.info("width changed $this=${rect}, $old=${oldRect}")
			true
		} else {
			false
		}
	}
}


fun main() {
	TestWidgets().startScript("127.0.0.1", "GIM", true)
}