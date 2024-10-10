package org.powbot.krulvis.tormenteddemon


const val USE_MELEE_OPTION = "Use Melee"
const val USE_RANGE_OPTION = "Use Range"
const val USE_MAGE_OPTION = "Use Mage"
const val MELEE_EQUIPMENT_OPTION = "Melee Equipment"
const val MELEE_PRAYER_OPTION = "Melee Prayer"
const val RANGE_EQUIPMENT_OPTION = "Range Equipment"
const val RANGE_PRAYER_OPTION = "Range Prayer"
const val MAGE_EQUIPMENT_OPTION = "Mage Equipment"
const val MAGE_PRAYER_OPTION = "Mage Prayer"
const val SPECIAL_WEAPON_OPTION = "Special Weapon"
const val RESURRECT_OPTION = "Resurrect Spell"
const val TEARS_OF_GUTHIX_TELEPORT_OPTION = "ToG Teleport"

const val TORMENTED_DEMON = "Tormented demon"

val lootNames = listOf(
	"Tormented synapse",
	"Burning claw",
	"Rune platebody",
	"Dragon dagger",
	"Battlestaff",
	"Rune kiteshield",
	"Soul rune",
	"Chaos rune",
	"Rune arrow",
	"Grimy kwuarm",
	"Grimy cadantine",
	"Grimy dwarf weed",
	"Grimy lantadyme",
	"Ranarr seed",
	"Snapdragon seed",
	"Torstol seed",
	"Yew seed",
	"Magic seed",
	"Spirit seed",
	"Palm tree seed",
	"Dragonfruit tree seed",
	"Celastrus seed",
	"Redwood tree seed",
	"Prayer potion(4)",
	"Prayer potion(2)",
	"Manta ray",
	"Dragon arrowtips",
	"Fire orb",
	"Guthixian temple teleport",
	"Dragon arrowtips",
	"Clue scroll"
).map { it.lowercase() }.toMutableList()

object Data {


	const val TOERMENTED_DEMON_MAGE_ANIM = 11388
	const val TOERMENTED_DEMON_RANGE_ANIM = 11389
	const val TOERMENTED_DEMON_MELEE_ANIM = 11392
	const val TOERMENTED_DEMON_DEATH_ANIM = 11394

	enum class TormentedPrayer {
		MELEE,
		MAGE,
		RANGE,
		NONE,
		;

		companion object {
			fun forOverheadId(id: Int): TormentedPrayer = if (id == -1) NONE else values()[id]
		}
	}
}