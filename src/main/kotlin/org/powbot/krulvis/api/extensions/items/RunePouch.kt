package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item.Companion.RUNE_POUCH
import org.slf4j.LoggerFactory

object RunePouch {

	val logger = LoggerFactory.getLogger(javaClass.simpleName)!!

	const val ROOT_ID = 15
	fun RunePouch.isEmpty() = runes().none { it.first != Rune.NIL }
	fun RunePouch.depositRunes(): Boolean {
		if (isEmpty() || !open()) return true
		return Bank.opened() && depositComp().interact("Deposit runes") && waitFor { isEmpty() }
	}

	fun RunePouch.withdrawRunes(vararg runes: Rune): Boolean {
		val inPouchRunes = runes().map { it.first }.filterNot { it == Rune.NIL }
		if (runes.all { inPouchRunes.contains(it) }) return true
		else if (!open()) return false
		else if (inPouchRunes.any { it !in runes }) {
			logger.info("invPouchRunes=${inPouchRunes}, !unWanted=${inPouchRunes.filter { it !in runes }}")
			depositRunes()
		} else {
			runes.filter { !inPouchRunes.contains(it) }.forEach {
				val bankItem = Bank.stream().id(it.id).first()
				if (Bank.scrollToItem(bankItem) && bankItem.interact("Withdraw-All"))
					sleep(250)
			}
			val newRunes = runes().map { it.first }
			logger.info("NewRunes after withdraw=${newRunes}")
			return runes.all { newRunes.contains(it) }
		}
		return false
	}

	private fun RunePouch.isOpen() = RunePouch.depositComp().visible()

	fun RunePouch.open() =
		isOpen() || Inventory.stream().id(RUNE_POUCH).first().interact("Configure") && waitFor { isOpen() }

	fun RunePouch.close() = !isOpen() || Components.stream(ROOT_ID).action("Dismiss").first().interact("Dismiss")

	private fun RunePouch.depositComp() = Components.stream(ROOT_ID).action("Deposit runes").first()
}