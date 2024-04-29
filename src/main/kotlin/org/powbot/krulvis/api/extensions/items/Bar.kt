package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.api.ATContext.containsOneOf
import kotlin.math.floor
import kotlin.math.min

enum class Bar(
    override val ids: IntArray,
    val shopId: Int,
    //settingId & shiftAmount are used for Blast furnace
    val bfSettingId: Int,
    val shiftAmount: Int,
    val primary: Ore,
    val secondary: Ore,
    val secondaryMultiplier: Int
) : Item {
    BRONZE(intArrayOf(2349), 109, -1, -1, Ore.TIN, Ore.COPPER, 1),
    IRON(intArrayOf(2351), 109, 545, 8, Ore.IRON, Ore.COAL, 0),
    SILVER(intArrayOf(2355), 113, -1, -1, Ore.SILVER, Ore.COAL, 0),
    STEEL(intArrayOf(2353), 110, 545, 16, Ore.IRON, Ore.COAL, 2),
    GOLD(intArrayOf(2357), 113, 546, 16, Ore.GOLD, Ore.COAL, 0),
    MITHRIL(intArrayOf(2359), 111, 545, 24, Ore.MITHRIL, Ore.COAL, 4),
    ADAMANTITE(intArrayOf(2361), 112, 546, 0, Ore.ADAMANTITE, Ore.COAL, 6),
    RUNITE(intArrayOf(2363), 113, 546, 8, Ore.RUNITE, Ore.COAL, 8);

    val blastFurnaceCount: Int
        get() = Varpbits.varpbit(bfSettingId) shr shiftAmount and 0x1F

    fun getSmeltableCount(): Int {
        val prim = primary.getCount()
        val sec = secondary.getCount()
        return if (secondaryMultiplier <= 0) prim else min(prim, floor(sec / secondaryMultiplier.toDouble()).toInt())
    }

    fun getSmeltComponent(): Component? {
        val comps = Components.stream().action("Smelt").list()
        val blurite = comps.size == 9
        return if (comps.size > ordinal) comps[if (blurite && ordinal > 0) ordinal + 1 else ordinal] else null
    }

    fun getOptimalSmelInventory(): Pair<Int, Int> {
        val space = 28
        val maxSmelts = space / (secondaryMultiplier + 1)
        return Pair(maxSmelts, maxSmelts * secondaryMultiplier)
    }

    override fun hasWith(): Boolean {
        return Inventory.containsOneOf(*ids)
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted)
    }

    override fun toString(): String {
        return name
    }
}

fun main() {
    Bar.values().forEach {
        println("$it, ${it.getOptimalSmelInventory()}")
    }
}





