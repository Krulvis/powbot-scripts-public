package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class FixStrut(script: Miner) : Leaf<Miner>(script, "Fixing strut") {
    override fun execute() {
        if (Inventory.containsOneOf(Item.HAMMER)) {
            val strut = script.getBrokenStrut()
            if (strut != null && Utils.walkAndInteract(strut, "Hammer")) {
                waitFor(long()) {
                    Objects.stream().at(strut.tile).name("Broken strut").isEmpty()
                }
            }
        } else {
            val crate = Objects.stream().at(Tile(3752, 5674, 0)).findFirst()
            crate.ifPresent {
                if (walkAndInteract(it, "Search")) {
                    waitFor(5000) { Inventory.containsOneOf(Item.HAMMER) }
                }
            }
        }
    }
}