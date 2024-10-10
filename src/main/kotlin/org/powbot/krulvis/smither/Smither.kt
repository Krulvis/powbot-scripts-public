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
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.smither.tree.branch.ShouldBank

@ScriptManifest(
	name = "krul Smither",
	description = "Smiths stuff from bars",
	author = "Krulvis",
	markdownFileName = "Smither.md",
	version = "1.0.2",
	category = ScriptCategory.Smithing
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
			allowedValues = ["DAGGER", "AXE", "MACE", "MEDIUM_HELM", "BOLTS", "SWORD", "DART_TIPS", "NAILS", "SCIMITAR", "ARROWTIPS", "LIMBS", "LONG_SWORD", "FULL_HELM", "KNIVES", "SQUARE_SHIELD", "WARHAMMER", "BATTLE_AXE", "CHAIN_BODY", "KITE_SHIELD", "CLAWS", "TWO_HAND_SWORD", "PLATE_SKIRT", "PLATE_LEGS", "PLATE_BODY"],
			defaultValue = "PLATE_BODY"
		)
	]
)
class Smither : KrulScript() {
	override fun createPainter(): ATPaint<*> = SmitherPainter(this)
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
	Smither().startScript(true)
}