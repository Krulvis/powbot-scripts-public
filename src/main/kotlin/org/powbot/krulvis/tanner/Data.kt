package org.powbot.krulvis.tanner

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Widgets

object Data {

	const val HIDE_OPTION = "Hide type"
	const val COWHIDE_OPTION = "Cowhide type"

	const val SOFT_LEATHER = "Soft leather"
	const val HARD_LEATHER = "Hard leather"
	const val SNAKESKIN_20 = "Snakeskin swamp"
	const val SNAKESKIN_15 = "Snakeskin"
	const val GREEN_D_HIDE = "Green d hide"
	const val BLUE_D_HIDE = "Blue d hide"
	const val RED_D_HIDE = "Red d hide"
	const val BLACK_D_HIDE = "Black d hide"

	val ROOT = 324
	val TANNING_ID=92

	enum class Hide(val raw: Int, val product: Int, val cost: Int) {
		SOFT_LEATHER(1739, 1741, 1),
		HARD_LEATHER(1739, 1743, 3),
		SNAKESKIN(6287, 6289, 15),
		SNAKESKIN_SWAMP(7801, 6289, 20),
		GREEN_D_HIDE(1753, 1745, 20),
		BLUE_D_HIDE(1751, 2505, 20),
		RED_D_HIDE(1749, 2507, 20),
		BLACK_D_HIDE(1747, 2509, 20),
		;

		fun rawCount(): Int = Inventory.stream().id(raw).count().toInt()
		fun hasRaw(): Boolean = rawCount() > 0
		fun hasCoins() = Inventory.stream().name("Coins").count(true) >= rawCount() * cost

		fun tanComp() = Widgets.component(ROOT, TANNING_ID+ordinal)
	}

	fun getHideForOption(name: String): Hide? =
		Hide.values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
}