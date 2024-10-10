package org.powbot.krulvis.darkcrabs.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.Data.DARK_BAIT
import org.powbot.krulvis.darkcrabs.Data.LOBSTER_POT
import org.powbot.krulvis.darkcrabs.tree.leaf.HandleBank
import org.powbot.krulvis.darkcrabs.tree.leaf.OpenBank

class ShouldBank(script: DarkCrabs) : Branch<DarkCrabs>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<DarkCrabs> = Fishing(script)
    override val successComponent: TreeComponent<DarkCrabs> = IsBankOpen(script)

    override fun validate(): Boolean {

        return Inventory.isFull() || Bank.opened() || script.forcedBanking || !Inventory.containsOneOf(DARK_BAIT) || !Inventory.containsOneOf(
            LOBSTER_POT
        )
    }
}

class IsBankOpen(script: DarkCrabs) : Branch<DarkCrabs>(script, "IsBankOpen?") {
    override val failedComponent: TreeComponent<DarkCrabs> = OpenBank(script)
    override val successComponent: TreeComponent<DarkCrabs> = HandleBank(script)

    override fun validate(): Boolean {
        return Bank.opened()
    }
}