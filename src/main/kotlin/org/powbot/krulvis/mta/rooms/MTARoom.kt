package org.powbot.krulvis.mta.rooms

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.mta.MTA

const val TELEKINETIC_METHOD = "Telekinetic"
const val GRAVEYARD_METHOD = "Graveyard"
const val ENCHANTING_METHOD = "Enchanting"
const val ALCHEMY_METHOD = "Alchemy"

interface MTARoom {
    val WIDGET_ID: Int
    val portalName: String

    fun rootComponent(mta: MTA): TreeComponent<MTA>

    fun points(): Int {
        val c = Components.stream(WIDGET_ID).text("Pizazz Points:").first()
        return Widgets.widget(WIDGET_ID).component(c.index() + 1).text().filter { it.isDigit() }.toInt()
    }

    fun inside(): Boolean {
        return Components.stream(WIDGET_ID).viewable().isNotEmpty()
    }
}

val rooms = mapOf(
    TELEKINETIC_METHOD to TelekineticRoom,
    GRAVEYARD_METHOD to GraveyardRoom,
    ENCHANTING_METHOD to EnchantingRoom,
    ALCHEMY_METHOD to AlchemyRoom
)