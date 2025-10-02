package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Cannon
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.fighter.Fighter
import kotlin.random.Random

class UsingCannon(script: Fighter) : Branch<Fighter>(script, "UsingCannon?") {
	override val successComponent: TreeComponent<Fighter> = ShouldPlaceCannon(script)
	override val failedComponent: TreeComponent<Fighter> = GettingDefenders(script)


	override fun validate(): Boolean {
		return script.useCannon && script.cannonTile.distance() <= script.killRadius + script.safespotRadius
	}
}

class ShouldPlaceCannon(script: Fighter) : Branch<Fighter>(script, "ShouldPlaceCannon?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "PlacingCannon") {
		if (me.tile() != script.cannonTile) {
			val localPath = LocalPathFinder.findPath(script.cannonTile)
			if (localPath.isNotEmpty()) {
				localPath.traverseUntilReached(0.0)
			} else {
				Movement.walkTo(script.cannonTile)
			}
		} else {
			val base = Inventory.stream().id(6).first()
			if (base.interact("Set-up")) {
				waitFor(10000) { Cannon.getCannon().valid() }
			}
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldRepairCannon(script)


	override fun validate(): Boolean {
		return Inventory.stream().id(*Cannon.items).count().toInt() == 4 && !Cannon.getCannon().valid()
	}

}

class ShouldRepairCannon(script: Fighter) : Branch<Fighter>(script, "ShouldRepairCannon?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Repairing cannon") {
		val cannon = Cannon.getCannon()
		if (walkAndInteract(cannon, "Repair")) {
			waitForDistance(cannon) { !validate() }
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldAddCannonballs(script)


	override fun validate(): Boolean {
		val cannon = Cannon.getCannon()
		script.logger.info("Cannon=$cannon, actions=[${cannon.actions().joinToString()}]")
		return cannon.actions().contains("Repair")
	}
}

class ShouldAddCannonballs(script: Fighter) : Branch<Fighter>(script, "ShouldAddCannonBalls?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Add balls") {
		val cannon = Cannon.getCannon()
		if (walkAndInteract(cannon, "Fire")) {
			waitForDistance(cannon) { !validate() }
			nextCannonRefill = Random.nextInt(0, 20)
		}
	}
	override val failedComponent: TreeComponent<Fighter> = GettingDefenders(script)

	private var nextCannonRefill = Random.nextInt(0, 20)

	override fun validate(): Boolean {
		return Cannon.balls() <= nextCannonRefill || !Cannon.firing()
	}
}

