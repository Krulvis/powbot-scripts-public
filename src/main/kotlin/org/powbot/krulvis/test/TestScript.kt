package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.mixology.Data.mixingPotion
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "", priv = true)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "rocks",
			description = "Click som rocks",
			optionType = OptionType.GAMEOBJECT_ACTIONS,
		),
		ScriptConfiguration(
			name = "Extra info",
			description = "Here comes extra info \n new lines?",
			optionType = OptionType.INFO
		),
		ScriptConfiguration(
			name = "tile",
			description = "Get Tile?",
			optionType = OptionType.TILE,
			defaultValue = "{\"floor\":0,\"x\":1640,\"y\":3944,\"rendered\":true}"
		),
		ScriptConfiguration(
			name = "rocks1",
			description = "NPCS?",
			optionType = OptionType.NPC_ACTIONS,
		),
		ScriptConfiguration(
			name = "rocks1",
			description = "ALL ACTIONS?",
			optionType = OptionType.GAME_ACTIONS,
		),
		ScriptConfiguration(
			name = "rocks2",
			description = "Want to have 0?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		),
		ScriptConfiguration(
			name = "rocks3",
			description = "Select",
			optionType = OptionType.STRING,
			defaultValue = "2",
			allowedValues = ["1", "2", "3"]
		),
	]
)
class TestScript : KrulScript() {
	override fun createPainter(): ATPaint<*> = TestPainter(this)

	var mixers: List<GameObject> = emptyList()

	val VARP_LYE_RESIN: Int = 4414
	val VARP_AGA_RESIN: Int = 4415
	val VARP_MOX_RESIN: Int = 4416
	var PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS: Int = 7064
	var rift = GameObject.Nil
	override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
	}

	@Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
//		logger.info("$e")
	}

	@Subscribe
	fun onMsg(e: MessageEvent) {
//		logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
	}

	@Subscribe
	fun onInventoryChange(evt: InventoryChangeEvent) {
		if (!painter.paintBuilder.trackingInventoryItem(evt.itemId)) {
//            painter.paintBuilder.trackInventoryItem(evt)
		}
	}

	@Subscribe
	fun onNpcAnimation(e: NpcAnimationChangedEvent) {
		val npc = e.npc
		if (npc.healthPercent() < 8 && npc.healthBarVisible()) {
			logger.info("DeadAnim=${e.animation}")
		}
	}


}

class TestPainter(script: TestScript) : ATPaint<TestScript>(script) {

	fun combatWidget(): Widget? {
		return Widgets.stream().firstOrNull { it.components().any { c -> c.text() == "Bloodveld" } }
	}

	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("MixingPotion") { mixingPotion().toString() }
			.addString("BUILD_REAGENTS") { Varpbits.value(script.PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS).toString() }
			.addString("VARP_LYE_RESIN") { Varpbits.value(script.VARP_LYE_RESIN).toString() }
			.addString("VARP_AGA_RESIN") { Varpbits.value(script.VARP_AGA_RESIN).toString() }
			.addString("VARP_MOX_RESIN") { Varpbits.value(script.VARP_MOX_RESIN).toString() }
			.build()
	}

	override fun paintCustom(g: Rendering) {
		g.setColor(Color.RED)
//		val rift = script.rift
//		rift.setBoundsUsingOrientation()
//		rift.boundingModel()?.drawWireFrame()
	}

	fun Tile.toWorld(): Tile {
		val a = Game.mapOffset()
		return this.derive(+a.x(), +a.y())
	}

}

private enum class Direction(val bounds: IntArray, vararg val orientations: Int) {
	NORTH(intArrayOf(-32, 32, -214, -122, -52, -58), 7),
	EAST(intArrayOf(-52, -58, -214, -122, -32, 32), 4),
	SOUTH(intArrayOf(-32, 32, -214, -122, 52, 58), 5),
	WEST(intArrayOf(52, 58, -214, -122, -32, 32), 6), NIL(intArrayOf());


	companion object {
		fun GameObject.direction() = values().firstOrNull { orientation() in it.orientations } ?: NIL
		fun GameObject.setBoundsUsingOrientation() {
			val direction = direction()
			this.bounds(direction.bounds)
		}
	}
}

fun main() {
	TestScript().startScript("127.0.0.1", "GIM", true)
}
