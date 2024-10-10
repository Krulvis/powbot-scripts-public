package org.powbot.krulvis.spices

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.spices.tree.branches.Killing

@ScriptManifest(
	name = "krul SpiceCollector",
	description = "Stand to curtain of your choice, uses karambwanji",
	version = "0.0.2",
	author = "Krulvis",
	scriptId = "f8514815-0ed1-4b1f-b29c-22325be77f75",
	category = ScriptCategory.Minigame
)
@ScriptConfiguration.List(
	[
//            ScriptConfiguration(
//                    name = "spice",
//                    description = "Spice color?",
//                    defaultValue = "Brown",
//                    optionType = OptionType.STRING,
//                    allowedValues = arrayOf(BROWN_STR)
//            ),
	]
)
class Spices : KrulScript() {

	val spice by lazy { Spice.valueOf(getOption("spice")) }
	override fun createPainter(): ATPaint<*> {
		return SpicesPainter(this)
	}

	override val rootComponent: TreeComponent<*> = Killing(this)

	fun cat() = Npcs.stream().name("cat", "hellcat", "overgrown cat").nearest().firstOrNull()

	fun fighting(): Boolean {
		val hellRat = Npcs.stream().name("Hell-Rat Behemoth").nearest().first()
		return hellRat.valid() && hellRat.healthBarVisible()
	}

	fun enterComponent() = Components.stream(219, 1).text("Insert your cat").firstOrNull()
}


class SpicesPainter(script: Spices) : ATPaint<Spices>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.addString("Cat HP") {
			script.cat()?.healthPercent()?.toString() ?: "-1"
		}.build()
	}

}


fun main() {
	Spices().startScript("127.0.0.1", "GIM", true)
}