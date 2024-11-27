package org.powbot.krulvis.araxxor

import org.powbot.api.Color.CYAN
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.RED
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Poison
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.mobile.drawing.Rendering

class AraxxorPainter(script: Araxxor) : KrulPaint<Araxxor>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Venom") { "venom=${Poison.envenomed()}, dmg=${Poison.nextDamage}" }
			.addString("Explo") { "Alive=${script.exploding.valid()}, exploding=${script.explodingTimer.isFinished()}" }
			.addString("Araxxor") { "Enrage=${script.enrage}, Attack=${script.ticks} - ${script.nextAttackTick}" }
			.build()
	}

	override fun paintCustom(g: Rendering) {
		val araxxor = script.araxxor
		if (araxxor.valid()) {
			script.araxxorCenter.drawOnScreen("Anim: ${script.lastAraxAnimation}, Att: ${script.attacks}")
		}
		val tiles = script.unsafeTiles
		tiles.forEach { it.drawOnScreen(outlineColor = RED) }
		val myTile = me.trueTile()
		val exploding = !script.explodingTimer.isFinished()
		val exploRemainder = if (exploding) script.explodingTimer.getRemainder().toString() else ""
		script.exploding.trueTile()
			.drawOnScreen(
				"A: ${script.exploding.animation()}, E: $exploRemainder ms",
				outlineColor = if (exploding) RED else GREEN
			)
		myTile.drawOnScreen(outlineColor = CYAN)
		script.exploDestination.drawOnScreen("E", outlineColor = CYAN)
	}
}