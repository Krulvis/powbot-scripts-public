package org.powbot.krulvis.cerberus

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.cerberus.Data.FLINCH_OPTION
import org.powbot.krulvis.cerberus.tree.branch.AtCerb
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.EQUIPMENT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.fighter.SPECIAL_EQUIPMENT_OPTION

@ScriptManifest("krul Cerberus", "Kills Cerberus", "Krulvis", "1.0.0", category = ScriptCategory.Combat, priv = true)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(EQUIPMENT_OPTION, "What to wear?", OptionType.EQUIPMENT),
		ScriptConfiguration(SPECIAL_EQUIPMENT_OPTION, "What to wear for special attack?", OptionType.EQUIPMENT),
		ScriptConfiguration(INVENTORY_OPTION, "What to bring?", OptionType.INVENTORY),
		ScriptConfiguration(FLINCH_OPTION, "Flinch Cerberus (walk under)?", OptionType.BOOLEAN),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION, "How to get to bank?", OptionType.STRING,
			defaultValue = CASTLE_WARS_JEWELLERY_BOX, allowedValues = ["NONE", CASTLE_WARS_JEWELLERY_BOX]
		),
	]
)
class Cerberus : KillerScript(true) {
	val equipment by lazy { EquipmentRequirement.forOption(getOption(EQUIPMENT_OPTION)) }
	val equipmentSpecial by lazy { EquipmentRequirement.forOption(getOption(SPECIAL_EQUIPMENT_OPTION)) }
	val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }
	val flinch by lazy { getOption<Boolean>(FLINCH_OPTION) }

	var cerberus = Npc.Nil

	override val ammoIds: IntArray = intArrayOf(-1)

	override fun GroundItem.isLoot(): Boolean {
		TODO("Not yet implemented")
	}

	override fun createPainter(): KrulPaint<*> = CerbPainter(this)

	override val rootComponent: TreeComponent<*> = AtCerb(this)


	@Subscribe
	fun onTick(e: TickEvent) {
		cerberus = Npcs.stream().name("Cerberus").first()
	}
}