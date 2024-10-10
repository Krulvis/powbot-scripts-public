package org.powbot.krulvis.woodcutter

import org.powbot.api.Tile
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.rt4.GroundItems
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.items.Item.Companion.TINDERBOX
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.woodcutter.tree.branch.ShouldBurn

@ScriptManifest(
    name = "krul Woodcutter",
    description = "Chops any tree, anywhere",
    author = "Krulvis",
    markdownFileName = "Woodcutter.md",
    version = "1.1.6",
    scriptId = "2834ffcc-a81d-4c08-b163-84cc9c8ef130",
    category = ScriptCategory.Woodcutting
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Trees",
            optionType = OptionType.GAMEOBJECT_ACTIONS,
            description = "What trees do you want to chop?"
        ),
        ScriptConfiguration(
            name = "Bank",
            optionType = OptionType.BOOLEAN,
            description = "Bank the logs?",
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = "Burn",
            optionType = OptionType.BOOLEAN,
            description = "Burn the logs?",
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "ForceWeb",
            optionType = OptionType.BOOLEAN,
            description = "Force Web Walking?",
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "BoundaryID",
            optionType = OptionType.INTEGER,
            description = "Debug -> Boundary Objects -> which id pops up under your player?",
            visible = false,
            defaultValue = "-1"
        )
    ]
)
class Woodcutter : KrulScript() {

    val redwoods by lazy { getOption<List<GameObjectActionEvent>>("Trees").any { it.name.contains("Redwood") } }
    val trees by lazy {
        getOption<List<GameObjectActionEvent>>("Trees").map {
            when (it.name) {
                "Redwood" -> {
                    if (it.tile.y in derpedRedWoodY)
                        it.tile.derive(0, 1)
                    else it.tile.derive(1, 0)
                }

                "Blisterwood Tree" -> it.tile.derive(2, 3)
                "Teak", "Teak tree" -> it.tile
                else -> it.tile.derive(1, 1)
            }
        }
    }

    val derpedRedWoodY = listOf(3480, 3494)
    val bank by lazy { getOption<Boolean>("Bank") }
    val burn by lazy { getOption<Boolean>("Burn") }
    val forceWeb by lazy { getOption<Boolean>("ForceWeb") }
    val boundaryId by lazy { getOption<Int>("BoundaryID") }


    val TOOLS = intArrayOf(
        1349,
        1351,
        1353,
        1355,
        1357,
        1359,
        1361,
        6739,
        13241,
        13242,
        14028,
        28220,
        28226,
        28211,
        28205,
        28196,
        28217,
        28199,
        28208,
        28214,
        28202,
        TINDERBOX
    )
    val LOGS = intArrayOf(1511, 1513, 1515, 1517, 1519, 1521, 2862, 6332, 6333, 19669)
    val NESTS = intArrayOf(5070, 5071, 5072, 5073, 5074)
    var lastChopAnim = 0L
    var burning = false
    var burnTile: Tile? = null
    val chopDelay = DelayHandler(2000, 4000, OddsModifier(), "Chop delay")

    fun nest() = GroundItems.stream().within(10.0).name("Bird nest").firstOrNull()

    override fun createPainter(): ATPaint<*> = WoodcutterPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBurn(this)

    @ValueChanged("Bank")
    fun onBankValueChanged(bank: Boolean) {
        updateVisibility("Burn", !bank)
        if (bank) {
            updateOption("Burn", false, OptionType.BOOLEAN)
        }
    }

    @ValueChanged("Burn")
    fun onBurnValueChanged(burn: Boolean) {
        updateVisibility("BoundaryID", burn)
        if (burn) {
            updateOption("Bank", false, OptionType.BOOLEAN)
        }
    }
}

fun main() {
    Woodcutter().startScript("127.0.0.1", "GIM", false)
}