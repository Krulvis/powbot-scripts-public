package org.powbot.krulvis.api.script

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.magic.Rune
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.RunePouch
import org.powbot.krulvis.api.extensions.items.RunePouch.depositRunes
import org.powbot.krulvis.api.extensions.items.RunePouch.runeCount
import org.powbot.krulvis.api.extensions.items.RunePouchLoadout
import org.slf4j.LoggerFactory
import kotlin.math.min

const val MAX_RUNE_COUNT = 16000

//Name of option
const val RUNE_POUCH_OPTION = "Rune Pouch Runes"
private val log = LoggerFactory.getLogger("RunePouchScript")

interface RunePouchScript {
    val runePouchRunes: List<Pair<Rune, Int>>

    /**
     * Override this to make the script wait until a rune has less then min amount in pouch
     */
    open val minRunepouchRuneAmount: Int
        get() = -1

    fun missingRunePouchRunes(): List<Pair<Rune, Int>> {
        val runes = RunePouch.runes()
        return runePouchRunes.filter {
            val currCount = runes.runeCount(it.first)
            if (minRunepouchRuneAmount == -1) {
                currCount < it.second
            } else {
                currCount < min(it.second, minRunepouchRuneAmount)
            }
        }
    }

    fun setRunePouchRunes(): Boolean {
        val requiredRuneAmount = runePouchRunes
        val requiredRunes = requiredRuneAmount.map { it.first }
        val inPouchRunes = RunePouch.runes()
        if (requiredRuneAmount.all { inPouchRunes.runeCount(it.first) >= it.second }) return true
        else if (!RunePouch.open()) return false

        val loadout = RunePouchLoadout.loadoutForRunes(requiredRuneAmount)
        if (loadout != null) {
            log.info("Found loadout $loadout")
            if (loadout.setLoadout()) {
                return waitFor { missingRunePouchRunes().isEmpty() }
            }
            return false
        }

        if (inPouchRunes.any { it.first !in requiredRunes }) {
            RunePouch.logger.warn("invPouchRunes=${inPouchRunes}, !unWanted=${inPouchRunes.filter { it.first !in requiredRunes }}")
            return if (depositRunes() && waitFor { RunePouch.runes().isEmpty() }) {
                requiredRuneAmount.withdraw(inPouchRunes)
            } else {
                false
            }
        } else {
            return requiredRuneAmount.filter { inPouchRunes.runeCount(it.first) < it.second }.withdraw(inPouchRunes)
        }
    }


    /**
     * Withdraws runes from the bank until the required amount is reached.
     * The currentRunes provide information regarding the current rune pouch
     */
    private fun Pair<Rune, Int>.withdraw(currentRunes: List<Pair<Rune, Int>>): Boolean {
        val bankItem = Bank.stream().id(first.id).first()
        val currentCount = currentRunes.runeCount(first)
        if (currentCount >= second) return true
        if (bankItem == Item.Nil || bankItem.stack == 0) return false
        if (second == MAX_RUNE_COUNT) {
            Bank.withdraw(first.id, Bank.Amount.ALL)
            return waitFor { RunePouch.runes().runeCount(first) >= second }
        } else {
            Bank.withdraw(bankItem, second - currentCount)
            val correctCount = waitFor { RunePouch.runes().runeCount(first) >= second }
            log.info("Withdrawn ${second - currentCount} ${first.name} from bank = ${correctCount}")
            return correctCount
        }
    }

    private fun List<Pair<Rune, Int>>.withdraw(currentRunes: List<Pair<Rune, Int>>): Boolean {
        return all { it.withdraw(currentRunes) }
    }

}