package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.RunePouch
import org.powbot.krulvis.api.extensions.items.RunePouch.depositRunes
import org.powbot.krulvis.api.extensions.items.RunePouch.runeCount
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.mobile.script.ScriptManager

interface RunePouchScript {
	val minRuneCount: Int
	val runePouchRunes: Array<Rune>

	fun missingRunePouchRunes(): List<Rune> {
		val runes = RunePouch.runes()
		return runePouchRunes.filter { runes.runeCount(it) < minRuneCount }
	}

	fun setRunePouchRunes(): Boolean {
		val runes = runePouchRunes
		val minRuneCount = minRuneCount
		val inPouchRunes = RunePouch.runes()
		if (runes.all { inPouchRunes.runeCount(it) >= minRuneCount }) return true
		else if (!RunePouch.open()) return false
		if (inPouchRunes.any { it.first !in runes }) {
			RunePouch.logger.info("invPouchRunes=${inPouchRunes}, !unWanted=${inPouchRunes.filter { it.first !in runes }}")
			if (depositRunes() && waitFor { RunePouch.runes().isEmpty() }) {
				runes.withdraw()
			} else {
				return false
			}
		} else {
			runes.filter { inPouchRunes.runeCount(it) < minRuneCount }.withdraw()
		}

		val newRunes = RunePouch.runes()
		RunePouch.logger.info("NewRunes after withdraw=${newRunes}")
		return waitFor {
			val rpr = RunePouch.runes()
			runes.all { rpr.runeCount(it) >= minRuneCount }
		}
	}

	private fun Rune.withdraw() {
		val bankItem = Bank.stream().id(id).first()
		if (Bank.scrollToItem(bankItem) && bankItem.interact("Withdraw-All"))
			sleep(250)
	}

	private fun Array<Rune>.withdraw() = forEach { it.withdraw() }

	private fun List<Rune>.withdraw() = forEach { it.withdraw() }
}

class ShouldSetRunePouchRunes<S>(script: S, override val failedComponent: TreeComponent<S>) :
	Branch<S>(script, "ShouldSetupRunePouch?") where S : KrulScript, S : RunePouchScript {


	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "SetupRunePouch") {
		script.logger.info("Missing runes in runepouch[${missingRunePouchRunes.joinToString { it.name }}]")
		if (!script.setRunePouchRunes()) {
			val missingRunes = script.missingRunePouchRunes()
			val rpr = RunePouch.runes()
			val missingRune = missingRunes.firstOrNull { mr ->
				val inRunePouch = (rpr.firstOrNull { it.first == mr }?.second ?: 0)
				Bank.stream().id(mr.id).count() + inRunePouch <= script.minRuneCount
			}
			if (missingRune != null) {
				Notifications.showNotification("Out of $missingRune, stopping script")
				ScriptManager.stop()
			}
		}
	}


	private var missingRunePouchRunes = emptyList<Rune>()

	override fun validate(): Boolean {
		if (!RunePouch.inInventory()) return false
		missingRunePouchRunes = script.missingRunePouchRunes()
		return missingRunePouchRunes.isNotEmpty()
	}
}