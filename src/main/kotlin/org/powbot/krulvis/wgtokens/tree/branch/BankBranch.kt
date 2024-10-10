package org.powbot.krulvis.wgtokens.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.wgtokens.WGTokens
import org.powbot.krulvis.wgtokens.tree.leaf.HandleBank

class ShouldBank(script: WGTokens) : Branch<WGTokens>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<WGTokens> = CanLoot(script)
    override val successComponent: TreeComponent<WGTokens> = IsBankOpen(script)

    override fun validate(): Boolean {
        val food = script.food ?: return false
        return script.armour.inInventory() && food.inInventory() && currentHP() / maxHP().toDouble() < .4
    }
}

class IsBankOpen(script: WGTokens) : Branch<WGTokens>(script, "IsBankOpen?") {
    override val failedComponent: TreeComponent<WGTokens> = SimpleLeaf(script, "Open bank") {
        Bank.openNearest()
    }
    override val successComponent: TreeComponent<WGTokens> = HandleBank(script)

    override fun validate(): Boolean {
        return Bank.opened()
    }
}