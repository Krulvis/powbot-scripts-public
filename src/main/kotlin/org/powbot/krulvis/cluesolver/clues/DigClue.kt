package org.powbot.krulvis.cluesolver.clues

import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item.Companion.SPADE

enum class DigClue(override val id: Int, override val solveSpot: Tile) : Clue {
    CHAMPIONS_GUILD_WEST(2713, Tile(3167, 3360, 0)),
    VARROCK_MINE(2713, Tile(3166, 3359, 0)),
    EAST_VARROCK_MINE(2716, Tile(3290, 3374, 0)),
    NORTH_EAST_FALADOR(2719, Tile(3043, 3398, 0)),
    GRAND_TREE(3510, Tile(2459, 3505, 0)),
    SEERS_CHURCH(3516, Tile(2612, 3481, 0)),
    WIZARD_TOWER(3518, Tile(3109, 3152, 0)),
    FALADOR_NORTH(7236, Tile(2969, 3414, 0)),
    FALADOR_STONES(12170, Tile(3040, 3399, 0)),
    ALKHARID(12179, Tile(3300, 3291, 0)),
    LUMBRIDGE_CASTLE(19814, Tile(3221, 3220)),

    //Medium
    COORDS1(2817, Tile(2849, 3033, 0)),
    ICE_DUNGEON_LADDER(2819, Tile(3007, 3145)),
    COORDS3(2823, Tile(3217, 3177, 0)),
    DRAYNOR_VILLAGE_FISHING(2827, Tile(3092, 3226, 0)),
    COORDS5(3592, Tile(2387, 3435)),
    COORDS6(3594, Tile(2417, 3516, 0)),
    MISCELANIA(7286, Tile(2535, 3865, 0)),
    CHAOS_ALTAR(7290, Tile(2455, 3230, 0)),
    CAMELOT_RELEKKA(7294, Tile(2666, 3563, 0)),
    COORDS10(7305, Tile(2538, 3881)),
    COORDS11(7311, Tile(3005, 3475, 0)),
    COORDS12(12045, Tile(2381, 3467, 0)),
    COORDS13(12049, Tile(2585, 3505, 0)),
    ;

    //Here we will be doing the clue-specific logic (digging)
    override fun solve(): Boolean {
        if (solveSpot.distance() > 0) {
            Movement.step(solveSpot, 0)
        } else if (Inventory.stream().id(SPADE).first().interact("Dig")) {
            return waitFor { Clue.getClue() != this }
        }
        return false
    }
}