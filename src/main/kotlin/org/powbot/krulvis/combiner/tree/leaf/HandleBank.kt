package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Poh
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.teleports.poh.openable.MountedGloryTeleport
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Combiner) : Leaf<Combiner>(script, "Handle Bank") {
	override fun execute() {
		val keepItems = script.items.map { it.key }.toIntArray()
		if (!Bank.opened()) {
			val mountedGlory = MountedGloryTeleport.Edgeville.getObject()
			if (Poh.inside() && mountedGlory != null) {
				if (MountedGloryTeleport.Edgeville.execute()) {
					waitFor { Bank.getBank().valid() }
				}
			} else if (Bank.openNearest()) {
				waitFor(long()) { Bank.opened() }
			}
		} else if (!Inventory.emptyExcept(*keepItems)) {
//            if (Inventory.stream().filtered { it.id !in keepItems }.list().map { it.id }.toSet().size > 1){
//                Bank.depositAll()
//            }
			Bank.depositAllExcept(*keepItems)
		} else {
			script.items.forEach { (id, amount) ->
				val stackable = ItemLoader.lookup(id)?.stackable() == true
				if (amount == 28 || (stackable && amount > 1)) {
					Bank.withdraw(id, Bank.Amount.ALL)
				} else if (!Bank.withdrawExact(id, amount, false) && amount <= 28) {
					return
				}
			}
			val outOfItem =
				script.items.firstNotNullOfOrNull {
					it.takeIf {
						(Inventory.getCount(it.key) < if (it.value > 28) 1 else it.value)
							&& !Bank.containsOneOf(it.key)
					}
				}
			if (outOfItem != null) {
				script.logger.info("Out of: ${outOfItem.key}, stopping script")
				ScriptManager.stop()
			}
		}
	}
}