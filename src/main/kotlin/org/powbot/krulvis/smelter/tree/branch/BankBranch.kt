package org.powbot.krulvis.smelter.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.extensions.items.Item.Companion.AMMO_MOULD
import org.powbot.krulvis.api.extensions.items.Item.Companion.AMMO_MOULD_DOUBLE
import org.powbot.krulvis.smelter.Smelter
import org.powbot.krulvis.smelter.tree.leaf.HandleBank

class ShouldBank(script: Smelter) : Branch<Smelter>(script, "ShouldBank") {
    override val successComponent: TreeComponent<Smelter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Smelter> = ShouldSmelt(script)

    override fun validate(): Boolean {
        if (script.cannonballs) {
            val shouldBank =
                !Inventory.containsOneOf(AMMO_MOULD, AMMO_MOULD_DOUBLE) || !Inventory.containsOneOf(Bar.STEEL.id)
            script.logger.info("Should bank for balls = $shouldBank")
            return shouldBank
        }
        return script.bar.getSmeltableCount() <= 0
    }
}

class IsBankOpen(script: Smelter) : Branch<Smelter>(script, "IsBankOpen") {
    override val successComponent: TreeComponent<Smelter> = HandleBank(script)
    override val failedComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Opening Bank") {
        if (Bank.openNearest()) {
            waitFor { Bank.opened() }
        }
    }

    override fun validate(): Boolean {
        return Bank.opened()
    }
}