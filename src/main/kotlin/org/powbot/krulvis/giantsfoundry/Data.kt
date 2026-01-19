package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.api.extensions.items.Bar

const val CRUCIBLE_BAR_COUNT_VARP = 3431
const val JOB_VARP = 3429
const val FOUNDRY_VARP = 3433
const val QUALITY_VARP = 3432
const val ROOT = 754
const val ACTIVITIES_CHILD_INDEX = 75

const val MOULD_WIDGET_ROOT = 718

const val LAST_SWORD_ID = "lastSword"

val METAL_ITEMS = mapOf(
    "scimitar" to 1,
    "longsword" to 1,
    "full helm" to 1,
    "sq shield" to 1,
    "claws" to 1,
    "warhammer" to 2,
    "battleaxe" to 2,
    "chainbody" to 2,
    "kiteshield" to 2,
    "2h sword" to 2,
    "platelegs" to 2,
    "plateskirt" to 2,
    "platebody" to 4,
    "bar" to 1,
)

val METAL_ITEM_NAMES = METAL_ITEMS.keys.toTypedArray()

fun getCrucibleBarsForItem(item: Item): Int {
    val key = METAL_ITEMS.keys.firstOrNull { item.name().contains(it, true) } ?: return 0
    return METAL_ITEMS[key]!!
}

fun Bar.crucibleInventoryCount(): Int {
    val invItems =
        Inventory.stream().nameContains(craftedBarItemPrefix(), itemName).nameContains(*METAL_ITEM_NAMES).toList()
    return invItems.sumOf { getCrucibleBarsForItem(it) }
}

fun Bar.crucibleCount() = Varpbits.varpbit(CRUCIBLE_BAR_COUNT_VARP, 5 * BARS.indexOf(this), 31)

val BARS = arrayOf(Bar.BRONZE, Bar.IRON, Bar.STEEL, Bar.MITHRIL, Bar.ADAMANTITE, Bar.RUNITE)

fun crucibleBars(): Map<Bar, Int> {
    return BARS.associateWith { it.crucibleCount() }
}

fun currentHeat(): Int = Varpbits.varpbit(FOUNDRY_VARP, 1023)
fun currentProgress(): Int = Varpbits.varpbit(FOUNDRY_VARP, 10, 1023)
fun currentQuality(): Int = Varpbits.varpbit(QUALITY_VARP, 8, 255)