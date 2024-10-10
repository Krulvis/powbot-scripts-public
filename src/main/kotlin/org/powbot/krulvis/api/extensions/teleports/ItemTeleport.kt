package org.powbot.krulvis.api.extensions.teleports

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.IEquipmentItem
import org.powbot.krulvis.api.extensions.items.ITeleportItem
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val EDGEVILLE_GLORY = "Edgeville glory"
const val FEROX_ENCLAVE_ROD = "Ferox Enclave ROD"
const val CASTLE_WARS_ROD = "Castle Wars ROD"
const val FREMENNIK_SLAYER = "Fremennik Slayer Ring"
const val MORYTANIA_SLAYER = "Morytania Slayer Ring"
const val STRONGHOLD_SLAYER = "Stronghold Slayer Ring"
const val TEARS_OF_GUTHIX_GAMES = "Tears of Guthix Games"
const val MOUNT_KARUULM_BLESSING = "Mount Karuulm Blessing"
const val GWD_GHOMMAL_HILT = "GWD Ghommal Hilt"
const val ROYAL_SEED_POD = "Royal Seed Pod"
const val LAVA_MAZE_BURNING = "Lava Maze BA"
const val BANDIT_CAMP_BURNING = "Bandit Camp BA"
const val DURADEL_KARAMJA_GLOVES = "Duradel Karamja Gloves"
const val KALEB_ACHIEVEMENT_DIARY = "Kaleb Achievement Diary"
const val GUILD_CRAFTING_CAPE = "Guild Crafting Cape"
const val GUILD_MYTH_CAPE = "Guild Myth Cape"

enum class ItemTeleport(
	val teleportItem: ITeleportItem,
	override val action: String,
	override val destination: Tile,
	override val wildernessLevel: Int = 20,
) : Teleport {
	EDGEVILLE_GLORY(TeleportEquipment.GLORY, "Edgeville", Tile(3087, 3496, 0), 30),
	FEROX_ENCLAVE_ROD(TeleportEquipment.ROD, "Ferox Enclave", Tile(3150, 3635, 0)),
	CASTLE_WARS_ROD(TeleportEquipment.ROD, "Castle Wars", Tile(2442, 3091, 0)),
	FREMENNIK_SLAYER_RING(TeleportEquipment.SLAYER, "Fremennik", Tile(2801, 9999, 0)),
	STRONGHOLD_SLAYER_RING(TeleportEquipment.SLAYER, "Stronghold", Tile(2431, 3423, 0)),
	MORYTANIA_SLAYER_RING(TeleportEquipment.SLAYER, "Morytania", Tile(3423, 3538, 0)),
	ROYAL_SEED_POD(TeleportItem.ROYAL_SEED_POD, "Commune", Tile(2465, 3495, 0), 30),
	TEARS_OF_GUTHIX_GAMES(TeleportEquipment.GAMES, "Tears of Guthix", Tile(3243, 9502, 2)),
	MOUNT_KARUULM_BLESSING(TeleportEquipment.RADAS_BLESSING, "Mount Karuulm", Tile(1310, 3796, 0)),
	GWD_GHOMMAL_HILT(TeleportEquipment.GHOMMAL_HILT, "Trollheim", Tile(2899, 3710, 0)),
	LAVA_MAZE_BA(TeleportEquipment.BURNING, "Lava Maze", Tile(3036, 3652, 0)),
	BANDIT_CAMP_BA(TeleportEquipment.BURNING, "Bandit Camp", Tile(3028, 3843, 0)),
	DURADEL_KARAMJA_GLOVES(TeleportEquipment.KARAMJA_GLOVES, "Slayer Master", Tile(2868, 2981, 1)),
	KALEB_ACHIEVEMENT_DIARY(TeleportEquipment.ACHIEVEMENT_DIARY_CAPE, "Kaleb Paramaya", Tile(2861, 2997, 1)),
	GUILD_CRAFTING_CAPE(TeleportEquipment.CRAFTING_CAPE, "Teleport", Tile(2933, 3283, 0)),
	GUILD_MYTH_CAPE(TeleportEquipment.MYTH_CAPE, "Teleport", Tile(2456, 2850, 0))
	;

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> by lazy {
		if (teleportItem is IEquipmentItem) {
			listOf(EquipmentRequirement(teleportItem, 1))
		} else {
			listOf(InventoryRequirement(teleportItem, 1))
		}
	}

	override fun execute(): Boolean {
		return teleportItem.teleport(action)
	}

	override fun toString(): String {
		return "ItemTeleport(name=$name, action=$action)"
	}

	companion object {
		fun forName(name: String): ItemTeleport? {
			return ItemTeleport.values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
		}
	}
}

