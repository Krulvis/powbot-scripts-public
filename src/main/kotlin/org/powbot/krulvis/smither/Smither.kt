package org.powbot.krulvis.smither

import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.rt4.Bank
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.smither.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Smither",
    description = "Smiths stuff from bars",
    author = "Krulvis",
    markdownFileName = "Smither.md",
    version = "1.0.4",
    category = ScriptCategory.Smithing,
    scriptId = "955e2c2a-c333-4adb-a4c6-c19993201e97"
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Bar",
            description = "Bar to smith items from",
            allowedValues = [BRONZE, IRON, STEEL, MITHRIL, ADAMANTITE, RUNITE],
            defaultValue = MITHRIL
        ),
        ScriptConfiguration(
            name = "Item",
            description = "Item to smith",
            allowedValues = ["DAGGER", "AXE", "MACE", "MED_HELM", "BOLTS", "SWORD", "DART_TIPS", "NAILS", "SCIMITAR", "ARROWTIPS", "JAVELIN_TIPS", "LIMBS", "LONGSWORD", "FULL_HELM", "KNIVES", "SQ_SHIELD", "WARHAMMER", "BATTLEAXE", "CHAINBODY", "KITESHIELD", "CLAWS", "TWO_HAND_SWORD", "PLATESKIRT", "PLATELEGS", "PLATEBODY"],
            defaultValue = "PLATEBODY"
        )
    ]
)
class Smither : KrulScript() {
    override fun createPainter(): KrulPaint<*> = SmitherPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    val bar by lazy { Bar.forName(getOption("Bar"))!! }
    val item by lazy { Smithable.valueOf(getOption("Item")) }

    @com.google.common.eventbus.Subscribe
    fun onInventChange(evt: InventoryChangeEvent) {
        if (evt.itemId != bar.id && painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == evt.itemId } } && !Bank.opened()) {
            painter.paintBuilder.trackInventoryItems(evt.itemId)
            val row =
                painter.paintBuilder.items.first { row -> row.any { it is InventoryItemPaintItem && it.itemId == evt.itemId } }
            (row.first { it is InventoryItemPaintItem && it.itemId == evt.itemId } as InventoryItemPaintItem).diff += evt.quantityChange
        }
    }
}

fun main() {
    Smither().startScript(false)
}