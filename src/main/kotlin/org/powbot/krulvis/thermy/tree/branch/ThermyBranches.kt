package org.powbot.krulvis.thermy.tree.branch

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.distanceC
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.thermy.Thermy
import org.powbot.krulvis.thermy.tree.leaf.Attack
import org.powbot.krulvis.thermy.tree.leaf.Freeze
import org.powbot.krulvis.thermy.tree.leaf.WalkToMaxRange

val THEMRY_AREA = Area(Tile(2350, 9460, 0), Tile(2377, 9438, 0))

class AtThermy(script: Thermy) : Branch<Thermy>(script, "AtThermy?") {
    override val failedComponent: TreeComponent<Thermy> = AtSmokeDevils(script)
    override val successComponent: TreeComponent<Thermy> =
        CanLoot(script, ShouldConsume(script, ShouldEscape(script), eatWhenPossible = false))

    override fun validate(): Boolean {
        return THEMRY_AREA.contains(me)
    }
}

class ShouldEscape(script: Thermy) : Branch<Thermy>(script, "ShouldEscape?") {
    override val failedComponent: TreeComponent<Thermy> = ThemryAlive(script)
    override val successComponent: TreeComponent<Thermy> = SimpleLeaf(script, "") {
        script.bankTeleport.execute()
    }

    override fun validate(): Boolean {
        return currentHP() <= 20 && !Food.hasFood()
    }
}

class ThemryAlive(script: Thermy) : Branch<Thermy>(script, "ThemryAlive?") {
    override val failedComponent: TreeComponent<Thermy> = SimpleLeaf(script, "Walk to start") {
        val dest = Movement.destination()
        if (dest != Tile.Nil && script.startTile.distanceTo(dest) > 0) {
            Movement.stepNoConfirm(script.startTile)
        } else if (dest == Tile.Nil && script.startTile.distance() > 0) {
            Movement.stepNoConfirm(script.startTile)
        }
    }
    override val successComponent: TreeComponent<Thermy> = ShouldFreeze(script)
    override fun validate(): Boolean = script.thermy.valid()
}

class ShouldFreeze(script: Thermy) : Branch<Thermy>(script, "OnMaxRange?") {
    override val failedComponent: TreeComponent<Thermy> = ShouldMakeDistance(script)
    override val successComponent: TreeComponent<Thermy> = Freeze(script)

    override fun validate(): Boolean {
        return script.freezeTimer.isFinished()
    }
}

class ShouldMakeDistance(script: Thermy) : Branch<Thermy>(script, "ShouldMakeDistance?") {
    override val failedComponent: TreeComponent<Thermy> = ShouldWalkToLoot(script)
    override val successComponent: TreeComponent<Thermy> = WalkToMaxRange(script)

    override fun validate(): Boolean {
        if (!script.freezeTimer.isFinished() && script.freezeTimer.getRemainder() <= 3000) {
            //At this point thermy is loose but can't yet be frozen, so walking away makes no sense.
            return false
        }
        val dest = Movement.destination()
        if (dest != Tile.Nil && script.tt.distanceC(dest) >= script.minDistance) return false
        return script.tt.distanceC(me.tile()) < script.minDistance
    }
}

class ShouldWalkToLoot(script: Thermy) : Branch<Thermy>(script, "ShouldWalkToLoot?") {
    override val failedComponent: TreeComponent<Thermy> = Attack(script)
    override val successComponent: TreeComponent<Thermy> = SimpleLeaf(script, "Walk to loot") {
        if (script.thermy.distance() > 2 && Movement.destination().distanceTo(script.thermy) > 2) {
            Movement.stepNoConfirm(script.tt)
        }
    }

    override fun validate(): Boolean {
        return script.thermy.dead()
    }
}

