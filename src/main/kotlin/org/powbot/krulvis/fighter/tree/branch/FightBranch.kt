package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Actor
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.gargoyle
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.SUPERIORS
import org.powbot.krulvis.fighter.tree.leaf.*

class ShouldDodgeProjectile(script: Fighter) : Branch<Fighter>(script, "ShouldDodgeProjectile?") {
	override val failedComponent: TreeComponent<Fighter> = IsKilling(script)
	override val successComponent: TreeComponent<Fighter> = DodgeProjectile(script)

	override fun validate(): Boolean {
		val dest = Movement.destination()
		val tile = if (dest.valid()) dest else Players.local().tile()
		return script.projectiles.any { it.destination().distanceTo(tile) <= 1 }
	}
}

class IsKilling(script: Fighter) : Branch<Fighter>(script, "Killing?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldReanimate(script)
	override val successComponent: TreeComponent<Fighter> = ShouldConsume(script, FightingSuperior(script))

	override fun validate(): Boolean {
		return killing(script.superiorActive)
	}

	companion object {
		fun killing(superiorActive: Boolean): Boolean {
			val interacting = Players.local().interacting()
			if (interacting == Actor.Nil) return false
			if (superiorActive && interacting.name.lowercase() !in SUPERIORS) return false
			return interacting.healthBarVisible() && (TargetWidget.health() > 0 || interacting.gargoyle())
		}
	}
}

class FightingSuperior(script: Fighter) : Branch<Fighter>(script, "Fighting Superior?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script, WaitWhileKilling(script))
	override val successComponent: TreeComponent<Fighter> = FightSuperior(script)

	override fun validate(): Boolean {
		return TargetWidget.name().lowercase() in SUPERIORS
	}

}

class IsWaitingForLoot(script: Fighter) : Branch<Fighter>(script, "Waiting for loot?") {

	override val successComponent: TreeComponent<Fighter> = WaitForLoot(script)
	override val failedComponent: TreeComponent<Fighter> = CanKill(script)

	override fun validate(): Boolean {
		val targetDied =
			script.currentTarget.valid() && script.currentTarget.healthBarVisible() && script.currentTarget.healthPercent() == 0
		return script.waitForLootAfterKill && (targetDied || script.isLootWatcherActive())
	}
}

class CanKill(script: Fighter) : Branch<Fighter>(script, "Can Kill?") {

	override val successComponent: TreeComponent<Fighter> = ShouldHop(script)
	override val failedComponent: TreeComponent<Fighter> = WalkToSpot(script)

	override fun validate(): Boolean {
		//When a projectile is flying towards safeSpot (centerTile), we dont care about walking back to spot
		if (script.projectiles.any { it.destination() == script.centerTile }) return true
		if (script.useSafespot) {
			return script.centerTile.distance() <= script.safespotRadius
		}
		return script.centerTile.distance() <= script.killRadius
	}
}

class ShouldHop(script: Fighter) : Branch<Fighter>(script, "Should hop?") {

	override val successComponent: TreeComponent<Fighter> = HopFromPlayers(script)
	override val failedComponent: TreeComponent<Fighter> = ShouldConsume(script, Attack(script))

	override fun validate(): Boolean {
		return script.hopFromPlayers && Players.stream().within(script.centerTile, script.killRadius).notLocalPlayer()
			.count() >= script.playerHopAmount
	}
}