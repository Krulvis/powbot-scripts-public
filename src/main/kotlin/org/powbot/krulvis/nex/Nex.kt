package org.powbot.krulvis.nex

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Npc
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.demonicgorilla.lootNames
import org.powbot.krulvis.fighter.EQUIPMENT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.nex.tree.branch.InArena

@ScriptManifest("krul Nex", "Kills Nex", "Krulvis", "1.0.0", category = ScriptCategory.Combat, priv = true)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(EQUIPMENT_OPTION, "What do you want to wear?", OptionType.EQUIPMENT),
		ScriptConfiguration(INVENTORY_OPTION, "What do you want to bring?", OptionType.INVENTORY),
	]
)
class Nex : KillerScript(true) {

	val equipment by lazy { EquipmentRequirement.forOption(getOption(EQUIPMENT_OPTION)) }
	val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }

	var nex = Npc.Nil

	override val ammoIds: IntArray by lazy {
		intArrayOf(equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.item?.id ?: -1)
	}

	override fun GroundItem.isLoot(): Boolean {
		val name = name().lowercase()
		return lootNames.any { name in it.lowercase() }
	}

	override fun createPainter(): ATPaint<*> = NexPainter(this)

	override val rootComponent: TreeComponent<*> = InArena(this)
}