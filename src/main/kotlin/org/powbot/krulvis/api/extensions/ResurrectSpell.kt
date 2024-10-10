package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Prayer
import org.powbot.krulvis.api.extensions.items.Item.Companion.BOOK_OF_THE_DEAD

const val GREATER_GHOST = "GREATER_GHOST"
const val GREATER_SKELETON = "GREATER_SKELETON"
const val GREATER_ZOMBIE = "GREATER_ZOMBIE"

enum class ResurrectSpell(val spell: Magic.ArceuusSpell, val prayerRequirement: Int) {
	LESSER_GHOST(Magic.ArceuusSpell.RESURRECT_LESSER_GHOST, 2),
	LESSER_SKELETON(Magic.ArceuusSpell.RESURRECT_LESSER_SKELETON, 2),
	LESSER_ZOMBIE(Magic.ArceuusSpell.RESURRECT_LESSER_ZOMBIE, 2),
	SUPERIOR_GHOST(Magic.ArceuusSpell.RESURRECT_SUPERIOR_GHOST, 4),
	SUPERIOR_SKELETON(Magic.ArceuusSpell.RESURRECT_SUPERIOR_SKELETON, 4),
	SUPERIOR_ZOMBIE(Magic.ArceuusSpell.RESURRECT_SUPERIOR_ZOMBIE, 4),
	GREATER_GHOST(Magic.ArceuusSpell.RESURRECT_GREATER_GHOST, 6),
	GREATER_SKELETON(Magic.ArceuusSpell.RESURRECT_GREATER_SKELETON, 6),
	GREATER_ZOMBIE(Magic.ArceuusSpell.RESURRECT_GREATER_ZOMBIE, 6),
	;

	fun hasBook(): Boolean = Inventory.stream().id(BOOK_OF_THE_DEAD).isNotEmpty()
	fun canCast(): Boolean = hasBook() && Prayer.prayerPoints() >= prayerRequirement && spell.canCast()
}