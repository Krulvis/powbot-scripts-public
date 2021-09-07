package org.powbot.krulvis.blastfurnace.tree.leaf


import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.ICE_GLOVES

class TakeBars(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Take bars") {


    override fun execute() {
        val gloves = Inventory.stream().id(ICE_GLOVES).findFirst()
        val takeWidget = takeWidget()
        if (gloves.isPresent) {
            if (gloves.get().interact("Wear")) {
                waitFor { !Inventory.containsOneOf(ICE_GLOVES) }
            }
        } else if (takeWidget.valid()) {
            val clickComp = takeWidget.components().maxByOrNull { it.componentCount() }
            script.log.info("Clicking on Widget[${clickComp?.widgetId()}], $clickComp")
            if (clickComp != null && clickComp.click() && waitFor { Inventory.isFull() }) {
                script.waitForBars = false
            }
        } else if (Chat.canContinue()) {
            Chat.clickContinue()
        } else if (Bank.close()) {
            val matrix = script.dispenserTile.matrix()
            if (script.interact(matrix, "Take")) {
                waitFor(long()) { takeWidget().valid() }
            }
        }
    }

    fun takeWidget() = Widgets.widget(270)

}