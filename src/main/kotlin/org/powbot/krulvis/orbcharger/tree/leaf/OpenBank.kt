package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.orbcharger.OrbCrafter

class OpenBank(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Opening Bank") {
	override fun execute() {
		if (script.orb.bank.tile.distance() < 50) {
			script.orb.bank.open()
		} else {
			script.bankTeleport.execute()
		}
	}
}