package org.powbot.krulvis.smither.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.smither.Smither
import org.powbot.krulvis.smither.tree.leaf.HandleBank

class ShouldBank(script: Smither) : Branch<Smither>(script, "ShouldBank") {
    override val successComponent: TreeComponent<Smither> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Smither> = ShouldSmith(script)

    override fun validate(): Boolean {
        return script.bar.getCount() < script.item.barsRequired
    }
}

class IsBankOpen(script: Smither) : Branch<Smither>(script, "IsBankOpen") {
    override val successComponent: TreeComponent<Smither> = HandleBank(script)
    override val failedComponent: TreeComponent<Smither> = SimpleLeaf(script, "Opening Bank") {
        Bank.openNearest()
    }

    override fun validate(): Boolean {
        return Bank.opened()
    }
}