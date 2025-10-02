package org.powbot.krulvis.fletching.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fletching.AuburnvaleFletcher

class Decorations(script: AuburnvaleFletcher) : Leaf<AuburnvaleFletcher>(script, "Carve Layers") {

    override fun execute() {
        val decorations = script.current.key.decorations()
        val needs = 4 - decorations
        val inventoryDeco = invDeco()
        if (inventoryDeco < needs) {
            script.fletching = true
            if (script.carveDecoration()) {
                waitFor(8000) { invDeco() >= needs }
            }
        } else {
            val totem = script.current.key.totem()
            script.fletching = false
            if (walkAndInteract(totem, "Decorate")) {
                waitFor(long()) { script.current.key.decorations() == 4 }
            }
        }
    }

    fun invDeco() = Inventory.stream().nameContains("${script.logs} longbow (u)").count()
}