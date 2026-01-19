package org.powbot.krulvis.smither.tree.leaf

import org.powbot.api.Production
import org.powbot.api.rt4.Components
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.smither.Smither

class ClickWidget(script: Smither) : Leaf<Smither>(script, "Click Widget") {
    override fun execute() {
//        val comps = Components.stream(312).list()
        val named = Components.stream(312).text(script.item.toString()).firstOrNull() ?: return
        script.logger.info("named=${named}")
//        val comp = comps.firstOrNull { it.name().contains(script.item.toString(), true) }
        if (named.parent().interact("Smith", false)) {
            val notStoppedMaking = waitFor(long()) { !Production.stoppedUsing(script.bar.id) }
            script.logger.warn("After widget click notStoppedMaking=$notStoppedMaking")
        }
    }
}