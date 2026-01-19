package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.mobile.rscache.loader.ItemLoader

const val DDS = "DDS"
const val ARCLIGHT = "ARCLIGHT"
const val EMBERLIGHT = "EMBERLIGHT"

enum class Weapon(val specialPercentage: Int, override val ids: IntArray, val statReducer: Boolean = false) :
    IEquipmentItem {
    NIL(101, intArrayOf(-1)),
    DDS(25, intArrayOf(1215, 1231, 5680, 5698)),
    ARCLIGHT(50, intArrayOf(19675), true),
    EMBERLIGHT(50, intArrayOf(29589), true),
    DRAGON_WARHAMMER(50, intArrayOf(13576), true),
    MAGIC_SHORTBOW(55, intArrayOf(861), true),
    MAGIC_SHORTBOW_I(50, intArrayOf(12788), true),
    ;

    override val itemName: String by lazy { ItemLoader.lookup(id)?.name() ?: "Nil" }

    override val slot: Equipment.Slot = Equipment.Slot.MAIN_HAND
    override val stackable: Boolean = false

    fun canSpecial(): Boolean = Combat.specialPercentage() >= specialPercentage

    companion object {
        fun forId(id: Int): Weapon = entries.firstOrNull { id in it.ids } ?: NIL

        fun List<EquipmentRequirement>.weapon() = map { forId(it.item.id) }.firstOrNull { it != NIL } ?: NIL
    }
}