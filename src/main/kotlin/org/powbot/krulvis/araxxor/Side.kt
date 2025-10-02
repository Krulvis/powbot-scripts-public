package org.powbot.krulvis.araxxor

import org.powbot.api.Point
import org.powbot.api.Rectangle
import org.powbot.api.Tile
import kotlin.math.abs

enum class Side {
    NORTH,
    EAST,
    SOUTH,
    WEST
    ;

    companion object {
        fun Rectangle.nearestSide(tile: Tile): Side {
            return this.nearestSide(Point(tile.x, tile.y))
        }

        fun Rectangle.nearestSide(point: Point): Side {
            val distanceToNorth = abs(point.y - y + height)
            val distanceToEast = abs(point.x - x + width)
            val distanceToSouth = abs(point.y - y)
            val distanceToWest = abs(point.x - x)
            val array = arrayOf(distanceToNorth, distanceToEast, distanceToSouth, distanceToWest)
            return Side.values()[array.indexOf(array.minOrNull()!!)]
        }
    }
}