package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.RenderEvent
import org.powbot.api.rt4.Varpbits
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.mobile.drawing.Rendering

@ScriptManifest("testVarps", "", priv = true)
class TestVarpbits : AbstractScript() {

	var varps = emptyArray<Int>()

	var changedVarps = mutableListOf<Triple<Int, Int, Int>>()
	val skip = arrayOf(3078, 3079, 3077)

	override fun poll() {
		val newVarps = Varpbits.array().clone()
		if (varps.isNotEmpty()) {
			newVarps.forEachIndexed { i, v ->
				if (i !in skip && v != varps[i]) {
					logger.info("$i changed from ${varps[i]} to $v")
					changedVarps.add(Triple(i, varps[i], v))
				}
			}
		}
		varps = newVarps
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
}


fun main() {
	TestVarpbits().startScript("127.0.0.1", "GIM", true)
}