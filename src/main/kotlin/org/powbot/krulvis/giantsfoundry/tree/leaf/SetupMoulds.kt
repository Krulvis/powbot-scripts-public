package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.data.BonusType
import org.powbot.krulvis.giantsfoundry.data.MouldType
import org.powbot.krulvis.giantsfoundry.getPageMoulds
import org.powbot.krulvis.giantsfoundry.mouldWidget
import org.powbot.krulvis.giantsfoundry.mouldWidgetOpen

class SetupMoulds(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Setup moulds") {

    private fun getComission(): List<BonusType> {
        val widget = mouldWidget()
        val first = BonusType.forText(widget.component(27).text()) ?: return emptyList()
        val second = BonusType.forText(widget.component(29).text()) ?: return emptyList()
        return listOf(first, second)
    }

    private fun selectPage(mouldType: MouldType): Boolean {
//		if (MouldType.openPage() == mouldType) return true
        val button = mouldWidget().firstOrNull { it?.name()?.contains(mouldType.name) == true } ?: return false
        return button.click()
                && waitFor { MouldType.openPage() == mouldType }
    }

    override fun execute() {
        val jig = script.jig()
        val action = if (jig.actions().contains("Check")) "Check" else "Setup"
        if (mouldWidgetOpen()) {
            if (action == "Check") {
                script.logger.warn("Something went wrong setting up moulds, resetting")
                val resetButton = mouldWidget().firstOrNull { it?.actions()?.contains("Reset") == true }
                if (resetButton?.interact("Reset") == true) {
                    waitFor { script.jig().name.contains("Empty") }
                }
            }
            val bonus = getComission()
            script.logger.warn("Setting moulds for Commission: [${bonus.joinToString(", ")}]")
            if (bonus.isEmpty()) {
                return
            }
            MouldType.entries.forEach { mt ->
                script.logger.warn("Finding MouldType=${mt}, pageOpen=${mt.open()}")

                if (!selectPage(mt)) {
                    script.logger.warn("Unable to navigate to unselected mould page...")
                    return
                }

                val pageMoulds = getPageMoulds()
                val bestMould = pageMoulds.maxByOrNull { mould ->
                    mould.bonuses.filter { it.type in bonus }.sumOf { it.amount }
                }?.takeIf { mould ->
                    mould.bonuses.any { it.type in bonus }
                } ?: pageMoulds.maxByOrNull { mould ->
                    // If none of the moulds have any relevant bonuses, just pick the one with highest overall bonuses
                    mould.bonuses.sumOf { it.amount }
                } ?: return

                val totalBonus = bestMould.bonuses.filter { it.type in bonus }.sumOf { it.amount }
                script.logger.warn("Found max mould=$bestMould, with total relevant bonus=${totalBonus}")

                if (bestMould.scrollTo()) {
                    val click = bestMould.buttonComponent.click()
                    script.logger.warn("Clicked bestMould=${click}, bonuses=${bestMould.bonuses.joinToString()}, name=${bestMould.name}")
                    if (!waitFor { mt.hasSelectedAny() }) {
                        return
                    }
                    sleep(1200)
                } else {
                    script.logger.warn("Failed to scroll to best mould...")
                }
            }
            if (close()) {
                waitFor { MouldType.selectedAll() && script.jig().name != "Mould jig (Empty)" }
            }
        } else if (Bank.close()) {
            script.logger.warn("MouldWidget is not open, interacting with jig=$jig")
            if (walkAndInteract(jig, action)) {
                waitFor(long()) { mouldWidgetOpen() }
            }
        }
    }


    private fun close(): Boolean {
        val mouldWidget = mouldWidget()
        val closeButton = mouldWidget.firstOrNull { it?.actions()?.contains("Set") == true }
            ?: mouldWidget.firstOrNull { it?.actions()?.contains("Close") == true }
        script.logger.warn("Closebutton=$closeButton")
        return closeButton?.click() == true
    }

}