package org.powbot.krulvis.tanner.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tanner.Data
import org.powbot.krulvis.tanner.Tanner
import org.powbot.mobile.script.ScriptManager

class HasSupplies(script: Tanner) : Branch<Tanner>(script, "HasSupplies") {
	override val failedComponent: TreeComponent<Tanner> = BankOpen(script)
	override val successComponent: TreeComponent<Tanner> = AtTanner(script)

	override fun validate(): Boolean = script.hide.hasRaw() && script.hide.hasCoins()
}

class BankOpen(script: Tanner) : Branch<Tanner>(script, "BankOpen?") {
	val stairsTile = Tile(2932, 3282, 1)
	override val failedComponent: TreeComponent<Tanner> = SimpleLeaf(script, "OpenBank") {
		val stairs =
			Objects.stream(stairsTile, GameObject.Type.INTERACTIVE).name("Staircase").action("Climb-down").first()
		if (stairs.valid()) {
			if (walkAndInteract(stairs, "Climb-down")) {
				waitForDistance(stairs) { Bank.getBank().valid() }
			}
		} else if (Bank.openNearest()) {
			waitFor(10000) { Bank.opened() }
		}
	}
	override val successComponent: TreeComponent<Tanner> = SimpleLeaf(script, "HandleBank") {
		if (Bank.depositAllExcept(995, script.hide.raw)) {
			if (Bank.containsOneOf(995)) {
				Bank.withdraw(995, Bank.Amount.ALL)
			} else if (Bank.withdraw(script.hide.raw, Bank.Amount.ALL)) {
				waitFor { script.hide.hasRaw() && script.hide.hasCoins() }
			} else if (script.hide.hasRaw() && !script.hide.hasRaw()) {
				Notifications.showNotification("Out of coins, stopping script!")
				ScriptManager.stop()
			} else if (!script.hide.hasRaw() && Bank.stream().id(script.hide.raw).isEmpty()) {
				if (script.all) {
					val index = Data.Hide.values().indexOf(script.hide)
					if (index == Data.Hide.values().size - 1) {
						Notifications.showNotification("Done with all hides, stopping script!")
						ScriptManager.stop()
					} else {
						script.hide = Data.Hide.values()[index + 1]
					}
				}
			}
		}
	}

	override fun validate(): Boolean = Bank.opened()
}

class AtTanner(script: Tanner) : Branch<Tanner>(script, "AtTanner") {
	val stairsTile = Tile(2932, 3283, 0)
	override val failedComponent: TreeComponent<Tanner> = SimpleLeaf(script, "WalkToTanner") {
		Bank.close()
		val stairs =
			Objects.stream(stairsTile, GameObject.Type.INTERACTIVE).name("Staircase").action("Climb-up").first()
		if (!stairs.valid()) {
			Movement.walkTo(stairsTile)
		} else if (walkAndInteract(stairs, "Climb-up")) {
			waitForDistance(stairs) { findTanner().valid() }
		}
	}
	override val successComponent: TreeComponent<Tanner> = SimpleLeaf(script, "TanHides") {
		var comp = script.hide.tanComp()
		if (!comp.visible()) {
			if (walkAndInteract(tanner, "Trade")) {
				waitForDistance(tanner) {
					comp = script.hide.tanComp()
					comp.visible()
				}
			}

		}
		if (comp.interact("Tan All")) {
			waitFor { !script.hide.hasRaw() }
		}
	}

	var tanner = Npc.Nil
	fun findTanner() = Npcs.stream().name("Tanner").first()

	override fun validate(): Boolean {
		tanner = findTanner()
		return tanner.valid()
	}
}