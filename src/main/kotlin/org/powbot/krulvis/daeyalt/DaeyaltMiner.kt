package org.powbot.krulvis.daeyalt

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement

@ScriptManifest(
	name = "krul DaeyaltMiner",
	description = "Mines Daeyalt essence",
	author = "Krulvis",
	version = "1.0.0",
	scriptId = "404633af-46ed-4121-82da-510d8a040ff7",
	category = ScriptCategory.Mining
)
@ScriptConfiguration.List([
	ScriptConfiguration("Equipment", "What to wear?", OptionType.EQUIPMENT,
		defaultValue = """{"22400":2,"11920":3,"24676":4,"24678":7,"24680":10}"""),
	ScriptConfiguration("Tick Manip", "Use 1.5 herb+tar tick-manipulation", OptionType.BOOLEAN, defaultValue = "false"),
	ScriptConfiguration("Special", "Perform special attack", OptionType.BOOLEAN, defaultValue = "false"),
])
class DaeyaltMiner : KrulScript() {

	override fun createPainter(): ATPaint<*> {
		return DaeyaltPainter(this)
	}

	var gameTick = 0
	var startTick = 0

	val herbs = arrayOf("Guam", "Irit", "Harralander", "Marrentill")
	val tar = "Swamp tar"
	var mine = GameObject.Nil

	fun canTickManip(): Boolean = Inventory.stream().name(*herbs).isNotEmpty() && Inventory.stream().name(tar).isNotEmpty()

	@Subscribe
	fun onTick(e: TickEvent) {
		gameTick++
	}

	val tickManip by lazy { getOption<Boolean>("Tick Manip") }
	val special by lazy { getOption<Boolean>("Special") }
	val equipment by lazy { EquipmentRequirement.forOption(getOption("Equipment")) }
	override val rootComponent: TreeComponent<*> = WearingEquipment(this)


}

val ESSENCE = 24706

class DaeyaltPainter(script: DaeyaltMiner) : ATPaint<DaeyaltMiner>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.trackInventoryItem(ESSENCE).trackSkill(Skill.Mining).addString("Cycles") { Game.cycle().toString() }.build()
	}
}

fun main() {
	DaeyaltMiner().startScript()
}