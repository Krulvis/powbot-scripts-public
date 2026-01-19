package org.powbot.krulvis.giantsfoundry.data

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.giantsfoundry.ROOT
import org.powbot.krulvis.giantsfoundry.currentHeat

enum class Action(
    val textureId: Int,
    val interactable: String,
    val tile: Tile,
    var minHeat: Int,
    var maxHeat: Int,
    val activeBarComponentId: Int,
    val heats: Boolean = false
) {
    HAMMER(4442, "Trip hammer", Tile(3365, 11497, 0), -1, -1, 21),
    GRIND(4443, "Grindstone", Tile(3362, 11492, 0), -1, -1, 20, true),
    POLISH(4444, "Polishing wheel", Tile(3363, 11485, 0), -1, -1, 19);

    fun canPerform(): Boolean {
        val currentHeat = currentHeat()
        if (currentHeat > maxHeat) {
            return false
        }
        return if (tile.distance() <= 2.5 && !heats) {
            // Near the action thing
            currentHeat >= minHeat + 2
        } else {
            currentHeat > minHeat + 4
        }
    }

    fun reset() {
        minHeat = -1
        maxHeat = -1
    }

    fun barComponent() = Widgets.component(ROOT, activeBarComponentId)

    fun calculateMinMax() {
        val totalWidth = Widgets.component(ROOT, 8).width()
        val barComp = barComponent()
        minHeat = (1000.0 / totalWidth * barComp.x()).toInt()
        maxHeat = (1000.0 / totalWidth * (barComp.x() + barComp.width())).toInt()
        debug("Calculated min=$minHeat, max=$maxHeat for $name with totalWidth=$totalWidth, barX=${barComp.x()}, barWidth=${barComp.width()}")
    }

    fun getObj(): GameObject {
        val obj = Objects.stream(tile, GameObject.Type.INTERACTIVE).name(interactable).first()
        if (!obj.valid()) return obj
        when (obj.name) {
            HAMMER.interactable -> obj.bounds(-112, 112, -114, 0, -32, 32)
            GRIND.interactable -> obj.bounds(-112, 112, -114, 0, -32, 32)
            POLISH.interactable -> obj.bounds(-72, 72, -164, 20, -72, 72)
        }
        return obj
    }

    companion object {
        fun forTexture(texture: Int) = Action.entries.firstOrNull { it.textureId == texture }
        fun calculateMinMax() = Action.entries.forEach { it.calculateMinMax() }
        fun reset() = Action.entries.forEach { it.reset() }
    }

}