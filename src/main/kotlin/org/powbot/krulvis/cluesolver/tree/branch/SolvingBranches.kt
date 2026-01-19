package org.powbot.krulvis.cluesolver.tree.branch

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.cluesolver.Cluesolver
import org.powbot.krulvis.cluesolver.tree.leaf.SolveClue

class AtClueSpot(script: Cluesolver) : Branch<Cluesolver>(script, "HasClue?") {
    override val failedComponent: TreeComponent<Cluesolver> = SimpleLeaf(script, "Walk To Clue Spot") {
        Movement.walkTo(script.clue!!.solveSpot)
    }
    override val successComponent: TreeComponent<Cluesolver> = SolveClue(script)

    override fun validate(): Boolean {
        return script.clue!!.solveSpot.distance() < 25
    }
}