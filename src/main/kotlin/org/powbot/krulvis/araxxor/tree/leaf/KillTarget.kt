package org.powbot.krulvis.araxxor.tree.leaf

import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.api.rt4.Combat
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.short
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data.ARAXXOR

class KillTarget(script: Araxxor) : Leaf<Araxxor>(script, "KillTarget") {
	override fun execute() {
		val targ = script.target
		targ.bounds(-32, 32, -64, 0, -32, 32)
		val enrage = targ.name == ARAXXOR && targ.healthPercent() <= 25
		if (targ.name == ARAXXOR && script.specWeapon.canSpecial()
			&& !Combat.specialAttack() && script.specWeapon.inEquipment()
		) {
			Combat.specialAttack(true)
		}

		val nextAttackIn = script.nextAttackTick - script.ticks
		val validEnrageTile = script.araxxorRect.contains(me.trueTile())
		if (enrage && !validEnrageTile && nextAttackIn != 0) {
			script.nextEnrageTile.matrix().interact("Walk here")
		} else if (me.interacting() == targ) {
			sleep(20)
		} else if ((!enrage || nextAttackIn == 0 || nextAttackIn >= 4)) {
			//In enraged mode we only want to attack JUST after Araxxor attacked
			if (walkAndInteract(targ, "Attack"))
				waitFor(short()) { me.interacting() == targ }
		}
	}

	private fun Rectangle.contains(tile: Tile) = contains(tile.x, tile.y)
}