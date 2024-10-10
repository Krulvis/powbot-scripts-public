package org.powbot.krulvis.tithe

object Data {

    val WATER_CAN_FULL = 5340
    val WATER_CANS = intArrayOf(WATER_CAN_FULL, 5339, 5338, 5337, 5336, 5335, 5334, 5333)
    val EMPTY_CAN = 5331

    val SEEDS = intArrayOf(13423, 13424, 13425)

    val GOLOVANOVA = 13426
    val BOLOGANO = 13427
    val LOGAVANO = 13428
    val HARVEST = intArrayOf(GOLOVANOVA, BOLOGANO, LOGAVANO)

    val NAMES = listOf("Golovanova", "Bologano", "Logavano")
    
}

fun main() {
    val total = 100
    val cycles = intArrayOf(8, 9, 10, 11, 12, 13, 14, 15)

    cycles.forEach {
        val leftOver = 100 % it
        println("cycle=$it, leftOver=$leftOver")
    }
}
