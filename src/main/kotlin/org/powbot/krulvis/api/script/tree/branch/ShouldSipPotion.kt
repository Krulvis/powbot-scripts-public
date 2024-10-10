package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.extensions.Timer

class ShouldSipPotion<S : KrulScript>(script: S, override val failedComponent: TreeComponent<S>) :
	Branch<S>(script, "Should sip potion?") {

	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Sipping") {
		val pot = potion!!
		if (pot.drink()) {
			if (Condition.wait({ !pot.needsRestore(pot.restore()) }, 250, 15)) {
				sipTimer.reset()
				nextRestore = Random.nextInt(45, 60)
			}
		}
	}

	var potion: Potion? = null
	var nextRestore = Random.nextInt(45, 60)
	val sipTimer = Timer(1200)

	private fun Potion.restore() = if (this == Potion.PRAYER) 100 + nextRestore else nextRestore
	fun potion(): Potion? = Potion.values().filter { !skippingPotions.contains(it) && it.hasWith() }
		.firstOrNull {
			it.needsRestore(it.restore())
		}

	override fun validate(): Boolean {
		if (!sipTimer.isFinished()) return false
		potion = potion()
		return potion != null
	}


	companion object {
		val skippingPotions = mutableListOf<Potion>()
	}
}