package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Food.Companion.needsFood
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.KrulScript

/**
 * ShouldConsume branch usable throughout all scripts
 * Consumes food and potions as long as they are present in
 */
class ShouldConsume<S : KrulScript>(
	script: S,
	override val failedComponent: TreeComponent<S>,
	private val eatWhenPossible: Boolean = true
) : Branch<S>(script, "Should consume?") {

	constructor(script: S, failedComponent: TreeComponent<S>) : this(script, failedComponent, true)

	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Consuming") {
		if (!Bank.opened()) Game.tab(Game.Tab.INVENTORY)
		val prayPot = potions.firstOrNull { it == Potion.PRAYER || it == Potion.SUPER_RESTORE }
		val canSipPray = prayPot?.needsRestore(0) == true
		val food = if (canSipPray && food == null) edibleFood() else food
		val pot = potion
		if (food != null) {
			if (!food.eat()) return@SimpleLeaf
			val prayPoints = Prayer.prayerPoints()
			val ateKaram = comboEatKarambwan(food)
			consumeTick = script.ticks
			nextEatExtra = Random.nextInt(1, 8)
			if (canSipPray && !ateKaram && prayPot!!.drink()) {
				script.logger.info("Tick-sipping prayer potion")
				consumeTick = script.ticks
				if (Condition.wait({ Prayer.prayerPoints() > prayPoints }, 250, 15))
					return@SimpleLeaf
			}
		}

		if (pot != null && canConsume()) {
			val drink = pot.drink()
			script.logger.info("Drink pot=${pot}, successful=${drink}")
			if (!drink) return@SimpleLeaf
			consumeTick = script.ticks
			potionRestore = Random.nextInt(40, 65)
			Condition.wait({ pot != potion() }, 250, 15)
		}
	}

	private fun comboEatKarambwan(food: Food): Boolean {
		val missingHp = Combat.maxHealth() - Combat.health()
		val healing = food.healing
		if (missingHp - healing < Food.KARAMBWAN.healing) {
			return false
		}
		return Food.KARAMBWAN.eat()
	}

	var food: Food? = null
	var nextEatExtra = Random.nextInt(1, 8)

	var potion: Potion? = null
	var potionRestore = Random.nextInt(45, 60)

	private fun food(): Food? {
		val missingHp = missingHP()
		val needsFood = needsFood()
		return foods.firstOrNull {
			(needsFood || (eatWhenPossible && missingHp >= it.healing + nextEatExtra)) && it.inInventory()
		}
	}

	private fun edibleFood(): Food? {
		val missingHp = missingHP()
		return foods.firstOrNull { missingHp > it.healing && it.hasWith() }
	}

	private fun potion(): Potion? = potions.filter { it.hasWith() }
		.firstOrNull {
			it.needsRestore(potionRestore)
		}

	override fun validate(): Boolean {
		if (!canConsume()) return false
		food = food()
		potion = potion()
		return food != null || potion != null
	}

	private fun canConsume() = script.ticks > consumeTick + 2

	companion object {
		var foods: Array<Food> = Food.values()
		var potions: Array<Potion> = Potion.values()
		var consumeTick = -3
		fun KrulScript.canConsume() = ticks > consumeTick + 2
	}
}