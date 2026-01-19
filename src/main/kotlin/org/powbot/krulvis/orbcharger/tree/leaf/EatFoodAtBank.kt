package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.orbcharger.OrbCrafter

class EatFoodAtBank(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Eating at bank") {
    override fun execute() {
        if (script.food.inInventory()) {
            val count = script.food.getInventoryCount()
            if (script.food.eat()) {
                waitFor { count < script.food.getInventoryCount() }
            }
        } else if (Inventory.emptySlotCount() < 5) {
            Bank.depositAllExcept(*script.necessaries)
        } else if (script.food.withdrawExact(5)) {
            waitFor { script.food.inInventory() }
        }
    }
}