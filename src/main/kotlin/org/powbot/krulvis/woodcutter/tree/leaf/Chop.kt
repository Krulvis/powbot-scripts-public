package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter

class Chop(script: Woodcutter) : Leaf<Woodcutter>(script, "Chop Tree") {
    override fun execute() {
        Bank.close()

        val trees = script.trees.map {
            Objects.stream().at(it).action("Chop down", "Cut", "Chop").firstOrNull()
        }

        script.logger.info("Trees: ${trees.joinToString { "${it?.name}: ${it?.tile}" }}")

        val tree = trees.filterNotNull().minByOrNull { it.distance() }
        val action = tree?.actions()?.firstOrNull() ?: "Chop down"
        if (tree != null && Utils.walkAndInteract(tree, action) && waitFor(long()) {
                Players.local().animation() != -1
            }) {
            script.lastChopAnim = System.currentTimeMillis()
        }
    }
}