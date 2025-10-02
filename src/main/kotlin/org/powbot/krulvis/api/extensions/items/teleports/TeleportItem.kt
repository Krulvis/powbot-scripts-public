package org.powbot.krulvis.api.extensions.items.teleports

import org.powbot.api.Random
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TeleportItem")

enum class TeleportItem(override val itemName: String, override val ids: IntArray) : ITeleportItem {
    ROYAL_SEED_POD("Royal seed pod", intArrayOf(19564)) {
        override fun getCharges(): Int = Int.MAX_VALUE
    },
    SALVE_GRAVEYARD_TELEPORT("Salve graveyard teleport", intArrayOf(19619)),
    ;

    override val stackable: Boolean = false

    override fun hasWith(): Boolean = inInventory()

    override fun getCharges(): Int = getInventoryCount(true)

    override fun getCount(countNoted: Boolean): Int = getInventoryCount(true)
    override fun teleport(destination: String): Boolean {
        logger.info("$itemName Teleport to: $destination")
        Bank.close()
        val w = if (Widgets.widget(300).componentCount() >= 91) Widgets.widget(300).component(91) else null
        if (w != null && w.interact("Close")) {
            sleep(Random.nextInt(200, 500))
        }
        return Inventory.stream().id(*ids).firstOrNull()?.interact(destination) == true
    }

    companion object {
        fun isTeleportItem(id: Int) = getTeleportItem(id) != null

        fun getTeleportItem(id: Int): TeleportItem? {
            for (ti in entries) {
                for (i in ti.ids) {
                    if (i == id) {
                        return ti
                    }
                }
            }
            return null
        }
    }


}



