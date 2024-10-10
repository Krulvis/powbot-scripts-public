package org.powbot.krulvis.api.extensions.watcher

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Npc
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.round

class SpecialAttackWatcher(val npc: Npc, val onHit: (Boolean) -> Unit) :
	Watcher() {

	private val logger = LoggerFactory.getLogger(javaClass.simpleName)
	private val latch = CountDownLatch(1)

	val active get() = latch.count >= 1
	var startHp = npc.healthPercent()
	val startSplat = npc.hitsplatCycles()
	var startSpecial = Combat.specialPercentage()
	var hit = false

	init {
		val startMilis = System.currentTimeMillis()
		GlobalScope.launch {
			waitForHit()
			logger.info("Waiting attack: ${round((System.currentTimeMillis() - startMilis) / 100.0) / 10.0}, hit=$hit")
			latch.countDown()
			unregister()
			onHit(hit)
		}
	}

	@Subscribe
	fun onTickEvent(_e: TickEvent) {
		if (!npc.hitsplatCycles().contentEquals(startSplat)) {
			hit = npc.healthPercent() < startHp
			latch.countDown()
		}
	}

	fun waitForHit() {
		latch.await(10, TimeUnit.SECONDS)
	}
}