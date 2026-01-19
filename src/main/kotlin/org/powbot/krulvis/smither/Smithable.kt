package org.powbot.krulvis.smither

enum class Smithable(val barsRequired: Int) {
    DAGGER(1),
    AXE(1),
    MACE(1),
    MED_HELM(1),
    BOLTS(1),
    SWORD(1),
    DART_TIPS(1),
    NAILS(1),
    SCIMITAR(2),
    ARROWTIPS(1),
    JAVELIN_TIPS(1),
    LIMBS(1),
    LONGSWORD(2),
    FULL_HELM(2),
    KNIVES(1),
    SQ_SHIELD(2),
    WARHAMMER(3),
    BATTLEAXE(3),
    CHAINBODY(3),
    KITESHIELD(3),
    CLAWS(2),
    TWO_HAND_SWORD(3) {
        override fun toString(): String {
            return "2h sword"
        }
    },
    PLATESKIRT(3),
    PLATELEGS(3),
    PLATEBODY(5);

    override fun toString(): String {
        return name.lowercase().replace("_", " ")
            .replaceFirst(name.first().lowercaseChar(), name.first())
    }
}

fun main() {
    println("[\"${Smithable.values().map { it.name }.joinToString("\", \"")}]")
}

