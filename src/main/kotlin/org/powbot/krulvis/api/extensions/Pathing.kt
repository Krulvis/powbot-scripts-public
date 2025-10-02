package org.powbot.krulvis.api.extensions

import org.powbot.api.Tile
import org.powbot.krulvis.api.ATContext.me
import kotlin.math.abs
import kotlin.math.sign

object Pathing {

    /**
     * Finds exact tiles player will walk when clicking destination tile.
     * Only works on unobstructed paths
     */
    fun findPath(destination: Tile): Array<Tile> {
        val start = me.trueTile()
        var curr = start
        var xDist = destination.x - curr.x
        var yDist = destination.y - curr.y
        var dx = 0
        var dy = 0

        val result = mutableListOf<Tile>()
        //Figure out which axis the character will walk along,
        //According to whether the x or y-distance to the destination is longer.
        if (abs(xDist) > abs(yDist)) {
            dx = sign(xDist.toDouble()).toInt()
        } else {
            dy = sign(yDist.toDouble()).toInt()
        }
        //Character will first walk on an axis until along a diagonal with the destination...
        while (abs(xDist) != abs(yDist)) {
            curr = curr.derive(dx, dy)
            result.add(curr)
            xDist = destination.x - curr.x
            yDist = destination.y - curr.y
        }

        //...Then walk diagonally to the destination.
        dx = sign(xDist.toDouble()).toInt()
        dy = sign(yDist.toDouble()).toInt()
        while (xDist != 0 && yDist != 0) {
            curr = curr.derive(dx, dy)
            result.add(curr)
            xDist = destination.x - curr.x
            yDist = destination.y - curr.y
        }

        return result.toTypedArray()
    }
}
