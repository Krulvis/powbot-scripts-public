package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.tithe.TitheFarmer

class WalkBack(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Walking back") {
	override fun execute() {

		val cornerX = script.patches.first().tile.x
		val walkableTile = Tile(cornerX + 2, script.patches.first().tile.y)
		val furthestReachable =
			script.patches.firstOrNull { it.tile.matrix().onMap() } ?: return
		if (walkableTile.distance() > 1 && Movement.step(Tile(cornerX + 2, furthestReachable.tile.y))) {
			Condition.wait({ walkableTile.distance() < 5 }, 500, 10)
		}

	}
}