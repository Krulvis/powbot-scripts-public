package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Input
import org.powbot.api.Point
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Component
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.*
import kotlin.math.abs
import kotlin.random.Random

class SetupMoulds(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Setup moulds") {


	fun mouldContainer() = mouldWidget().component(9)

	private fun resetButton() = mouldWidget().firstOrNull { it?.actions()?.contains("Reset") == true }


	private fun getComission(): List<BonusType> {
		val widget = mouldWidget()
		val first = BonusType.forText(widget.component(27).text()) ?: return emptyList()
		val second = BonusType.forText(widget.component(29).text()) ?: return emptyList()
		return listOf(first, second)
	}

	private fun selectPage(mouldType: MouldType): Boolean {
		if (MouldType.openPage() == mouldType) return true
		val button = mouldWidget().firstOrNull { it?.name()?.contains(mouldType.name) == true } ?: return false
		return button.click()
			&& waitFor { MouldType.openPage() == mouldType }
	}

	override fun execute() {
		val jig = script.jig()
		val action = if (jig.actions().contains("Check")) "Check" else "Setup"
		if (script.mouldWidgetOpen()) {
			if (action == "Check") {
				script.logger.info("Something went wrong setting up moulds, resetting")
				val resetButton = mouldWidget().firstOrNull { it?.actions()?.contains("Reset") == true }
				if (resetButton?.interact("Reset") == true) {
					waitFor { script.jig().name.contains("Empty") }
				}
			}
			val mouldType = MouldType.values().firstOrNull { !it.hasSelectedAny() }
			if (mouldType == null) {
				script.logger.info("All are set, closing widget!")
				val setButton = mouldWidget().firstOrNull { it?.actions()?.contains("Set") == true }
				if (setButton?.interact("Set") == true) {
					if (!waitFor { script.jig().name.contains("Setup") }) {
						val selectionContainer = mouldWidget().component(MOULD_SELECTION_CONTAINER)
						selectionContainer.firstOrNull { it?.textColor() == MOULD_SELECTED_COLOR } ?: Component.Nil
						val index = selectionContainer.indexOf(selectionContainer)
						script.logger.info(
							"Glitch with selected=[${
								MouldType.values().map { it.selected() }
							}], textures=[${
								MouldType.values().map { it.selectedTexture() }
							}] indexOfSelector=$index"
						)
						resetButton()?.click()
					}
				}
				return
			}
			if (!selectPage(mouldType)) {
				script.logger.info("Unable to navigate to unselected mould page...")
				return
			}

			val bonus = getComission()
			script.logger.info("Setting moulds for types: [${bonus.joinToString(", ")}]")
			if (bonus.isEmpty()) {
				return
			}

			val bestMould = getPageMoulds().maxByOrNull { mould ->
				mould.second.filter { it.type in bonus }.sumOf { it.amount }
			} ?: return

			val bonusStr = bestMould.second.joinToString(separator = ", ") { "${it.type}: ${it.amount}" }
			script.logger.info("Found max mould=${bonusStr}, nameIsBlank=${bestMould.first.name().isBlank()}")

			if (bestMould.first.name().isNotBlank()) {
				val scrollBar = mouldWidget().component(11).component(1)
				if (verticalScrollTo(bestMould.first, mouldContainer(), scrollBar)) {
					bestMould.first.click()
					val selected = waitFor { mouldType.hasSelectedAny() }
					script.logger.info("Selected bestMould successfully=$selected")
				}
			} else {
				script.logger.info("notSelected=${mouldType}, bestMouldName=${bestMould.first.name()}, jigName=${script.jig().name}")
			}
		} else if (Bank.close()) {
			if (jig.interact(action)) {
				waitFor { script.mouldWidgetOpen() }
			}
		}
	}

	private fun getPageMoulds(): List<Pair<Component, List<Bonus>>> {
		val container = mouldContainer()
		val buttons = container.filterNotNull().filter { it.width() == container.width() }
		return buttons.map { button ->
			val bonuses = container.filterNotNull().filter { comp ->
				comp.index() in button.index() + 1..button.index() + 16 && BonusType.isBonus(comp)
			}.map { Bonus(BonusType.forComp(it)!!, container.component(it.index() + 1).text().toInt()) }
//            script.logger.info("Children for ${button.text()}: size= ${children.size}, bonuses=${children.joinToString()}")
			Pair(button, bonuses)
		}
	}

	fun verticalScrollTo(mouldButton: Component, container: Component, scrollBar: Component): Boolean {
		val topY = container.screenPoint().y - 5
		val bottomY = topY + container.height() - 20

		fun visible() = mouldButton.screenPoint().y in topY..bottomY

		fun grabPoint(): Point {
			val point = scrollBar.screenPoint()
			return Point(
				point.x + Random.nextInt(3, scrollBar.width() - 3),
				point.y + Random.nextInt(3, scrollBar.height() - 3)
			)
		}

		val grabPoint = grabPoint()
		val scrollY = mouldButton.screenPoint().y
		val distance = abs(scrollY - topY)
		val minY = grabPoint.y - distance
		val maxY = grabPoint.y + distance
		return Input.dragUntil(grabPoint.x, grabPoint.y, minY, maxY, 5) { visible() }
	}

	data class Bonus(val type: BonusType, val amount: Int)

}