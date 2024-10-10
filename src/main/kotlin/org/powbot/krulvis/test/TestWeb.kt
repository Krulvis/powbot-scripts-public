package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : KrulScript() {
	override fun createPainter(): ATPaint<*> = TestWebPainter(this)

	var destination = Tile(2339, 3109, 0)

	var patch: GameObject? = null
	var collisionMap: TransientGetter2D<Int>? = null


	override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
		Movement.builder(destination).setForceWeb(true).move()
		sleep(600)
	}

	@com.google.common.eventbus.Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
	}

}


class TestWebPainter(script: TestWeb) : ATPaint<TestWeb>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.build()
	}

	override fun paintCustom(g: Rendering) {
		val collisionMap = script.collisionMap
//        if (collisionMap != null) {
//            val neighbor = script.neighbor
//            Players.local().tile().drawCollisions(collisionMap)
//            val colorTile = if (script.destination.blocked(collisionMap)) Color.RED else Color.GREEN
//            val colorPatch = if (script.patch?.tile?.blocked(collisionMap) == true) Color.ORANGE else Color.GREEN
//            val colorNeighbor = if (script.neighbor?.blocked(collisionMap) == true) Color.RED else Color.GREEN
////            script.patchTile.drawOnScreen(outlineColor = colorTile)
//            script.patch?.tile?.drawOnScreen(outlineColor = colorPatch)
//            neighbor?.drawOnScreen(outlineColor = colorNeighbor)
//        }

	}

	fun Tile.toWorld(): Tile {
		val a = Game.mapOffset()
		return this.derive(+a.x(), +a.y())
	}

}

fun main() {
	TestWeb().startScript("127.0.0.1", "banned", true)
}
