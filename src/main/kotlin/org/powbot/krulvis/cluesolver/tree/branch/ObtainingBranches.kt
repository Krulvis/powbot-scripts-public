package org.powbot.krulvis.cluesolver.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.cluesolver.Cluesolver
import org.powbot.krulvis.cluesolver.tree.leaf.StealClue
import org.powbot.mobile.script.ScriptManager

class IsObtainingClue(script: Cluesolver) : Branch<Cluesolver>(script, "IsObtainingClues?") {
    override val failedComponent: TreeComponent<Cluesolver> = SimpleLeaf(script, "Stopping because finished clue") {
        Notifications.showNotification("Finished clue")
        ScriptManager.stop()
    }
    override val successComponent: TreeComponent<Cluesolver> = AtPickpocketSpot(script)

    override fun validate(): Boolean {
        return script.obtainClue
    }
}

class AtPickpocketSpot(script: Cluesolver) : Branch<Cluesolver>(script, "AtPickpocketSpot?") {

    override val failedComponent: TreeComponent<Cluesolver> = SimpleLeaf(script, "WalkToObtainingSpot") {
        Movement.walkTo(script.obtainingLevel.obtainingTile)
    }
    override val successComponent: TreeComponent<Cluesolver> = StealClue(script)

    override fun validate(): Boolean {
        return script.obtainingLevel.obtainingTile.distance() <= 25
    }
}