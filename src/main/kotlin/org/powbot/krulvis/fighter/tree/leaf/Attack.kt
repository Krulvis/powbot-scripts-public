package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Cannon
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.branch.IsKilling
import org.powbot.krulvis.fighter.unreachable
import org.powbot.mobile.rscache.loader.NpcLoader

class Attack(script: Fighter) : Leaf<Fighter>(script, "Attacking") {
	override fun execute() {
		val target = script.target()
		if (script.canActivateQuickPrayer()) {
			Prayer.quickPrayer(true)
		}
		if (script.useCannon && script.autoRetaliate) {
			val config = NpcLoader.lookup(target.id)
			Combat.autoRetaliate(true)
			val tile = Cannon.standingTiles(config?.size()?.toInt() ?: 2).minByOrNull { it.distance() } ?: return
			if (tile.distance() > 0) {
				Movement.step(tile)
				waitForDistance(tile, extraWait = 600) { me.tile() == tile }
			}
			return
		}

		target.bounds(-32, 32, -192, 0, 0 - 32, 32)

		if (script.shouldSpec() && script.specialEquipment.all { it.meets() } && !Combat.specialAttack()) {
			Combat.specialAttack(true)
		}

		if (attack(target)) {
			script.currentTarget = target
			Condition.wait({
				IsKilling.killing(script.superiorActive) || script.shouldReturnToSafespot()
			}, 250, 10)
			if (script.shouldReturnToSafespot()) {
				Movement.step(script.centerTile, 0)
			}
		}
	}

	fun attack(target: Npc?): Boolean {
		val t = target ?: return false
		val action = if (t.name == "Whirlpool") "Disturb" else "Attack"
		return if (script.useSafespot || t.unreachable()) {
			target.interact(action)
		} else if (!t.reachable()) {
			LocalPathFinder.findWalkablePath(t.tile()).traverse()
		} else {
			ATContext.walkAndInteract(t, action)
		}
	}
}