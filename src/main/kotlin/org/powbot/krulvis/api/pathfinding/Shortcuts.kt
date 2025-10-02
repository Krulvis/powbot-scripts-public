package org.powbot.krulvis.api.pathfinding

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Shortcuts")

class GameObjectInteraction(val tile: Tile, val name: String, val type: GameObject.Type, vararg val actions: String) :
    Interaction {

    fun find(): GameObject = Objects.stream(tile, type).name(name).action(*actions).first()

    override fun execute(): Boolean {
        val obj = find()
        val action = actions.lastOrNull { a -> obj.actions().contains(a) }
        logger.info("GameObjectInteraction(tile=${tile}, name=${name}): obj=${obj}, action=${action}")
        return obj.valid() && obj.interact(action)
    }

}

interface Interaction {
    fun execute(): Boolean
}

class Shortcut(val from: Tile, val to: Tile, val interaction: Interaction) {
    fun execute(): Boolean {
        if (to.distance() < 1) {
            return true
        } else if (from.distance() > 5) {
            val path = LocalPathFinder.findPath(from)
            path.traverse()
            return false
        } else {
            return interaction.execute()
        }
    }
}