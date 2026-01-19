package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color.CYAN
import org.powbot.api.Color.GREEN
import org.powbot.api.event.RenderEvent
import org.powbot.api.rt4.GameObject
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.Cannon

@ScriptManifest("cannon debug", "Debugs cannon", priv = true)
class DebugCannon : AbstractScript() {

	private var cannon = GameObject.Nil
	override fun onStart() {
		addPaint(PaintBuilder()
			.addString("Cannon state") { Cannon.state().toString() }
			.addString("Cannon coords") { Cannon.tile().toString() }
			.addString("Cannon firing") { Cannon.firing().toString() }
			.addString("Cannon") { cannon.tile.toString() }
			.build())
	}

	@Subscribe
	fun onRenderEvent(e: RenderEvent) {
		val c = cannon
		if (c.valid()) {
			c.tile.drawOnScreen(outlineColor = GREEN)
		}
		val optimalTiles = Cannon.standingTiles(2)
		optimalTiles.forEach {
			it.drawOnScreen(outlineColor = CYAN)
		}
	}

	override fun poll() {
		cannon = Cannon.getCannon()
	}
}

fun main() {
	DebugCannon().startScript()
}