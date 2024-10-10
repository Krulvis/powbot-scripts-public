package org.powbot.krulvis.vorkath

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Npc
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.EQUIPMENT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.fighter.MONSTER_TELEPORT_OPTION
import org.powbot.krulvis.vorkath.tree.branches.Fighting

@ScriptManifest("krul Vorkath", "Kills Vorkath", "Krulvis", "1.0.0", priv = true)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(EQUIPMENT_OPTION, "What to wear?", OptionType.EQUIPMENT),
		ScriptConfiguration(INVENTORY_OPTION, "What to bring in Inventory?", OptionType.INVENTORY),
		ScriptConfiguration(BANK_TELEPORT_OPTION, "How to get to bank?", OptionType.STRING),
		ScriptConfiguration(MONSTER_TELEPORT_OPTION, "How to get to Vorkath?", OptionType.STRING),
	]
)
class Vorkath : KillerScript(true) {

	val equipment by lazy { EquipmentRequirement.forOption(getOption(EQUIPMENT_OPTION)) }
	val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val vorkathTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }

	var vorkath: Npc = Npc.Nil

	override val ammoIds: IntArray by lazy {
		intArrayOf(
			equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.item?.id ?: -1,
			*inventory.filter { it.item.stackable }.map { it.item.id }.toList().toIntArray()
		)
	}

	override fun GroundItem.isLoot(): Boolean {
		val name = name().lowercase()
		return Data.lootNames.any { it in name }
	}

	override fun createPainter(): ATPaint<*> = VorkathPainter(this)

	override val rootComponent: TreeComponent<*> = Fighting(this)
}