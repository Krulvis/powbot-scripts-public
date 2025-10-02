package org.powbot.krulvis.test

import kotlinx.coroutines.launch
import org.powbot.api.*
import org.powbot.api.Color.GREEN
import org.powbot.api.Condition.sleep
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Npc
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Monster.borderingLayer
import org.powbot.krulvis.api.extensions.Monster.rectangle
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.araxxor.Data.SIZE
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul TestMonster", version = "1.0.1", description = "", priv = true)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "monsterName",
            description = "Monster Name",
            optionType = OptionType.STRING,
            defaultValue = "Giant Mole",
        ),

    ]
)
class MonsterScript : KrulScript() {
    override fun createPainter(): KrulPaint<*> = MonsterPainter(this)
    val monsterName by lazy { getOption<String>("monsterName") }

    var hitSplats = intArrayOf()


    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "MonsterLeaf") {

        sleep(1000)
    }

    override fun onStart() {
        super.onStart()
        PowDispatchers.Script.launch {
            EventFlows.ticks().collect { onTick(it) }
        }
    }


    var monster = Npc.Nil
    var monsterTile = Tile.Nil
    var rect = Rectangle.Nil
    var myNextAttackTick = -1
    var boundingTiles = emptyArray<Tile>()
    var nearestOutsideTile = Tile.Nil
    private fun onTick(e: TickEvent) {
        monster = if (monster.valid()) {
            monster
        } else {
            val interacting = me.interacting()
            interacting as? Npc ?: Npc.Nil
        }
        if (monster.valid()) {
            val hits = monster.hitsplatCycles()
            if (hitSplats.isNotEmpty()) {
                if (hits[0] > hitSplats[0]) {
                    myNextAttackTick = ticks + 4
                }
                logger.info("Hits=${hits.joinToString()}, oldHits=${hitSplats.joinToString()}")
                hitSplats.zip(hits).forEachIndexed { i, hit ->
                    logger.info("$i: $hit")
                }
            }
            hitSplats = hits
        }

        val t = me.tile()
//        monsterTile = monster.trueTile()
//        rect = monster.rectangle(3)
//        boundingTiles = rect.borderingLayer()
//        nearestOutsideTile = boundingTiles.minByOrNull { it.distanceTo(t) } ?: t
    }

}

class MonsterPainter(script: MonsterScript) : KrulPaint<MonsterScript>(script) {


    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Hitsplats") { script.hitSplats.joinToString() }
            .addString("nextAttack") { "tick=${script.ticks}, next=${script.myNextAttackTick}" }
            .build()
    }

    override fun paintCustom(g: Rendering) {
        g.setColor(Color.RED)

        script.monsterTile.drawOnScreen(outlineColor = Color.CYAN)
        val borderTiles = script.boundingTiles
        borderTiles.forEach { it.drawOnScreen(outlineColor = GREEN) }

        script.nearestOutsideTile.drawOnScreen(outlineColor = Color.RED, fillColor = Color.RED)

    }
}

fun main() {
    MonsterScript().startScript("127.0.0.1", "GIM", true)
}
