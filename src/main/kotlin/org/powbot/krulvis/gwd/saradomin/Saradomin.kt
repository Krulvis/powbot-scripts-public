package org.powbot.krulvis.gwd.saradomin

import org.powbot.api.Tile
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.teleports.GWD_GHOMMAL_HILT
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.fighter.MONSTER_TELEPORT_OPTION
import org.powbot.krulvis.gwd.GWD
import org.powbot.krulvis.gwd.GWD.EQUIPMENT_BODYGUARDS_OPTION
import org.powbot.krulvis.gwd.GWD.EQUIPMENT_GENERAL_OPTION
import org.powbot.krulvis.gwd.GWD.EQUIPMENT_KILLCOUNT_OPTION
import org.powbot.krulvis.gwd.GWD.STACK_BODYGUARDS_OPTION
import org.powbot.krulvis.gwd.GWDScript
import org.powbot.krulvis.gwd.branches.InsideGeneralRoom
import org.powbot.krulvis.gwd.saradomin.tree.CanAttack

@ScriptManifest(
	"krul GWD Saradomin",
	"Kills Commander Zilyana",
	"Krulvis",
	"1.0.0",
	scriptId = "f6f38057-4914-4e37-9ddb-1ea7d90b7634",
	priv = true,
	category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(EQUIPMENT_GENERAL_OPTION, "What to wear killing Zilyana and Mage?", OptionType.EQUIPMENT),
		ScriptConfiguration(
			EQUIPMENT_BODYGUARDS_OPTION,
			"What to wear killing melee & range bodyguard?",
			OptionType.EQUIPMENT
		),
		ScriptConfiguration(
			EQUIPMENT_KILLCOUNT_OPTION,
			"What to wear when getting KC?",
			OptionType.EQUIPMENT
		),
		ScriptConfiguration(INVENTORY_OPTION, "What to bring in Inventory?", OptionType.INVENTORY),
		ScriptConfiguration(STACK_BODYGUARDS_OPTION, "Stack melee and range bodyguard?", OptionType.BOOLEAN),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION,
			"How to get to bank?",
			OptionType.STRING,
			defaultValue = CASTLE_WARS_JEWELLERY_BOX,
			allowedValues = [CASTLE_WARS_JEWELLERY_BOX]
		),
		ScriptConfiguration(
			MONSTER_TELEPORT_OPTION,
			"How to get to GWD?",
			OptionType.STRING,
			defaultValue = GWD_GHOMMAL_HILT,
			allowedValues = [GWD_GHOMMAL_HILT]
		),
	]
)
class Saradomin : GWDScript<Saradomin>(GWD.God.Saradomin) {

	override val rootComponent: TreeComponent<*> = InsideGeneralRoom(this)

	override fun generalAliveBranch(): TreeComponent<Saradomin> = CanAttack(this)
	override fun generalEquipment(): List<EquipmentRequirement> =
		EquipmentRequirement.forOption(getOption(EQUIPMENT_GENERAL_OPTION))

	override fun meleeEquipment(): List<EquipmentRequirement> =
		EquipmentRequirement.forOption(getOption(EQUIPMENT_BODYGUARDS_OPTION))

	override fun rangeEquipment(): List<EquipmentRequirement> =
		EquipmentRequirement.forOption(getOption(EQUIPMENT_BODYGUARDS_OPTION))

	override fun mageEquipment(): List<EquipmentRequirement> =
		EquipmentRequirement.forOption(getOption(EQUIPMENT_GENERAL_OPTION))

	override fun kcEquipment(): List<EquipmentRequirement> =
		EquipmentRequirement.forOption(getOption(EQUIPMENT_KILLCOUNT_OPTION))

	override fun createPainter(): ATPaint<*> = SaradominPainter(this)

	val deltas = arrayOf(
		intArrayOf(-8, -5),
		intArrayOf(-8, 1),
		intArrayOf(-2, 9),
		intArrayOf(2, 9),
		intArrayOf(8, 3),
		intArrayOf(7, -2),
		intArrayOf(2, -8),
		intArrayOf(-3, -7)
	)

	var lastAttackTile = Tile.Nil
	var tileArrival = Timer(1200)

	val zilyanaTiles: Array<Tile>
		get() {
			val ct = god.generalArea.centralTile
			return deltas.map { Tile(ct.x - it[0], ct.y - it[1]) }.toTypedArray()
		}
}

fun main() {
	Saradomin().startScript("127.0.0.1", "GIM", false)
}