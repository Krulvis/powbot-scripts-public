package org.powbot.krulvis.cluesolver.clues

import org.powbot.api.Tile
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement

enum class Emote {
    CHEER, PANIC, BOW, CLAP, YAWN, WAVE, HEADBANG, SPIN, THINK, DANCE, SHRUG, RASPBERRY, JUMP_FOR_JOY, JIG, LAUGH, BECKON
}

enum class EmoteClue(
    override val id: Int,
    override val solveSpot: Tile, val equipment: List<EquipmentRequirement>, vararg emotes: Emote
) : Clue {
    //With the empty requirements we assume its naked and unequip whatever they have
    CHEER_GAMES_ROOM(10212, Tile(2207, 4950, 0), emptyList(), Emote.CHEER),
    PANIC_FISHING_TRAWLER(10224, Tile(2676, 3169, 0), emptyList(), Emote.PANIC),
    CLAP_WIZARD_TOWER(
        10182, Tile(3113, 3190, 0),
        listOf(EquipmentRequirement(1639), EquipmentRequirement(1005), EquipmentRequirement(1137)),
        Emote.CLAP
    ),
    YAWN_DRAYNOR_MARKET(
        10184, Tile(3080, 3250, 0),
        listOf(EquipmentRequirement(1097), EquipmentRequirement(1191), EquipmentRequirement(1295)),
        Emote.YAWN
    ),
    PANIC_LIMESTONE_MINE(
        10186, Tile(3370, 3498, 0),
        listOf(EquipmentRequirement(1075), EquipmentRequirement(1269), EquipmentRequirement(1141)),
        Emote.PANIC
    ),
    BOW_LEGENDS_GUILD(
        10188, Tile(2728, 3348, 0),
        listOf(EquipmentRequirement(1067), EquipmentRequirement(845), EquipmentRequirement(1696)),
        Emote.BOW,
    ),
    WAVE_MUDSKIPPER_POINT(
        10190, Tile(2984, 3112, 0),
        listOf(EquipmentRequirement(1095), EquipmentRequirement(1424), EquipmentRequirement(1019)),
        Emote.WAVE,
    ),
    CHEER_PORT_SARIM_MONKS(
        10192, Tile(3046, 3236, 0),
        listOf(EquipmentRequirement(1169), EquipmentRequirement(1083), EquipmentRequirement(1656)),
        Emote.CHEER,
    ),
    HEADBANG_ALKHARID_MINE(
        10194, Tile(3299, 3300, 0),
        listOf(EquipmentRequirement(1833), EquipmentRequirement(1059), EquipmentRequirement(1061)),
        Emote.HEADBANG
    ),
    SPIN_DRAYNOR_MANOR_FOUNTAIN(
        10196, Tile(3087, 3332, 0),
        listOf(EquipmentRequirement(1115), EquipmentRequirement(1097), EquipmentRequirement(1155)),
        Emote.SPIN,
    ),
    THINK_LUMBY_WEED_FIELD(
        10198, Tile(3158, 3299, 0),
        listOf(EquipmentRequirement(843), EquipmentRequirement(640), EquipmentRequirement(654)),
        Emote.THINK,
    ),
    DANCE_DRAYNOW_CROSSROADS(
        10200, Tile(3109, 3295, 0),
        listOf(EquipmentRequirement(1101), EquipmentRequirement(1637), EquipmentRequirement(839)),
        Emote.DANCE,
    ),
    SHRUG_RIMMINGTON(
        10202, Tile(2974, 3239, 0),
        listOf(EquipmentRequirement(1654), EquipmentRequirement(1237), EquipmentRequirement(1635)),
        Emote.SHRUG,
    ),
    YAWN_VARROCK_LIBRARY(
        10204, Tile(3210, 3494, 0),
        listOf(EquipmentRequirement(1335), EquipmentRequirement(4300), EquipmentRequirement(638)),
        Emote.YAWN,
    ),
    CLAP_ARDOUGNE_MILL(
        10206, Tile(2633, 3388, 2),
        listOf(EquipmentRequirement(4300), EquipmentRequirement(5525), EquipmentRequirement(640)),
        Emote.CLAP,
    ),
    DANCE_FALADOR_PARTY(
        10208, Tile(3044, 3375, 0),
        listOf(EquipmentRequirement(1157), EquipmentRequirement(1119), EquipmentRequirement(1081)),
        Emote.DANCE,
    ),
    CHEER_DRUIDS_CRICLE(
        10210, Tile(2924, 3482, 0),
        listOf(EquipmentRequirement(4310), EquipmentRequirement(579), EquipmentRequirement(1307)),
        Emote.CHEER,
    ),
    JUMP_CAMELOT_BEE(
        10214, Tile(2758, 3444, 0),
        listOf(EquipmentRequirement(1353), EquipmentRequirement(1833), EquipmentRequirement(648)),
        Emote.JUMP_FOR_JOY,
    ),
    RASPBERRY_MONKEY_CAGE(
        10216, Tile(2602, 3275, 0),
        listOf(EquipmentRequirement(1379), EquipmentRequirement(1075), EquipmentRequirement(1133)),
        Emote.RASPBERRY,
    ),
    SPIN_RIMMINGTON_XROADS(
        10218, Tile(2983, 3276, 0),
        listOf(EquipmentRequirement(642), EquipmentRequirement(1095), EquipmentRequirement(658)),
        Emote.SPIN,
    ),
    JIG_FISHING_GUILD(
        10220, Tile(2612, 3392, 0),
        listOf(EquipmentRequirement(1639), EquipmentRequirement(1694), EquipmentRequirement(1103)),
        Emote.JIG,
    ),
    RASPBERRY_KEEP_LE_FAYE(
        10222, Tile(2762, 3401, 0),
        listOf(EquipmentRequirement(1169), EquipmentRequirement(1115), EquipmentRequirement(1059)),
        Emote.RASPBERRY,
    ),
    LAUGH_SINCLAIR_MANSION(
        10226, Tile(2740, 3536),
        listOf(EquipmentRequirement(1167), EquipmentRequirement(577), EquipmentRequirement(1323)),
        Emote.LAUGH,
    ),
    CLAP_EXAM_ROOM(
        10228, Tile(3360, 3342, 0),
        listOf(EquipmentRequirement(1005), EquipmentRequirement(1059), EquipmentRequirement(628)),
        Emote.CLAP,
    ),
    WAVE_LUMBERYARD(
        10230, Tile(3310, 3491, 0),
        listOf(EquipmentRequirement(1131), EquipmentRequirement(1095), EquipmentRequirement(1351)),
        Emote.WAVE,
    ),
    BOW_DUEL_ARENA(
        10232, Tile(3313, 3243, 0),
        listOf(EquipmentRequirement(1095), EquipmentRequirement(1101), EquipmentRequirement(1169)),
        Emote.BOW,
    ),
    CHEER_VARROCK_CASTLE(
        12162, Tile(3213, 3462, 0),
        listOf(EquipmentRequirement(1361), EquipmentRequirement(1169), EquipmentRequirement(1641)),
        Emote.SPIN,
    ),
    WAVE_FALADOR_GEMS(
        12164, Tile(2945, 3335, 0),
        listOf(EquipmentRequirement(1273), EquipmentRequirement(1125), EquipmentRequirement(1191)),
        Emote.WAVE,
    ),
    DANCE_GRAND_EXCHANGE(
        19831, Tile(3164, 3467),
        listOf(EquipmentRequirement(1013), EquipmentRequirement(636), EquipmentRequirement(5533)),
        Emote.DANCE,
    ),
    JIG_VARROCK_MUSEUM(
        19833, Tile(3252, 3401),
        listOf(EquipmentRequirement(1383), EquipmentRequirement(5527)),
        Emote.JIG,
    ),


    //Medium
    BECKONG_DIGSITE(
        10274, Tile(3369, 3427, 0),
        listOf(EquipmentRequirement(658), EquipmentRequirement(6328), EquipmentRequirement(1267)),
        Emote.BECKON,
        Emote.BOW
    ),
    YAWN_CASTLE_WARS(
        10262, Tile(2442, 3090, 0),
        listOf(EquipmentRequirement(1698), EquipmentRequirement(1329), EquipmentRequirement(4325)),
        Emote.YAWN,
        Emote.SHRUG
    );

    ;

    //Here we will be doing the clue-specific logic (equipping the gear, performing the emote)
    override fun solve(): Boolean {
        TODO("Not yet implemented")
    }



}