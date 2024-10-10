package org.powbot.krulvis.blastfurnace.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.tree.leaf.AddCoffer
import org.powbot.krulvis.blastfurnace.tree.leaf.PayForeman

class ShouldPay(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Pay") {
    override val successComponent: TreeComponent<BlastFurnace> = PayForeman(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldAddToCoffer(script)

    override fun validate(): Boolean {
        return script.shouldPayForeman()
    }
}

class ShouldAddToCoffer(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Coffer") {
    override val successComponent: TreeComponent<BlastFurnace> = AddCoffer(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldDrinkPotion(script)

    override fun validate(): Boolean {
        return script.cofferCount() <= 100
    }
}