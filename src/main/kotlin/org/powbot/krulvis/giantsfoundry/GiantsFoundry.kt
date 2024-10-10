package org.powbot.krulvis.giantsfoundry

import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.giantsfoundry.tree.branch.IsSmithing


@ScriptManifest(
	name = "krul GiantFoundry",
	description = "Makes swords for big giant.",
	author = "Krulvis",
	version = "1.0.7",
	scriptId = "6e058edd-cc5b-4b20-b4aa-6def55e9e903",
	category = ScriptCategory.Smithing,
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			"SingleMetal", "Use only one metal type (not recommended)?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			"FirstMetal", "Which bar to use?",
			optionType = OptionType.STRING,
			defaultValue = ADAMANTITE,
			allowedValues = arrayOf(BRONZE, IRON, STEEL, MITHRIL, ADAMANTITE, RUNITE)
		),
		ScriptConfiguration(
			"SecondMetal", "Which bar to combine with?",
			optionType = OptionType.STRING,
			defaultValue = RUNITE,
			allowedValues = arrayOf(BRONZE, IRON, STEEL, MITHRIL, ADAMANTITE, RUNITE)
		),
		ScriptConfiguration(
			"UseMetalItems", "Do you want to smelt weapons and armour?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		)
	]
)
class GiantsFoundry : KrulScript() {
	override fun createPainter(): ATPaint<*> {
		return GiantsFoundryPainter(this)
	}

	var coins = 0
	var qualities = 0
	var currentAction: Action? = null

	val singleMetal by lazy { getOption<Boolean>("SingleMetal") }
	val useItems by lazy { getOption<Boolean>("UseMetalItems") }
	val firstBar by lazy { Bar.forName(getOption("FirstMetal"))!! }
	val secondBar by lazy { Bar.forName(getOption("SecondMetal"))!! }

	val barsToGet by lazy {
		if (singleMetal) {
			listOf(firstBar to 28)
		} else {
			listOf(firstBar to 14, secondBar to 14)
		}
	}

	override val rootComponent: TreeComponent<*> = IsSmithing(this)

	private val MOULD_JIG_TILE = Tile(3371, 11489, 0)
	val POURING_TILE = Tile(3374, 11492, 0)

	fun inventoryBarCounts(): Map<Bar, Int> = BARS.associateWith { it.crucibleInventoryCount() }

	fun correctCrucibleCount(bar: Bar) = bar.crucibleCount() == if (singleMetal) 28 else 14

	fun isSmithing() = Equipment.stream().name("Preform").isNotEmpty()

	fun jig() = Objects.stream(MOULD_JIG_TILE, GameObject.Type.INTERACTIVE)
		.nameContains("Mould jig").first()

	fun areBarsPoured() = jig().name.contains("(Poured metal)")

	fun activeActionComp(): Component = Widgets.component(ROOT, 76)

	fun kovac() = Npcs.stream().name("Kovac").firstOrNull()

	fun hasCommission() = Varpbits.varpbit(JOB_VARP, 0, 63) != 0

	fun mouldWidgetOpen() = mouldWidget().component(2).any { it?.text() == "Giants' Foundry Mould Setup" }

	fun activeAction(): Action? {
		val activeComp = activeActionComp()
		val x = activeComp.x()
		val maxX = x + activeComp.width()
		val actionComps = Components.stream(ROOT, 75)
			.filtered { it.x() in x..maxX }.list()
		val actionComp = actionComps.firstOrNull { Action.forTexture(it.textureId()) != null } ?: return null
		return Action.forTexture(actionComp.textureId())
	}

	fun interactObj(obj: GameObject, action: String): Boolean {
		if (obj.distance() > 3) Movement.step(obj.tile)
		return Utils.waitFor { obj.inViewport() } && obj.interact(action)
	}

	@com.google.common.eventbus.Subscribe
	fun onTickEvent(_e: TickEvent) {
		currentAction = activeAction()
	}

	fun stopActivity(tile: Tile?) = Movement.step(tile ?: me.tile())

	fun parseResults() {
		val comp = Widgets.component(229, 1)
		if (!comp.visible()) return
		val text = comp.text()
		if (!text.contains("Sword completed in:")) return
		val quality = text.substring(text.indexOf("quality: ") + 9, text.indexOf("<br>")).toInt()
		val smithingXp = text.substring(text.indexOf("You're awarded: ") + 16, text.indexOf(" Smithing XP")).toInt()
		val coinsGained = text.substring(text.indexOf("XP and ") + 7, text.indexOf(" coins")).replace(",", "").toInt()
		coins += coinsGained
		qualities += quality
	}


	@ValueChanged("SingleMetal")
	fun onSingleMetal(singleMetal: Boolean) {
		updateVisibility("SecondMetal", !singleMetal)
	}


}

fun main() {
	GiantsFoundry().startScript("127.0.0.1", "GIM", false)
}