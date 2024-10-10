package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.data.model.cloud.Message
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul Test Loop", version = "1.0.1", description = "", priv = true)
class LoopScript : AbstractScript() {

	var collisionMap: Array<IntArray> = emptyArray()
	var npc: Npc = Npc.Nil
	var projectiles = emptyList<Projectile>()

	val STEPS =
		arrayOf(
			"Select Melee",
			"Select another Melee",
			"Select Range",
			"Select another Range",
			"Select Mage",
			"Select another Mage",
			"Compare"
		)
	var step = 0


	var mobCache: Array<Map<Int, Int>> = arrayOf(mapOf(), mapOf(), mapOf())

	@Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
		if (e is NpcActionEvent) {
			npc = e.npc()
			logger.info("Interacted with npc=${npc.name()}, step=${step}")
			val values = readMemory(npc)
			if (step == 0) {
				logger.info("Initializing values")
				mobCache[0] = values
				step++
				return
			}
			val cacheIndex = (step - 1) / 2
			val oldValues = mobCache[cacheIndex]
			logger.info("Getting cacheIndex=$cacheIndex, oldValues.Size=${oldValues.size}")
			when (step) {
				1, 3, 5 -> {
					mobCache[cacheIndex] = values.filter { it.key in oldValues.keys }
						.filter { it.value != -1 && it.value == oldValues.getOrDefault(it.key, -1) }
					logger.info("Getting ${mobCache[cacheIndex].size} values that are the same, -1s=${oldValues.filterValues { it == -1 }.size}")
				}

				2, 4 -> {
					val sameKeys = values.filter { it.key in oldValues.keys }
					logger.info("Values with same keys = ${sameKeys.size}, -1s=${oldValues.filterValues { it == -1 }.size}")
					val newValues = sameKeys.filter { it.value != oldValues.getOrDefault(it.key, -1) }
					logger.info("Values that are not the same: ${newValues.size}")
					mobCache[cacheIndex + 1] = newValues
				}
			}
			if (step < STEPS.size - 1) {
				step++
			}
			if (step == STEPS.size - 1) {
				val latestMob = mobCache[mobCache.size - 1]
				logger.info("Comparing")
				latestMob.forEach { (k, v) ->
					logger.info("$k: [${mobCache[0][k]}, ${mobCache[1][k]}, $v]")
				}
			}
		}
	}

	@Subscribe
	fun onProject(e: ProjectileDestinationChangedEvent) {
		if (e.target() == Actor.Nil) {
			logger.info("ProjectileEvent id=${e.id}, destination=${e.destination()}")
		}
	}

	@Subscribe
	fun onMessage(e: MessageEvent) {
		logger.info("Received message: type=${e.messageType}, m=${e.message}")
	}

	@Subscribe
	fun onTick(e: TickEvent) {
		val projectiles = Projectiles.stream().filtered { it.target() == Actor.Nil }.toList()
		val npc = Npcs.stream().name("Tormented demon").nearest().first()

		if (projectiles.isNotEmpty()) {
			logger.info("Found (${projectiles.size}) projectiles without target")
		}
	}


	fun readMemory(npc: Npc): Map<Int, Int> {
		logger.info("Reading memory")
		var x = 0
		val map = mutableMapOf<Int, Int>()
		while (x < 1000) {
			map[x] = getMemValue(npc, x)
			x += 4
		}
		return map
	}


	val prayIndex = arrayOf(-604367896, -602369656, -597684280)
	fun getMemValue(npc: Npc, x: Int): Int {
//		val rnpc = RNpcs.npc(npc.index()).get()
////		val base = rnpc.baseAddrLong()
//		val base = npc.address()
//		val cb = RMemory.readLong(base + 1000, false)
//		val index = RMemory.readLong(cb + x, false)
//		val value = RMemory.readInt(index, false)
//		return value
		return -1
	}

	fun getPrayerId(npc: Npc, x: Int): Int {
		val value = getMemValue(npc, x)
		return prayIndex.indexOf(value)
	}

	override fun poll() {
		val gorillas = Npcs.stream().name("Demonic gorilla").toList()
		logger.info("Gorillas")
		gorillas.groupBy { it.prayerHeadIconId() }.forEach {
			logger.info("Gorilla overHead=${it.key}, ids=[${it.value.joinToString { g -> g.id.toString() }}]")
		}
		val npc = npc
		if (npc.valid()) {
			Condition.sleep(5000)
		}
	}

	//304, 568, 576, 640, 664, 712

	@Subscribe
	fun onRender(e: RenderEvent) {
		val g = Rendering
		val x = 20
		var y = 100
		g.drawString("Step=${STEPS[step]}", x, y)
		y += 15
		val npc = npc
		if (npc.valid()) {
			val memValue = getMemValue(npc, 304)
			val prayIndex = prayIndex.indexOf(memValue)
			g.drawString("${npc.name}: id=${npc.id}, MemValue=${memValue}, Prayer=${prayIndex}", x, y)
		}
		y += 15
	}

}

fun main() {
	LoopScript().startScript("127.0.0.1", "GIM", true)
}
