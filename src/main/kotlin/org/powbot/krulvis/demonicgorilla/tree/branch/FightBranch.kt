package org.powbot.krulvis.demonicgorilla.tree.branch

import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.demonicgorilla.DemonicGorilla
import org.powbot.krulvis.demonicgorilla.tree.leaf.Attack
import org.powbot.krulvis.demonicgorilla.tree.leaf.WaitWhileKilling
import org.powbot.krulvis.demonicgorilla.tree.leaf.WalkToSpot

class ShouldCastResurrect(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "ShouldResurrect?") {
	override val failedComponent: TreeComponent<DemonicGorilla> = IsKilling(script)
	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "Resurrecting") {
		val spell = script.resurrectSpell!!.spell
		if (spell.cast()) {
			script.resurrectedTimer.reset(0.6 * Skills.level(Skill.Magic) + Random.nextInt(1000, 10000))
		}
	}

	override fun validate(): Boolean {
		return script.resurrectSpell != null && script.resurrectedTimer.isFinished()
	}
}


class IsKilling(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Killing?") {
	override val failedComponent: TreeComponent<DemonicGorilla> = CanKill(script)
	override val successComponent: TreeComponent<DemonicGorilla> = ShouldConsume(script, WaitWhileKilling(script))

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

class CanKill(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Can Kill?") {

	override val successComponent: TreeComponent<DemonicGorilla> = Attack(script)
	override val failedComponent: TreeComponent<DemonicGorilla> = WalkToSpot(script)

	override fun validate(): Boolean {
		return script.centerTile.distance() <= 25
	}
}
