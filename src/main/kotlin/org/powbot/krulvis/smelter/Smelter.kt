package org.powbot.krulvis.smelter

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.smelter.tree.branch.ShouldBank

@ScriptManifest(
	name = "krul Smelter",
	description = "Smelt bars or cannonballs",
	author = "Krulvis",
	markdownFileName = "Smelter.md",
	version = "1.1.4",
	category = ScriptCategory.Smithing,
	scriptId = "fe46b025-f1cc-4009-a4db-01f97189fe8e"
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Cannonball",
			description = "Smelt cannonballs instead",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			name = "Ring of forging",
			description = "Equip a ring of forging",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			name = "Bar",
			description = "Bar to smelt",
			allowedValues = ["BRONZE", "SILVER", "IRON", "STEEL", "GOLD", "MITHRIL", "ADAMANTITE", "RUNITE"],
			defaultValue = "GOLD"
		)
	]
)
class Smelter : KrulScript() {
	override fun createPainter(): ATPaint<*> = SmelterPainter(this)
	override val rootComponent: TreeComponent<*> = ShouldBank(this)

	val bar by lazy { Bar.valueOf(getOption<String>("Bar")) }
	val cannonballs by lazy { getOption<Boolean>("Cannonball") }
	val forgingRing by lazy { getOption<Boolean>("Ring of forging") }

}

fun main() {
	Smelter().startScript("127.0.0.1", "GIM", false)
}