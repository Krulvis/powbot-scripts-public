package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.rt4.GrandExchange
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.Looting
import org.powbot.krulvis.api.script.tree.leaf.Loot
import kotlin.math.max

class CanLoot<S>(script: S, override val failedComponent: TreeComponent<S>) :
	Branch<S>(script, "CanLoot") where S : KrulScript, S : Looting {
	override val successComponent: TreeComponent<S> = Loot(script)

	private fun makeSpace(worth: Int): Boolean {
		val edibleFood = Food.getFirstFood()
		if (edibleFood != null) {
			return edibleFood.eat()
		} else if (worth > 5000) {
			val pot = Inventory.stream().name("(1)").action("Drink").first()
			return pot.interact("Drink")
		}
		return false
	}

	private fun GroundItem.stacks(herbSack: Boolean, seedBox: Boolean, gemBag: Boolean): Boolean {
		return (stackable() && Inventory.containsOneOf(id()))
			|| (herbSack && name.contains("grimy", true))
			|| (gemBag && name.contains("uncut", true))
			|| (seedBox && name.contains("seed", true))
	}

	override fun validate(): Boolean {
		Loot.loot = script.getGroundLoot()
			.map { it to it.stackSize() * max(GrandExchange.getItemPrice(it.id()), it.price()) }
		if (Loot.loot.isEmpty() || !Loot.loot.first().first.reachable()) {
			return false
		}
		val lootString = Loot.loot.joinToString { it.first.name() + ": " + it.second }
		script.logger.info("Loot found: [${lootString}]")
		if (!Inventory.isFull()) return true

		val worth = Loot.loot.sumOf { it.second }
		val hasHerbSack = Container.HERB_SACK.hasWith()
		val hasSeedBox = Container.SEED_BOX.hasWith()
		val hasGemBag = Container.GEM_BAG.hasWith()
		return Loot.loot.any { it.first.stacks(hasHerbSack, hasSeedBox, hasGemBag) } || makeSpace(worth)
	}
}