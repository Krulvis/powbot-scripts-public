package org.powbot.krulvis.fighter

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Prayer

const val WARRIOR_GUILD_OPTION = "Warrior Guild"
const val RADIUS_OPTION = "Kill Radius"
const val INVENTORY_OPTION = "Inventory"
const val EQUIPMENT_OPTION = "Equipment"
const val SPECIAL_EQUIPMENT_OPTION = "Special Equipment"
const val MONSTERS_OPTION = "Monsters"
const val HOP_FROM_PLAYERS_OPTION = "Hop From Players"
const val PLAYER_HOP_COUNT_OPTION = "Player Count"
const val USE_SAFESPOT_OPTION = "Use Safespot"
const val SAFESPOT_RADIUS = "Safespot radius"
const val WAIT_FOR_LOOT_OPTION = "Wait For Loot"
const val WALK_BACK_TO_SAFESPOT_OPTION = "Walk Back"
const val CENTER_TILE_OPTION = "Safespot Center"
const val IRONMAN_DROPS_OPTION = "Ironman Drops"
const val LOOT_PRICE_OPTION = "Min Loot Price"
const val LOOT_OVERRIDES_OPTION = "Loot Overrides"
const val BURY_BONES_OPTION = "Bury Bones"
const val PRAY_AT_ALTAR_OPTION = "Pray At Altar"
const val BANK_TELEPORT_OPTION = "Bank Teleport"
const val MONSTER_TELEPORT_OPTION = "Monster Teleport"
const val MONSTER_AUTO_DESTROY_OPTION = "Auto Kill"
const val USE_CANNON_OPTION = "Use Cannon"
const val CANNON_TILE_OPTION = "Cannon Tile"

val CATACOMBS_AREA = Area(Tile(1589, 10115, 0), Tile(1746, 9976, 0))

enum class Superior(val protectPrayer: Prayer.Effect = Prayer.Effect.PROTECT_FROM_MELEE) {
	CRUSHING_HAND,
	CHASM_CRAWLER,
	SCREAMING_BANSHEE,
	SCREAMING_TWISTED_BANSHEE,
	GIANT_ROCKSLUG,
	COCKATHRICE,
	FLAMING_PYRELORD,
	INFERNAL_PYRELORD,
	MONSTROUS_BASILISK,
	MALEVOLENT_MAGE,
	INSATIABLE_BLOODVELD,
	INSATIABLE_MUTATED_BLOODVELD,
	VITREOUS_JELLY,
	VITREOUS_WARPED_JELLY,
	SPIKED_TUROTH,
	MUTATED_TERRORBIRD,
	MUTATED_TORTOISE,
	CAVE_ABOMINATION,
	ABHORRENT_SPECTRE,
	REPUGNANT_SPECTRE,
	BASILISK_SENTINEL,
	SHADOW_WYRM,
	CHOKE_DEVIL,
	KING_KURASK,
	MARBLE_GARGOYLE,
	NECHRYARCH,
	GUARDIAN_DRAKE,
	GREATER_ABYSSAL_DEMON,
	NIGHT_BEAST,
	NUCLEAR_SMOKE_DEVIL,
	COLOSSAL_HYDRA,
	;

	companion object {
		fun Npc.superior() = forName(name)
		fun forName(monster: String): Superior? {
			val monsterName = monster.replace(" ", "_").uppercase()
			return values().firstOrNull { it.name == monsterName }
		}
	}
}

val SUPERIORS = listOf(
	"crushing hand",
	"chasm crawler",
	"screaming banshee",
	"screaming twisted banshee",
	"giant rockslug",
	"cockathrice",
	"flaming pyrelord",
	"infernal pyrelord",
	"monstrous basilisk",
	"malevolent mage",
	"insatiable bloodveld",
	"insatiable mutated bloodveld",
	"vitreous jelly",
	"vitreous warped jelly",
	"spiked turoth",
	"mutated terrorbird",
	"mutated tortoise",
	"cave abomination",
	"abhorrent spectre",
	"repugnant spectre",
	"basilisk sentinel",
	"shadow wyrm",
	"choke devil",
	"king kurask",
	"marble gargoyle",
	"nechryarch",
	"guardian drake",
	"greater abyssal demon",
	"night beast",
	"nuclear smoke devil",
	"colossal hydra",
)