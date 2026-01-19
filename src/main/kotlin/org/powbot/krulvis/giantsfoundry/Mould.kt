package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.ScrollHelper
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.giantsfoundry.data.BonusType
import org.slf4j.LoggerFactory

/**
 * Root widget for the moulds.
 */
fun mouldWidget() = Widgets.widget(MOULD_WIDGET_ROOT)

/**
 * Container that holds the BoundingBox to check if a button is visible.
 */
fun mouldContainer() = mouldWidget().component(10)

/**
 * Scrollbar that can be used to scroll through the moulds.
 */
fun scrollBar() = mouldWidget().component(11)

/**
 * Parent of the moulds, used to find the buttons.
 */
fun mouldsParent() = mouldWidget().component(9)

const val MOULD_SELECTION_CONTAINER = 9
const val MOULD_EMPTY_TEXTURE = 3593
const val MOULD_SELECTED_COLOR = 16777215

private val logger = LoggerFactory.getLogger("Mould")

fun mouldWidgetOpen() = mouldWidget().component(2).any { it?.text() == "Giants' Foundry Mould Setup" }

data class Bonus(val type: BonusType, val amount: Int) {
    override fun toString(): String {
        return "Bonus($type, $amount)"
    }
}

data class Mould(val buttonComponent: Component) {
    val name by lazy { buttonComponent.parent().component(buttonComponent.index() + 2).text() }

    val bonuses: List<Bonus> by lazy {
        val parent = buttonComponent.parent()
        val index = buttonComponent.index()
        parent.filterNotNull().filter { comp ->
            comp.index() in index + 1..index + 16 && BonusType.isBonus(comp)
        }.map {
            Bonus(
                BonusType.forComp(it)!!,
                parent.component(it.index() + 1).text().toIntOrNull() ?: 0
            )
        }
    }

    fun scrollTo(): Boolean {
        return ScrollHelper.scrollTo(
            { mouldsParent().component(buttonComponent.index()) },
            { mouldsParent() },
            { scrollBar() })
    }

    override fun toString(): String {
        val bonuses = bonuses.joinToString { "${it.type}: ${it.amount}" }
        return "Mould(${name}, index=${buttonComponent.index()}, bonuses=[$bonuses])"
    }
}

fun getSelectedMouldComp(): Component {
    val comp = mouldWidget().component(MOULD_SELECTION_CONTAINER)
    return comp.firstOrNull { it?.textColor() == MOULD_SELECTED_COLOR } ?: return Component.Nil
}


fun getPageMoulds(): List<Mould> {
    val parent = mouldsParent()
    val moulds = parent.filterNotNull().filter { it.actions().contains("Select") }.map { Mould(it) }
    logger.warn("Found ${moulds.size} moulds")
    moulds.forEach { logger.warn(it.toString()) }
    return moulds
}