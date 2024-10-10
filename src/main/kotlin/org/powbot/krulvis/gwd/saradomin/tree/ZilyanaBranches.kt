package org.powbot.krulvis.gwd.saradomin.tree

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume.Companion.canConsume
import org.powbot.krulvis.gwd.saradomin.Saradomin
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Zilyana")
private const val minDistance = 8
private fun walkToNextTile(tiles: Array<Tile>): Boolean {
	val closest = tiles.minByOrNull { it.distance() }!!
	val index = tiles.indexOf(closest)
	val next = if (index == tiles.size - 1) tiles[0] else tiles[index + 1]
	logger.info("Standing closest to $index, walking to $next")
	var tries = 0
	val timer = Timer()
	do {
		val destination = Movement.destination()
		if (Movement.stepNoConfirm(next))
			waitFor { destination != Movement.destination() }
		tries++
	} while (Movement.destination() != next && timer.getElapsedTime() < 5000)
	logger.info("Walking to $next took $tries tries in $timer")
	return Movement.destination() == next
}


class CanAttack(script: Saradomin) : Branch<Saradomin>(script, "CanAttack") {
	private val bowfaAttackAnim = 426
	override val failedComponent: TreeComponent<Saradomin> = ShouldWalk(script)
	override val successComponent = SimpleLeaf(script, "AttackZilyana") {
		val targ = script.general
		targ.bounds(-32, 32, -192, 0, -32, 32)
		if (targ.interact("Attack")) {
			if (waitFor { me.animation() == bowfaAttackAnim }) {
				script.lastAttackTick = script.ticks
				if (targ.distance() <= minDistance) {
					walkToNextTile(tiles)
					waitFor { me.trueTile() != tt }
				}
			}
		} else {
			Movement.step(tt)
		}
	}

	var tiles = arrayOf<Tile>()
	private var tt = Tile.Nil

	override fun validate(): Boolean {
		tt = me.trueTile()
		tiles = script.zilyanaTiles
		if (!script.zilyanaTiles.any { it == tt }) {
			return false
		}
		if (tt != script.lastAttackTile) {
			script.lastAttackTile = tt
			script.tileArrival.reset()
		}
		val distance = script.general.trueTile().distanceTo(tt)
		script.logger.info("On right tile, distance=$distance")
		return script.canAttack() && script.canConsume()
	}
}

class ShouldWalk(script: Saradomin) : Branch<Saradomin>(script, "ShouldWalk") {
	override val failedComponent: TreeComponent<Saradomin> = SimpleLeaf(script, "Waiting to reach destination") {
		sleep(50)
	}
	override val successComponent: TreeComponent<Saradomin> = SimpleLeaf(script, "Walking to next tile") {
		walkToNextTile(tiles)
		waitFor { me.trueTile() != tt }
	}

	var tiles: Array<Tile> = emptyArray()
	private var tt = Tile.Nil

	override fun validate(): Boolean {
		tiles = script.zilyanaTiles
		tt = me.trueTile()
		val generalTT = script.general.trueTile()
		val distance = generalTT.distanceTo(tt)
		val dest = Movement.destination()
		val onAttackTile = tiles.any { it == tt }
		val walkingTowardsNextTile = tiles.any { it == dest }
		if (walkingTowardsNextTile) {
			script.logger.info("Already walking towards next tile")
			return false
		}
		script.logger.info("ShouldWalk? onAttackTile=$onAttackTile, distance=$distance")
		return true
	}
}