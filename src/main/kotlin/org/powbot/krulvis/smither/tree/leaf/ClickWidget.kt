package org.powbot.krulvis.smither.tree.leaf

import org.powbot.api.Production
import org.powbot.api.rt4.Components
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.smither.Smither

class ClickWidget(script: Smither) : Leaf<Smither>(script, "Click Widget") {
    override fun execute() {
        val comp = Components.stream(312).text(script.item.toString()).firstOrNull()
        val clickable = comp?.parent() ?: return
        script.logger.info("Found widget with text: ${comp.text()}, parent: $clickable")
        if (clickable.interact("Smith", false)) {
            val notStoppedMaking = waitFor(long()) { !Production.stoppedUsing(script.bar.id) }
            script.logger.info("After widget click notStoppedMaking=$notStoppedMaking")
        }
    }
}