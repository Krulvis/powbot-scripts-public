package org.powbot.krulvis.wgtokens.tree.leaf

import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.wgtokens.WGTokens

class Loot(script: WGTokens) : Leaf<WGTokens>(script, "Looting") {
    override fun execute() {
        val loot = script.loot()
        loot.forEach {
            if (it.interact("Take", it.name())) {
                waitFor { Players.local().tile() == it.tile }
                sleep(600)
            }
        }
        waitFor { script.loot().isEmpty() }
    }
}