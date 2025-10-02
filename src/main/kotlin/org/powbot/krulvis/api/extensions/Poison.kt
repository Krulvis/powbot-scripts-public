package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.Varpbits
import kotlin.math.ceil

object Poison {
//	const val POISON_TICK_MILLIS: Long = 18200
	const val VARP = 102
	const val VENOM_THRESHOLD = 1000000
	const val VENOM_MAX_DMG = 20

	var varpValue = -1
	var nextDamage = -1

	fun envenomed() = varpValue >= VENOM_THRESHOLD

	fun calculateDamage(): Int {
		varpValue = Varpbits.varpbit(VARP)
		var poisonValue = varpValue
		if (poisonValue >= VENOM_THRESHOLD) {
			//Venom Damage starts at 6, and increments in twos;
			//The VarPlayer increments in values of 1, however.
			poisonValue -= VENOM_THRESHOLD - 3
			nextDamage = poisonValue * 2
			//Venom Damage caps at 20, but the VarPlayer keeps increasing
			if (nextDamage > VENOM_MAX_DMG) {
				nextDamage = VENOM_MAX_DMG
			}
		} else {
			nextDamage = ceil((poisonValue / 5.0f).toDouble()).toInt()
		}

		return nextDamage
	}
}