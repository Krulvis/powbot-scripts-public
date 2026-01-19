package org.powbot.krulvis.giantsfoundry

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.PaintCheckboxChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.giantsfoundry.data.Stage
import org.powbot.krulvis.giantsfoundry.tree.branch.IsSmithing
import org.powbot.krulvis.giantsfoundry.tree.leaf.FixTemperature
import org.powbot.krulvis.giantsfoundry.tree.leaf.SmithAndWait


@ScriptManifest(
    name = "krul GiantFoundry",
    description = "Makes swords for big giant.",
    author = "Krulvis",
    version = "1.1.4",
    scriptId = "6e058edd-cc5b-4b20-b4aa-6def55e9e903",
    category = ScriptCategory.Smithing,
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "requirement", "Do not buy moulds that you can't use as the script is unable to differentiate filter them.",
            OptionType.INFO
        ),
        ScriptConfiguration(
            "SingleMetal", "Use only one metal type (not recommended)?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            "FirstMetal", "Which bar to use?",
            optionType = OptionType.STRING,
            defaultValue = ADAMANTITE,
            allowedValues = arrayOf(BRONZE, IRON, STEEL, MITHRIL, ADAMANTITE, RUNITE)
        ),
        ScriptConfiguration(
            "SecondMetal", "Which bar to combine with?",
            optionType = OptionType.STRING,
            defaultValue = RUNITE,
            allowedValues = arrayOf(BRONZE, IRON, STEEL, MITHRIL, ADAMANTITE, RUNITE)
        ),
        ScriptConfiguration(
            "UseMetalItems", "Do you want to smelt weapons and armour?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        )
    ]
)
class GiantsFoundry : KrulScript() {
    override fun createPainter(): KrulPaint<*> {
        return GiantsFoundryPainter(this)
    }

    var coins = 0
    var qualities = 0
    val currentStage: Stage
        get() {
            val progress = currentProgress()
            return stages.lastOrNull { it.isActive(progress) } ?: Stage.Nil
        }
    var stages = emptyArray<Stage>()
    var lastSword = false
    val singleMetal by lazy { getOption<Boolean>("SingleMetal") }
    val useItems by lazy { getOption<Boolean>("UseMetalItems") }
    val firstBar by lazy { Bar.forName(getOption("FirstMetal"))!! }
    val secondBar by lazy { Bar.forName(getOption("SecondMetal"))!! }

    val barsToGet by lazy {
        if (singleMetal) {
            listOf(firstBar to 28)
        } else {
            listOf(firstBar to 14, secondBar to 14)
        }
    }

    override fun canBreak(): Boolean {
        return lastLeaf !is SmithAndWait && lastLeaf !is FixTemperature
    }

    override val rootComponent: TreeComponent<*> = IsSmithing(this)

    private val MOULD_JIG_TILE = Tile(3371, 11489, 0)
    val POURING_TILE = Tile(3374, 11492, 0)

    fun inventoryBarCounts(): Map<Bar, Int> = BARS.associateWith { it.crucibleInventoryCount() }

    fun isSmithing() = Equipment.stream().name("Preform").isNotEmpty()

    fun jig() = Objects.stream(MOULD_JIG_TILE, GameObject.Type.INTERACTIVE)
        .nameContains("Mould jig").first()

    fun areBarsPoured() = jig().name.contains("(Poured metal)")

    fun kovac() = Npcs.stream().name("Kovac").firstOrNull()

    fun hasCommission() = Varpbits.varpbit(JOB_VARP, 0, 63) != 0

    fun stopActivity(tile: Tile?) {
        val tile = tile ?: me.tile()
        tile.matrix().interact("Walk here", false)
    }

    fun parseResults() {
        val comp = Widgets.widget(229).filterNotNull()
            .firstOrNull { it.text().contains("You're awarded:") && it.visible() } ?: return
        val text = comp.text()
        val quality = text.substring(text.indexOf("quality: ") + 9, text.indexOf("<br>")).toInt()
        val coinsGained = text.substring(text.indexOf("XP and ") + 7, text.indexOf(" coins")).replace(",", "").toInt()
        coins += coinsGained
        qualities += quality
    }


    @ValueChanged("SingleMetal")
    fun onSingleMetal(singleMetal: Boolean) {
        updateVisibility("SecondMetal", !singleMetal)
    }

    @Subscribe
    fun onCheckBoxEvent(e: PaintCheckboxChangedEvent) {
        if (e.checkboxId == LAST_SWORD_ID) {
            lastSword = e.checked
        }
    }


}

fun main() {
    GiantsFoundry().startScript("127.0.0.1", "GIM", false)
}