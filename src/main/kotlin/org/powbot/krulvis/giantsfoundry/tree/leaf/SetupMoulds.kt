package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Input
import org.powbot.api.Point
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Component
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.BonusType
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.MouldType
import org.powbot.krulvis.giantsfoundry.mouldWidget
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
//		if (MouldType.openPage() == mouldType) return true
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
			MouldType.values().forEach { mt ->
				script.logger.info("Setting mouldType=${mt}, pageOpen=${mt.open()}")
				if (!selectPage(mt)) {
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
						val selected = waitFor { mt.hasSelectedAny() }
						script.logger.info("Selected bestMould successfully=$selected")
					} else {
						script.logger.info("Failed to scroll to best mould...")
					}
				} else {
					script.logger.info("notSelected=${mt}, bestMouldName=${bestMould.first.name()}, jigName=${script.jig().name}")
				}
			}
			if (close()) {
				waitFor { MouldType.selectedAll() && script.jig().name != "Mould jig (Empty)" }
			}
		} else if (Bank.close()) {
			script.logger.info("MouldWidget is not open, interacting with jig=$jig")
			if (walkAndInteract(jig, action)) {
				waitFor { script.mouldWidgetOpen() }
			}
		}
	}


	private fun close(): Boolean {
		val mouldWidget = mouldWidget()
		val closeButton = mouldWidget.firstOrNull { it?.actions()?.contains("Set") == true }
			?: mouldWidget.firstOrNull { it?.actions()?.contains("Close") == true }
		script.logger.info("Closebutton=$closeButton")
		return closeButton?.click() == true
	}

	private fun getPageMoulds(): List<Pair<Component, List<Bonus>>> {
		val container = mouldContainer()
		val buttons = container.filterNotNull().filter { it.width() == container.width() }
		return buttons.map { button ->
			val bonuses = container.filterNotNull().filter { comp ->
				comp.index() in button.index() + 1..button.index() + 16 && BonusType.isBonus(comp)
			}
			bonuses.forEach {
				script.logger.info(
					"Bonus(index=${it.index()}, type=${BonusType.forComp(it)}, bonusAmount=${
						container.component(
							it.index() + 1
						).text()
					})"
				)
			}
			Pair(
				button,
				bonuses.map {
					Bonus(
						BonusType.forComp(it)!!,
						container.component(it.index() + 1).text().toIntOrNull() ?: 0
					)
				})
		}
	}

	fun verticalScrollTo(mouldButton: Component, container: Component, scrollBar: Component): Boolean {
		val topY = container.screenPoint().y - 5
		val bottomY = topY + container.height() - 20

		fun visible() = mouldButton.screenPoint().y in topY..bottomY

		fun grabPoint(): Point {
			val point = scrollBar.screenPoint()
			if (point == Point.Nil || scrollBar.width() <= 6 || scrollBar.height() <= 6) {
				script.logger.info("Cannot find grabPoint() point=${point}, scrollbar.width=${scrollBar.width()}, scrollbar.height=${scrollBar.height()}")
			}
			return Point(
				point.x + Random.nextInt(3, scrollBar.width() - 3),
				point.y + Random.nextInt(3, scrollBar.height() - 3)
			)
		}

		val grabPoint = grabPoint()
		if (grabPoint == Point.Nil) {
			script.logger.info("Failed to scroll, grappoint = null")
			return false
		}
		val scrollY = mouldButton.screenPoint().y
		val distance = abs(scrollY - topY)
		val minY = grabPoint.y - distance
		val maxY = grabPoint.y + distance
		return Input.dragUntil(grabPoint.x, grabPoint.y, minY, maxY, 5) { visible() }
	}

	data class Bonus(val type: BonusType, val amount: Int)

}