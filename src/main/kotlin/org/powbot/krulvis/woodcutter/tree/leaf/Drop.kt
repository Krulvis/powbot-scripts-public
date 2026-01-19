package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter

class Drop(script: Woodcutter) : Leaf<Woodcutter>(script, "Dropping") {
    override fun execute() {
        if (!Game.tab(Game.Tab.INVENTORY)) return
        val items = Inventory.stream().id(*script.LOGS).list()
        items.forEach {
            it.click("Drop")
            sleep(100, 200)
        }
        waitFor { Inventory.stream().id(*script.LOGS).isEmpty() }
    }
}