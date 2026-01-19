package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.ScrollHelper
import org.powbot.api.rt4.Widgets
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val PORTAL_NEXUS = "Portal Nexus"
private const val PORTAL_WIDGET = 17
private const val PORTAL_COMPONENT = 12
private const val PORTAL_CLOSE_BUTTON_COMPONENT = 1
private const val PORTAL_CLOSE_BUTTON = 13


const val FALADOR_TELEPORT_NEXUS = "Falador nexus teleport (poh)"

enum class NexusPortalTeleport(override val action: String, override val destination: Tile) : OpenableHouseTeleport {
	ArceuusLibrary("Arceuus Library", Tile.Nil),
	DraynorManor("Draynor Manor",Tile.Nil),
	Battlefront("Battlefront",Tile.Nil),
	Varrock("Varrock",Tile(3213, 3423, 0)),
	GrandExchange("Grand Exchange", Tile(3162, 3478, 0)),
	MindAltar("Mind Altar",Tile.Nil),
	Lumbridge("Lumbridge",Tile.Nil),
	Falador("Falador",Tile(2966, 3377, 0)),
	SalveGraveyard("Salve Graveyard",Tile.Nil),
	Camelot("Camelot",Tile.Nil),
	SeersVillage("Seers' Village",Tile.Nil),
	FenkenstrainsCastle("Fenkenstrain's Castle",Tile.Nil),
	EastArdougne("East Ardougne",Tile.Nil),
	Watchtower("Watchtower",Tile.Nil),
	Yanille("Yanille",Tile.Nil),
	Senntisten("Senntisten",Tile.Nil),
	WestArdougne("West Ardougne",Tile.Nil),
	Marim("Marim",Tile.Nil),
	HarmonyIsland("Harmony Island",Tile(3797, 2865, 0)),
	Kharyrll("Kharyrll",Tile.Nil),
	KourendCastle("Kourend Castle",Tile.Nil),
	LunarIsle("Lunar Isle",Tile(2092, 3914, 0)),
	ForgottenCemetery("The Forgotten Cemetery",Tile.Nil),
	WaterbirthIsland("Waterbirth Island",Tile.Nil),
	Barrows("Barrows",Tile(3567, 3316, 0)),
	Carrallanger("Carrallanger",Tile.Nil),
	FishingGuild("Fishing Guild",Tile.Nil),
	Catherby("Catherby",Tile(2802, 3447, 0)),
	Annakarl("Annakarl",Tile.Nil),
	ApeAtollDungeon("Ape Atoll Dungeon",Tile.Nil),
	Ghorrock("Ghorrock",Tile.Nil),
	Weiss("Weiss",Tile(2847, 3942, 0)),
	TrollStronghold("Troll Stronghold",Tile(2838, 3694, 0));

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

	override val WIDGET_ID: Int = PORTAL_WIDGET
	override val COMP_ID: Int = PORTAL_COMPONENT
	override val OBJECT_NAMES = arrayOf(PORTAL_NEXUS)
	override val requirements: List<Requirement> = emptyList()
	private fun scrollComponent(): Component = Widgets.component(WIDGET_ID, 14)
	private fun paneComponent(): Component = Widgets.component(WIDGET_ID, 11)

	override fun toString(): String {
		return "NexusPortalTeleport($name)"
	}

	override fun scroll(): Boolean {
		val teleportComp = getTeleportComponent()
		val paneComp = paneComponent()
		val scrollComp = scrollComponent()
		return ScrollHelper.scrollTo({ teleportComp }, { paneComp }, { scrollComp })
	}

	companion object {
		fun forName(name: String) = values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}
}