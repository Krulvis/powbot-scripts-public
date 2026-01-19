package org.powbot.krulvis.cluesolver.clues

import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.Item


interface Clue : Item {

    val solveSpot: Tile

    fun solve(): Boolean

    override val ids: IntArray
        get() = intArrayOf(id)

    override val itemName: String
        get() = "Clue scroll"

    override val stackable: Boolean
        get() = true

    override fun hasWith(): Boolean {
        return getCount() > 0
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount()
    }

    enum class Level(val obtainingTile: Tile, val npcName: String) {
        EASY(Tile(-1, -1), "H.A.M. Member"),
        MEDIUM(Tile(-1, -1), "Gnome"),
        HARD(Tile(-1, -1), "Paladin"),
        ELITE(Tile(-1, -1), "Hero");
    }

    companion object {
        fun getClueItem() = Inventory.stream().nameContains("Clue").first()
        fun getClue() = forItem(getClueItem())
        val allClues: Array<out Clue> = arrayOf(*EmoteClue.values(), *DigClue.values(), *InteractionClue.values())

        fun forItem(item: org.powbot.api.rt4.Item): Clue? = allClues.firstOrNull { it.id == item.id }
    }

}