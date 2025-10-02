package org.powbot.krulvis.araxxor

import org.powbot.api.Area
import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Npc
import org.powbot.krulvis.api.ATContext.surroundingTiles
import org.powbot.krulvis.api.extensions.Pathing
import org.powbot.util.TransientGetter2D
import kotlin.math.abs

object Data {

    val ARAXXOR = "Araxxor"
    val MIRROR = "Mirrorback Araxyte"
    val ACIDIC = "Acidic Araxyte"
    val EXPLODING = "Ruptura Araxyte"
    val ENRAGED_ATTACK_ANIMATION = 11487
    val TOXIC_ATTACK_ANIM = 11477
    val ARAXXOR_DEATH_ANIM = 11481
    val HADUKEN_ATTACK_ANIM = 11476
    val ARAXXOR_ATTACK_ANIMS = arrayOf(11479, 11480)
    val SIZE = 7


    enum class Egg(val id: Int) {
        WHITE(13670), GREEN(13674), RED(13672),
        ;

        companion object{
            fun forNpc(npc: Npc): Egg = entries.firstOrNull { it.id == npc.id } ?: GREEN
        }
    }

    enum class AraxxorOrientations(val orientation: Int) {
        NORTH(4),
        EAST(6),
        SOUTH(0),
        WEST(2),
        NORTH_EAST(5),
        NORTH_WEST(3),
        SOUTH_EAST(7),
        SOUTH_WEST(1),
        ;

        fun hadukenTile(my: Tile, araxxorCenter: Tile): Tile {
            // Calculate the tile to movs to based on Araxxor's orientation
            // Calculate relative position to Araxxor's center
            val relativeX = my.x - araxxorCenter.x
            val relativeY = my.y - araxxorCenter.y

            return when (this) {
                NORTH, SOUTH -> {
                    // When Araxxor faces north or south, beam goes north or south and we should move east or west
                    if (my.x > araxxorCenter.x) {
                        // We're on east side, move further east
                        Tile(my.x + 3, my.y, my.floor())
                    } else {
                        // We're on west side, move further west
                        Tile(my.x - 3, my.y, my.floor())
                    }
                }

                EAST, WEST -> {
                    // When Araxxor faces east or west, beam goes east or west and we should move north or south
                    if (my.y > araxxorCenter.y) {
                        Tile(my.x, my.y + 3, my.floor())
                    } else {
                        Tile(my.x, my.y - 3, my.floor())
                    }
                }

                NORTH_EAST -> {
                    // Beam goes northeast
                    if (relativeX > relativeY) {
                        // We're more east, move south
                        Tile(my.x, my.y - 3, my.floor())
                    } else {
                        // We're more south, move more west
                        Tile(my.x - 3, my.y, my.floor())
                    }
                }

                NORTH_WEST -> {
                    // Beam goes northwest
                    if (abs(relativeX) > relativeY) {
                        // We're more west, move south
                        Tile(my.x, my.y - 3, my.floor())
                    } else {
                        // We're more north, move more west
                        Tile(my.x + 3, my.y, my.floor())
                    }
                }

                SOUTH_EAST -> {
                    // Beam goes southeast
                    if (relativeX > abs(relativeY)) {
                        // We're more east, move north
                        Tile(my.x, my.y + 3, my.floor())
                    } else {
                        // We're more south, move more west
                        Tile(my.x - 3, my.y, my.floor())
                    }
                }

                SOUTH_WEST -> {
                    // Beam goes southwest
                    if (relativeX < relativeY) {
                        // We're more west, move north
                        Tile(my.x, my.y + 3, my.floor())
                    } else {
                        // We're more south, move more east
                        Tile(my.x + 3, my.y, my.floor())
                    }
                }
            }
        }

        companion object {
            fun from(value: Int): AraxxorOrientations {
                return entries.firstOrNull { it.orientation == value } ?: NORTH_EAST
            }
        }
    }

    fun crawlingTunnel() = Components.stream(229).text("You crawl through the webbed tunnel.").first().visible()

    val webTunnelTile = Tile(3656, 9816, 0)

    val lairArea = Area(Tile(3606, 9838, 0), Tile(3649, 9796, 0))
    val spiderDungeonArea = Area(Tile(3650, 9868, 0), Tile(3713, 9794, 0))
    val outsideLairArea = Area(
        Tile(3671, 3348, 0),
        Tile(3671, 3385, 0),
        Tile(3664, 3386, 0),
        Tile(3664, 3394, 0),
        Tile(3637, 3394, 0),
        Tile(3638, 3422, 0),
        Tile(3700, 3422, 0),
        Tile(3715, 3349, 0)
    )
    val outsidePath = listOf(
        Tile(3673, 3375, 0),
        Tile(3677, 3378, 0),
        Tile(3681, 3381, 0),
        Tile(3676, 3388, 0),
        Tile(3671, 3393, 0),
        Tile(3667, 3396, 0),
        Tile(3663, 3400, 0),
        Tile(3659, 3405, 0)
    )
    val darkmeyerPath = listOf(
        Tile(3592, 3337, 0),
        Tile(3597, 3341, 0),
        Tile(3597, 3347, 0),
        Tile(3597, 3352, 0),
        Tile(3597, 3357, 0),
        Tile(3598, 3363, 0),
        Tile(3597, 3369, 0),
        Tile(3596, 3374, 0),
        Tile(3596, 3380, 0),
        Tile(3601, 3383, 0),
        Tile(3604, 3387, 0),
        Tile(3609, 3388, 0),
        Tile(3613, 3385, 0),
        Tile(3619, 3383, 0),
        Tile(3624, 3383, 0),
        Tile(3625, 3377, 0),
        Tile(3631, 3376, 0),
        Tile(3636, 3376, 0),
        Tile(3639, 3381, 0),
        Tile(3644, 3381, 0),
        Tile(3651, 3379, 0),
        Tile(3657, 3378, 0),
        Tile(3663, 3375, 0)
    )
    val wallArea = Area(Tile(3670, 3384, 0), Tile(3670, 3365, 0))
    val darkmeyerArea = Area(
        Tile(3590, 3399, 0),
        Tile(3588, 3331, 0),
        Tile(3662, 3331, 0),
        Tile(3669, 3375, 0),
        Tile(3669, 3383, 0),
        Tile(3660, 3391, 0),
        Tile(3634, 3398, 0)
    )


    fun predictAcidPools(araxxorCenter: Tile, playerTile: Tile): Array<Tile> {

        val araxxorTile = araxxorCenter
        if (!araxxorTile.valid()) return emptyArray()

        val predictedPools = mutableListOf<Tile>()
        // Player's tile always gets a pool
        predictedPools.add(playerTile)

        // Get the relative position of Araxxor to the player
        val dx = araxxorTile.x - playerTile.x
        val dy = araxxorTile.y - playerTile.y

        val isAraxxorEastWest = abs(dx) > abs(dy)
        val isAraxxorNorthSouth = abs(dy) > abs(dx)

        // Helper function to check if a tile is in Araxxor's direction
        fun isInAraxxorDirection(testTile: Tile): Boolean {
            val testDx = testTile.x - playerTile.x
            val testDy = testTile.y - playerTile.y

            // Check if the tile is in the same general direction as Araxxor
            return when {
                isAraxxorEastWest && abs(testDy) > 0 -> true  // Araxxor is east or west
                isAraxxorNorthSouth && abs(testDx) > 0 -> true  // Araxxor is south or north
                else -> false
            }
        }

        // Get adjacent tiles and filter those that face Araxxor
        val adjacentTiles = playerTile.surroundingTiles()
            .filter { isInAraxxorDirection(it) }

        predictedPools.addAll(adjacentTiles)

        return predictedPools.toTypedArray()
    }

    fun backstepTile(from: Tile, araxxorCenter: Tile, flags: TransientGetter2D<Int>): Tile {
        // Calculate the direction vector from Araxxor to the player
        val dx = from.x - araxxorCenter.x
        val dy = from.y - araxxorCenter.y

        // Create the potential backwards tile
        val backwardsTile = from.basedOnDelta(dx, dy)

        return if (!backwardsTile.blocked()) {
            backwardsTile
        } else {
            // If direct backwards tile is blocked, try adjacent tiles
            val alternatives = if (abs(dx) > abs(dy)) {
                listOf(
                    backwardsTile.derive(0, 1),
                    backwardsTile.derive(0, -1)
                )
            } else {
                listOf(
                    backwardsTile.derive(1, 0),
                    backwardsTile.derive(-1, 0)
                )
            }
            alternatives.firstOrNull { !it.blocked() } ?: from
        }
    }

    private fun Tile.basedOnDelta(dx: Int, dy: Int): Tile {
        return if (abs(dx) > abs(dy)) derive(dx.coerceIn(-1, 1), 0) else derive(0, dy.coerceIn(-1, 1))
    }


    /**
     * Determines the safest tile to move to based on a set of potential tiles, avoiding unsafe tiles
     * and considering factors like proximity and path through unsafe areas.
     *
     * @param potentialTiles An array of tiles thatsare potential candidates for movement.
     * @param unsafeTiles A list of tiles that are deemed unsafe and should be avoided.
     * @param currentTile The tile representing the current position.
     * @param flags A getter function providing transient data about each tile, such as whether it is blocked.
     * @param surroundingWeight The weight applied to the factor considering proximity to unsafe tiles (default is 1.0).
     * @param distanceWeight The weight applied to the factor considering distance from the current position (default is -1.0).
     * @param pathWeight The weight applied to the factor considering unsafe tiles along the path (default is 1.0).
     * @return The safest tile from the given potential tiles, or the current tile if no safe option is available.
     */
    fun findSafestTile(
        potentialTiles: Array<Tile>,
        unsafeTiles: List<Tile>,
        currentTile: Tile,
        flags: TransientGetter2D<Int>,
        surroundingWeight: Double = 1.0, distanceWeight: Double = -1.0, pathWeight: Double = 1.0
    ): Tile {
        if (potentialTiles.isEmpty()) return currentTile
        if (unsafeTiles.isEmpty()) return potentialTiles.minByOrNull { it.distanceTo(currentTile) } ?: currentTile

        // Calculate safety scores for each potential tile
        val tilesWithScores = potentialTiles
            .filter { tile ->
                // Filter out tiles that are themselves unsafe
                !unsafeTiles.contains(tile) &&
                        // Ensure the tile isn't blocked
                        !tile.blocked()
            }
            .map { tile ->
                val safetyScore = calculateSafetyScore(tile, unsafeTiles, currentTile, surroundingWeight, distanceWeight, pathWeight)
                tile to safetyScore
            }

        // Return the tile with the highest safety score, or current tile if no safe tiles found
        return tilesWithScores.maxByOrNull { it.second }?.first ?: currentTile
    }

    /**
     * Calculates the safety score of a given tile based on its proximity to unsafe tiles,
     * the distance from the current position, and the path through unsafe tiles.
     *
     * A high safety score is good. By default the distance is seen as a negative, but can be made positive by changing the weight.
     *
     * @param tile The tile for which the safety score is being calculated.
     * @param unsafeTiles A list of tiles considered unsafe.
     * @param currentTile The tile representing the player's current position.
     * @param surroundingWeight The weight applied to the surrounding unsafe tile factor (default is 1.0).
     * @param distanceWeight The weight applied to the proximity to the current tile factor (default is -1.0).
     * @param pathWeight The weight applied to the unsafe tiles encountered along the path factor (default is 1.0).
     * @return The calculated safety score, where a higher score represents a safer tile.
     */
    private fun calculateSafetyScore(
        tile: Tile,
        unsafeTiles: List<Tile>,
        currentTile: Tile,
        surroundingWeight: Double = 1.0,
        distanceWeight: Double = -1.0,
        pathWeight: Double = 1.0
    ): Double {
        // Calculate minimum distance to any unsafe tile
        val unsafeTilesNear = 12 - unsafeTiles.count { unsafe ->
            tile.distanceTo(unsafe) < 3
        }

        //Paths that player will walk through when walking towards tile
        val unsafeTilesAlongTheWay = Pathing.findPath(tile).count { it in unsafeTiles }

        // Calculate distance from current position
        val distanceFromCurrent = tile.distanceTo(currentTile)

        // Calculate final safety score
        // Higher score = safer tile
        // We want to maximize distance from unsafe tiles while minimizing distance from current position
        return (unsafeTilesNear * surroundingWeight) + (distanceFromCurrent * distanceWeight) - (unsafeTilesAlongTheWay * pathWeight)
    }


    fun Rectangle.getEnrageQuickWalkTiles(): Array<Tile> {
        val tiles = mutableListOf<Tile>()
        for (xi in x until x + width) {
            tiles.add(Tile(xi, y + 1))
            tiles.add(Tile(xi, y))

            tiles.add(Tile(xi, y + height - 1))
            tiles.add(Tile(xi, y + height))
        }

        for (yi in y until y + height) {
            tiles.add(Tile(x + 1, yi))
            tiles.add(Tile(x, yi))

            tiles.add(Tile(x + width - 1, yi))
            tiles.add(Tile(x + width, yi))
        }
        return tiles.toTypedArray()
    }
}