package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.disablePrayers
import org.powbot.krulvis.api.ATContext.distanceToDest
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data
import org.powbot.krulvis.araxxor.Data.crawlingTunnel
import org.powbot.krulvis.araxxor.Data.lairArea
import org.powbot.krulvis.araxxor.Data.outsideLairArea
import org.powbot.krulvis.araxxor.Data.spiderDungeonArea
import org.powbot.krulvis.araxxor.Data.webTunnelTile

class InLair(script: Araxxor) : Branch<Araxxor>(script, "InLair") {
	override val failedComponent: TreeComponent<Araxxor> = InSpiderDungeon(script)
	override val successComponent: TreeComponent<Araxxor> = ShouldPray(script)

	override fun validate(): Boolean {
		if (lairArea.contains(me)) {
			script.inside = true
		}
		if (crawlingTunnel()) {
			script.inside = true
		}
		return script.inside
	}
}

class InSpiderDungeon(script: Araxxor) : Branch<Araxxor>(script, "InSpiderDungeon") {

	override val failedComponent: TreeComponent<Araxxor> = OutsideDungeon(script)
	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "EnterLair") {
		val web = Objects.stream(webTunnelTile, GameObject.Type.INTERACTIVE).name("Web tunnel")
			.action("Squeeze-through").first()
		if (web.distance() > 12) {
			Movement.step(webTunnelTile)
		} else if (walkAndInteract(web, "Squeeze-through")) {
			waitForDistance(web, extraWait = 1200) {
				lairArea.contains(me) || crawlingTunnel()
			}
		}
	}

	override fun validate(): Boolean {
		return spiderDungeonArea.contains(me)
	}
}

class OutsideDungeon(script: Araxxor) : Branch<Araxxor>(script, "OutsideDungeon") {
	override val failedComponent: TreeComponent<Araxxor> = OnTopof(script)

	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "EnterDungeon") {
		val cave = Objects.stream(Tile(3658, 3409, 0), GameObject.Type.INTERACTIVE).name("Cave").action("Enter").first()
		Prayer.disablePrayers()
		if (cave.distance() < 10) {
			if (walkAndInteract(cave, "Enter")) {
				waitForDistance(cave, extraWait = 2400) {
					spiderDungeonArea.contains(me)
				}
			}
		} else {
			Data.outsidePath.traverse()
		}
	}

	override fun validate(): Boolean {
		return outsideLairArea.contains(me)
	}
}

class OnTopof(script: Araxxor) : Branch<Araxxor>(script, "OnTopOfWall") {
	override val failedComponent: TreeComponent<Araxxor> = InDarkmeyer(script)

	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "ClimbDownWall") {
		val wall = Objects.stream(Tile(3672, 3375, 0), GameObject.Type.INTERACTIVE).name("Wall").action("Climb").first()
		Prayer.disablePrayers()
		if (walkAndInteract(wall, "Climb")) {
			sleep(1500)
		}
	}

	override fun validate(): Boolean {
		return Data.wallArea.contains(me)
	}
}

class InDarkmeyer(script: Araxxor) : Branch<Araxxor>(script, "InDarkmeyer") {
	override val failedComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "TeleportToDarkmeyer") {
		script.araxTeleport.execute()
	}

	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "ClimbWall") {
		val wall = Objects.stream(Tile(3669, 3375, 0), GameObject.Type.INTERACTIVE).name("Wall").action("Climb").first()
		if (wall.distance() < 10) {
			if (walkAndInteract(wall, "Climb")) {
				waitForDistance(wall, extraWait = 1200) {
					Data.wallArea.contains(me)
				}
			}
		} else {
			Data.darkmeyerPath.traverse {
				Prayer.prayer(
					Prayer.Effect.PROTECT_FROM_MELEE,
					Npcs.stream().nameContains("Vyrewatch").any { it.distance() < 6 }
				)
			}
		}
	}

	override fun validate(): Boolean {
		return Data.darkmeyerArea.contains(me)
	}
}