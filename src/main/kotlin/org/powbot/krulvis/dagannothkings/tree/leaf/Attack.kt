package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king

class Attack(script: DagannothKings) : Leaf<DagannothKings>(script, "Attack") {

	private val attackTimer = Timer(2400)

	override fun execute() {
		val target = script.target
		val king = target.king() ?: return
		script.logger.info(" Going to attack $king")
		target.bounds(-32, 32, -222, -30, -32, 32)
		if (king == Data.King.Rex) {
			attackRex(target)
		} else {
			attack(target)
		}
	}

	fun attackRex(rex: Npc) {
		if (!rex.valid() || !attackTimer.isFinished() || rex.distance() > 14 || Movement.moving()) return

		val otherCloser = Npcs.stream()
			.filtered { it.name.contains("Dagannoth") && it.name != rex.name && !it.dead() }
			.any { it.distance() < rex.distance() || it.distanceTo(rex) <= 4 }
		if (otherCloser) {
			script.logger.info("Not attacking because there's another closer")
		} else {
			attack(rex)
		}
	}

	fun attack(king: Npc) {
		val interactTile = me.tile()
		if (king.interact("Attack", king.name(), useMenu = true)) {
			if (waitFor { me.interacting() == king } && king.king() == Data.King.Rex && king.tile().x < script.rexTile.x && script.lureTile.distance() > 0) {
				Movement.step(script.lureTile, 0)
			}
			attackTimer.reset()
		} else if (waitFor { Movement.moving() }) {
			Movement.step(interactTile)
		}
	}
}