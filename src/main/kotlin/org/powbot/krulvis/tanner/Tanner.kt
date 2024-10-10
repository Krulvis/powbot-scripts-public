package org.powbot.krulvis.tanner

import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.tanner.Data.BLACK_D_HIDE
import org.powbot.krulvis.tanner.Data.BLUE_D_HIDE
import org.powbot.krulvis.tanner.Data.COWHIDE_OPTION
import org.powbot.krulvis.tanner.Data.GREEN_D_HIDE
import org.powbot.krulvis.tanner.Data.HARD_LEATHER
import org.powbot.krulvis.tanner.Data.HIDE_OPTION
import org.powbot.krulvis.tanner.Data.RED_D_HIDE
import org.powbot.krulvis.tanner.Data.SNAKESKIN_15
import org.powbot.krulvis.tanner.Data.SNAKESKIN_20
import org.powbot.krulvis.tanner.Data.SOFT_LEATHER
import org.powbot.krulvis.tanner.Data.getHideForOption
import org.powbot.krulvis.tanner.tree.branch.HasSupplies

@ScriptManifest("krul Tanner", "Tans hides in Crafting Guild", "Krulvis", "1.0.0", category = ScriptCategory.Crafting)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			HIDE_OPTION,
			"Which hide to tan",
			OptionType.STRING,
			defaultValue = "ALL",
			allowedValues = ["ALL", SOFT_LEATHER, HARD_LEATHER, SNAKESKIN_15, SNAKESKIN_20, GREEN_D_HIDE, BLUE_D_HIDE, RED_D_HIDE, BLACK_D_HIDE]
		),
		ScriptConfiguration(
			COWHIDE_OPTION,
			"How to tan cowhides?",
			OptionType.STRING,
			defaultValue = SOFT_LEATHER,
			allowedValues = [SOFT_LEATHER, HARD_LEATHER],
			visible = true
		),
	]
)
class Tanner : KrulScript() {

	@ValueChanged(HIDE_OPTION)
	fun hideOption(hideText: String) {
		updateVisibility(COWHIDE_OPTION, hideText == "ALL")
	}

	var hide: Data.Hide = Data.Hide.SOFT_LEATHER
	val all by lazy { getOption<String>(HIDE_OPTION) == "ALL" }

	override fun onStart() {
		super.onStart()
		hide = getHideForOption(getOption(if (all) COWHIDE_OPTION else HIDE_OPTION)) ?: return
	}

	override fun createPainter(): ATPaint<*> = TannerPainter(this)

	override val rootComponent: TreeComponent<*> = HasSupplies(this)
}

fun main() {
	Tanner().startScript("127.0.0.1", "GIM", false)
}
