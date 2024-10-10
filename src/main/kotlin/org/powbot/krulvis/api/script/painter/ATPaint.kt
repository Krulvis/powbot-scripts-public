package org.powbot.krulvis.api.script.painter

import org.powbot.api.script.paint.*
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.rscache.loader.ItemLoader

abstract class ATPaint<S : KrulScript>(val script: S, val x: Int = 110, val y: Int = 70) {

	val paintBuilder = PaintBuilder()

	init {
		paintBuilder.x(x)
		paintBuilder.y(y)
		paintBuilder.addString("Leaf: ") { script.lastLeaf.name }
	}

	fun PaintBuilder.trackInventoryItemQ(itemId: Int) =
		trackInventoryItem(itemId = itemId, textSize = null, TrackInventoryOption.QuantityChange)

	abstract fun buildPaint(paintBuilder: PaintBuilder): Paint

	open fun paintCustom(g: Rendering) {}

	fun perHourText(amount: Int) = "$amount, ${script.timer.getPerHour(amount)}/hr"

	fun containsLabel(label: String) = paintRowIndexForLabel(label) != -1

	fun paintRowIndexForLabel(label: String): Int {
		val rowWithLabel =
			paintBuilder.items.firstOrNull { row -> row.any { it is TextPaintItem && it.text().equals(label, true) } }
				?: return -1
		return paintBuilder.items.indexOf(rowWithLabel)
	}

	fun isTrackingItem(id: Int): Boolean {
		return paintBuilder.items.any { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }
	}

	fun trackItem(id: Int, amount: Int) {
		if (!isTrackingItem(id)) {
			paintBuilder.trackInventoryItems(id)
			script.logger.info("Now tracking: ${ItemLoader.lookup(id)?.name()} adding $amount as start")
			paintBuilder.items.forEach { row ->
				val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
				if (item != null) (item as InventoryItemPaintItem).diff += amount
			}
		}
	}
}