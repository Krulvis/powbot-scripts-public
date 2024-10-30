package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Weapon
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class WaitWhileKilling(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Wait for kill confirm...") {
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
			val makesSense =
				specialWeapon != Weapon.ARCLIGHT || (!script.reducedStats && script.currentTarget.healthPercent() >= 75)
			if (makesSense && !Combat.specialAttack() && Combat.specialAttack(true)) {
				script.logger.info("Casting special because arclight=${specialWeapon == Weapon.ARCLIGHT}, reducedStats=${script.reducedStats}, hp=${script.currentTarget.healthPercent()}")

			}
		}
	}
}