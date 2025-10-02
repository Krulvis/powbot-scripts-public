package org.powbot.krulvis.fletching.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.fletching.AuburnvaleFletcher
import org.powbot.krulvis.fletching.tree.leaf.HandleBank
import org.powbot.krulvis.fletching.tree.leaf.OpenBank
import org.powbot.krulvis.fletching.tree.leaf.WalkToNext

class ShouldBank(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = NearNext(script)
    override val successComponent: TreeComponent<AuburnvaleFletcher> = BankOpen(script)

    override fun validate(): Boolean {
        val invDeco = script.deco.carvedCount()
        val invLogs = script.logs.invCount()
        return if (script.current.key.totemTile.distance() > 10 || !script.current.key.built()) {
            invLogs < 1 || invDeco + (invLogs - 1) / script.deco.logs < 4
        } else {
            invDeco + invLogs / script.deco.logs < script.current.key.decorations()
        }
    }
}

class BankOpen(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "BankOpen?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = OpenBank(script)
    override val successComponent: TreeComponent<AuburnvaleFletcher> = HandleBank(script)
    override fun validate(): Boolean {
        return Bank.opened()
    }
}