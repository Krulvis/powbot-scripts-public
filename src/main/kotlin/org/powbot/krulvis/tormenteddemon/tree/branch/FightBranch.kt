package org.powbot.krulvis.tormenteddemon.tree.branch

import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.tormenteddemon.TormentedDemon
import org.powbot.krulvis.tormenteddemon.tree.leaf.Attack
import org.powbot.krulvis.tormenteddemon.tree.leaf.WaitWhileKilling
import org.powbot.krulvis.tormenteddemon.tree.leaf.WalkToSpot

class ShouldCastResurrect(script: TormentedDemon) : Branch<TormentedDemon>(script, "ShouldResurrect?") {
	override val failedComponent: TreeComponent<TormentedDemon> = IsKilling(script)
	override val successComponent: TreeComponent<TormentedDemon> = SimpleLeaf(script, "Resurrecting") {
		val spell = script.resurrectSpell!!.spell
		if (spell.cast()) {
			script.resurrectedTimer.reset(0.6 * Skills.level(Skill.Magic) + Random.nextInt(1000, 10000))
		}
	}

	override fun validate(): Boolean {
		return script.resurrectSpell != null && script.resurrectedTimer.isFinished()
	}
}


class IsKilling(script: TormentedDemon) : Branch<TormentedDemon>(script, "Killing?") {
	override val failedComponent: TreeComponent<TormentedDemon> = CanKill(script)
	override val successComponent: TreeComponent<TormentedDemon> = ShouldConsume(script, WaitWhileKilling(script))

	override fun validate(): Boolean {
		return killing()
	}

	companion object {
		fun killing(): Boolean {
			val interacting = Players.local().interacting()
			if (interacting == Actor.Nil) return false
			return interacting.healthBarVisible() && TargetWidget.health() > 0
		}
	}
}

class CanKill(script: TormentedDemon) : Branch<TormentedDemon>(script, "Can Kill?") {

	override val successComponent: TreeComponent<TormentedDemon> = Attack(script)
	override val failedComponent: TreeComponent<TormentedDemon> = WalkToSpot(script)

	override fun validate(): Boolean {
		return script.centerTile.distance() <= 25
	}
}
