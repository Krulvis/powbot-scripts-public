package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.RenderEvent
import org.powbot.api.rt4.Varpbits
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.turnRunOn
import org.powbot.krulvis.api.extensions.items.RunePouchLoadout
import org.powbot.mobile.drawing.Rendering

@ScriptManifest("testVarps", "", priv = true)
class TestVarpbits : AbstractScript() {

    var varps = intArrayOf()

    var changedVarps = mutableListOf<Triple<Int, Int, Int>>()
    val skip = arrayOf(3078, 3079, 3077)

    override fun poll() {
        turnRunOn()
        val newVarps = Varpbits.array().clone()
        if (varps.isNotEmpty()) {
            newVarps.forEachIndexed { i, v ->
                if (i !in skip && v != varps[i]) {
                    logger.warn("$i changed from ${varps[i]} to $v")
                    changedVarps.add(Triple(i, varps[i], v))
                }
            }
        }
        varps = newVarps
    }

    override fun onStart() {
        addPaint(
            PaintBuilder()
                .addString("E") { RunePouchLoadout.E.toString() }
                .build()
        )
    }

    @Subscribe
    fun onRenderEvent(e: RenderEvent) {
        val g = Rendering
        val x = 10
        val yy = 15
        var y = 100
        val last = changedVarps.takeLast(5)
        last.forEach {
            g.drawString("${it.first} changed from ${it.second} to ${it.third}", x, y)
            y += yy
        }
    }

    // E 2nd rune Water  -> None
    //[INFO] 4858 changed from 524288 to 0
    //[INFO] 4865 changed from 536870912 to 0

    // E 1st rune None -> Air 1
//    [INFO] 4864 changed from 0 to 268435456
//    [INFO] 4857 changed from 0 to 262144

    // E 1st rune Air 1 -> Air 2
//    [INFO] 4857 changed from 262144 to 0
//    [INFO] 4857 changed from 0 to 524288

    // E 1st rune Air 2 -> Air 3
//    4857 changed from 524288 to 786432

    // E 1st rune Air 3 -> Water 3
//    [INFO] 4857 changed from 786432 to 0
//    [INFO] 4864 changed from 268435456 to 536870912
//    [INFO] 4857 changed from 0 to 786432

    // E 2nd rune None -> Air 1
    // 4865 changed from 0 to 268435456 //Rune type
    // 4858 changed from 0 to 262144
}


fun main() {
    TestVarpbits().startScript("127.0.0.1", "GIM", true)
}