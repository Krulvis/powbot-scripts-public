package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.Game
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(
	name = "PathBuilder",
	version = "1.0.0",
	description = "Build a tile path and prints it in log for manual pathing",
	priv = true
)
class PathBuilder : KrulScript() {
	override fun createPainter(): ATPaint<*> = PathBuilderPainter(this)

	val path: MutableList<Tile> = mutableListOf()

	override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
		val currentPosition = me.tile()
		val distanceLast = path.lastOrNull()?.distance() ?: 6.0
		if (distanceLast >= 5.0) {
			path.add(currentPosition)
		}
		logger.info("CURRENTLY WE HAVE")
		logger.info(path.joinToString(", ") { "Tile(${it.x}, ${it.y}, ${it.floor})" })
		sleep(500)
	}

	@Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
	}
}


class PathBuilderPainter(script: PathBuilder) : ATPaint<PathBuilder>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.build()
	}

	override fun paintCustom(g: Rendering) {
		script.path.draw()
	}

	fun List<Tile>.draw() {
		forEachIndexed { index, tile ->
			tile.drawOnScreen(index.toString())
		}
	}


	fun Tile.toWorld(): Tile {
		val a = Game.mapOffset()
		return this.derive(+a.x(), +a.y())
	}

}

fun main() {
	PathBuilder().startScript("127.0.0.1", "banned", true)
}
