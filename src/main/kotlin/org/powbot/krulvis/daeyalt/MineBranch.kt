package org.powbot.krulvis.daeyalt

import org.powbot.api.Condition
import org.powbot.api.Production
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.miner.Data.SPECIAL_ATTACK_PICKS
import kotlin.random.Random




class NextToMine(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "NextToMine?") {
	override val successComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "WalkToMine") {
		if (script.tickManip || walkTimer?.isFinished() == true) {
			Movement.step(script.mine)
			if (waitForDistance(script.mine) { script.mine.distance() < 5 })
				walkTimer = null
		}
	}
	override val failedComponent: TreeComponent<DaeyaltMiner> = ShouldSpecial(script)

	var walkTimer: Timer? = null
	override fun validate(): Boolean {
		script.mine = Objects.stream().name("Daeyalt Essence").action("Mine").nearest().first()
		if (script.mine.distance() > 5) {
			if (walkTimer == null) {
				walkTimer = Timer(Random.nextInt(1000, 5000))
			}
			return true
		}
		return false
	}

}

class ShouldSpecial(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "ShouldSpecial?") {
	override val successComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "Special") {
		if (Combat.specialAttack(true))
			specTimer = null
	}
	override val failedComponent: TreeComponent<DaeyaltMiner> = ShouldTickManip(script)

	var specTimer: Timer? = null
	override fun validate(): Boolean {
		if (!script.special) return false
		if (Equipment.stream().id(*SPECIAL_ATTACK_PICKS).isEmpty()) return false
		if (Combat.specialPercentage() == 100 && specTimer == null) {
			specTimer = Timer(Random.nextInt(5000, 25000))
		}
		return specTimer != null && specTimer!!.isFinished()
	}

}


class ShouldTickManip(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "ShouldTickManip?") {
	override val failedComponent: TreeComponent<DaeyaltMiner> = ShouldMine(script)
	override val successComponent: TreeComponent<DaeyaltMiner> = TickManip(script)

	override fun validate(): Boolean {
		return script.tickManip && script.canTickManip()
	}

}

class ShouldMine(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "ShouldMine?") {
	override val successComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "Mine") {
		val mine = Objects.stream().name("Daeyalt Essence").action("Mine").firstOrNull() ?: return@SimpleLeaf
		if (mine.distance() > 5) {
			Movement.step(mine.tile)
			Condition.wait({ mine.distance() <= 5 }, 500, 10)
		} else if (Utils.walkAndInteract(mine, "Mine")) {
			if (!script.tickManip) {
				Condition.wait({ !Production.stoppedMaking(ESSENCE) }, 500, 10)
				return@SimpleLeaf
			}
			val tar = Inventory.stream().name("Swamp tar").first()
			if (tar.valid()) {
				tar.interact("Use")
			}
			waitFor { me.animationCycle() > 50 || script.gameTick > script.startTick + 2 }
		}
	}
	override val failedComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "Chill") {
		sleep(600)
	}

	override fun validate(): Boolean =
		Production.stoppedMaking(ESSENCE) || script.tickManip


}
