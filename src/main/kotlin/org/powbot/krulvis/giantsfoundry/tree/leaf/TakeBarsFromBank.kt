package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class TakeBarsFromBank(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Take bars from bank") {

    override fun execute() {
        if (script.mouldWidgetOpen()) {
            val button = script.mouldWidget().firstOrNull { it?.text()?.contains("Set Mould") == true } ?: return
            button.click()
            waitFor { !script.mouldWidgetOpen() }
        }
        if (openBank()) {
            script.barsToUse
                .map { Pair(it.first, it.second - script.crucibleBarCount(it.first)) }
                .forEach { (bar, amount) ->
                    val curCount = bar.getInventoryCount()
                    if (curCount <= amount) {
                        if (bar.withdrawExact(amount)) {
                            waitFor { bar.getInventoryCount() == amount }
                        }
                    }
                }
        }
    }

    fun openBank(): Boolean {
        if (Bank.opened()) {
            return true
        }
        val bankObj = Objects.stream(30).type(GameObject.Type.INTERACTIVE).name("Bank chest").firstOrNull() ?: return false
        return script.interactObj(bankObj, "Use") && waitFor { Bank.opened() }
    }
}