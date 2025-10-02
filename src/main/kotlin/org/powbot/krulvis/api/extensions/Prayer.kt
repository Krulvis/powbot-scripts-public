package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory

object Prayer {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)
    const val QUICK_PRAYER_ROOT = 77

    fun Prayer.quickPrayerDone() = Components.stream(QUICK_PRAYER_ROOT).action("Done").first()

    fun Prayer.quickPrayOpen() = quickPrayerDone().visible()

    private fun Prayer.quickPrayerComp(effect: Prayer.Effect) = Widgets.component(QUICK_PRAYER_ROOT, 4, effect.ordinal)

    fun Prayer.closeQuickPray(): Boolean {
        val done = quickPrayerDone()
        if (!done.visible()) return true
        return done.click()
    }

    fun Prayer.setQuickPrayers(vararg effects: Prayer.Effect): Boolean {
        var current = quickPrayers()
        if (current.contentEquals(effects)) {
            logger.info("Already has correct quickprayers")
            return closeQuickPray()
        }

        if (!Prayer.quickSelection(true)) {
            logger.info("Failed to open quickprayer screen")
            return false
        }


        val missing = effects.filter { it !in current }
        missing.forEach { m ->
            val comp = quickPrayerComp(m)
            comp.interact("Toggle")
        }
        if (waitFor {
                current = quickPrayers()
                current.contentEquals(effects)
            }) {
            return true
        }
        val extra = current.filter { it !in effects }
        extra.forEach { m ->
            val comp = quickPrayerComp(m)
            comp.interact("Toggle")
        }
        return waitFor { quickPrayers().contentEquals(effects) } && closeQuickPray()
    }
}