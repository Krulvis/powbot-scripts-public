package org.powbot.krulvis.smither

enum class Smithable(val barsRequired: Int) {
    DAGGER(1),
    AXE(1),
    MACE(1),
    MEDIUM_HELM(1),
    BOLTS(1),
    SWORD(1),
    DART_TIPS(1),
    NAILS(1),
    SCIMITAR(2),
    ARROWTIPS(1),
    LIMBS(1),
    LONG_SWORD(2),
    FULL_HELM(2),
    KNIVES(1),
    SQUARE_SHIELD(2),
    WARHAMMER(3),
    BATTLE_AXE(3),
    CHAIN_BODY(3),
    KITE_SHIELD(3),
    CLAWS(2),
    TWO_HAND_SWORD(3) {
        override fun toString(): String {
            return "2-hand sword"
        }
    },
    PLATE_SKIRT(3),
    PLATE_LEGS(3),
    PLATE_BODY(5);

    override fun toString(): String {
        return name.lowercase().replace("_", " ")
            .replaceFirst(name.first().lowercaseChar(), name.first())
    }
}

fun main() {
    println("[\"${Smithable.values().map { it.name }.joinToString("\", \"")}]")
}

