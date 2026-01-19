package org.powbot.krulvis.cluesolver.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.cluesolver.Cluesolver
import org.powbot.krulvis.cluesolver.clues.Clue
import org.powbot.mobile.script.ScriptManager

class HasClue(script: Cluesolver) : Branch<Cluesolver>(script, "HasClue?") {
    override val failedComponent: TreeComponent<Cluesolver> = IsObtainingClue(script)
    override val successComponent: TreeComponent<Cluesolver> = AtClueSpot(script)

    override fun validate(): Boolean {
        val clueItem = Inventory.stream().nameContains("Clue").first()
        script.clue = Clue.forItem(clueItem)
        if (clueItem.valid() && script.clue == null) {
            Notifications.showNotification("Has a clue that is not yet added with ID=${clueItem.id}")
            ScriptManager.stop()
            return false
        }
        return script.clue != null
    }
}