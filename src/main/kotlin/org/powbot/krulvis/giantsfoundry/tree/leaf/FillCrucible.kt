package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.METAL_ITEM_NAMES
import org.powbot.krulvis.giantsfoundry.crucibleBars

class FillCrucible(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fill Crucible") {

	val metalBarWidget get() = Widgets.widget(270)
	val metalItemWidget get() = Widgets.widget(219)

	fun metalBarWidgetOpen() = metalBarWidget.any { it?.text() == "What metal would you like to add?" }
	fun metalItemWidgetOpen() =
		metalItemWidget.valid() && Components.stream().text("How many would you like to add?").viewable().isNotEmpty()

	fun barButton(bar: String): Component? =
		metalBarWidget.firstOrNull { it?.name()?.contains(bar, ignoreCase = true) == true }

	override fun execute() {
		Bank.close()
		val crucible = Objects.stream(30, GameObject.Type.INTERACTIVE)
			.name("Crucible (empty)", "Crucible (partially full)").firstOrNull()
		val item = Inventory.stream().nameContains(*METAL_ITEM_NAMES).firstOrNull()
		if (item == null || crucible == null) return
		val name = item.name()
		script.logger.info("Going to addd bar=${name}")

		val crucibleBars = crucibleBars()
		if (name.contains("bar")) {
			if (!metalBarWidgetOpen()) {
				script.logger.info("Clicking crucible=$crucible to fill it with bars")
				if (crucible.interact("Fill")) {
					waitFor(2500) { metalBarWidgetOpen() }
				}
			}

			if (metalBarWidgetOpen()) {
				val barButton = barButton(name)
				script.logger.info("Adding bar by clicking on comp=${barButton}")
				if (barButton?.click() == true) {
					waitFor { crucibleBars != crucibleBars() }
				}
			}
		} else {
			if (!metalItemWidgetOpen()) {
				val count = Inventory.stream().name(name).count()
				if (item.useOn(crucible)) {
					waitFor(5000) { if (count > 1) metalItemWidgetOpen() else crucibleBars != crucibleBars() }
				}
			}

			if (metalItemWidgetOpen() && Chat.continueChat("All")) {
				waitFor(5000) { crucibleBars != crucibleBars() }
			}
		}
	}
}