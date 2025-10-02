package org.powbot.krulvis.test

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.Utils.sleep

@ScriptManifest("FindEntity", "Finds and prints nearest obj.", "Krulvis", priv = true)
class FairyRingTile : AbstractScript() {

	lateinit var ring: GameObject
	override fun poll() {
		ring = Objects.stream().nameContains("Log balance").action("Walk-across").nearest().first()
		logger.info(ring.toString())
		sleep(1000)
	}

	override fun onStart() {
		addPaint(PaintBuilder.newBuilder()
			.build())
	}
}

fun main() {
	FairyRingTile().startScript()
}