package org.powbot.krulvis.gwd.branches

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.alive
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.gwd.GWDScript
import org.powbot.krulvis.gwd.leaf.Kill
import org.powbot.krulvis.gwd.leaf.PrayAtAltar


class MageAlive<S>(script: S) : Branch<S>(script, "MageAlive?") where S : GWDScript<S> {
	val kill = Kill(script, true)
	override val failedComponent: TreeComponent<S> = ShouldStack(script)
	override val successComponent =
		ShouldEquipGear(script, script.equipmentMage, ShouldProtPray(script, Prayer.Effect.PROTECT_FROM_MISSILES, kill))

	override fun validate(): Boolean {
		if (script.mageBG.alive()) {
			kill.target = script.mageBG
			return true
		}
		return false
	}
}

class ShouldStack<S>(script: S) : Branch<S>(script, "StackGuards?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = MeleeAlive(script)
	override val successComponent = SimpleLeaf(script, "StackBGs") {
		val rangeT = script.rangeBG.tile()
		val meleeT = script.meleeBG.tile()
		val dx = (rangeT.x - meleeT.x).coerceIn(-1, 1)
		val dy = (rangeT.y - meleeT.y).coerceIn(-1, 1)
		val targetTile = Tile(rangeT.x - dx, rangeT.y - dy)
		Movement.step(targetTile)
		waitFor { me.tile() == targetTile }
		stackTimer.reset()
	}

	private val stackTimer = Timer(600)
	override fun validate(): Boolean {
		if (!script.stackGuards || !stackTimer.isFinished()) return false
		if (script.meleeBG.alive() && script.rangeBG.alive()) {
			return script.meleeBG.tile().distanceTo(script.rangeBG.tile()) > 1
		}
		return false
	}
}

class MeleeAlive<S>(script: S) : Branch<S>(script, "MeleeAlive?") where S : GWDScript<S> {
	private val kill = Kill(script, true)
	override val failedComponent: TreeComponent<S> = RangeAlive(script)
	override val successComponent =
		ShouldEquipGear(script, script.equipmentMelee, ShouldProtPray(script, Prayer.Effect.PROTECT_FROM_MELEE, kill))

	override fun validate(): Boolean {
		if (script.meleeBG.alive()) {
			kill.target = script.meleeBG
			return true
		}
		return false
	}
}


class RangeAlive<S>(script: S) : Branch<S>(script, "RangeAlive?") where S : GWDScript<S> {
	private val kill = Kill(script, true)
	override val failedComponent: TreeComponent<S> = CanLoot(script, CanPray(script))
	override val successComponent = ShouldEquipGear(
		script,
		script.equipmentRange,
		ShouldProtPray(script, Prayer.Effect.PROTECT_FROM_MISSILES, kill)
	)

	override fun validate(): Boolean {
		if (script.rangeBG.alive()) {
			kill.target = script.rangeBG
			return true
		}
		return false
	}
}

class CanPray<S>(script: S) : Branch<S>(script, "CanPray?") where S : GWDScript<S> {
	private val prayTimer = Timer(10 * 60 * 1000)

	override val failedComponent: TreeComponent<S> =
		ShouldEquipGear(script, script.equipmentGeneral, SimpleLeaf(script, "Chill") {
			sleep(150)
		})
	override val successComponent = PrayAtAltar(script, prayTimer)

	init {
		prayTimer.stop()
	}

	override fun validate(): Boolean {
		return prayTimer.isFinished() && Prayer.prayerPoints() <= 20
	}
}