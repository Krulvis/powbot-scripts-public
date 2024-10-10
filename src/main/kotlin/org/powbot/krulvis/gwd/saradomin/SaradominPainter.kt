package org.powbot.krulvis.gwd.saradomin

import org.powbot.api.Color
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.PaintFormatters.round
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume.Companion.canConsume
import org.powbot.mobile.drawing.Rendering
import kotlin.math.round

class SaradominPainter(script: Saradomin) : ATPaint<Saradomin>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Tick") { script.ticks.toString() }
			.addString("TileArrival") { "${(script.tileArrival.getElapsedTime() / 1000.0).round(1)}s" }
			.addString("CanAttack") { "${script.lastAttackTick}, ${script.canAttack()}" }
			.addString("CanConsume") { "${ShouldConsume.consumeTick}, ${script.canConsume()}" }
			.build()
	}

	override fun paintCustom(g: Rendering) {
		super.paintCustom(g)
		script.zilyanaTiles.forEachIndexed { i, tile ->
			tile.drawOnScreen(i.toString(), outlineColor = Color.CYAN)
		}
		me.trueTile().drawOnScreen(outlineColor = Color.BLACK)
		g.drawPolygon(script.god.generalArea.getPolygon().scaleToGame())
	}
}