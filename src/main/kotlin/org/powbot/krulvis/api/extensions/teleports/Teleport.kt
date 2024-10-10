package org.powbot.krulvis.api.extensions.teleports

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.teleports.poh.HousePortal
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FairyRingTeleport
import org.powbot.krulvis.api.extensions.teleports.poh.openable.OpenableHouseTeleport
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Teleport {

	val wildernessLevel: Int
		get() = 20

	val logger: Logger
	val action: String
	val requirements: List<Requirement>
	val destination: Tile
	fun execute(): Boolean

	companion object {

		const val USE_WEB_OPTION = "Use Web"
		const val DISABLE_TELEPORTS_OPTION = "Disable Teleports"

		fun forName(name: String): Teleport? {
			LoggerFactory.getLogger("Teleport")!!.info("Getting teleport for name=$name")
			return SpellTeleport.forName(name) ?: ItemTeleport.forName(name) ?: HousePortal.forName(name)
			?: FairyRingTeleport.forName(name) ?: OpenableHouseTeleport.find(name)
		}

	}
}