package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.TelekineticRoom
import org.powbot.krulvis.mta.tree.leafs.CastTelekineticGrab
import org.powbot.krulvis.mta.tree.leafs.MoveToTelekineticPosition

class FinishedMaze(script: MTA) : Branch<MTA>(script, "Finished maze?") {
	override val failedComponent: TreeComponent<MTA> = ShouldInitialize(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Get new maze") {
		val guardian = TelekineticRoom.mazeGuardian
		if (!guardian.inViewport()) {
			Camera.turnTo(guardian)
		}
		script.logger.info("guardian in viewport = ${guardian.inViewport()}, actions=${guardian.actions}")
		if (guardian.interact("New-maze")) {
			TelekineticRoom.resetRoom()
			val flags = TelekineticRoom.getFlags()
			waitForDistance(guardian) { TelekineticRoom.getGuardian(flags).id != guardian.id }
		}
	}

	override fun validate(): Boolean {
		return TelekineticRoom.getGuardian(TelekineticRoom.getFlags()).actions.contains("New-maze")
	}
}

class ShouldInitialize(script: MTA) : Branch<MTA>(script, "Should initialize room?") {
	override val failedComponent: TreeComponent<MTA> = CanCastTelekinesis(script)
	override val successComponent: TreeComponent<MTA> = ShouldWalkToCenter(script)

	override fun validate(): Boolean {
		return TelekineticRoom.shouldInstantiate() || TelekineticRoom.currentMove.first == Tile.Nil
	}
}

class ShouldWalkToCenter(script: MTA) : Branch<MTA>(script, "Should walk to center?") {
	override val failedComponent: TreeComponent<MTA> = SimpleLeaf(script, "Initialize") {
		script.logger.info("Maze guardian=${TelekineticRoom.mazeGuardian}")
		TelekineticRoom.instantiateRoom()
	}
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Walk to center") {
		val center = TelekineticRoom.nearestWall()
		script.logger.info("Walking to nearestWall=$center")
		Movement.step(center)
	}

	override fun validate(): Boolean {
		return !TelekineticRoom.getGuardian(TelekineticRoom.getFlags()).valid()
	}
}

class CanCastTelekinesis(script: MTA) : Branch<MTA>(script, "Can cast telekinesis") {
	override val failedComponent: TreeComponent<MTA> = MoveToTelekineticPosition(script)
	override val successComponent: TreeComponent<MTA> = CastTelekineticGrab(script)

	override fun validate(): Boolean {
		return if (TelekineticRoom.standingDirection() != TelekineticRoom.requiredStandingDirection()) {
			false
		} else {
			TelekineticRoom.withinBounds()
		}
	}
}