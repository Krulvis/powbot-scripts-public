package org.powbot.krulvis.mole.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mole.GiantMole
import org.powbot.krulvis.mole.tree.leaf.HandleBank
import kotlin.random.Random

class ShouldBank(script: GiantMole) : Branch<GiantMole>(script, "Should Bank?") {
	override val failedComponent: TreeComponent<GiantMole> = CanLoot(script)
	override val successComponent: TreeComponent<GiantMole> = IsBankOpen(script)

	override fun validate(): Boolean {
		val prayerPot = script.prayerPotion
		return Prayer.prayerPoints() < 10 && (prayerPot == null || !prayerPot.hasWith())
	}
}

class IsBankOpen(script: GiantMole) : Branch<GiantMole>(script, "IsBankOpen?") {
	private val ropeTile = Tile(1752, 5136, 0)
	override val failedComponent: TreeComponent<GiantMole> = SimpleLeaf(script, "Opening Bank") {
		if (moleArea.contains(me) && script.teleportToBank.teleport == null) {
			if (ropeTile.distance() > 10) {
				Movement.step(ropeTile)
				sleep(Random.nextInt(600, 100))
			} else {
				val rope = Objects.stream().at(ropeTile).name("Rope").action("Climb").first()
				if (walkAndInteract(rope, "Climb")) {
					waitForDistance(rope) { moleArea.contains(me) }
				}
			}
		} else if (script.teleportToBank.execute()) {
			Bank.openNearest()
		}
	}
	override val successComponent: TreeComponent<GiantMole> = HandleBank(script)

	override fun validate(): Boolean {
		return Bank.opened()
	}
}