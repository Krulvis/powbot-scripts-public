package org.powbot.krulvis.api.script

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.ProjectileDestinationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptState
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.mobile.script.ScriptManager

abstract class KillerScript(val dodgeProjectiles: Boolean = true) : KrulScript(), Looting {

	abstract val ammoIds: IntArray
	var currentTarget: Npc = Npc.Nil
	var lootWachter: LootWatcher? = null
	val deathWatchers = mutableListOf<NpcDeathWatcher>()
	var kills: Int = 0
	private val slayerBraceletNames = arrayOf("Bracelet of slaughter", "Expeditious bracelet")
	fun getSlayerBracelet() = Inventory.stream().name(*slayerBraceletNames).first()
	val hasSlayerBracelet by lazy { getSlayerBracelet().valid() }
	fun wearingSlayerBracelet() = Equipment.stream().name(*slayerBraceletNames).isNotEmpty()

	var reducedStats = false

	override val ironmanLoot = mutableListOf<GroundItem>()
	abstract fun GroundItem.isLoot(): Boolean

	fun isLootWatcherActive() = lootWachter?.active == true
	private fun watchLootDrop(tile: Tile) {
		if (!isLootWatcherActive()) {
			logger.info("Waiting for loot at $tile")
			lootWachter = LootWatcher(tile, ammoIds, lootList = ironmanLoot, isLoot = { it.isLoot() })
		} else {
			logger.info("Already watching loot at tile: $tile for loot")
		}
	}

	open fun onDeath(npc: Npc) {
		kills++
		reducedStats = false
		if (hasSlayerBracelet && !wearingSlayerBracelet()) {
			val slayBracelet = getSlayerBracelet()
			if (slayBracelet.valid()) {
				getSlayerBracelet().fclick()
				logger.info("Wearing bracelet on death at ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
			}
		}
		watchLootDrop(npc.tile())
	}

	private fun setCurrentTarget() {
		val interacting = me.interacting()
		if (interacting is Npc && interacting != Npc.Nil) {
			currentTarget = interacting
			val activeLW = lootWachter
			if (activeLW?.active == true && activeLW.tile.distanceTo(currentTarget.tile()) < 2) return
			val deathWatcher = deathWatchers.firstOrNull { it.npc == currentTarget }
			if (deathWatcher == null || !deathWatcher.active) {
				val newDW = NpcDeathWatcher(interacting) { onDeath(interacting) }
				deathWatchers.add(newDW)
			}
		}
		deathWatchers.removeAll { !it.active }
	}

	@Subscribe
	fun onKillerTickEvent(_e: TickEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		setCurrentTarget()

		projectiles.forEach {
			if (Game.cycle() > it.cycleEnd) {
				projectiles.remove(it)
			}
		}
	}


	val projectiles: MutableList<Projectile> = mutableListOf()
	var projectileSafespot: Tile = Tile.Nil
	var melee = true

	private fun findSafeSpotFromProjectile(target: Npc) {
		val dangerousTiles = projectiles.map { it.destination() }
		val targetTile = target.tile()
		val centerTile = if (melee) targetTile else me.tile()
		val distanceToTarget = targetTile.distance()
		val collisionMap = Movement.collisionMap(centerTile.floor).collisionMap.flags
		val grid = mutableListOf<Pair<Tile, Double>>()
		for (x in -2 until 2) {
			for (y in -2 until 2) {
				val t = Tile(centerTile.x + x, centerTile.y + y, centerTile.floor)
				if (t.blocked(collisionMap) || (!melee && target.distanceTo(t) < 1)) continue
				//If we are further than 5 tiles away, make sure that the tile is closer to the target so we don't walk further away
				if (distanceToTarget <= 5 || t.distanceTo(targetTile) < distanceToTarget) {
					grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
				}
			}
		}
		projectileSafespot = grid.maxByOrNull { it.second }!!.first
	}

	@Subscribe
	fun onKillerProjectile(e: ProjectileDestinationChangedEvent) {
		if (e.target() == Actor.Nil && dodgeProjectiles) {
			val myDest = Movement.destination()
			val tile = if (myDest.valid()) myDest else me.tile()
			val dest = e.destination()
			if (dest == tile) {
				logger.info("Dangerous projectile spawned! tile=${e.destination()}")
				projectiles.add(e.projectile)
				findSafeSpotFromProjectile(currentTarget)
				val targetTile = ironmanLoot.firstOrNull { it.valid() && it.tile.valid() && it.tile != dest }?.tile
					?: projectileSafespot
				Movement.step(targetTile, 0)
			}
		} else if (melee && e.target() == currentTarget) {
			logger.info("Found projectile going to currentTarget, setting style to NOT melee")
			melee = false
		}
	}

}