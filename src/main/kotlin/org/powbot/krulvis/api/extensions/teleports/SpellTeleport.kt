package org.powbot.krulvis.api.extensions.teleports

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.requirement.RunePowerRequirement
import org.powbot.api.rt4.Magic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val FALADOR_TELEPORT = "Falador teleport"
const val HOUSE_TELEPORT = "House teleport"
const val MOONCLAN_TELEPORT = "Moonclan teleport"
const val OURANIA_TELEPORT = "Ourania teleport"

enum class SpellTeleport(
	val spell: Magic.MagicSpell,
	override val destination: Tile,
	override val action: String = "Cast"
) : Teleport {
	FALADOR_TELEPORT(Magic.Spell.FALADOR_TELEPORT, Tile(2966, 3377, 0)),
	VARROCK_TELEPORT(Magic.Spell.VARROCK_TELEPORT, Tile(3213, 3423, 0)),
	HOUSE_TELEPORT(Magic.Spell.TELEPORT_TO_HOUSE, Tile.Nil),
	MOONCLAN_TELEPORT(Magic.LunarSpell.MOONCLAN_TELEPORT, Tile(2092, 3914, 0)),
	OURANIA_TELEPORT(Magic.LunarSpell.OURANIA_TELEPORT, Tile(2468, 3244, 0))
	;

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = spell.requirements().filterIsInstance<RunePowerRequirement>()

	override fun execute(): Boolean {
		return spell.cast(action)
	}

	override fun toString(): String {
		return "MagicTeleport($name)"
	}

	companion object {
		fun forName(name: String): SpellTeleport? {
			return values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
		}
	}
}


