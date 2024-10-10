package org.powbot.krulvis.cluesolver

import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.cluesolver.clues.Clue
import org.powbot.krulvis.cluesolver.tree.branch.HasClue

@ScriptManifest("krul ClueSolver", description = "1.0.0", category = ScriptCategory.Other, priv = true)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            OBTAIN_CLUE_CONFIG,
            "Obtain new clue after finishing one",
            OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            CLUE_LEVEL_CONFIG,
            "What level clue to obtain?",
            OptionType.STRING,
            defaultValue = "EASY",
            allowedValues = ["EASY", "MEDIUM", "HARD", "ELITE"]
        )
    ]
)
class Cluesolver : KrulScript() {
    //Current clue
    var clue: Clue? = null

    val obtainClue: Boolean by lazy { getOption(OBTAIN_CLUE_CONFIG) }
    val obtainingLevel: Clue.Level by lazy { Clue.Level.valueOf(getOption(CLUE_LEVEL_CONFIG)) }

    override fun createPainter(): ATPaint<*> = CluePainter(this)

    override val rootComponent: TreeComponent<*> = HasClue(this)

    @ValueChanged(OBTAIN_CLUE_CONFIG)
    fun onObtainClue(obtain: Boolean) {
        updateVisibility(CLUE_LEVEL_CONFIG, obtain)
    }
}