package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color
import org.powbot.api.Condition.sleep
import org.powbot.api.Tile
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.ProjectileDestinationChangedEvent
import org.powbot.api.event.RenderEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.me
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul Test Projectiles", version = "1.0.1", description = "", priv = true)
class ProjectileScript : AbstractScript() {

	var projectiles = mutableListOf<Pair<Projectile, Long>>()
	var safeTile = Tile.Nil
	val projectileDuration = 2400
	var prayHeadIcon = -1
	var currentTarget = Npc.Nil

	@Subscribe
	fun onProjectile(e: ProjectileDestinationChangedEvent) {
//		logger.info("ProjectileDestinationChangedEvent(ms=${System.currentTimeMillis()}, id=${e.id}, target=${e.target()}, destination=${e.destination()}, tile=${e.projectile.tile()}, valid=${e.projectile.valid()})")
		if (e.target() == Actor.Nil) {
			projectiles.add(e.projectile to System.currentTimeMillis())
		}
	}

	@Subscribe
	fun onTick(e: TickEvent) {
		val projectiles = Projectiles.stream().toList()
		val me = Players.local()
		val myTile = me.tile()
//		logger.info("Projectiles=${projectiles.count()}, onMe=${projectiles.count { it.target() == me }}, onMyTile=${projectiles.count { it.destination() == myTile }}")
		safeTile = findSafeTile()
		val interacting = me.interacting()
		if (interacting.valid() && interacting is Npc) {
			currentTarget = interacting
			prayHeadIcon = interacting.prayerHeadIconId()
		}
	}

	@Subscribe
	fun onNpcAnim(anim: NpcAnimationChangedEvent) {
		if (currentTarget == anim.npc) {
			logger.info("New Animation = ${anim.animation}")
		}
	}

	override fun poll() {
		sleep(600)
//		if (projectiles.isNotEmpty()) {
//			logger.info("There is dangerous projectile!")
//			projectiles.forEach { (p, t) -> logger.info("Proj(destination=${p.destination()}, tile=${.tile()}, distance=${it.distance()}, valid=${it.valid()})") }
//		}
	}

	@Subscribe
	fun onRender(e: RenderEvent) {
		var y = 100
		val x = 20
		val projs = projectiles.map { it.first }
		projs.forEach { p ->
			p.tile().drawOnScreen(outlineColor = Color.ORANGE, text = p.valid().toString())
			p.destination().drawOnScreen(outlineColor = Color.RED)
			Rendering.drawString("id=${p.id}, valid=${p.valid()}", x, y)
			y += 15
		}
		Rendering.drawString("Prayer=${prayHeadIcon}", x, y)
		if (safeTile.valid()) {
			safeTile.drawOnScreen(outlineColor = Color.GREEN)
		}
	}

	fun findSafeTile(): Tile {
		val time = System.currentTimeMillis()
		projectiles.forEach { (p, t) ->
			if (time - t > projectileDuration) projectiles.remove(p to t)
		}
		val myTile = me.tile()
		val dangerousTiles = projectiles.map { it.first.destination() }
		if (dangerousTiles.isEmpty()) return myTile

		val collisionMap = Movement.collisionMap(myTile.floor).collisionMap.flags
		val grid = mutableListOf<Pair<Tile, Double>>()
		for (x in -2 until 2) {
			for (y in -2 until 2) {
				val t = Tile(myTile.x + x, myTile.y + y, myTile.floor)
				if (!t.blocked(collisionMap)) {
					grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
				}
			}
		}
		return grid.maxByOrNull { it.second }?.first ?: Tile.Nil
	}

}

fun main() {
	ProjectileScript().startScript("127.0.0.1", "GIM", true)
}
