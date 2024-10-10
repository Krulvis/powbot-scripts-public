package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.krulvis.api.extensions.items.Item.Companion.TINDERBOX
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager
import org.powbot.util.TransientGetter2D
import kotlin.system.measureTimeMillis

class Burn(script: Woodcutter) : Leaf<Woodcutter>(script, "Burning") {
    override fun execute() {
        val logs = Inventory.stream().firstOrNull { it.id in script.LOGS }
        if (Players.local().animation() == 733) {
            //Stil tryna burn
            waitFor { Players.local().animation() != 733 && Players.local().tile() != script.burnTile }
            return
        }
        if (logs == null) {
            script.burning = false
            script.burnTile = null
            return
        } else {
            script.burning = true
        }

        waitFor(2000) { !Movement.moving() }

        val flags = Movement.collisionMap(0).flags()
//        val destination = Movement.destination()
        script.burnTile = Players.local().tile()

        if (script.burnTile?.canMakeFire(flags) != true) {
            script.burnTile = findGoodSpot(flags)
        }

        if (script.burnTile == null) {
            script.logger.info("Can't find good burning spot...")
            script.burning = true
            return
        } else if (script.burnTile != Players.local().tile()) {
            if (Movement.walkTo(script.burnTile)) {
                waitFor { script.burnTile == Players.local().tile() }
            }
        }

        Game.tab(Game.Tab.INVENTORY)
        if (Inventory.selectedItem() == Item.Nil) {
            val tinderBox = Inventory.stream().id(TINDERBOX).firstOrNull()
            if (tinderBox == null) {
                script.logger.info("No tinderbox, stopping script")
                ScriptManager.stop()
            } else if (tinderBox.interact("Use")) {
                waitFor { Inventory.selectedItem().id == TINDERBOX }
            }
        }

        if (Inventory.selectedItem().id == TINDERBOX) {
            val floor = GroundItems.stream().at(script.burnTile!!).firstOrNull { it.id() in script.LOGS }
            val interaction = floor?.interact("Use") ?: logs.click()
            if (interaction) {
                script.logger.info(
                    "waitFor{} took=${
                        measureTimeMillis {
                            waitFor(long()) {
                                Players.local().tile() != script.burnTile
                            }
                        }
                    } and is successful=${Players.local().tile() != script.burnTile}"
                )

            }
        }
    }

    fun Tile.canMakeFire(flags: TransientGetter2D<Int>): Boolean {
        val blocked = blocked(flags)
        val objBlocking = Objects.stream().at(this).firstOrNull { it.name.isNotEmpty() }
        if (this == script.burnTile && (blocked || objBlocking != null)) {
            script.logger.info("Can't build fire on tile=${script.burnTile}, blocked=$blocked, obj exists=${objBlocking != null}, name=${objBlocking?.name}, id=${objBlocking?.id()}")
        }
        return !blocked && (objBlocking == null || objBlocking.id() == script.boundaryId)
    }

    fun findGoodSpot(flags: TransientGetter2D<Int>): Tile? {
        var longestStreak = 0
        var bestTile: Tile? = null
        val myTile = Players.local().tile()
        for (y in myTile.y - 5..myTile.y + 5) {
            var startTileY = Tile.Nil
            var streakY = 0
            for (x in myTile.x + 5 downTo myTile.x - 5) {
                val tile = Tile(x, y)
                if (tile.canMakeFire(flags)) {
                    if (streakY == 0) {
                        startTileY = tile
                    }
                    streakY++
                    if (streakY > longestStreak) {
                        bestTile = startTileY
                        longestStreak = streakY
                    }
                } else {
                    streakY = 0
                }
            }
        }

        return bestTile
    }

}
