package org.powbot.krulvis.wgtokens.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.wgtokens.WGTokens

class HandleBank(script: WGTokens) : Leaf<WGTokens>(script, "Looting") {
    override fun execute() {
        if (!Inventory.emptyExcept(*ids())) {
            Bank.depositAllExcept(*ids())
        } else if (script.food!!.withdrawExact(25) && !script.food!!.canEat()) {
            Bank.close()
        }
    }

    fun ids() = intArrayOf(*script.armour.ids, *script.food!!.ids, *script.tokens)
}