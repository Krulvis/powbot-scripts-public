package org.powbot.krulvis.test

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.ScrollHelper
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.giantsfoundry.mouldsParent
import org.powbot.krulvis.giantsfoundry.scrollBar

@ScriptManifest("testWidgets", "", priv = true)
class TestWidgets : AbstractScript() {

    val widgets = mutableListOf<Widget>()
    val components = mutableListOf<Component>()


    fun parseResults() {
        val comp = Widgets.widget(229).filterNotNull()
            .firstOrNull { it.text().contains("You're awarded:") && it.visible() } ?: return
        val text = comp.text()
        val quality = text.substring(text.indexOf("quality: ") + 9, text.indexOf("<br>")).toInt()
        val coinsGained = text.substring(text.indexOf("XP and ") + 7, text.indexOf(" coins")).replace(",", "").toInt()
        logger.warn("Quality=$quality, coins=$coinsGained")
    }


    override fun poll() {
        val scroll = ScrollHelper.scrollTo(
            { mouldsParent().component(85) },
            { mouldsParent() },
            { scrollBar() })

        logger.info("Scroll=${scroll}")
//        parseResults()
//		val newWidgets = Widgets.get().filterNotNull()
//		if (widgets.isEmpty()) {
//			widgets.addAll(newWidgets)
//		} else {
//			widgets.filter { !newWidgets.contains(it) }.forEach {
//				logger.warn("Widget=${it} is gone")
//			}
//			newWidgets.forEach { nw ->
//				val index = nw.index
//				val old = widgets.firstOrNull { it.index == index }
//				if (old == null) logger.warn("newWidget=$nw")
//				else {
//					val oldComponents = old.components()
//					val newComponents = nw.components()
//					newComponents.forEach { nc ->
//						val cIndex = nc.index()
//						val oldC = oldComponents[cIndex]
//						if (nc.visible() && !oldC.visible()) logger.warn("$nc is visible")
//						nc.compareBounds(oldC)
//					}
//				}
//			}
//			widgets.clear()
//			widgets.addAll(newWidgets)
//		}
    }

    fun Component.compareBounds(old: Component): Boolean {
        val rect = boundingRect()
        val oldRect = old.boundingRect()

        return if (rect.x != oldRect.x) {
            logger.warn("x changed $this=${rect}, $old=${oldRect}")
            true
        } else if (rect.y != oldRect.y) {
            logger.warn("y changed $this=${rect}, $old=${oldRect}")
            true
        } else if (rect.height != oldRect.height) {
            logger.warn("height changed $this=${rect}, $old=${oldRect}")
            true
        } else if (rect.width != oldRect.width) {
            logger.warn("width changed $this=${rect}, $old=${oldRect}")
            true
        } else {
            false
        }
    }
}


fun main() {
    TestWidgets().startScript("127.0.0.1", "GIM", true)
}