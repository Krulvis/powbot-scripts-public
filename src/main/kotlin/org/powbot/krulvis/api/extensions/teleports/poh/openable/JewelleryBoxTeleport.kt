package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val JEWELLERY_BOX = "Jewellery box (POH)"
private const val JEWELLERYBOX_WIDGET = 590
private const val JEWELLERYBOX_COMPONENT = 1
private const val JEWELLERYBOX_CLOSE_BUTTON = 11

private const val BASIC_JEWELLERY_BOX = "Basic jewellery box"
private const val FANCY_JEWELLERY_BOX = "Fancy jewellery box"
private const val ORNATE_JEWELLERY_BOX = "Ornate jewellery box"

const val PVP_ARENA_JEWELLERY_BOX = "PvP arena $JEWELLERY_BOX"
const val CASTLE_WARS_JEWELLERY_BOX = "Castle wars $JEWELLERY_BOX"
const val TEARS_OF_GUTHIX_JEWELLERY_BOX = "Tears of Guthix $JEWELLERY_BOX"
const val FEROX_ENCLAVE_JEWELLERY_BOX = "Ferox enclave $JEWELLERY_BOX"

enum class JewelleryBoxTeleport(override val action: String, override val destination: Tile, override val COMP_ID: Int) : OpenableHouseTeleport {
	PVP_ARENA("PvP Arena", Tile(3316, 3234, 0), 2),
	CASTLE_WARS("Castle Wars", Tile(2442, 3091, 0),2),
	FEROX_ENCLAVE("Ferox Enclave",Tile(3150, 3635, 0), 2),
	BURTHORPE("Burthorpe", Tile(2896, 3552, 0),3),
	BARBARIAN_OUTPOST("Barbarian Outpost",Tile(2518, 3570, 0), 3),
	CORPORAL_BEAST("Corporeal Beast", Tile.Nil,3),
	TEARS_OF_GUTHIX("Tears of Guthix",Tile(3243, 9502, 2), 3),
	WINTERTODT("Wintertodt Camp", Tile.Nil,3),
	WARRIORS_GUILD("Warriors' Guild",Tile.Nil, 4),
	CHAMPIONS_GUILD("Champions' Guild", Tile.Nil,4),
	MONASTERY("Monastery", Tile.Nil,3),
	RANGING_GUILD("Ranging Guild", Tile.Nil,4),
	FISHING_GUILD("Fishing Guild", Tile.Nil,5),
	MINING_GUILD("Mining Guild", Tile.Nil,5),
	CRAFTING_GUILD("Crafting Guild", Tile(2932, 3295, 0),5),
	COOKING_GUILD("Cooking Guild", Tile.Nil,5),
	WOODCUTTING_GUILD("Woodcutting Guild", Tile(1664, 3503, 0),5),
	FARMING_GUILD("Farming Guild", Tile(1248, 3723, 0),5),
	MISCELLANIA("Miscellania", Tile(2537, 3863, 0),6),
	GRAND_EXCHANGE("Grand Exchange", Tile(3162, 3478, 0),6),
	FALADOR_PARK("Falador Park", Tile(2998, 3373, 0),6),
	DONDAKANS_ROCK("Dondakans' Rock", Tile(2830 ,10169, 0),6),
	EDGEVILLE("Edgeville", Tile(3087, 3496, 0),7),
	KARAMJA("Karamja", Tile(2918, 3176, 0),7),
	DRAYNOR_VILLAGE("Draynor Village", Tile(3105, 3251, 0),7),
	AL_KHARID("Al Kharid", Tile(3293, 3163, 0),7);

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val WIDGET_ID: Int = JEWELLERYBOX_WIDGET
	override val OBJECT_NAMES = arrayOf(BASIC_JEWELLERY_BOX, FANCY_JEWELLERY_BOX, ORNATE_JEWELLERY_BOX)
	override fun toString(): String {
		return "JewelleryBoxTeleport($name)"
	}


	companion object {
		fun forName(teleport: String): JewelleryBoxTeleport? {
			return if (!teleport.contains(JEWELLERY_BOX)) null
			else values().firstOrNull { teleport.contains(it.name.replace("_", " "), true) }
		}
	}

}