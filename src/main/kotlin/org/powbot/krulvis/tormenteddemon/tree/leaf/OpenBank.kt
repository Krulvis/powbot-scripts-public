package org.powbot.krulvis.tormenteddemon.tree.leaf

import org.powbot.api.InteractableEntity
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class OpenBank(script: TormentedDemon) : Leaf<TormentedDemon>(script, "Opening bank") {
	override fun execute() {
		val bankTeleport = script.bankTeleport
		if (Game.clientState() == Constants.GAME_LOGGED) {
			val bank = Bank.getBank()
			script.forcedBanking = true
			if (Prayer.quickPrayer()) {
				Prayer.quickPrayer(false)
			} else if (bank.valid()) {
				if (walkAndInteract(bank, bank.bankAction())) {
					waitForDistance(bank) { Bank.opened() }
				}
			} else if (bankTeleport.execute()) {
				Movement.moveToBank()
			}
		}
	}

	val BANK_ACTIONS = listOf("Bank", "Open", "Use")
	fun InteractableEntity.bankAction(): String {
		val actions = actions()
		return actions.first { it in BANK_ACTIONS }
	}
}