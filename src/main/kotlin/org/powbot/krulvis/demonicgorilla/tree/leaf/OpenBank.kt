package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.InteractableEntity
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class OpenBank(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Opening bank") {
	override fun execute() {
		val bankTeleport = script.bankTeleport
		if (Game.clientState() == Constants.GAME_LOGGED) {
			val bank = Bank.getBank()
			script.forcedBanking = true
			if (bank.valid()) {
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