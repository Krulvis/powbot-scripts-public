package org.powbot.krulvis.nmz

import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.nmz.tree.branch.OutsideNMZ

@ScriptManifest(
	name = "krul NMZ",
	author = "Krulvis",
	version = "1.0.3",
	description = "Nightmarezone: Sips all potions, guzzles rock cake and flicks rapid heal",
	scriptId = "c60ff7ed-2236-4b94-913c-2d2b90d18b9c",
	category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Extra info",
			description = "When you have pray pots, this script will active quick prayer. \n So make sure you have the quick prayer set to the prayers you want to use.",
			optionType = OptionType.INFO,
			defaultValue = "A"
		),
		ScriptConfiguration(
			name = "Extra info pots",
			description = "If a potion is not sipped, it is not supported. Just tag me in discord to add it.",
			optionType = OptionType.INFO,
			defaultValue = "A"
		),
		ScriptConfiguration(
			name = "Inventory",
			description = "Inventory setup",
			optionType = OptionType.INVENTORY
		),
		ScriptConfiguration(
			name = "StopAfterNMZ",
			description = "Stop when outside of NMZ",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		),
	]
)
class NightmareZone : KrulScript() {
	override fun createPainter(): ATPaint<*> {
		return NMZPainter(this)
	}

	override val rootComponent: TreeComponent<*> = OutsideNMZ(this)
	val inventoryItems by lazy { getOption<Map<Int, Int>>("Inventory") }
	val stopOutside by lazy { getOption<Boolean>("StopAfterNMZ") }
	var nextFlick = Timer(1)

	fun outsideNMZ() = Npcs.stream().name("Dominic Onion").isNotEmpty()


}

class NMZPainter(script: NightmareZone) : ATPaint<NightmareZone>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Absorption: ") { Potion.getAbsorptionRemainder().toString() }
			.addString("NextFlick: ") { script.nextFlick.getRemainderString() }
			.trackSkill(Skill.Attack)
			.trackSkill(Skill.Strength)
			.trackSkill(Skill.Defence)
			.trackSkill(Skill.Hitpoints)
			.trackSkill(Skill.Slayer)
			.trackSkill(Skill.Magic)
			.trackSkill(Skill.Ranged)
			.build()
	}
}

fun main() {
	NightmareZone().startScript(useDefaultConfigs = true)
}