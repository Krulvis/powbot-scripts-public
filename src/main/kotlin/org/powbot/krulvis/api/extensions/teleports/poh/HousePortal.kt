package org.powbot.krulvis.api.extensions.teleports.poh

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val LUNAR_ISLE_HOUSE_PORTAL = "Lunar isle portal POH"

enum class HousePortal(override val destination: Tile) : HouseTeleport {
	ARCEUUS_LIBRARY(Tile.Nil),
	DRAYNOR_MANOR(Tile.Nil),
	BATTLEFRONT(Tile.Nil),
	VARROCK(Tile(3213, 3423, 0)),
	MIND_ALTAR(Tile.Nil),
	LUMBRIDGE(Tile.Nil),
	FALADOR(Tile(2966, 3377, 0)),
	SALVE_GRAVEYARD(Tile.Nil),
	CAMELOT(Tile.Nil),
	FENKENSTRAIN(Tile.Nil),
	KOUREND_CASTLE(Tile.Nil),
	EAST_ARDOUGNE(Tile.Nil),
	CIVITAS_ILLA_FORTIS(Tile.Nil),
	WATCHTOWER(Tile.Nil),
	DIGSITE(Tile.Nil),
	WEST_ARDOUGNE(Tile.Nil),
	MARIM(Tile.Nil),
	HARMONY_ISLAND(Tile(3797, 2865, 0)),
	CANIFIS(Tile.Nil),
	LUNAR_ISLE(Tile(2092, 3914, 0)),
	FORGOTTEN_CEMETERY(Tile.Nil),
	WATERBIRTH_ISLAND(Tile.Nil),
	BARROWS(Tile(3567, 3316, 0)),
	GRAVEYARD_OF_SHADOWS(Tile.Nil),
	FISHING_GUILD(Tile.Nil),
	CATHERBY(Tile(2802, 3447, 0)),
	DEMONIC_RUINS(Tile.Nil),
	APE_ATOLL_DUNGEON(Tile.Nil),
	FROZEN_WASTE_PLATEAU(Tile.Nil),
	TROLL_STRONGHOLD(Tile(2838, 3694, 0)),
	WEIS(Tile(2847, 3942, 0))
	;

	override val logger: Logger = LoggerFactory.getLogger("HousePortal")
	override val action: String = "Enter"
	override val requirements: List<Requirement> = emptyList()

	private val portal: GameObject
		get() = Objects.stream().type(GameObject.Type.INTERACTIVE)
			.nameContains(name.replace("_", " "))
			.nameContains("portal")
			.action("Enter").first()

	override fun insideHouseTeleport(): Boolean {
		return walkAndInteract(portal, "Enter")
	}

	companion object {
		fun forName(portalName: String): HousePortal? {
			return if (!portalName.contains("portal POH")) null
			else values().firstOrNull { portalName.contains(it.name.replace("_", " "), true) }
		}
	}

}

fun main() {
	println(HousePortal.forName(LUNAR_ISLE_HOUSE_PORTAL))
}
