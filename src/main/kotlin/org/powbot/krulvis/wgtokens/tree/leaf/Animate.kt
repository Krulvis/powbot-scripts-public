package org.powbot.krulvis.wgtokens.tree.leaf

import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.wgtokens.WGTokens

class Animate(script: WGTokens) : Leaf<WGTokens>(script, "Animating") {
    override fun execute() {
        val npc = script.armour.npc()
        val animator = Objects.stream().name("Magical Animator").nearest().firstOrNull()
        if (animator?.reachable() == false) {
            Movement.walkTo(Tile(2851, 3541, 0))
        } else {
            if (npc == null && animator != null && Utils.walkAndInteract(animator, "Animate")) {
                waitFor(long()) { script.armour.npc() != null }
            }
            val npc2 = script.armour.npc()
            if (npc2 != null && Random.nextBoolean()) {
                if (npc2.interact("Attack", npc2.name)) {
                    waitFor { Players.local().healthBarVisible() }
                }
            }
        }
    }
}