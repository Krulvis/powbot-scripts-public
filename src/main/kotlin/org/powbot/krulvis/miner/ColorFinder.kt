package org.powbot.krulvis.miner

import org.powbot.api.Condition.sleep
import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Ore.Companion.hasOre
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(
        name = "krul ColorFinder",
        description = "Helps find colors of non-working miner rocks",
        author = "Krulvis",
        version = "1.0.0",
        markdownFileName = "Miner.md",
        category = ScriptCategory.Mining
)
class ColorFinder : KrulScript() {
    override fun createPainter(): ATPaint<*> {
        return Painter(this)
    }

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "Chill") {
//        val ironRocks = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Iron rocks").toList()
//        ironRocks.forEach {
//            val rocksAtTile = Objects.stream().at(it.tile).toList()
//            val names = rocksAtTile.joinToString { it.name }
//            log.info("IRON ROCK AT=${it.tile}, objs=${rocksAtTile.size}, names=${names}")
//        }
//        Objects.stream(50).filtered { it.originalColors().isNotEmpty() }.forEach {
//            log.info("Found obj with colors: ${it.name}, ${it.originalColors().joinToString()}")
//        }
        sleep(1000)
    }
}

class Painter(script: ColorFinder) : ATPaint<ColorFinder>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.build()
    }

    fun paintColors() {
        val myTile = Players.local().tile()
        script.logger.info("My tile=${myTile}")
        val tiles = listOf(
                Tile(myTile.x - 1, myTile.y, myTile.floor),
                Tile(myTile.x - 1, myTile.y + 1, myTile.floor),
                Tile(myTile.x - 1, myTile.y - 1, myTile.floor),
                Tile(myTile.x, myTile.y + 1, myTile.floor),
                Tile(myTile.x, myTile.y - 1, myTile.floor),
                Tile(myTile.x + 1, myTile.y + 1, myTile.floor),
                Tile(myTile.x + 1, myTile.y, myTile.floor),
                Tile(myTile.x + 1, myTile.y - 1, myTile.floor),
        )
        tiles.forEach { tile ->
            val gos = Objects.stream().at(tile).filtered { it.name.isNotEmpty() }
            gos.forEach { script.logger.info("GO AT TILE=${tile}: ${it.name}, has ores=${it.hasOre()}, ") }
            val colors = gos.firstOrNull { it.modifiedColors().isNotEmpty() }?.modifiedColors()
            if (colors != null) {
                tile.drawOnScreen(colors.joinToString("\n"))
            }
        }
    }

    fun paintCalcifiedRocks() {
        Objects.stream().name("Calcified rocks").forEach {
            script.logger.info("Calcified rock at=${it.tile} ")
//            it.tile.drawOnScreen("DynamicMainId=${it.dynamicMainId()}, mainId=${it.mainId()}")
            it.tile.drawOnScreen("Objects At Tile = ${Objects.stream().at(it.tile).count()}")
        }
    }

    fun paintAngle() {
        fun facingTile(): Tile {
            val orientation: Int = Players.local().orientation()
            val t: Tile = Players.local().tile()
            when (orientation) {
                4 -> return Tile(t.x(), t.y() + 1, t.floor())
                6 -> return Tile(t.x() + 1, t.y(), t.floor())
                0 -> return Tile(t.x(), t.y() - 1, t.floor())
                2 -> return Tile(t.x() - 1, t.y(), t.floor())
            }
            return Tile.Nil
        }

        val facingTile = facingTile()

        val rock = Objects.stream().at(facingTile).nameContains("rock").first()
        facingTile.drawOnScreen("Orientation=${rock.name}, angle=${Camera.yaw()}, \n " +
                "- modColors=${rock.modifiedColors().joinToString()}, \n " +
                "- orgColors=${rock.originalColors().joinToString()}")

    }

    override fun paintCustom(g: Rendering) {
//        paintColors()
//        paintCalcifiedRocks()
        paintAngle()
    }
}

fun main() {
    ColorFinder().startScript()
}