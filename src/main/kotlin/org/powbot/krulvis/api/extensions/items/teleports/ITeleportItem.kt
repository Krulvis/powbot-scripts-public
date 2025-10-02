package org.powbot.krulvis.api.extensions.items.teleports

import org.powbot.api.rt4.stream.FilterParameters
import org.powbot.api.rt4.stream.SimpleStream
import org.powbot.api.rt4.stream.item.ItemStream
import org.powbot.krulvis.api.extensions.items.Item

interface ITeleportItem : Item {
    fun teleport(destination: String): Boolean

    /***
     * Overriding the default filterItems method to make sure that we don't allow name filtering since this will
     * allow "Amulet of Glory" as valid teleport item when it doesn't actually have any charges
     */
    override fun <S : SimpleStream<org.powbot.api.rt4.Item, S, FilterParameters>> ItemStream<S>.filterItems(allowNoted: Boolean) =
        filtered { it.id in ids }

    /**
     * Return total amount of charges in inventory
     */
    fun getCharges(): Int

    companion object {
        fun isTeleportItem(id: Int) = getTeleportItem(id) != null

        fun getTeleportItem(id: Int): ITeleportItem? =
            TeleportItem.getTeleportItem(id) ?: TeleportEquipment.getTeleportEquipment(id)
    }
}