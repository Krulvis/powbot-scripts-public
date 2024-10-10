package org.powbot.krulvis.tormenteddemon.tree.leaf

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class WalkToSpot(script: TormentedDemon) : Leaf<TormentedDemon>(script, "Walking to spot") {
	private val togTeleportDesination = Tile(2465, 3495, 0)

	private val demonArea = Area(Tile(4033, 4475), Tile(4160, 4350))
	private val chasmArea = Area(Tile(4035, 4610), Tile(4080, 4545))
	private val lightCreatureArea = Area(Tile(3210, 9535, 2), Tile(3229, 9524, 2))
	private val tearsOfGuthix = Area(Tile(3203, 9539, 2), Tile(3268, 9485, 2))

	override fun execute() {

		val spot = script.centerTile
		val tile = me.tile()
		if (tearsOfGuthix.contains(tile) || togTeleportDesination.distance() < 50 || chasmArea.contains(tile)) {
			script.togTeleport.executed = true
		}
		if (demonArea.contains(tile)) {
			script.logger.info("Already in demon area..")
			val path = LocalPathFinder.findPath(spot)
			if (path.isNotEmpty()) {
				script.bankTeleport.executed = false
				path.traverseUntilReached(0.0)
			} else {
				script.aggressionTimer.reset()
				Movement.builder(spot).setWalkUntil { spot.distance() < 25 }.move()
			}
			return
		}
		if (chasmArea.contains(tile)) {
			val floor = tile.floor()
			if (floor <= 1) {
				val wall = Objects.stream(50).name("Wall").action("Climb-up").nearest().first()
				if (!wall.valid()) {
					Movement.step(Tile(4065, 4555, 0))
				} else if (walkAndInteract(wall, "Climb-up")) {
					waitForDistance(wall) { me.floor() != floor }
				}
			} else {
				val opening = Objects.stream(50).name("Opening").action("Climb-through").nearest().first()
				if (walkAndInteract(opening, "Climb-through")) {
					waitForDistance(opening) { !chasmArea.contains(tile) }
				}
			}
		} else if (lightCreatureArea.contains(tile)) {
			if (!Chat.chatting()) {
				val attractSpot = Tile(3230, 9526, 2)
				val lightCreature = Npcs.stream().name("Light creature").nearest().first()
				if (lightCreature.distance() > 7 && attractSpot.distance() > 1) {
					Movement.step(attractSpot, 0)
				} else if (lightCreature.interact("Attract")) {
					waitFor(long()) { Chat.chatting() }
				}
			} else if (Chat.continueChat("Travel into the chasm.")) {
				waitFor(long()) { chasmArea.contains(me) }
			}
		} else if (tearsOfGuthix.contains(tile)) {
			val rockTile = Tile(3240, 9524, 2)
			val rocks = Objects.stream(rockTile, 5).name("Rock").action("Climb").nearest().first()
			if (!rocks.valid()) {
				Movement.walkTo(rockTile)
			} else if (walkAndInteract(rocks, "Climb")) {
				waitForDistance(rocks) { lightCreatureArea.contains(me) }
			}
		} else if (script.togTeleport.execute()) {
			Movement.walkTo(tearsOfGuthix.centralTile)
		}

	}
}