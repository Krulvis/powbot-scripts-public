package org.powbot.krulvis.thiever.chest

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.GrandExchange
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.PaintFormatters
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Item.Companion.BIRD_SNARE
import org.powbot.krulvis.api.extensions.items.Item.Companion.BOX_TRAP
import org.powbot.krulvis.api.extensions.items.Item.Companion.GRIMY_GUAM
import org.powbot.krulvis.api.extensions.items.Item.Companion.MITHRIL_AXE
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
    name = "chest Thiever",
    description = "Picklocks chest",
    author = "Krulvis",
    version = "1.0.0",
//    markdownFileName = "Thiever.md",
//    scriptId = "e6043ead-e607-4385-b67a-a86dcf699204",
    category = ScriptCategory.Thieving,
    priv = true
)
class ChestThiever : KrulScript() {


    var stopping = false
    val chestTile = Tile(2139, 9299, 0)
    var success = 0
    var fails = 0
    var traps = 0
    override val rootComponent: TreeComponent<*> = ShouldClearInv(this)


    override fun createPainter(): ATPaint<*> {
        return ChestPainter(this)
    }

    val priceMap = mutableMapOf<Int, Int>()

    val trash = intArrayOf(BIRD_SNARE, BOX_TRAP, GRIMY_GUAM, MITHRIL_AXE)

    val herbSacks = intArrayOf(13226, 24478)

    fun eatFood() {
        val food = Food.getFirstFood()
        if (food != null) {
            val count = food.getInventoryCount()
            if (food.eat()) {
                Utils.waitFor { food.getInventoryCount() != count }
            }
        } else {
            Notifications.showNotification("Out of food stopping script!")
            stopping = true
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        if (evt.quantityChange > 0 && id !in trash) {
            if (painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
                painter.paintBuilder.trackInventoryItems(id)
                logger.info("Now tracking: ${ItemLoader.lookup(id)?.name()} adding ${evt.quantityChange} as start")
                painter.paintBuilder.items.forEach { row ->
                    val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
                    if (item != null) (item as InventoryItemPaintItem).diff += evt.quantityChange
                }
                priceMap[id] = GrandExchange.getItemPrice(id)
            }
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onMessage(e: MessageEvent) {
        when (e.message) {
            "You fail to picklock the chest." -> fails++
            "You manage to unlock the chest." -> success++
            "You have activated a trap on the chest." -> traps++
        }
    }

}


class ChestPainter(script: ChestThiever) : ATPaint<ChestThiever>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Success:") { "${script.success}, ${if (script.success == 0) 0 else (script.success * 100 / (script.success + script.fails))}%" }
            .addString("Fails:") { "${script.fails}, ${if (script.fails == 0) 0 else (script.fails * 100 / (script.success + script.fails))}%" }
            .addString("Totals:") {
                "${script.success + script.fails}, ${
                    PaintFormatters.perHour(
                        script.success + script.fails,
                        ScriptManager.getRuntime(true)
                    )
                }/hr"
            }
            .addString("Teleports:") {
                "${script.traps}, ${
                    PaintFormatters.perHour(
                        script.traps,
                        ScriptManager.getRuntime(true)
                    )
                }/hr"
            }
            .trackSkill(Skill.Thieving)
            .build()
    }

}

fun main() {
    ChestThiever().startScript("127.0.0.1", "GIM", false)
}
