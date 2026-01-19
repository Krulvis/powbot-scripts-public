package org.powbot.krulvis.api.extensions.watcher

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Npc
import org.powbot.krulvis.api.ATContext.GARGOYLE_DESTROY_ANIM
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.gargoyle
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NpcDeathWatcher(val npc: Npc, private val onDeath: () -> Unit) : Watcher() {

	val logger = LoggerFactory.getLogger(javaClass.simpleName)
	val latch = CountDownLatch(1)
	val active get() = latch.count > 0

	init {
		GlobalScope.launch {
			awaitDeath()
			latch.countDown()
			unregister()
		}
	}

	fun awaitDeath() = latch.await(10, TimeUnit.SECONDS)

	@Subscribe
	fun onTickEvent(_e: TickEvent) {
		if (!npc.valid() && !npc.gargoyle()) {
			debug("Watching Npc=${npc} not valid anymore")
			latch.countDown()
		} else if (npc.dead()) {
			debug("NpcWatcher found death=$npc, valid=${npc.valid()}, animation=${npc.animation()}")
			onDeath()
			latch.countDown()
		} else {
//            debug("Watching NPC=${npc.name}, HP Visible=${npc.healthBarVisible()}, Percent=${npc.healthPercent()}")
		}
	}

}