package org.powbot.krulvis.orbcharger

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.RunePower
import org.powbot.api.rt4.magic.Staff
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.items.Item.Companion.RUNE_POUCH
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.requirements.ItemRequirement
import org.slf4j.LoggerFactory

enum class Orb(
	val id: Int,
	val obeliskTile: Tile,
	val spell: Magic.Spell,
	val bank: BankLocation,
	vararg val requirements: ItemRequirement
) {
	WATER(
		571,
		Tile(2845, 3424, 0),
		Magic.Spell.CHARGE_WATER_ORB,
		BankLocation.FALADOR_WEST_BANK,
		InventoryRequirement(RUNE_POUCH, 1),
	),
	FIRE(
		569,
		Tile(2818, 9828, 0),
		Magic.Spell.CHARGE_FIRE_ORB,
		BankLocation.FALADOR_WEST_BANK,
		InventoryRequirement(RUNE_POUCH, 1),
	),
	EARTH(
		575,
		Tile.Nil,
		Magic.Spell.CHARGE_EARTH_ORB,
		BankLocation.EDGEVILLE_BANK,
		EquipmentRequirement(TeleportEquipment.GLORY)
	),
	AIR(
		573,
		Tile.Nil,
		Magic.Spell.CHARGE_AIR_ORB,
		BankLocation.EDGEVILLE_BANK,
		EquipmentRequirement(TeleportEquipment.GLORY)
	);

	val logger = LoggerFactory.getLogger(javaClass.simpleName)
	fun castOnObelisk(): Boolean {
		val magicSpell = Magic.magicspell()
		logger.info("Spell=$magicSpell")
		if (magicSpell != spell) {
			if (!Game.tab(Game.Tab.MAGIC)) return false
			val spellC = spell.component()
			if (!spellC.valid()) {
				logger.info("Spell component is null")
				return false
			}
			if (spellC.click()) {
				waitFor { Magic.magicspell() == spell }
			}
		}
		return getObelisk()?.interact("Cast") == true
	}

	fun getObelisk(): GameObject? {
		val name = this.name.lowercase().replaceFirstChar { it.uppercase() }
		return Objects.stream().within(10.0).name("Obelisk of $name").firstOrNull()
	}

	fun staffEquipped() = Staff.equippedPowers().contains(RunePower.valueOf(name))

	companion object {
		val UNPOWERED = 567
		val COSMIC = 564
	}

}