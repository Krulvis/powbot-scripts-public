package org.powbot.krulvis.api.extensions

import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.api.rt4.Npc

object Monster {

    fun Npc.rectangle(size: Int): Rectangle {
        val t = trueTile()
        return Rectangle(t.x, t.y, size, size)
    }

    fun Rectangle.borderingLayer(layers: Int = 1): Array<Tile> {
        val outerTiles = mutableListOf<Tile>()

        // For each layer
        for (layer in 1..layers) {
            // Top and bottom borders
            for (xi in (x - layer) until (x + width + layer)) {
                // Top border (layer tiles up)
                outerTiles.add(Tile(xi, y - layer))
                // Bottom border (layer tiles down)
                outerTiles.add(Tile(xi, y + height + (layer - 1)))
            }

            // Left and right borders
            for (yi in (y - layer + 1) until (y + height + layer - 1)) {
                // Left border (layer tiles left)
                outerTiles.add(Tile(x - layer, yi))
                // Right border (layer tiles right)
                outerTiles.add(Tile(x + width + (layer - 1), yi))
            }
        }

        return outerTiles.distinct().toTypedArray()
    }


    fun Npc.attackingDestination(from: Tile, size: Int): Tile {
        val rect = rectangle(size)
        val layer = rect.borderingLayer()
        return layer.minByOrNull { it.distanceTo(from) } ?: from
    }

//    private fun Array<Tile>.nearestInner(): Tile {
//        if (isEmpty()) return Tile.Nil
//        val inner = filterIndexed { index, _ -> index % 2 == 0 }.minByOrNull { it.distance() }!!
//        val index = indexOf(inner)
//        val rebuild = sliceArray(0 until index) + sliceArray(index until size)
//        for (i in indices) {
//            val tile = rebuild[i]
//            val nearestOutside = rect.getNearestOutsideTile(tile)
//            if (tile !in unsafeTiles && nearestOutside !in unsafeTiles) {
//                return tile
//            }
//        }
//        return Tile.Nil
//    }

    fun Rectangle.getNearestOutsideForInsideTile(tile: Tile): Tile {
        val rectMin = Tile(x, y)
        val rectMax = Tile(x + width, y + height)
        // Calculate distances from the tile to each border
        val distanceLeft = tile.x - rectMin.x
        val distanceRight = rectMax.x - tile.x
        val distanceTop = tile.y - rectMin.y
        val distanceBottom = rectMax.y - tile.y

        // Find the direction of the nearest border
        return when (minOf(distanceLeft, distanceRight, distanceTop, distanceBottom)) {
            distanceLeft -> Tile(rectMin.x - 1, tile.y)       // Just outside the left border
            distanceRight -> Tile(rectMax.x + 1, tile.y)      // Just outside the right border
            distanceTop -> Tile(tile.x, rectMin.y - 1)        // Just outside the top border
            distanceBottom -> Tile(tile.x, rectMax.y + 1)     // Just outside the bottom border
            else -> throw IllegalStateException("Unexpected case")
        }
    }

}