package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class GetRope(script: Tempoross) : Leaf<Tempoross>(script, "Getting rope") {
    override fun execute() {
        val ropes =
            Objects.stream(50)
                .type(GameObject.Type.INTERACTIVE)
                .name("Ropes")
                .filtered { it.tile().distanceTo(script.side.mastLocation) <= 6 }
                .firstOrNull()
        if (ropes == null || ropes.distance() >= 25) {
            script.walkWhileDousing(script.side.anchorLocation, true)
        } else if (script.interactWhileDousing(ropes, "Take", script.side.mastLocation, true)) {
            waitFor(long()) { Inventory.containsOneOf(ROPE) }
        }
    }
}