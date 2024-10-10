package org.powbot.krulvis.nmz.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.nmz.NightmareZone
import org.powbot.mobile.script.ScriptManager

class EnterDream(script: NightmareZone) : Leaf<NightmareZone>(script, "Entering Dream") {
    override fun execute() {
        val nmzWidget = nightmareZoneWidget()
        val dreamPotion = dreamPotion()
        val rumbleSelector = rumbleSelector()
        val agreeComponent = agreeComponent()
        if (outOfMoney()) {
            script.logger.info("Out of money! stopping script")
            ScriptManager.stop()
        } else if (nmzWidget != null) {
            if (nmzWidget.click())
                Utils.waitFor(5000) { !script.outsideNMZ() }
        } else if (dreamPotion != null) {
            if (dreamPotion.interact("Drink")) {
                Utils.waitFor { nightmareZoneWidget() != null }
            }
        } else if (rumbleSelector != null) {
            script.logger.info("Found component with text = ${rumbleSelector.text()}")
            if (rumbleSelector.click())
                Utils.waitFor { Chat.canContinue() }
        } else if (Chat.canContinue()) {
            script.logger.info("Continueing widget..")
            if (Chat.clickContinue())
                Utils.waitFor { agreeComponent() != null }
        } else if (agreeComponent != null) {
            script.logger.info("Agree component up..")
            if (agreeComponent.parent().component(1).click())
                Utils.waitFor { dreamPotion() != null }
        } else if (Objects.stream().name("Empty Vial").isNotEmpty()) {
            script.logger.info("Couldn't find component ${Widgets.component(219, 1, 4).text()}")
            Npcs.stream().name("Dominic Onion").firstOrNull()?.interact("Dream")
        }
    }

    private fun outOfMoney() = Components.stream(193).text("You need to have").isNotEmpty()

    private fun agreeComponent() = Components.stream(219, 1).text("Agree to pay").firstOrNull()
    private fun rumbleSelector() = Components.stream(219, 1).text("Previous:").firstOrNull()
    private fun nightmareZoneWidget() = Components.stream(129, 6).text("Accept").firstOrNull()

    private fun dreamPotion() = Objects.stream().name("Potion").action("Drink").firstOrNull()
}