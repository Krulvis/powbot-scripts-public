package org.powbot.krulvis.api.script

import com.google.common.eventbus.Subscribe
import org.powbot.api.Preferences
import org.powbot.api.Random
import org.powbot.api.event.RenderEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.script.tree.TreeScript
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.randoms.BondPouch
import org.powbot.krulvis.api.extensions.randoms.RandomHandler
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.mobile.drawing.Rendering

abstract class KrulScript : TreeScript() {

	override fun onStart() {
		logger.info("Starting..")
		addPaint(painter.buildPaint(painter.paintBuilder))
		val username = Preferences.getString("username")
		logger.info("Username: $username")
	}

	val painter by lazy { createPainter() }

	abstract fun createPainter(): ATPaint<*>

	var ticks = -1

	@Subscribe
	fun onTickTimer(e: TickEvent) {
		ticks++
	}

	fun waitForTicks(ticks: Int = 1): Boolean {
		val endTick = this.ticks + ticks
		val startTimer = System.currentTimeMillis()
		val waited = waitFor(ticks * 600) { this.ticks >= endTick }
		logger.info("waitForTicks($ticks) took ${System.currentTimeMillis() - startTimer} ms")
		return waited
	}


	val timer = Timer()
	val oddsModifier = OddsModifier()
	val walkDelay = DelayHandler(500, 700, oddsModifier, "Walk Delay")
	var nextRun: Int = Random.nextInt(1, 6)
	val randomHandlers = mutableListOf<RandomHandler>(BondPouch())

	override fun poll() {
		val rh = randomHandlers.firstOrNull { it.validate() }
		if (rh != null) rh.execute() else super.poll()
	}

	@Subscribe
	fun onRender(e: RenderEvent) {
		painter.paintCustom(Rendering)
	}

}
