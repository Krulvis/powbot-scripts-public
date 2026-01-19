package org.powbot.krulvis.wgtokens.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.wgtokens.WGTokens


class ShouldEat(script: WGTokens) : Branch<WGTokens>(script, "ShouldEat?") {
    override val failedComponent: TreeComponent<WGTokens> = ShouldBank(script)
    override val successComponent: TreeComponent<WGTokens> = SimpleLeaf(script, "Eat") {
        val oldHp = currentHP()
        if (script.food?.eat() == true) {
            waitFor { oldHp < currentHP() }
            sleep(1200)
        }
    }

    override fun validate(): Boolean {
        val food = script.food ?: return false
        return food.canEat() && food.inInventory()
    }


}