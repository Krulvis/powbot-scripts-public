package org.powbot.krulvis.api.extensions

import org.powbot.api.Interactable
import org.powbot.api.InteractableEntity
import org.powbot.api.Nameable
import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.distanceM
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory


enum class BankType {
	BOOTH, CHEST, NPC, DEPOSIT_BOX
}


enum class BankLocation(
	val tile: Tile,
	val type: BankType,
	private vararg val requirements: Requirement
) {
	ARDOUGNE_NORTH_BANK(Tile(2615, 3332, 0), BankType.BOOTH),
	ARDOUGNE_SOUTH_BANK(Tile(2655, 3283, 0), BankType.BOOTH),
	AL_KHARID_BANK(Tile(3269, 3167, 0), BankType.BOOTH),
	BURTHORPE_BANK(
		Tile(3047, 4974, 1), BankType.NPC
	),
	CANIFIS_BANK(
		Tile(3512, 3480, 0), BankType.BOOTH
	),
	CATHERBY_BANK(Tile(2809, 3441, 0), BankType.BOOTH),
	CASTLE_WARS_BANK(
		Tile(2443, 3083, 0), BankType.CHEST
	),
	DRAYNOR_BANK(Tile(3092, 3245, 0), BankType.BOOTH),
	EDGEVILLE_BANK(Tile(3094, 3491, 0), BankType.BOOTH),
	FALADOR_WEST_BANK(Tile(2947, 3368, 0), BankType.BOOTH),
	FALADOR_EAST_BANK(Tile(3013, 3355, 0), BankType.BOOTH),
	FARMING_GUILD_85(
		Tile(1248, 3758, 0), BankType.BOOTH,
	),
	FARMING_GUILD_65(
		Tile(1253, 3741, 0), BankType.CHEST,
	),
	FEROX_ENCLAVE(Tile(3135, 3628), BankType.CHEST),
	GRAND_EXCHANGE(
		Tile(3167, 3489, 0), BankType.NPC,
	),
	HOSIDIUS_BEST_BANK_SPOT(
		Tile(1676, 3615, 0), BankType.CHEST,
	),
	KOUREND_TOP_BUILDING(Tile(1611, 3681, 2), BankType.BOOTH),
	LUMBRIDGE_TOP(Tile(3208, 3221, 2), BankType.BOOTH),
	LUMBRIDGE_CASTLE_BANK(Tile(3208, 3220, 2), BankType.BOOTH),
	GNOME_STRONGHOLD_BANK(Tile(2445, 3424, 1), BankType.BOOTH),
	FISHING_GUILD_BANK(
		Tile(2586, 3419, 0), BankType.BOOTH,
	),
	MISCELLANIA_BANK(
		Tile(2618, 3895, 0), BankType.NPC,
	),
	MINING_GUILD(
		Tile(3013, 9718, 0), BankType.CHEST,
	),
	MOTHERLOAD_MINE(
		Tile(3760, 5666, 0), BankType.CHEST,
	),
	MOTHERLOAD_MINE_DEPOSIT(
		Tile(3758, 5664, 0), BankType.DEPOSIT_BOX,
	),
	PRIFIDDINAS(
		Tile(3256, 6108, 0), BankType.BOOTH,
	),
	PRIFIDDINAS_MINE_CART(
		Tile(3307, 12443, 0), BankType.DEPOSIT_BOX
	),
	PORT_SARIM_DB(
		Tile(3045, 3235, 0), BankType.DEPOSIT_BOX,
	),
	SEERS_BANK(
		Tile(2727, 3493, 0), BankType.BOOTH,
	),
	SHAYZIEN_NORTH_CHEST(
		Tile(1483, 3646, 0), BankType.CHEST,
	),
	SHAYZIEN_SOUTH_BOOTH(
		Tile(1488, 3592, 0), BankType.BOOTH,
	),
	SHANTAY_PASS_BANK(
		Tile(3308, 3120, 0), BankType.CHEST,
	),
	SHILO_GEM_MINE(
		Tile(2842, 9382, 0), BankType.DEPOSIT_BOX,
	),
	TZHAAR_BANK(
		Tile(2446, 5178, 0), BankType.NPC,
	),
	VARROCK_WEST_BANK(Tile(3185, 3436, 0), BankType.BOOTH),
	VARROCK_EAST_BANK(Tile(3253, 3420, 0), BankType.BOOTH),
	WOODCUTTING_GUILD(
		Tile(1592, 3476, 0), BankType.CHEST,
	),
	WARRIORS_GUILD(
		Tile(2843, 3543, 0), BankType.BOOTH,
		requirements = arrayOf(object : Requirement {
			override fun meets(): Boolean {
				return Skills.realLevel(Constants.SKILLS_STRENGTH) + Skills.realLevel(Constants.SKILLS_ATTACK) >= 130
			}
		})
	),
	WINTERTODT(
		Tile(1640, 3944, 0), BankType.CHEST,
	),
	YANILLE_BANK(Tile(2613, 3094, 0), BankType.BOOTH),
	FOSSIL_ISLAND_CAMP(Tile(3741, 3805), BankType.CHEST),
	MINE_CART_PRIF(Tile(3307, 12443, 0), BankType.DEPOSIT_BOX),
	;


	/**
	 * Opens the bank at the location
	 */
	fun open(): Boolean {
		logger.info("Banking at: $this")
		if (Bank.opened()) {
			return true
		} else if (CollectionBox.opened()) {
			CollectionBox.close()
		} else if (me.floor() != tile.floor || tile.distance() >= 14) {
			Movement.walkTo(tile)
			return false
		}
		return Bank.open()
	}

	/**
	 *
	 * @return true if all the requirements are met to access this bank
	 */
	fun canUse(): Boolean {
		return requirements.all { it.meets() }
	}

	override fun toString(): String = "$name, tile=$tile, type=$type"

	companion object {

		private val logger = LoggerFactory.getLogger("BankLocation")

		fun DepositBox.openNearestDB(): Boolean {
			val nearest = values()
				.filter { it.type == BankType.DEPOSIT_BOX }
				.minByOrNull { it.tile.distance() } ?: return false
			val box = Objects.stream().name("Mine Cart", "Bank deposit box", "Bank deposit chest").action("Deposit")
				.firstOrNull()
			if (box == null || nearest.tile.distance() > 15 || !nearest.tile.reachable()) {
				logger.info("nearest=$nearest, distance=${nearest.tile.distance()} is not reachable!")
				val localPath = LocalPathFinder.findPath(nearest.tile.getWalkableNeighbor())
				if (localPath.isNotEmpty()) {
					logger.info("LocalWalking to=${localPath.finalDestination()}")
					localPath.traverseUntilReached()
				} else {
					try {
						logger.info("WebWalking to=${nearest.tile}")
						Movement.walkTo(nearest.tile)
					} catch (e: Exception) {
						logger.info("Failed to move to bank!")
						logger.info(e.stackTraceToString())
					}
				}
			} else {
				return walkAndInteract(box, "Deposit") && waitFor(5000) { opened() }
			}
			return false
		}

		fun Bank.openNearest(teleportMethod: TeleportMethod = TeleportMethod(null)): Boolean {
			if (opened()) {
				return true
			}
			val nearest = getBank()
			logger.info("Nearest bank: $nearest")
			if (nearest.getWalkableNeighbor { it.reachable() } != null) {
				nearest.setBankBounds()
				return walkAndInteract(nearest, nearest.bankAction())
			} else if (teleportMethod.execute()) {
				Movement.moveToBank()
			}
			return false
		}

	}


}

val GE_BANK_BOUNDS = intArrayOf(28, 52, -94, 0, -32, 32)
val BANK_BOUNDS = intArrayOf(-32, 32, -64, 0, -32, 32)

fun InteractableEntity.setBankBounds() {
	val name = (this as Nameable).name()
	val bounds = if (name == "Grand Exchange booth") GE_BANK_BOUNDS else BANK_BOUNDS
	(this as Interactable).bounds(bounds)
}

private val BANK_ACTIONS = listOf("Bank", "Open", "Use")
fun InteractableEntity.bankAction(): String {
	val actions = actions()
	return actions.first { it in BANK_ACTIONS }
}

fun main() {
	println(
		"[\"${
			BankLocation.values().map {
				it.name
			}.joinToString("\", \"")
		}\"]"
	)
}