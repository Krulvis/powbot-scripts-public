package org.powbot.krulvis.fletching

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.pathfinding.GameObjectInteraction
import org.powbot.krulvis.api.pathfinding.Shortcut

val OFFERINGS = 31054
val ANIMALS = arrayOf("Buffalo", "Jaguar", "Eagle", "Snake", "Scorpion")

enum class Totem(val totemTile: Tile, val offeringTile: Tile, val varp: Int) {
    NORTH_WEST(Tile(1370, 3375, 0), Tile(1369, 3374, 0), 4766),
    WEST(Tile(1346, 3319, 0), Tile(1347, 3319, 0), 4768),
    SOUTH_WEST(Tile(1385, 3274, 0), Tile(1385, 3275, 0), 4770),
    SOUTH(Tile(1413, 3286, 0), Tile(1412, 3286, 0), 4772),
    SOUTH_EAST(Tile(1438, 3305, 0), Tile(1438, 3306, 0), 4774),
    EAST(Tile(1477, 3332, 0), Tile(1478, 3332, 0), 4776),
    NORTH_EAST(Tile(1453, 3341, 0), Tile(1452, 3341, 0), 4778),
    CENTER(Tile(1398, 3329, 0), Tile(1398, 3330, 0), 4780),
    ;

    fun varpValue(): Int = Varpbits.varpbit(varp)

    fun built() = isBuilt(varpValue())

    fun layers(): Array<Int> {
        val varp = varpValue()
        return (0..2).map { animalBits(varp, it) }.toTypedArray()
    }

    fun decorations() = decorations(varpValue())

    fun hasLayers(): Boolean {
        return layers().all { it >= 10 }
    }

    fun hasDecorations() = decorations() == 4

    fun totem(): GameObject =
        Objects.stream(totemTile, GameObject.Type.INTERACTIVE).nameContains("totem").first()

    fun offerings() =
        Objects.stream(offeringTile, GameObject.Type.INTERACTIVE).name("Offerings").action("Claim").first()
}

fun isBuilt(value: Int): Boolean = value.and(0b11) == 0b11
fun decorations(value: Int): Int {
    return value.shr(16).and(0b111)
}

fun animalBits(value: Int, index: Int): Int = value.shr(4 * index + 4).and(0b1111)

fun animalNameForIndex(index: Int): String {
    if (index < 0) return "None"
    return ANIMALS[index]
}

class Decoration(val name: String, val logs: Int, val requirement: Int) {

    fun carvedCount() = Inventory.stream().nameContains(name).count(false).toInt()

    fun carveComp() = Components.stream(270).nameContains(name).first()

}

enum class Logs(val logName: String, vararg val decoration: Decoration) {
    Willow(
        "Willow logs",
        Decoration("shortbow", 1, 35),
        Decoration("longbow", 1, 40),
        Decoration("shield", 2, 42)
    ),
    Maple(
        "Maple logs",
        Decoration("shortbow", 1, 50),
        Decoration("longbow", 1, 55),
        Decoration("shield", 2, 57)
    ),
    Yew(
        "Yew logs",
        Decoration("shortbow", 1, 65),
        Decoration("longbow", 1, 70),
        Decoration("shield", 2, 72)
    ),
    Magic(
        "Magic logs",
        Decoration("shortbow", 1, 80),
        Decoration("longbow", 1, 85),
        Decoration("shield", 2, 87)
    ),
    Redwood(
        "Redwood logs",
        Decoration("hiking staff", 1, 90),
        Decoration("shield", 2, 92)
    ),
    ;

    fun invCount() = Inventory.stream().name(logName).count(false).toInt()

    companion object {
        fun forName(logs: String) = entries.firstOrNull { it.name.equals(logs, true) }
    }
}

const val LOGS_OPTION = "Logs"
const val DECORATION_OPTION = "Decoration"
const val WILLOW = "Willow"
const val MAPLE = "Maple"
const val YEW = "Yew"
const val MAGIC = "Magic"
const val REDWOORD = "Redwood"

val log5to4 = Shortcut(
    Tile(1401, 3291, 0), Tile(1401, 3283, 0),
    GameObjectInteraction(Tile(1401, 3290, 0), "Log balance", GameObject.Type.INTERACTIVE, "Walk-across")
)

val log4to5 = Shortcut(
    Tile(1401, 3283, 0), Tile(1401, 3291, 0),
    GameObjectInteraction(Tile(1401, 3284, 0), "Log balance", GameObject.Type.INTERACTIVE, "Walk-across")
)

val logNEtoEast = Shortcut(
    Tile(1453, 3336, 0), Tile(1453, 3329, 0),
    GameObjectInteraction(Tile(1453, 3335, 0), "Log balance", GameObject.Type.FLOOR_DECORATION, "Walk-across")
)

val logEastToNE = Shortcut(
    Tile(1453, 3329, 0), Tile(1453, 3336, 0),
    GameObjectInteraction(
        Tile(1453, 3330, 0), "Log balance", GameObject.Type.FLOOR_DECORATION, "Walk-across"
    )
)

val brokenwallSouthToBank = Shortcut(
    Tile(1387, 3301, 0), Tile(1387, 3303, 0),
    GameObjectInteraction(
        Tile(1387, 3302, 0), "Broken wall", GameObject.Type.INTERACTIVE, "Climb-over"
    )
)

val brokenwallBankToSouth = Shortcut(
    Tile(1387, 3303, 0), Tile(1387, 3301, 0),
    GameObjectInteraction(
        Tile(1387, 3302, 0), "Broken wall", GameObject.Type.INTERACTIVE, "Climb-over"
    )
)

val brokenwallBankToEast = Shortcut(
    Tile(1389, 3309, 0), Tile(1391, 3309, 0),
    GameObjectInteraction(
        Tile(1390, 3309, 0), "Broken wall", GameObject.Type.INTERACTIVE, "Climb-over"
    )
)

val brokenwallEastToBank = Shortcut(
    Tile(1391, 3309, 0), Tile(1389, 3309, 0),
    GameObjectInteraction(
        Tile(1390, 3309, 0), "Broken wall", GameObject.Type.INTERACTIVE, "Climb-over"
    )
)

val rocksBankToCenter = Shortcut(
    Tile(1391, 3323, 0), Tile(1396, 3323, 0),
    GameObjectInteraction(
        Tile(1392, 3323, 0), "Rocks", GameObject.Type.FLOOR_DECORATION, "Climb"
    )
)

val rocksCenterToBank = Shortcut(
    Tile(1396, 3323, 0), Tile(1391, 3323, 0),
    GameObjectInteraction(
        Tile(1395, 3323, 0), "Rocks", GameObject.Type.FLOOR_DECORATION, "Climb"
    )
)

val bankToNE = listOf(
    Tile(1419, 3353, 0),
    Tile(1422, 3349, 0),
    Tile(1426, 3346, 0),
    Tile(1432, 3346, 0),
    Tile(1439, 3346, 0),
    Tile(1444, 3344, 0),
    Tile(1449, 3342, 0),
    Tile(1454, 3342, 0)
)

val bankToEast = listOf(
    Tile(1419, 3353, 0),
    Tile(1423, 3347, 0),
    Tile(1429, 3346, 0),
    Tile(1434, 3346, 0),
    Tile(1435, 3341, 0),
    Tile(1435, 3336, 0),
    Tile(1436, 3330, 0),
    Tile(1441, 3329, 0),
    Tile(1446, 3329, 0),
    Tile(1452, 3328, 0),
    Tile(1457, 3328, 0),
    Tile(1460, 3324, 0),
    Tile(1466, 3320, 0),
    Tile(1471, 3320, 0),
    Tile(1476, 3325, 0),
    Tile(1476, 3330, 0)
)

val siteEastToSE = listOf(
    Tile(1477, 3331, 0),
    Tile(1478, 3326, 0),
    Tile(1471, 3321, 0),
    Tile(1469, 3316, 0),
    Tile(1463, 3314, 0),
    Tile(1458, 3314, 0),
    Tile(1451, 3313, 0),
    Tile(1446, 3315, 0),
    Tile(1440, 3315, 0),
    Tile(1439, 3309, 0),
    Tile(1437, 3304, 0)
)

val siteSouthTOSE = listOf(
    Tile(1413, 3285, 0),
    Tile(1413, 3290, 0),
    Tile(1418, 3295, 0),
    Tile(1419, 3301, 0),
    Tile(1419, 3306, 0),
    Tile(1422, 3310, 0),
    Tile(1422, 3316, 0),
    Tile(1429, 3318, 0),
    Tile(1436, 3316, 0),
    Tile(1437, 3311, 0),
    Tile(1438, 3306, 0)
)

val siteSWtobank = listOf(
    Tile(1386, 3274, 0),
    Tile(1385, 3280, 0),
    Tile(1382, 3284, 0),
    Tile(1380, 3289, 0),
    Tile(1382, 3294, 0),
    Tile(1387, 3300, 0)
)

val siteCenterToNE = listOf(
    Tile(1399, 3329, 0),
    Tile(1405, 3330, 0),
    Tile(1407, 3335, 0),
    Tile(1413, 3337, 0),
    Tile(1418, 3342, 0),
    Tile(1423, 3345, 0),
    Tile(1428, 3346, 0),
    Tile(1434, 3346, 0),
    Tile(1439, 3346, 0),
    Tile(1444, 3344, 0),
    Tile(1451, 3342, 0)
)

val siteSEToBank = listOf(
    Tile(1437, 3307, 0),
    Tile(1437, 3313, 0),
    Tile(1434, 3317, 0),
    Tile(1429, 3318, 0),
    Tile(1424, 3317, 0),
    Tile(1422, 3310, 0),
    Tile(1416, 3310, 0),
    Tile(1410, 3305, 0),
    Tile(1407, 3301, 0),
    Tile(1404, 3295, 0),
    Tile(1399, 3295, 0),
    Tile(1393, 3296, 0),
    Tile(1387, 3298, 0)
)