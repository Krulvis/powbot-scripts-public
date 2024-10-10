package org.powbot.krulvis.blastfurnace.tree.leaf


import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.ICE_GLOVES
import org.powbot.krulvis.blastfurnace.SMITHS_GLOVES

class TakeBars(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Take bars") {

    private fun setQuantity(widget: Widget): Boolean {
        val comp = widget.components().firstOrNull { it.actions().contains(Quantity.ALL.action) }
        return comp == null || comp.click()
    }

    enum class Quantity(val action: String) {
        ONE("1"),
        FIVE("5"),
        TEN("10"),
        X("Other quantity"),
        ALL("All")
    }

    override fun execute() {
        val gloves = Inventory.stream().id(ICE_GLOVES, SMITHS_GLOVES).firstOrNull()
        val takeWidget = takeWidget()
        debug("Take widget=$takeWidget")
        Bank.close()
        if (gloves != null) {
            val equip = gloves.interact("Wear")
            debug("Putting on ice gloves=$equip")
            if (equip) {
                waitFor { !Inventory.containsOneOf(ICE_GLOVES, SMITHS_GLOVES) }
            }
        } else if (takeWidget.valid()) {
            val allButton = takeWidget.components().firstOrNull { it.actions().contains(Quantity.ALL.action) }
            if (allButton != null && !allButton.click()) {
                debug("Failing to click ALL quantity, even though widget is open")
                return
            }

            val clickComp = takeWidget.components().maxByOrNull { it.componentCount() }
            debug("Clicking on Widget[${clickComp?.widgetId()}], $clickComp")
            if (clickComp != null && clickComp.click() && waitFor { Inventory.isFull() }) {
                script.waitForBars.stop()
            }
        } else if (Chat.canContinue()) {
            debug("Can continue before taking bars.")
            Chat.clickContinue()
        } else if (Bank.close()) {

            val dispenserMatrix = script.dispenserTile.matrix()
            val clickDispenser = script.interact(dispenserMatrix, "Take")
            debug("Closed bank, clicking dispenser=$clickDispenser")
            if (clickDispenser) {
                waitFor(long()) { takeWidget().valid() }
            }
        } else {
            debug("Stuck taking bars")
        }
    }

    fun takeWidget() = Widgets.widget(270)

}