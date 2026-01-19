package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.World
import org.powbot.api.rt4.Worlds
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class HopFromPlayers(script: Fighter) : Leaf<Fighter>(script, "Hopping") {
	override fun execute() {
		val currentWorld = Worlds.current()
		val type = if (Worlds.isCurrentWorldMembers()) World.Type.MEMBERS else World.Type.FREE
		val world = Worlds.stream().filtered { it != currentWorld && it.type() == type }.first()
		world.hop()
	}

}