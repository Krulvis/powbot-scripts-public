package org.powbot.krulvis.mta.rooms

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.tree.branches.CanCastEnchant

object EnchantingRoom : MTARoom {
	override val WIDGET_ID = 195
	override val portalName: String = "Enchanters"

	override fun rootComponent(mta: MTA): TreeComponent<MTA> = CanCastEnchant(mta)

	const val SHAPE_INDEX_START = 10
	private val spells = listOf(
		Magic.Spell.ENCHANT_LEVEL_7_JEWELLERY,
		Magic.Spell.ENCHANT_LEVEL_6_JEWELLERY,
		Magic.Spell.ENCHANT_LEVEL_5_JEWELLERY,
		Magic.Spell.ENCHANT_LEVEL_4_JEWELLERY,
		Magic.Spell.ENCHANT_LEVEL_3_JEWELLERY,
		Magic.Spell.ENCHANT_LEVEL_2_JEWELLERY,
		Magic.Spell.ENCHANT_LEVEL_1_JEWELLERY
	)

	fun getBonusShape(): Shape {
		val visibleComp = Components.stream(WIDGET_ID).first { it.index() >= SHAPE_INDEX_START && it.visible() }
		return Shape.values()[visibleComp.index() - SHAPE_INDEX_START]
	}

	fun getEnchantable() = Inventory.stream().name(getBonusShape().name, "Dragonstone").first()

	fun getBonusPile() = Objects.stream().nameContains(getBonusShape().name).action("Take-from").nearest().first()

	fun getEnchantSpell(): Magic.MagicSpell? = spells.firstOrNull { it.canCast() }

	fun getDroppables() =
		Inventory.stream().name(*(Shape.values().toList() - getBonusShape()).map { it.name }.toTypedArray()).toList()

	enum class Shape {
		Cube,
		Cylinder,
		None,
		Pentamid,
		None2,
		Icosahedron,
	}

}