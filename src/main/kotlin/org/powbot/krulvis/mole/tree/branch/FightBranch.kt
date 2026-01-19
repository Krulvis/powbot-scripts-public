package org.powbot.krulvis.mole.tree.branch

import org.powbot.api.Area
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mole.GiantMole
import org.powbot.krulvis.mole.tree.leaf.*

val moleArea = Area(Tile(1700, 5130, 0), Tile(1800, 5250, 0))

class AtMole(script: GiantMole) : Branch<GiantMole>(script, "Should Sip Potion?") {

	override val failedComponent: TreeComponent<GiantMole> = GoToMole(script)
	override val successComponent: TreeComponent<GiantMole> = WaitingForLoot(script)

	override fun validate(): Boolean {
		return moleArea.contains(me)
	}
}

class WaitingForLoot(script: GiantMole) : Branch<GiantMole>(script, "Waiting for loot?") {
	override val failedComponent: TreeComponent<GiantMole> = NearMole(script)
	override val successComponent: TreeComponent<GiantMole> = WaitForLoot(script)

	override fun validate(): Boolean {
		if (currentHP() > 1) {
			val rock = Inventory.stream().name("Dwarven rock cake").first()
			if (rock.valid()) {
				rock.interact("Guzzle")
			}
		} else if (script.rapidHealTimer.isFinished()) {
			Prayer.prayer(Prayer.Effect.RAPID_HEAL, true)
			sleep(200, 300)
			script.rapidHealTimer.reset(Random.nextInt(10000, 45000))
		} else if (Prayer.prayerActive(Prayer.Effect.RAPID_HEAL)) {
			Prayer.prayer(Prayer.Effect.RAPID_HEAL, false)
			waitFor(600) { !Prayer.prayerActive(Prayer.Effect.RAPID_HEAL) }
		}

		return script.lootWatcher?.active == true
	}

}

class NearMole(script: GiantMole) : Branch<GiantMole>(script, "Is near mole?") {

	override val failedComponent: TreeComponent<GiantMole> = LookForMole(script)
	override val successComponent: TreeComponent<GiantMole> = ShouldSipPotion(script, IsFighting(script))

	override fun validate(): Boolean {
		return script.findMole().distance() < 20
	}
}

class IsFighting(script: GiantMole) : Branch<GiantMole>(script, "Is Fighting?") {

	override val failedComponent: TreeComponent<GiantMole> = Attack(script)
	override val successComponent: TreeComponent<GiantMole> = ProtectMelee(script)

	override fun validate(): Boolean {
		return me.interacting() == script.findMole()
	}
}

class ProtectMelee(script: GiantMole) : Branch<GiantMole>(script, "Is Protect on?") {

	override val failedComponent: TreeComponent<GiantMole> = SimpleLeaf(script, "Put Protect On") {
		if (Prayer.prayer(Prayer.Effect.PROTECT_FROM_MELEE, true)) {
			waitFor(1000) { Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MELEE) }
		}
	}
	override val successComponent: TreeComponent<GiantMole> = FlickOffensive(script)

	override fun validate(): Boolean {
		return Prayer.activePrayers().contains(Prayer.Effect.PROTECT_FROM_MELEE)
	}
}