package org.powbot.krulvis.fletching.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fletching.*

class WalkToNext(script: AuburnvaleFletcher) : Leaf<AuburnvaleFletcher>(script, "WalkToSite") {

    val makingTimer = Timer(2000)
    var lastCount = -1
    override fun execute() {
        if (Bank.opened()) {
            Bank.close()
            return
        }
        val path = script.current.value
        val currPos = me.trueTile()
        if (script.current.key == Totem.CENTER) {
            val localPath = LocalPathFinder.findPath(script.current.key.offeringTile)
            if (!localPath.walkable()) {
                script.logger.info("Can't find path to CENTER offeringTile")
                val localPathToRocks = LocalPathFinder.findPath(rocksBankToCenter.from)
                if (localPathToRocks.walkable()) {
                    script.logger.info("Can find path to rocks to CENTER")
                    if (rocksBankToCenter.execute()) {
                        waitFor(long()) { LocalPathFinder.findPath(script.current.key.offeringTile).walkable() }
                    }
                } else {
                    if (brokenwallBankToEast.execute()) {
                        waitFor { LocalPathFinder.findPath(rocksBankToCenter.from).walkable() }
                    }
                }
            } else {
                localPath.traverse()
            }
        } else if (script.current.key == Totem.EAST && logNEtoEast.from.distance() <= 7 && logNEtoEast.from.y <= currPos.y) {
            //We are above the log and need to walk over it
            logNEtoEast.execute()
        } else if (path != null) {
            val invDeco = script.deco.carvedCount()
            path.traverse(3, whileWaiting = {
                if (script.deco.carvedCount() < 4)
                    quickFletch()
            })
            if (!makingTimer.isFinished()) {
                if (waitFor(mid()) { script.deco.carvedCount() > invDeco }) {
                    makingTimer.stop()
                }
            }
        } else {
            script.logger.info("No path found for ${script.current}")
        }
    }

    fun quickFletch() {
        if (makingTimer.isFinished()) {
            if (script.carveDecoration()) {
                makingTimer.reset()
            }
        }
        script.fletching = true
        val newCount = script.deco.carvedCount()
        if (newCount > lastCount) {
            makingTimer.reset()
        }
        lastCount = newCount
        false
    }


}