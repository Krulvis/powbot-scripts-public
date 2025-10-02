package org.powbot.krulvis.fletching.tree.leaf

import org.powbot.api.InteractableEntity
import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.animating
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.fletching.AuburnvaleFletcher
import org.powbot.krulvis.fletching.brokenwallSouthToBank
import org.powbot.krulvis.fletching.walkable

class OpenBank(script: AuburnvaleFletcher) : Leaf<AuburnvaleFletcher>(script, "Open Bank") {
    override fun execute() {
        script.current = script.totems.entries.first()
        val b = getBank()
        val walkableNeighbor =
            if (script.bank == BankLocation.AUBERVALE_SOUTH) Tile(1387, 3309, 0) else script.bank.tile.getWalkableNeighbor(false)
        val localPath = LocalPathFinder.findPath(walkableNeighbor)
        script.logger.info("Bank=${b}, walkableNeighbor=$walkableNeighbor, locaPath=${localPath.size}")
        if (b.valid() && localPath.walkable()) {
            script.logger.info("Opening bank")
            if (walkAndInteract(b, "Bank")) {
                waitForDistance(b, extraWait = 4000) { Bank.opened() }
            }
        } else if (brokenwallSouthToBank.from.distance() < 10) {
            script.logger.info("Attempting broken wall shortcut")
            if (brokenwallSouthToBank.execute()) {
                waitFor(long()) {
                    !me.animating() && LocalPathFinder.findPath(walkableNeighbor).walkable()
                }
            }
        } else {
            script.logger.info("Traversing path to bank")
            script.bankPath.traverse()
        }
    }

    fun getBank(): InteractableEntity {
        return Objects.stream(script.bank.tile).type(GameObject.Type.INTERACTIVE).action("Bank").first()
    }

}