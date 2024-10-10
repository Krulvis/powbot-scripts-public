package org.powbot.krulvis.cluesolver.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.cluesolver.Cluesolver

class SolveClue(script: Cluesolver) : Leaf<Cluesolver>(script, "Solving Clue") {
    override fun execute() {
        script.clue!!.solve()
    }
}