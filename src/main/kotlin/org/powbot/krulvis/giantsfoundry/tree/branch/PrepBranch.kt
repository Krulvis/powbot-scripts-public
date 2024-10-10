package org.powbot.krulvis.giantsfoundry.tree.branch

import org.powbot.api.Random
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.MouldType
import org.powbot.krulvis.giantsfoundry.crucibleBars
import org.powbot.krulvis.giantsfoundry.crucibleCount
import org.powbot.krulvis.giantsfoundry.tree.leaf.FillCrucible
import org.powbot.krulvis.giantsfoundry.tree.leaf.GetAssignment
import org.powbot.krulvis.giantsfoundry.tree.leaf.SetupMoulds
import org.powbot.krulvis.giantsfoundry.tree.leaf.TakeMetalFromBank

class HasAssignment(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Has assignment?") {
	override val failedComponent: TreeComponent<GiantsFoundry> = GetAssignment(script)
	override val successComponent: TreeComponent<GiantsFoundry> = CanTakeSword(script)

	override fun validate(): Boolean {
		return script.hasCommission()
	}
}

class CanTakeSword(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Can take sword?") {
	override val failedComponent: TreeComponent<GiantsFoundry> = HasSetupMoulds(script)
	override val successComponent: TreeComponent<GiantsFoundry> = SimpleLeaf(script, "Take Sword") {
		val jig = script.jig()
		if (script.interactObj(jig, "Pick-up")) {
			waitFor(long()) { script.isSmithing() }
		}
	}


	override fun validate(): Boolean {
		return script.areBarsPoured()
	}
}

class HasSetupMoulds(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Has best moulds?") {
	override val failedComponent: TreeComponent<GiantsFoundry> = SetupMoulds(script)
	override val successComponent: TreeComponent<GiantsFoundry> = IsCrucibleFull(script)

	override fun validate(): Boolean {
		return MouldType.selectedAll() && script.jig().name != "Mould jig (Empty)"
	}
}

class IsCrucibleFull(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Is crucible full?") {
	override val failedComponent: TreeComponent<GiantsFoundry> = HasCrucibleItems(script)
	override val successComponent: TreeComponent<GiantsFoundry> = SimpleLeaf(script, "Pour crucible") {
		val crucible = Objects.stream(30)
			.type(GameObject.Type.INTERACTIVE)
			.name("Crucible (full)").firstOrNull() ?: return@SimpleLeaf
		if (script.interactObj(crucible, "Pour")) {
			val jig = script.jig()
			if (jig.valid() && script.POURING_TILE.distance() <= 1) {
				sleep(Random.nextInt(1000, 1500))
				Movement.step(jig.tile)
			}
			waitFor(long()) { script.areBarsPoured() }
		}
	}

	override fun validate(): Boolean {
		return crucibleBars().toList().sumOf { it.second } >= 28
	}
}

class HasCrucibleItems(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Has items for crucible") {
	override val failedComponent: TreeComponent<GiantsFoundry> = TakeMetalFromBank(script)
	override val successComponent: TreeComponent<GiantsFoundry> = FillCrucible(script)

	override fun validate(): Boolean {
		val totalBarCount =
			script.inventoryBarCounts().map { it.key to it.value + it.key.crucibleCount() }.filter { it.second > 0 }
		script.logger.info("barsToGet=${script.barsToGet}, invBarCount=$totalBarCount")
		return Inventory.isFull() || script.barsToGet.all { bg ->
			val invBarCount = totalBarCount.firstOrNull { barCount -> barCount.first == bg.first }?.second ?: 0
			invBarCount == bg.second
		}
	}
}

