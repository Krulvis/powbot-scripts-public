package org.powbot.krulvis.tormenteddemon.tree.leaf

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Weapon
import org.powbot.krulvis.api.extensions.watcher.SpecialAttackWatcher
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class WaitWhileKilling(script: TormentedDemon) : Leaf<TormentedDemon>(script, "Wait for kill confirm...") {
	var specialWatcher: SpecialAttackWatcher? = null
	override fun execute() {

		val offensivePrayer = script.offensivePrayer
		script.logger.info("defensivePray=${script.protectionPrayer}, offensivePray=${script.offensivePrayer}")
		if (!Prayer.prayerActive(script.protectionPrayer)) {
			Prayer.prayer(script.protectionPrayer, true)
		}
		if (offensivePrayer != null && !Prayer.prayerActive(offensivePrayer)) {
			Prayer.prayer(offensivePrayer, true)
		}

		val specialWeapon = script.specialWeapon
		if (script.equipment != script.meleeEquipment && script.currentTarget.distance() <= 1) {
			script.logger.info("Walking step back because ranging")
//			Movement.step()
		}

		if (specialWeapon?.canSpecial() == true && script.equipment.any { it.item.id == specialWeapon.id }) {
			val makesSense = specialWeapon != Weapon.ARCLIGHT || (!script.reducedStats && script.currentTarget.healthPercent() >= 80)
			val specialWatcherOff = specialWatcher == null || !specialWatcher!!.active
			if (makesSense && specialWatcherOff && !Combat.specialAttack() && Combat.specialAttack(true)) {
				script.logger.info("Casting special because arclight=${specialWeapon == Weapon.ARCLIGHT}, reducedStats=${script.reducedStats}, hp=${script.currentTarget.healthPercent()}")
				specialWatcher = SpecialAttackWatcher(script.currentTarget) {
					script.reducedStats = it
				}
			}
		}
	}
}