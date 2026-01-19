package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val MOUNTED_XERICS_WIDGET = 187
private const val MOUNTED_XERICS_COMPONENT = 3

enum class MountedXericsTeleport(override val action: String, override val destination: Tile) : OpenableHouseTeleport {
	XericsLookout("Xeric's Lookout", Tile.Nil),
	XericsGlade("Xeric's Glade", Tile.Nil),
	XericsInferno("Xeric's Inferno", Tile.Nil),
	XericsHeart("Xeric's Heart", Tile.Nil),
	XericsHonour("Xeric's Honour", Tile.Nil);

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val COMP_ID: Int = MOUNTED_XERICS_WIDGET
	override val WIDGET_ID: Int = MOUNTED_XERICS_COMPONENT
	override val OBJECT_NAMES = arrayOf("Xeric's Talisman")
	override fun toString(): String {
		return "MountedXericsTeleport($name)"
	}
	companion object {
		fun forName(name: String) = values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}

}