package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king

class Attack(script: DagannothKings) : Leaf<DagannothKings>(script, "Attack") {

	private val attackRexTimer = Timer(2400)

	override fun execute() {
		val target = script.target
		script.logger.info("Going to attack target=$target")
		val king = target.king() ?: return
		target.bounds(-32, 32, -222, -30, -32, 32)
		if (king == Data.King.Rex) {
			attackRex(target)
		} else {
			if (script.centerTileEvadeSupreme.distance() > 1 && (target.distance() > 13 || !target.inViewport())) {
				Movement.step(script.centerTileEvadeSupreme)
			}
			attack(target)
		}
	}

	private fun attackRex(rex: Npc) {
		if (!rex.valid() || !attackRexTimer.isFinished() || rex.distance() > 14 || Movement.moving()) return

		val otherCloser = script.aliveKings.any { it.npc.distance() < rex.distance() || it.npc.distanceTo(rex) <= 4 }
		val killing = Data.King.values().filter { it.kill }
		if (killing.size == 1 && otherCloser) {
			script.logger.info("Not attacking because there's another closer")
		} else if (rex.inViewport()) {
			attack(rex)
		}
	}

	fun attack(king: Npc) {
		val interactTile = me.tile()

		val attack = king.click()
		script.logger.info("Attack success=$attack")
		if (attack) {
			if (waitFor { me.interacting() == king } && king.king() == Data.King.Rex && king.tile().x < script.rexTile.x && script.lureTile.distance() > 0) {
				Movement.step(script.lureTile, 0)
			}
			attackRexTimer.reset()
		} else if (waitFor { Movement.moving() }) {
			Movement.step(interactTile)
		}
	}
}