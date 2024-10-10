package org.powbot.krulvis.dagannothkings

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class DKPaint(script: DagannothKings) : ATPaint<DagannothKings>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Kills") { perHourText(script.kills) }
			.addString("Target") { "${script.target.name}, valid=${script.target.valid()}, dead=${script.target.dead()}" }
			.build()
	}


	override fun paintCustom(g: Rendering) {
		if (script.lureTile != Tile.Nil) {
			script.lureTile.drawOnScreen(outlineColor = Color.ORANGE)
			script.safeTile.drawOnScreen(outlineColor = Color.GREEN)
			val t = me.tile()
			val target = script.target
			if (target.valid()) {
				target.tile().drawOnScreen(target.distanceTo(t).toString() + " " + target.distanceTo(script.rexTile), Color.RED)
			}
			val x = 10
			var y = 25
			val yy = 20
			Data.King.values().forEach {
				val killTile = it.killTile
				if (killTile.valid()) {
					killTile.drawOnScreen(outlineColor = Color.CYAN)
				}
				g.drawString(it.name + "kill=${it.kill}, respawnTime=${it.respawnTimer}", x, y)
				y += yy
			}
		} else {
			script.logger.info("LureTile still is nill")
		}
	}
}