package org.powbot.krulvis.tithe.tree.branch

import org.powbot.api.Condition
import org.powbot.api.Production
import org.powbot.api.Random
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Patch.Companion.sameState
import org.powbot.krulvis.tithe.TitheFarmer
import org.powbot.krulvis.tithe.tree.leaf.*

class ShouldStart(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should start") {
	override val successComponent: TreeComponent<TitheFarmer> = Start(script)
	override val failedComponent: TreeComponent<TitheFarmer> = ShouldRefill(script)

	override fun validate(): Boolean {
		return script.getPoints() == -1
	}
}

class ShouldRefill(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should refill") {
	override val successComponent: TreeComponent<TitheFarmer> = Refill(script)
	override val failedComponent: TreeComponent<TitheFarmer> = DoneFarming(script)

	override fun validate(): Boolean {
		script.refreshPatches()
		if (script.startPoints == -1) {
			script.startPoints = script.getPoints()
		} else {
			script.gainedPoints = script.getPoints() - script.startPoints
		}
		if (script.lastLeaf.name != "Waiting...") {
			script.chillTimer.reset()
		}
		return Inventory.stream().list().none { it.id() in Data.WATER_CANS } ||
			!Production.stoppedMaking(Data.WATER_CAN_FULL) ||
			(!script.hasEnoughWater() && script.patches.all { it.isEmpty() })
	}
}

class DoneFarming(script: TitheFarmer) : Branch<TitheFarmer>(script, "Done farming") {
	override val successComponent: TreeComponent<TitheFarmer> = ShouldDeposit(script)
	override val failedComponent: TreeComponent<TitheFarmer> = ShouldMoveCamera(script)

	override fun validate(): Boolean {
		return script.getPoints() > -1 && !script.hasSeeds() && script.patches.all { it.isEmpty() }
	}
}

class ShouldDeposit(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should deposit") {
	override val successComponent: TreeComponent<TitheFarmer> = Deposit(script)
	override val failedComponent: TreeComponent<TitheFarmer> = Leave(script)

	override fun validate(): Boolean {
		return Inventory.stream().id(*Data.HARVEST).isNotEmpty()
	}
}

class ShouldMoveCamera(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should turn camera") {
	override val successComponent: TreeComponent<TitheFarmer> = SimpleLeaf(script, "Moving camera") {
		Camera.angle(Random.nextInt(255, 290))
		Camera.pitch(Random.nextInt(95, 99))
		Condition.wait { !validate() }
	}

	override val failedComponent: TreeComponent<TitheFarmer> = ShouldPlant(script)

	override fun validate(): Boolean {
		return Camera.yaw() !in 255..290 || Camera.pitch() < 95
	}
}

class ShouldPlant(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should plant") {
	override val successComponent: TreeComponent<TitheFarmer> = Plant(script)

	override val failedComponent: TreeComponent<TitheFarmer> = ShouldHandlePatch(script)

	override fun validate(): Boolean {
		return script.planting || script.patches.all { it.isEmpty() }
	}
}

class ShouldHandlePatch(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should handle patch") {
	override val successComponent: TreeComponent<TitheFarmer> = HandlePatch(script)

	override val failedComponent: TreeComponent<TitheFarmer> = ShouldWalkBack(script)

	override fun validate(): Boolean {
		return script.patches.any { it.needsAction() }
	}
}

class ShouldWalkBack(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should plant") {
	override val successComponent: TreeComponent<TitheFarmer> = WalkBack(script)
	override val failedComponent: TreeComponent<TitheFarmer> = SimpleLeaf(script, "Waiting...") {}

	override fun validate(): Boolean {
		return script.chillTimer.isFinished() || script.patches.sameState()
	}
}





