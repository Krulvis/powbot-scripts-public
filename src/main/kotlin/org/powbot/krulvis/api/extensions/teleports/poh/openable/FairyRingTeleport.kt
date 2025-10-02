package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.FairyRing
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.api.extensions.teleports.poh.HouseTeleport
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val IDENTIFYER = "fairy ring (POH)"
const val FAIRY_RING_DJR = "DJR $IDENTIFYER"
const val FAIRY_RING_BLS = "BLS $IDENTIFYER"
const val FAIRY_RING_DLS = "DLS $IDENTIFYER"
const val FAIRY_RING_CKS = "CKS $IDENTIFYER"
const val FAIRY_RING_AKQ = "AKQ $IDENTIFYER"
const val FAIRY_RING_CKR = "CKR $IDENTIFYER"
const val FAIRY_RING_CKQ = "CKQ $IDENTIFYER"
const val FAIRY_RING_ZANARIS = "Zanaris $IDENTIFYER"

private const val maxDistance = 15

enum class FairyRingTeleport(override val destination: Tile) : HouseTeleport {
	BKP(Tile(2385, 3035, 0)),
	BJS(Tile(2150, 3070, 0)),
	BLS(Tile(1295, 3493, 0)),
	DJR(Tile(1455, 3658, 0)),
	DJP(Tile(2658, 3230, 0)),
	DLS(Tile(3447, 9824, 0)),
	CKS(Tile(3447, 3470, 0)),
	CKR(Tile(2801, 3003, 0)),
	CKQ(Tile(1359, 2941, 0)),
	AKQ(Tile(2319, 3619, 0)),
	Zanaris(Tile(2412, 4434, 0)),
	;

	private fun getRing(): GameObject {
		return Objects.stream(maxDistance, GameObject.Type.INTERACTIVE).name("Spiritual Fairy Tree", "Fairy ring")
			.first()
	}

	override fun insideHouseTeleport(): Boolean {
		val ring = getRing()
		val lastDestinationAction = ring.actions().firstOrNull { it.contains(name, true) }
		if (lastDestinationAction != null) {
			return walkAndInteract(ring, lastDestinationAction)
		}
		val configureAction = ring.actions().firstOrNull { it.contains("configure", true) } ?: "Ring-configure"
		if (!FairyRing.opened()) {
			if (!ring.valid()) {
				val nearest = entries.minByOrNull { it.destination.distance() }!!.destination
				Movement.builder(nearest).setWalkUntil({ nearest.distance() < maxDistance }).move()
			} else if (walkAndInteract(ring, configureAction)) {
				waitForDistance(ring) { FairyRing.opened() }
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