package org.powbot.krulvis.demonicgorilla


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
const val DEMONIC_GORILLA = "Demonic gorilla"
const val RESURRECT_OPTION = "Resurrect Spell"

val lootNames = listOf(
	"Zenyte shard",
	"Ballista limbs",
	"Ballista spring",
	"Light frame",
	"Heavy frame",
	"Monkey tail",
	"Rune platelegs",
	"Rune plateskirt",
	"Rune chainbody",
	"Dragon scimitar",
	"Runite bolts",
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
	"Prayer potion(3)",
	"Shark",
	"Coins",
	"Rune javelin heads",
	"Dragon javelin heads",
	"Adamantite bar",
	"Diamond",
	"Runite bar",
	"Clue scroll"
).map { it.lowercase() }.toMutableList()

object Data {


	const val DEMONIC_GORILLA_MAGE_ANIM = 7225
	const val DEMONIC_GORILLA_MELEE_ANIM = 7226
	const val DEMONIC_GORILLA_RANGE_ANIM = 7227
	const val DEMONIC_GORILLA_DEATH_ANIM = 7229

	enum class DemonicPrayer {
		MELEE,
		MAGE,
		RANGE,
		NONE,
		;

		companion object {
			fun forOverheadId(id: Int): DemonicPrayer = if (id == -1) NONE else values()[id]
		}
	}
}