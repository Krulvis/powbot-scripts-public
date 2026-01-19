package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.RunePouch
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.RunePouchScript
import org.powbot.mobile.script.ScriptManager


class ShouldSetRunePouchRunes<S>(script: S, override val failedComponent: TreeComponent<S>, val minAmount: Int = -1) :
    Branch<S>(script, "ShouldSetupRunePouch?") where S : KrulScript, S : RunePouchScript {


    override val successComponent: TreeComponent<S> = SimpleLeaf(script, "SetupRunePouch") {
        script.logger.warn("Missing runes in runepouch[${missingRunePouchRunes.joinToString { it.first.name }}]")
        if (!Bank.opened()) return@SimpleLeaf
        if (!script.setRunePouchRunes()) {
            val missingRunes = script.missingRunePouchRunes()
            val rpr = RunePouch.runes()
            val missingRune = missingRunes.firstOrNull { mr ->
                val inRunePouch = (rpr.firstOrNull { it.first == mr.first }?.second ?: 0)
                Bank.stream().id(mr.first.id).count() + inRunePouch < mr.second
            }
            if (missingRune != null) {
                Notifications.showNotification("Not enough ${missingRune.first} left to reach ${missingRune.second}, stopping script")
                ScriptManager.stop()
            }
        }
    }


    private var missingRunePouchRunes = emptyList<Pair<Rune, Int>>()

    override fun validate(): Boolean {
        if (!RunePouch.inInventory()) return false
        missingRunePouchRunes = script.missingRunePouchRunes()
        return missingRunePouchRunes.isNotEmpty()
    }
}