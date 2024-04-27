package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Input
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.GiantsFoundryItem
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class FillCrucible(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fill Crucible") {

    val metalBarWidget get() = Widgets.widget(270)
    val metalItemWidget get() = Widgets.widget(219)
    val overflowWidget get() = Widgets.widget(162)

    fun metalBarWidgetOpen() = metalBarWidget.any { it?.text() == "What metal would you like to add?" }

    // TODO: There's probably a better way to do these component checks below...  .any{} was not working for me.
    fun metalItemWidgetOpen() = metalItemWidget.valid() && Components.stream()
        .filtered { it.text() == "How many would you like to add?" }.viewable().isNotEmpty();
    fun overflowWidgetOpen() = overflowWidget.valid() && Components.stream()
        .filtered { it.text().startsWith("Adding this item will overflow the crucible.") }.viewable()
        .isNotEmpty()

    fun barButton(bar: String): Component? =
        metalBarWidget.firstOrNull { it?.name()?.contains(bar, ignoreCase = true) == true }

    override fun execute() {
        Bank.close()
        val crucible = Objects.stream(30).type(GameObject.Type.INTERACTIVE)
            .name("Crucible (empty)", "Crucible (partially full)").firstOrNull()
        val bar = script.getInvBar()
        script.log.info("Going to add bar=${bar?.name()}")
        if (bar == null || crucible == null) return

        // TODO There's definitely a better way to do this predicate
        if (bar.name().endsWith("bar")) {
            fillBars(bar, crucible)
        } else {
            fillItems(bar, crucible)
        }
    }

    fun fillBars(bar: Item, crucible: GameObject) {
        if (!metalBarWidgetOpen()) {
            script.log.info("Clicking crucible=$crucible to fill it with bars")
            if (crucible.interact("Fill")) {
                waitFor(2500) { metalBarWidgetOpen() }
            }
        }

        if (metalBarWidgetOpen()) {
            val barButton = barButton(bar.name())
            script.log.info("Adding bar by clicking on comp=${barButton}")
            if (barButton?.click() == true) {
                waitFor(4000) { script.correctCrucibleCount(GiantsFoundryItem.forId(bar.id)!!) }
            }
        }
    }

    fun fillItems(item: Item, crucible: GameObject) {
        if (!metalItemWidgetOpen() && !overflowWidgetOpen()) {
            if (item.interact("Use") && crucible.interact("Use")) {
                script.log.info("Clicking crucible=$crucible to fill it with items")
                waitFor { metalItemWidgetOpen() }
            }
        }

        if ((metalItemWidgetOpen() && Chat.continueChat("All")) || (overflowWidgetOpen() && Input.send("2"))) {
            waitFor(4000) { script.correctCrucibleCount(GiantsFoundryItem.forId(item.id)!!) }
        }
    }
}