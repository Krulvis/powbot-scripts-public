package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Objects
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering
import kotlin.math.abs

@ScriptManifest("giantFoundryDebug", "Debug GiantFoundry", priv = true)
class GiantFoundryDebug : KrulScript() {
	override fun createPainter(): ATPaint<*> = DebugPainter(this)
	override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "Debug") {
		sleep(1000)
		val selectionContainer = mouldWidget().component(MOULD_SELECTION_CONTAINER)
		selectionContainer.firstOrNull { it?.textColor() == MOULD_SELECTED_COLOR } ?: Component.Nil
		val index = selectionContainer.indexOf(selectionContainer)
		val selectedTexture = selectionContainer.component(index + 1).textureId()
		val selectedMouldTexture = MouldType.values().firstOrNull { it.open() }?.selectedTexture() ?: 0
		logger.info(
			"SelectedTexture=$selectedTexture, MouldTexture=$selectedMouldTexture, difference=${
				abs(
					selectedTexture - selectedMouldTexture
				)
			}"
		)

		val mouldJig = Objects.stream().nameContains("Mould jig").first()

		val objectsAtMouldJig = Objects.stream().within(mouldJig, 1).toList()
		logger.info(
			"MouldJig=${mouldJig.name}, actions=[${
				mouldJig.actions().joinToString()
			}], id=${mouldJig.id}, tile=${mouldJig.tile}, objects=${objectsAtMouldJig.size}, objects=[${objectsAtMouldJig.map { it.name }}]"
		)
	}
}

class DebugPainter(script: GiantFoundryDebug) : ATPaint<GiantFoundryDebug>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder
			.addString("Forte") { "open=${MouldType.Forte.open()}, selected=${MouldType.Forte.selected()}, selectedTexture=${MouldType.Forte.selectedTexture()}" }
			.addString("Blades") { "open=${MouldType.Blades.open()}, selected=${MouldType.Blades.selected()}, selectedTexture=${MouldType.Blades.selectedTexture()}" }
			.addString("Tips") { "open=${MouldType.Tips.open()}, selected=${MouldType.Tips.selected()}, selectedTexture=${MouldType.Tips.selectedTexture()}" }
//            .addString("SelectedTexture") {
//                "texture=${getSelectedMouldTexture()}, difference=${
//                    abs(
//                        getSelectedMouldTexture() - (MouldType.values().firstOrNull { it.open() }?.selectedComp() ?: 0)
//                    )
//                }"
//            }

		BARS.forEachIndexed { index, bar ->
			paintBuilder.addString(bar.itemName) { "${bar.crucibleCount()}" }
		}
		return paintBuilder.build()
	}

	override fun paintCustom(g: Rendering) {
	}

}

fun main() {
	GiantFoundryDebug().startScript("127.0.0.1", "", true)
}