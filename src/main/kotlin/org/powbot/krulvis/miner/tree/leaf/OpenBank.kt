package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestDB
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.miner.Miner

class OpenBank(script: Miner) : Leaf<Miner>(script, "Open Bank") {

	fun openBankCamTorum() {
		val bankObj = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Bank table").nearest().firstOrNull()
		if (bankObj != null && walkAndInteract(bankObj, "Bank")) {
			waitFor { Bank.opened() }
		} else {
			script.tilesToCamTorumMine.reversed().traverse()
		}
	}

	override fun execute() {
		Container.requireEmpty()

		if (script.escapeTopFloor()) {
			if (script.useDepositBox) DepositBox.openNearestDB()
			else if (script.inCamTorum()) openBankCamTorum()
			else Bank.openNearest()
		}
	}
}