package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever

class Eat(script: Thiever) : Leaf<Thiever>(script, "Eat food") {
    override fun execute() {
        val hp = currentHP()
        if (script.food == Food.WINE && Bank.opened()) {
            Bank.close()
            waitFor(2500) { !Bank.opened() }
        }
        if (script.food.eat()) {
            waitFor(long()) { hp < currentHP() }
            sleep(1500)
        }
    }
}