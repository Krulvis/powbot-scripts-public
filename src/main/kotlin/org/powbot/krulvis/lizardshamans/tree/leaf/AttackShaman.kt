package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.lizardshamans.Data
import org.powbot.krulvis.lizardshamans.LizardShamans

class AttackShaman(script: LizardShamans) : Leaf<LizardShamans>(script, "AttackShaman") {
	override fun execute() {
		if (!script.jumpEvent.timer.isFinished()) {
			script.logger.info("Not attacking because jumpEvent not finished ${script.jumpEvent}")
			return
		}
		//There is a possibility for shamans to PJ when the other jumps
		val currentTarget = getcurrentTarget()
		if (currentTarget.animation() == Data.AttackAnimation.Falling.animation || currentTarget.animation() == Data.AttackAnimation.Jump.animation) {
			script.logger.info("Not attacking in middle of jump animation")
			return
		}

		currentTarget.bounds(-42, 12, -282, -120, -52, 62)
		if (currentTarget.interact("Attack")) {
			waitFor { me.interacting() == currentTarget }
		}
	}

	fun getcurrentTarget(): Npc {
		val shamans = Npcs.stream().name("Lizardman shaman").nearest().filtered {
			!it.dead() && it.reachable()
		}.toList()
		val me = me
		if (!script.currentTarget.dead() && script.currentTarget.interacting() == me) {
			return script.currentTarget
		}
		return shamans.firstOrNull { it.interacting() == me } ?: shamans.firstOrNull() ?: Npc.Nil
	}
}