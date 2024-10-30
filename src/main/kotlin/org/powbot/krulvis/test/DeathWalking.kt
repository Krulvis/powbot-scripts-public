package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.event.VarpbitChangedEvent
import org.powbot.api.rt4.Varpbits
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Death

@ScriptManifest("deathwalker", "Loots gravestone after death", priv = true)
class DeathWalking : AbstractScript() {

	var deathTile = Tile.Nil
	var ticks = 0


	@Subscribe
	fun onTick(e: TickEvent) {
		ticks++
		if (me.healthPercent() == 0) {
			logger.info("Died on tick=$ticks")
			deathTile = me.tile()
		}
	}

	val skip = intArrayOf(3079, 3077)
	val gravestone = 843

	val graveTile = Tile(3082, 9961, 0)

	private fun Int.transformToTile(): Tile {
		val x = (shr(14) and 0x3FFF)
		val y = (this and 0x3FFF)
		val z = shr(28) and 0x3
		return Tile(x, y, z)
	}

	@Subscribe
	fun varpChange(e: VarpbitChangedEvent) {
		if (e.index !in skip) {
			logger.info("tick=${ticks}: index=${e.index}, newValue=${e.newValue}, oldValue=${e.previousValue}")
		}
	}

	override fun onStart() {
		addPaint(PaintBuilder.newBuilder().addString("Time remaining") {
			val remain = Death.timeRemaining()
			val minutes = remain / 60
			val seconds = remain.mod(60.0)
			"$remain- ${minutes.toInt()}:${seconds.toInt()}"
		}.build())
	}

	override fun poll() {
//		logger.info("${ticks}: timer=$timer, ticks=${timer / 600}")
//		val ti = Varpbits.varpbit(3916).transformToTile()
//		logger.info("tile=$ti")
//		Varpbits.array().forEachIndexed { i, v ->
//			val tile = v.transformToTile()
//			if (tile.distanceTo(graveTile) < 10) {
//				logger.info("Found candidate=$i")
//			}
//		}
	}
}

fun main() {
	DeathWalking().startScript()
}