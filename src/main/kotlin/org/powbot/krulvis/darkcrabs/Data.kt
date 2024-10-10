package org.powbot.krulvis.darkcrabs

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory

object Data {

    val logger = LoggerFactory.getLogger(javaClass.simpleName)

    val FISHING_SPOT = Tile(3188, 3927, 0)
    val LOBSTER_POT = 301
    val DARK_BAIT = 11940
    val SPIRIT_FLAKES = 25588
    val FISHING_ANIMATION = 619
    val RESOURCE_AREA = Area(Tile(3173, 3944), Tile(3196, 3924))
    val RESOURCE_AREA_DOOR = Tile(3184, 3944, 0)
    val WEB_TILE = Tile(3158, 3951, 0)
    val KEEP_LEVER = Tile(3153, 3923, 0)
    val EDGE_LEVER = Tile(3090, 3475, 0)

    val DESERTED_KEEP = Area(
        Tile(3155, 3954),
        Tile(3151, 3939),
        Tile(3144, 3932),
        Tile(3147, 3920),
        Tile(3161, 3914),
        Tile(3168, 3918),
        Tile(3160, 3930),
        Tile(3169, 3945),
        Tile(3161, 3954)
    )

    val SWORDS = intArrayOf(13108, 13109, 13110, 13111)

    const val DIARY_WILDERNESS_EASY: Int = 4466
    const val DIARY_WILDERNESS_MEDIUM: Int = 4467
    const val DIARY_WILDERNESS_HARD: Int = 4468
    const val DIARY_WILDERNESS_ELITE: Int = 4469
    fun resourceGate() =
        Objects.stream(RESOURCE_AREA_DOOR, GameObject.Type.BOUNDARY).name("Gate").action("Open").first()

    fun web() = Objects.stream(WEB_TILE, GameObject.Type.INTERACTIVE).name("Web").action("Slash").first()
    fun cutWeb(): Boolean {
        var web = web()
        if (web.valid()) {
            val slashTimer = Timer(5000)
            do {
                if (walkAndInteract(web, "Slash")) {
                    waitFor { web.distance() < 1 }
                }
                web = web()
            } while (web.valid() && !slashTimer.isFinished())
            logger.info("Slashed web=${web.valid()}, timerFinished=${slashTimer.isFinished()}")
        }
        return !web.valid()
    }

    val resourceAreaCost: Int by lazy { getResourceAreaEntryCost() }

    private fun getResourceAreaEntryCost(): Int {
        return if (Varpbits.value(DIARY_WILDERNESS_ELITE) == 1) 0
        else if (Varpbits.value(DIARY_WILDERNESS_HARD) == 1) 3750
        else if (Varpbits.value(DIARY_WILDERNESS_MEDIUM) == 1) 6000
        else 7500
    }


}
