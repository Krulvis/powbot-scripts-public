package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.FairyRing
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.teleports.poh.HouseTeleport
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val IDENTIFYER = "fairy ring"
const val FAIRY_RING_DJR = "DJR $IDENTIFYER (POH)"
const val FAIRY_RING_BLS = "BLS $IDENTIFYER (POH)"
const val FAIRY_RING_DLS = "DLS $IDENTIFYER (POH)"
const val FAIRY_RING_CKS = "CKS $IDENTIFYER (POH)"
const val FAIRY_RING_ZANARIS = "Zanaris $IDENTIFYER (POH)"

enum class FairyRingTeleport(override val destination: Tile) : HouseTeleport {
	DJR(Tile(1455, 3658, 0)),
	BLS(Tile(1295, 3493, 0)),
	DLS(Tile(3447, 9824, 0)),
	CKS(Tile(3447, 3470, 0)),
	Zanaris(Tile(2412, 4434, 0)),
	;

	private fun getRings(): GameObject {
		return Objects.stream().type(GameObject.Type.INTERACTIVE).name("Spiritual Fairy Tree", "Fairy ring").first()
	}

	override fun insideHouseTeleport(): Boolean {
		val rings = getRings()
		val lastDestinationAction = rings.actions().firstOrNull { it.contains(name, true) }
		if (lastDestinationAction != null) {
			return walkAndInteract(rings, lastDestinationAction)
		}
		val configureAction = rings.actions().firstOrNull { it.contains("configure", true) } ?: "Ring-configure"
		if (!FairyRing.opened()) {
			if (walkAndInteract(rings, configureAction)) {
				waitForDistance(rings) { FairyRing.opened() }
			}
		}
		if (FairyRing.opened()) {
			return FairyRing.teleport(name)
		}

		return false
	}


	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val action: String = "last-destination ($name)"
	override val requirements: List<Requirement> = emptyList()

	override fun toString(): String {
		return "FairyRingTeleport($name)"
	}

	companion object {
		fun forName(name: String): FairyRingTeleport? {
			return if (!name.contains(IDENTIFYER)) null
			else {
				val combination = name.split(" ").first()
				return FairyRingTeleport.values().find { it.name.equals(combination, true) }
			}
		}
	}

}