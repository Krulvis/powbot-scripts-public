package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.InteractableEntity
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.dax.api.DaxWalker
import org.powbot.dax.teleports.Teleport
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.fighter.Fighter

class OpenBank(script: Fighter) : Leaf<Fighter>(script, "Opening bank") {
	override fun execute() {
		script.superiorActive = false
		script.currentTarget = Npc.Nil

		Container.requireEmpty()
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
				Movement.builder(null).setToBank(true).setUseTeleports(script.useTeleportsToBank).move()
			}
		}
	}

	val BANK_ACTIONS = listOf("Bank", "Open", "Use")
	fun InteractableEntity.bankAction(): String {
		val actions = actions()
		return actions.first { it in BANK_ACTIONS }
	}
}