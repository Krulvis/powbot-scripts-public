package org.powbot.krulvis.spices.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.spices.Spices

class Feed(script: Spices) : Leaf<Spices>(script, "Feeding") {

    val RAW_KARAMBWANJI = 3150
    override fun execute() {
        var selectedItem = Inventory.selectedItem()
        val curtain = Objects.stream().name("Curtain").nearest().firstOrNull() ?: return
        if (selectedItem.id != RAW_KARAMBWANJI) {
            val karambwanji = Inventory.stream().id(RAW_KARAMBWANJI).firstOrNull() ?: return
            val cat = script.cat() ?: return
            val hp = cat.healthPercent()

            if (karambwanji.useOn(curtain)) {
                waitFor(5000) { cat.healthPercent() > hp }
            }
        }
    }
}