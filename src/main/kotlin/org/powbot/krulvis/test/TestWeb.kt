package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Pathing
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : KrulScript() {
    override fun createPainter(): KrulPaint<*> = TestWebPainter(this)

    var destination = Tile(2339, 3109, 0)

    var patch: GameObject? = null

    var dest = Tile.Nil
    var path = LocalPath(emptyList())

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//		Movement.builder(destination).setForceWeb(true).move()
        dest = Tile(1387, 3309, 0)
        path = LocalPathFinder.findPath(dest)
        logger.info("Path = ${path.size}")
        sleep(600)
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        logger.info("$e")
    }

}


class TestWebPainter(script: TestWeb) : KrulPaint<TestWeb>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Destination") { "${script.dest}, distance=${script.dest.distance()}" }
            .build()
    }

    override fun paintCustom(g: Rendering) {
        val dest = script.dest
//        if (dest != Tile.Nil) {
//            val path = Pathing.findPath(dest)
//            path.forEachIndexed { i, tile -> tile.drawOnScreen("$i", outlineColor = Color.GREEN) }
//        }
        val p = script.path
        p.draw()

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
