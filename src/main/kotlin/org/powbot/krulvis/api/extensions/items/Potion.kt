package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.uppercaseFirst
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.floor

enum class Potion(
	val skill: Int, private val restore: Int,
	override vararg val ids: Int
) : Item {
	ENERGY(Constants.SKILLS_AGILITY, 10, 3008, 3010, 3012, 3014),
	SUPER_ENERGY(Constants.SKILLS_AGILITY, 20, 3016, 3018, 3020, 3022),
	STAMINA(Constants.SKILLS_AGILITY, 20, 12625, 12627, 12629, 12631),
	RANGED(Constants.SKILLS_RANGE, -1, 2444, 169, 171, 173),
	DIVINE_RANGED(Constants.SKILLS_RANGE, -1, 23733, 23736, 23739, 23742),
	BASTION(Constants.SKILLS_RANGE, -1, 22461, 22464, 22467, 22470),
	DIVINE_BASTION(Constants.SKILLS_RANGE, -1, 24635, 24638, 24641, 24644),
	ATTACK(Constants.SKILLS_ATTACK, -1, 2428, 121, 123, 125),
	STRENGTH(Constants.SKILLS_STRENGTH, -1, 113, 115, 117, 119),
	DEFENCE(Constants.SKILLS_DEFENSE, -1, 2432, 133, 135, 137),
	COMBAT(Constants.SKILLS_STRENGTH, -1, 9739, 9741, 9743, 9745),
	SUPER_ATTACK(Constants.SKILLS_ATTACK, -1, 2436, 145, 147, 149),
	DIVINE_SUPER_ATTACK(Constants.SKILLS_ATTACK, -1, 23697, 23700, 23703, 23706),
	SUPER_STRENGTH(Constants.SKILLS_STRENGTH, -1, 2440, 157, 159, 161),
	DIVINE_SUPER_STRENGTH(Constants.SKILLS_STRENGTH, -1, 23709, 23712, 23715, 23718),
	SUPER_DEFENCE(Constants.SKILLS_DEFENSE, -1, 2442, 163, 165, 167),
	DIVINE_SUPER_DEFENCE(Constants.SKILLS_DEFENSE, -1, 23721, 23724, 23727, 23730),
	SUPER_COMBAT(Constants.SKILLS_STRENGTH, -1, 12695, 12697, 12699, 12701),
	DIVINE_SUPER_COMBAT(Constants.SKILLS_STRENGTH, -1, 23685, 23688, 23691, 23694),
	PRAYER(Constants.SKILLS_PRAYER, -1, 2434, 139, 141, 143),
	OVERLOAD(Constants.SKILLS_STRENGTH, -1, 11730, 11731, 11732, 11733),
	ABSORPTION(Constants.SKILLS_HITPOINTS, -1, 11734, 11735, 11736, 11737),
	ANTIFIRE_EXTENDED(-1, -1, 11951, 11953, 11955, 11957),
	ANTIFIRE(-1, -1, 2452, 2454, 2456, 2458),
	ANTIPOISON(-1, -1, 2446, 175, 177, 179),
	SUPER_ANTIPOISON(-1, -1, 2448, 181, 183, 185),
	SUPER_RESTORE(Constants.SKILLS_PRAYER, -1, 3024, 3026, 3028, 3030)
	;

	override val itemName: String = "${name.uppercaseFirst().replace("_", " ")} potion"
	override val stackable: Boolean = false
	val bestPot: Int = ids[0]

	var lastSip = Timer()

	fun getRestore(): Int {
		return when (this) {
			OVERLOAD -> 5 + floor(Skills.realLevel(skill) * 0.15).toInt()
			RANGED, BASTION, DIVINE_BASTION, DIVINE_RANGED -> 4 + floor(Skills.realLevel(skill) * 0.1).toInt()
			PRAYER -> floor(7 + Skills.realLevel(skill) / 4.0).toInt()
			SUPER_RESTORE -> floor(8 + Skills.realLevel(skill) / 4.0).toInt()
			ATTACK, STRENGTH, DEFENCE, COMBAT -> 3 + floor(Skills.realLevel(skill) / 10.0).toInt()
			SUPER_ATTACK, SUPER_STRENGTH, SUPER_DEFENCE, SUPER_COMBAT,
			DIVINE_SUPER_COMBAT, DIVINE_SUPER_ATTACK, DIVINE_SUPER_STRENGTH, DIVINE_SUPER_DEFENCE
				-> 5 + floor(Skills.realLevel(skill) * 0.15).toInt()

			else -> {
				restore
			}
		}
	}

	fun drink(): Boolean {
		val item = getInvItem(true)
		if (item != null) {
			if (!Bank.opened()) Game.tab(Game.Tab.INVENTORY)
			val id = item.id
			val drank = item.interact("Drink")
			if (drank && (this == ANTIFIRE_EXTENDED || this == ANTIFIRE)
				&& waitFor { id != (getInvItem()?.id ?: -1) }
			) {
				antiFireTimer = Timer(if (this == ANTIFIRE_EXTENDED) 12 * 60 * 1000 else 5.8 * 60 * 1000)
			}
			logger.info("Drinking $name, success=$drank")
			if (drank) {
				lastSip = Timer()
			}
			return drank

		}
		return false
	}


	fun needsRestore(percentage: Int): Boolean {
		return when (this) {
			ANTIFIRE_EXTENDED, ANTIFIRE -> !isProtectedFromFire()
			ANTIPOISON, SUPER_ANTIPOISON -> Combat.isPoisoned()
			PRAYER, SUPER_RESTORE -> {
				val rl = Skills.realLevel(skill)
				val cl = Skills.level(skill)
				cl <= 5 || rl - cl >= getRestore() + (percentage / 5.0)
			}

			ABSORPTION -> getAbsorptionRemainder() < 50
			STAMINA, ENERGY, SUPER_ENERGY -> !isHighOnStamina() && Movement.energyLevel() <= percentage
			else -> {
				val rl = if (skill == Constants.SKILLS_AGILITY) 100 else Skills.realLevel(skill)
				val cl = if (skill == Constants.SKILLS_AGILITY) Movement.energyLevel() else Skills.level(skill)
				val difference = abs(rl - cl)
				val restore = getRestore()
				val restorePercent = (restore * (percentage / 100.0)).toInt()
				difference <= restorePercent || cl < rl
			}
		}
	}

	fun doses(): Int = Inventory.stream().id(*ids).sumOf { ids.size - ids.indexOf(it.id) }

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(countNoted)
	}

	override fun hasWith(): Boolean {
		return inInventory()
	}

	override fun withdrawExact(amount: Int, worse: Boolean, wait: Boolean): Boolean {
		return super.withdrawExact(amount, worse = false, wait = true)
	}

	companion object {

		private val logger = LoggerFactory.getLogger("Potion")
		var antiFireTimer = Timer(1)

		fun isHighOnStamina(): Boolean {
			val actual = Varpbits.varpbit(1575)
			return (actual == 12582912 || actual == 4194304) || Movement.energyLevel() >= 60
		}

		fun getAbsorptionRemainder(): Int {
			val s = Varpbits.varpbit(1067) and 0x7fff
			return if (s > 0) s / 32 else 0
		}

		fun isProtectedFromFire(): Boolean {
			return !antiFireTimer.isFinished()
		}

		fun forId(id: Int): Potion? {
			return values().firstOrNull { id in it.ids }
		}

		fun isPotion(id: Int) = forId(id) != null

		private fun getAntipots(): List<Potion> = listOf(ANTIPOISON, SUPER_ANTIPOISON)
		fun getAntipot(): Potion? = getAntipots().firstOrNull { it.inInventory() }
		fun getAntipotBank(): Potion? = getAntipots().firstOrNull { it.inBank() }
		fun hasAntipot(): Boolean = getAntipot() != null
		fun hasAntipotBank(): Boolean = getAntipotBank() != null

		fun getPrayerPotion(): Potion? = listOf(PRAYER, SUPER_RESTORE).firstOrNull { it.hasWith() }
	}


}
