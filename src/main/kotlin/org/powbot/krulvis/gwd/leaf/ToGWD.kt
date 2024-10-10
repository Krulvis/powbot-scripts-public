package org.powbot.krulvis.gwd.leaf

import org.powbot.api.Point
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.gwd.GWD.CRACK_TILE
import org.powbot.krulvis.gwd.GWD.GOD_WARS_AREA
import org.powbot.krulvis.gwd.GWD.GOD_WARS_OUTSIDE_AREA
import org.powbot.krulvis.gwd.GWD.HOLE_TILE
import org.powbot.krulvis.gwd.GWD.PATH_TO_ENTRANCE
import org.powbot.krulvis.gwd.GWDScript

class ToGWD<S>(script: S) : Leaf<S>(script, "Moving to GWD") where S : GWDScript<S> {
	override fun execute() {
		val outsideGWD = GOD_WARS_OUTSIDE_AREA.contains(me)
		if (!outsideGWD) {
			val crack = Objects.stream(CRACK_TILE, GameObject.Type.INTERACTIVE).name("Little crack").first()
			script.logger.info("Found crack=${crack}")
			if (!crack.valid() && script.gwdTeleport.execute()) {
				Movement.walkTo(CRACK_TILE)
			} else if (walkAndInteract(crack, "Crawl-through")) {
				waitFor(10000) { GOD_WARS_OUTSIDE_AREA.contains(me) }
			}
		} else {
			val hole =
				Objects.stream(HOLE_TILE, GameObject.Type.INTERACTIVE).name("Hole").action("Climb-down").first()
			script.logger.info("Found entrance=${hole}")
			if (hole.valid() && hole.distance() < 15) {
				if (walkAndInteract(hole, "Climb-down")) {
					waitForDistance(hole) {
						val t = me.tile()
						GOD_WARS_AREA.getPolygon().contains(Point(t.x, t.y))
					}
				}
			} else {
				PATH_TO_ENTRANCE.traverse()
			}
		}
	}
}