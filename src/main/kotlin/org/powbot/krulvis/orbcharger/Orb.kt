package org.powbot.krulvis.orbcharger

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.items.Staff
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.extensions.magic.Rune
import org.powbot.krulvis.api.extensions.magic.RunePower
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.requirements.EquipmentRequirement
import org.powbot.krulvis.api.utils.requirements.InventoryRequirement
import org.powbot.krulvis.api.utils.requirements.ItemRequirement

enum class Orb(
    val id: Int,
    val obeliskTile: Tile,
    val spell: Magic.Spell,
    val bank: BankLocation,
    vararg val requirements: ItemRequirement
) {
    WATER(
        571, Tile(2845, 3424, 0), Magic.Spell.CHARGE_WATER_ORB, BankLocation.FALADOR_WEST_BANK,
        InventoryRequirement(Rune.AIR, 3, allowMore = true), InventoryRequirement(Rune.LAW, 1, allowMore = true)
    ),
    FIRE(
        569, Tile.Nil, Magic.Spell.CHARGE_FIRE_ORB, BankLocation.FALADOR_WEST_BANK,
        InventoryRequirement(Rune.AIR, 3, allowMore = true), InventoryRequirement(Rune.LAW, 1, allowMore = true)
    ),
    EARTH(
        575,
        Tile.Nil,
        Magic.Spell.CHARGE_EARTH_ORB,
        BankLocation.EDGEVILLE_BANK,
        EquipmentRequirement(TeleportItem.GLORY)
    ),
    AIR(
        573,
        Tile.Nil,
        Magic.Spell.CHARGE_AIR_ORB,
        BankLocation.EDGEVILLE_BANK,
        EquipmentRequirement(TeleportItem.GLORY)
    )
    ;

    fun castOnObelisk(): Boolean {
        if (Magic.magicspell() != spell) {
            if (Magic.cast(spell)) {
                waitFor { Magic.magicspell() == spell }
            }
        }
        return getObelisk()?.interact("Cast") == true
    }

    fun getObelisk(): GameObject? {
        val name = this.name.lowercase().replaceFirstChar { it.uppercase() }
        return Objects.stream().within(10.0).name("Obelisk of $name").firstOrNull()
    }

    fun staffEquipped() = Staff.getEquippedStaff()?.runePowers?.contains(RunePower.valueOf(name)) == true

    companion object {
        val UNPOWERED = 567
        val COSMIC = 564
    }

}