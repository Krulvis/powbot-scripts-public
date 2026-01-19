package org.powbot.krulvis.gwd

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.RenderEvent
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.mobile.drawing.Rendering

@ScriptManifest("krul GWDDebug", "Debugs GWD", priv = true)
class GWDDebug : AbstractScript() {
	val god = GWD.God.Saradomin

	val zilyanaTiles = arrayOf(
		Tile(2906, 5272, 0),
		Tile(2906, 5266, 0),
		Tile(2900, 5258, 0),
		Tile(2896, 5258, 0),
		Tile(2890, 5264, 0),
		Tile(2891, 5269, 0),
		Tile(2896, 5275, 0),
		Tile(2901, 5274, 0),
	)

	val deltas = arrayOf(
		intArrayOf(-8, -5),
		intArrayOf(-8, 1),
		intArrayOf(-2, 9),
		intArrayOf(2, 9),
		intArrayOf(8, 3),
		intArrayOf(7, -2),
		intArrayOf(2, -8),
		intArrayOf(-3, -7)
	)

	var tiles = listOf<Tile>()

	override fun poll() {
		god.calculateGeneralArea()
		val center = god.generalArea.centralTile
		tiles = deltas.map { Tile(center.x - it[0], center.y - it[1]) }
		sleep(50)
	}

	override fun onStart() {
		super.onStart()
		addPaint(
			PaintBuilder.newBuilder()
				.addString("Animation") { me.animation().toString() }.build()
		)
	}

	@Subscribe
	fun onRender(e: RenderEvent) {
		val g = Rendering
		val area = god.generalArea._tiles
		val floor = tiles[0].floor
		g.setColor(Color.CYAN)
		area.forEach { it.drawOnScreen(outlineColor = Color.RED) }
		tiles.forEachIndexed { i, it -> it.drawOnScreen(i.toString(), outlineColor = Color.GREEN) }
		zilyanaTiles.forEachIndexed { i, it -> it.drawOnScreen(i.toString(), outlineColor = Color.ORANGE) }
	}
}

fun main() {
	GWDDebug().startScript("127.0.0.1", "GIM", false)
}