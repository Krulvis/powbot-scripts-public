package org.powbot.krulvis.giantsfoundry

import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.GiantsFoundryItem
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.giantsfoundry.tree.branch.IsSmithing


@ScriptManifest(
    name = "krul GiantFoundry",
    description = "Makes swords for big giant.",
    author = "Krulvis",
    version = "1.0.5",
    scriptId = "6e058edd-cc5b-4b20-b4aa-6def55e9e903",
    category = ScriptCategory.Smithing,
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Inventory", "What bars do you want to smelt?",
            optionType = OptionType.INVENTORY,
            defaultValue = "{\"1335\":7,\"1339\":7}"
        )
    ]
)
class GiantsFoundry : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return GiantsFoundryPainter(this)
    }

    var coins = 0
    var qualities = 0
    var currentAction: Action? = null
    val barsToUse by lazy {
        getOption<Map<Int, Int>>("Inventory")
            .filter { GiantsFoundryItem.forId(it.key) != null }.map { Pair(GiantsFoundryItem.forId(it.key)!!, it.value) }
    }

    override val rootComponent: TreeComponent<*> = IsSmithing(this)
//    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "SimpleLeaf") { sleep(2000) }

    fun getInvBar() = Inventory.stream().id(*GiantsFoundryItem.values().map { it.id }.toIntArray()).firstOrNull()

    fun crucibleBars(): List<Pair<GiantsFoundryItem, Int>> {
        return GiantsFoundryItem.METAL_BARS.map { it to it.giantsFoundryCount }.filter { it.second > 0 }
    }

    fun crucibleBarCount(bar: GiantsFoundryItem): Int = crucibleBars().firstOrNull { it.first == bar }?.second ?: 0

    fun correctCrucibleCount(bar: GiantsFoundryItem) =
        crucibleBarCount(bar) == (barsToUse.firstOrNull { it.first == bar }?.second ?: 0)

    fun isSmithing() = Equipment.stream().name("Preform").isNotEmpty()

    fun emptyJig() = Objects.stream(30).type(GameObject.Type.INTERACTIVE).name("Mould jig (empty)").firstOrNull()
    fun fullJig() = Objects.stream(30).type(GameObject.Type.INTERACTIVE).name("Mould jig (Poured metal)").firstOrNull()
    fun areBarsPoured() = fullJig() != null

    fun activeActionComp(): Component = Widgets.component(ROOT, 76)

    fun kovac() = Npcs.stream().name("Kovac").firstOrNull()

    fun hasCommission() = Varpbits.varpbit(JOB_VARP, 0, 63) != 0

    fun mouldWidget() = Widgets.widget(718)

    fun mouldWidgetOpen() = mouldWidget().component(2).any { it?.text() == "Giants' Foundry Mould Setup" }

    fun activeAction(): Action? {
        val activeComp = activeActionComp()
        val x = activeComp.x()
        val maxX = x + activeComp.width()
        val actionComps = Components.stream(ROOT, 75)
            .filtered { it.x() in x..maxX }.list()
        val actionComp = actionComps.firstOrNull { Action.forTexture(it.textureId()) != null } ?: return null
        return Action.forTexture(actionComp.textureId())
    }

    fun interactObj(obj: GameObject, action: String): Boolean {
        if (obj.distance() > 3) Movement.step(obj.tile)
        return Utils.waitFor { obj.inViewport() } && obj.interact(action)
    }

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        currentAction = activeAction()
    }

    fun stopActivity(tile: Tile?) = Movement.step(tile ?: me.tile())

    fun parseResults() {
        val comp = Widgets.component(229, 1)
        if (!comp.visible()) return
        val text = comp.text()
        if (!text.contains("Sword completed in:")) return
        val quality = text.substring(text.indexOf("quality: ") + 9, text.indexOf("<br>")).toInt()
        val smithingXp = text.substring(text.indexOf("You're awarded: ") + 16, text.indexOf(" Smithing XP")).toInt()
        val coinsGained = text.substring(text.indexOf("XP and ") + 7, text.indexOf(" coins")).replace(",", "").toInt()
        coins += coinsGained
        qualities += quality
    }


    enum class Action(
        val textureId: Int,
        val interactable: String,
        val tile: Tile,
        var min: Int,
        var max: Int,
        val activeBarComponentId: Int,
        val heats: Boolean = false
    ) {
        HAMMER(4442, "Trip hammer", Tile(3367, 11497), -1, -1, 21),
        GRIND(4443, "Grindstone", Tile(3364, 11492), -1, -1, 20, true),
        POLISH(4444, "Polishing wheel", Tile(3365, 11485), -1, -1, 19);

        fun canPerform() = currentTemp() in min + 4..max

        fun calculateMinMax() {
            val totalWidth = Widgets.component(ROOT, 8).width()
            val barComp = Widgets.component(ROOT, activeBarComponentId)
            min = (1000.0 / totalWidth * barComp.x()).toInt()
            max = (1000.0 / totalWidth * (barComp.x() + barComp.width())).toInt()
            debug("Calculated min=$min, max=$max for $name with totalWidth=$totalWidth, barX=${barComp.x()}, barWidth=${barComp.width()}")
        }

        fun getObj() = Objects.stream(30).type(GameObject.Type.INTERACTIVE).name(interactable).firstOrNull()

        companion object {
            fun forTexture(texture: Int) = values().firstOrNull { it.textureId == texture }
            fun calculateMinMax() = values().forEach { it.calculateMinMax() }
        }

    }

}

fun main() {
    GiantsFoundry().startScript("127.0.0.1", "GIM", true)
}