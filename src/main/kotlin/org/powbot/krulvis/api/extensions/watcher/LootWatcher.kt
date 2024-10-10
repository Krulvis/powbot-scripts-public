package org.powbot.krulvis.api.extensions.watcher

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.getPrice
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.round

class LootWatcher(val tile: Tile, private val ignoreIds: IntArray, private val radius: Int = 4, private val lootList: MutableList<GroundItem>, private val isLoot: (GroundItem) -> Boolean) :
	Watcher() {

	private val logger = LoggerFactory.getLogger(javaClass.simpleName)
	private val latch = CountDownLatch(1)
	private val startLoot = groundItems()
	private val loot = mutableListOf<GroundItem>()

	val active get() = latch.count >= 1

	init {
		val startMilis = System.currentTimeMillis()
		GlobalScope.launch {
			lootList.addAll(waitForLoot())
			val lootNames = loot.joinToString { it.name() }
			logger.info("Waiting for loot took: ${round((System.currentTimeMillis() - startMilis) / 100.0) / 10.0} seconds, found loot=[${lootNames}]")
			latch.countDown()
			unregister()
		}
	}

	private fun groundItems() = GroundItems.stream().within(tile, radius).list()

	@com.google.common.eventbus.Subscribe
	fun onTickEvent(_e: TickEvent) {
		val groundItems = groundItems()
		val newItems = groundItems.filterNot { it in startLoot }
		if (newItems.count { it.id() !in ignoreIds } > 0) {
			val newLoot = newItems.filter(isLoot)
			debug("New groundItems found: [${newItems.joinToString { it.name() + ": " + it.getPrice() * it.stackSize() }}], loot: [${newLoot.joinToString { it.name() }}]")
			loot.addAll(newLoot)
			latch.countDown()
		}
	}

	fun waitForLoot(): List<GroundItem> {
		latch.await(10, TimeUnit.SECONDS)
		return loot
	}

}