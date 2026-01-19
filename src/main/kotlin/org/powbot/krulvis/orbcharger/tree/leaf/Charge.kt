package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.Production
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.orbcharger.OrbCrafter

class Charge(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Charge Orbs") {
    override fun execute() {
        if (getChargeComponent()?.visible() != true && script.orb.castOnObelisk()) {
            waitFor { getChargeComponent()?.visible() == true }
        }

        val chargeComponent = getChargeComponent()
        if (chargeComponent?.visible() == true && chargeComponent.interact("Charge")) {
            sleep(600)
            if (script.fastCharge) {
                script.orb.spell.cast()
            }
            waitFor(long()) { !Production.stoppedMaking(script.orb.id) }
        }
    }

    var widgetId = -1
    var componentId = -1

    fun getChargeComponent(): Component? {
        if (widgetId != -1 && componentId != -1) {
            return Widgets.widget(widgetId).component(componentId)
        }
        val comp = Components.stream().firstOrNull { it.visible() && it.actions().contains("Charge") }
        if (comp != null) {
            widgetId = comp.widgetId()
            componentId = comp.index()
        }
        return comp
    }
}