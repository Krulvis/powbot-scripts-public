package org.powbot.krulvis.smither.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.items.Item.Companion.HAMMER
import org.powbot.krulvis.smither.Smither
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Smither) : Leaf<Smither>(script, "Handling Bank") {
    override fun execute() {
        if (!Inventory.emptyExcept(script.bar.id, HAMMER)) {
            Bank.depositAllExcept(script.bar.id, HAMMER)
        }
        if (!Inventory.containsOneOf(HAMMER)) {
            if (!Bank.containsOneOf(HAMMER)) {
                script.logger.info("No hammer found, stopping script")
                ScriptManager.stop()
            } else {
                Bank.withdraw(HAMMER, 1)
            }
        }
        if (!Inventory.isFull() && !Bank.containsOneOf(script.bar.id)) {
            script.logger.info("Out of ${script.bar.itemName}, stopping script")
            ScriptManager.stop()
        } else if (Bank.withdraw(script.bar.id, Bank.Amount.ALL))
            Bank.close()
    }
}