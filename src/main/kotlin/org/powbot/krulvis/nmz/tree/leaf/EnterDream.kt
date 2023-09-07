package org.powbot.krulvis.nmz.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.nmz.NightmareZone
import org.powbot.mobile.script.ScriptManager

class EnterDream(script: NightmareZone) : Leaf<NightmareZone>(script, "Entering Dream") {
    override fun execute() {
        val nmzWidget = nightmareZoneWidget()
        val potion = drinkablePotion()
        val rumbleSelector = rumbleSelector()
        val agreeComponent = agreeComponent()
        if (outOfMoney()) {
            script.log.info("Out of money! stopping script")
            ScriptManager.stop()
        } else if (nmzWidget != null) {
            if (nmzWidget.click())
                Utils.waitFor(5000) { !script.outsideNMZ() }
        } else if (potion != null) {
            if (potion.interact("Drink")) {
                Utils.waitFor { nightmareZoneWidget() != null }
            }
        } else if (rumbleSelector != null) {
            script.log.info("Found component with text = ${rumbleSelector.text()}")
            if (rumbleSelector.click())
                Utils.waitFor { Chat.canContinue() }
        } else if (Chat.canContinue()) {
            script.log.info("Continueing widget..")
            if (Chat.clickContinue())
                Utils.waitFor { agreeComponent() != null }
        } else if (agreeComponent != null) {
            script.log.info("Agree component up..")
            if (agreeComponent.parent().component(1).click())
                Utils.waitFor { drinkablePotion() != null }
        } else if (Objects.stream().name("Empty Vial").isNotEmpty()) {
            script.log.info("Couldn't find component ${Widgets.component(219, 1, 4).text()}")
            Npcs.stream().name("Dominic Onion").firstOrNull()?.interact("Dream")
        }
    }

    private fun outOfMoney() = Components.stream(193).text("You need to have").isNotEmpty()

    private fun agreeComponent() = Components.stream(219, 1).text("Agree to pay").firstOrNull()
    private fun rumbleSelector() = Components.stream(219, 1).text("Previous:").firstOrNull()
    private fun nightmareZoneWidget() = Components.stream(129, 6).text("Accept").firstOrNull()

    private fun drinkablePotion() = Objects.stream().name("Potion").action("Drink").firstOrNull()
}