package org.powbot.krulvis.grotesqueguardians

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.tormenteddemon.MELEE_EQUIPMENT_OPTION
import org.powbot.krulvis.tormenteddemon.MELEE_PRAYER_OPTION
import org.powbot.krulvis.tormenteddemon.RANGE_EQUIPMENT_OPTION
import org.powbot.krulvis.tormenteddemon.RANGE_PRAYER_OPTION
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.grotesqueguardians.tree.branch.ShouldBank

@ScriptManifest(name = "krul GrotesqueGuardians", description = "Kills grotesuqe guardians", version = "1.0.0", priv = true, category = ScriptCategory.Combat)
@ScriptConfiguration.List([
	ScriptConfiguration(MELEE_EQUIPMENT_OPTION, "What melee to wear?", OptionType.EQUIPMENT),
	ScriptConfiguration(MELEE_PRAYER_OPTION, "Which melee offensive prayer?", OptionType.STRING, defaultValue = "PIETY", allowedValues = ["CHIVALRY", "PIETY"]),
	ScriptConfiguration(RANGE_EQUIPMENT_OPTION, "What range to wear?", OptionType.EQUIPMENT),
	ScriptConfiguration(RANGE_PRAYER_OPTION, "Which range offensive prayer?", OptionType.STRING, defaultValue = "EAGLE_EYE", allowedValues = ["EAGLE_EYE", "RIGOURv"]),
	ScriptConfiguration(BANK_TELEPORT_OPTION, "How to teleport to bank?", OptionType.STRING, defaultValue = CASTLE_WARS_JEWELLERY_BOX, allowedValues = [CASTLE_WARS_JEWELLERY_BOX]),
])
class GrotesqueGuardians : KrulScript() {
	override fun createPainter(): ATPaint<*> = GGPaint(this)

	var banking = false
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }

	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}