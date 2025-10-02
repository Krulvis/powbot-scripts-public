package org.powbot.krulvis.fletching

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.ValueChanged
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.fletching.tree.branch.ShouldBank

@ScriptManifest(
    "krul ValeTotems",
    "Does the fletching minigame in Auburnvale",
    scriptId = "46feb019-4028-4345-a3a9-d8272550b4c8",
    version = "1.0.0",
    priv = true
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            LOGS_OPTION,
            "What logs to use?",
            OptionType.STRING,
            allowedValues = [WILLOW, MAPLE, YEW, MAGIC, REDWOORD],
            defaultValue = MAPLE
        ),
        ScriptConfiguration(
            DECORATION_OPTION,
            "What to use as decoration?",
            OptionType.STRING,
            allowedValues = ["shortbow", "longbow", "shield"],
            defaultValue = "longbow"
        )
    ]
)
class AuburnvaleFletcher : KrulScript() {
    override fun createPainter(): KrulPaint<*> = ValeTotemPainter(this)

    val logs by lazy { Logs.valueOf(getOption(LOGS_OPTION)) }
    val deco by lazy { logs.decoration.first { it.name == getOption(DECORATION_OPTION) } }

    val totems = mapOf(
        Totem.CENTER to null,
        Totem.NORTH_EAST to siteCenterToNE,
        Totem.EAST to bankToEast,
        Totem.SOUTH_EAST to siteEastToSE,
        Totem.SOUTH to siteSouthTOSE.reversed()
    )
    var current = totems.entries.first()
    val totemTimer = Timer(30000)
    val bank = BankLocation.AUBERVALE_SOUTH
    val bankPath = siteSEToBank
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    fun setNextTotem() {
        val nextIndex = totems.entries.indexOf(current) + 1
        current = totems.entries.toTypedArray()[if (nextIndex >= totems.size) 0 else nextIndex]
        totemTimer.reset()
    }

    var fletching = false

    override fun canBreak(): Boolean {
        //To avoid fletching out our whole inventory, we take a quick step right where we currently are standing.
        logger.info("Can break activated, stepping to avoid fletching out our inventory.")
        val tile = me.trueTile()
        if (tile.matrix().interact("Walk here")) {
            return super.canBreak()
        } else {
            logger.info("Failed to step to avoid fletching out our inventory.")
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        current = totems.entries.minByOrNull { it.key.totemTile.distance() } ?: totems.entries.first()
    }

    @ValueChanged(LOGS_OPTION)
    fun onLogsChanged(logs: String) {
        val logs = Logs.valueOf(logs)
        val allowed = logs.decoration.map { it.name }.toTypedArray()
        updateAllowedOptions(DECORATION_OPTION, allowed)
        updateOption(DECORATION_OPTION, allowed.first(), OptionType.STRING)
    }

    fun carveDecoration(): Boolean {
        val knife = Inventory.stream().nameContains("knife").first()
        val logs = Inventory.stream().nameContains(logs.logName).first()
        var makeComp = deco.carveComp()
        if (!makeComp.visible()) {
            if (knife.useOn(logs)) {
                waitFor {
                    makeComp = deco.carveComp()
                    makeComp.visible()
                }
            }
        }
        return makeComp.visible() && makeComp.click()
    }
}

fun LocalPath.walkable() = isNotEmpty() && size < 35

fun main() {
    AuburnvaleFletcher().startScript("127.0.0.1", "GIM", true)
}