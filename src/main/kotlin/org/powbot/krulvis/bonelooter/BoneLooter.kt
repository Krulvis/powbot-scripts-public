package org.powbot.krulvis.bonelooter

import org.powbot.api.Area
import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import kotlin.random.Random

@ScriptManifest(
	name = "krul BonesLooter",
	description = "Loots and buries bones, then hops to another random F2P world",
	version = "3.1.7",
	author = "Krulvis",
	scriptId = "6f918c67-9913-4702-ad5d-ee6e7d6531c6",
	priv = true
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "enableWorldHopping",
			description = "Enable or disable world hopping if no bones are available or if a player is near",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		)
	]
)
class BonesLooter : AbstractScript() {

	private val boneIds = intArrayOf(532, 526)

	private var boneAREA: Area = Area(
		Tile(1446, 3611, 0),
		Tile(1446, 3607, 0),
		Tile(1449, 3607, 0),
		Tile(1449, 3611, 0),
	)

	private var lastPosition: Tile? = null

	val hopping by lazy { getOption<Boolean>("enableWorldHopping") }

	override fun poll() {
		if (hopping && playerNearby()) {
			logger.info("Condition met for world hopping (player nearby), hopping to a random P2P world.")
			hopToRandomP2PWorld()
			lastPosition = Players.local().tile()
			if (!waitForMovement()) {
				logger.info("Bot did not move after hopping, restarting loop.")
				return
			}
		} else {
			buryBones()
			lootBones()
			val randomEnergyThreshold = Random.nextInt(35, 76) // Random number between 35 and 75
			if (Movement.energyLevel() > randomEnergyThreshold && !Movement.running()) {
				logger.info("Enabling running as energy level is above $randomEnergyThreshold.")
				Movement.running(true) // Enable running
			}
		}
	}

	override fun onStart() {
		val paint = PaintBuilder.newBuilder()
			.x(40)
			.y(80)
			.trackInventoryItem(boneIds[0], "Big Bones")
			.trackInventoryItem(boneIds[1], "Bones")
			.trackSkill(Skill.Prayer)
			.build()
		addPaint(paint)
		logger.info("Script started.")
	}

	private fun lootBones() {
		val bones = GroundItems.stream()
			.id(*boneIds)
			.filtered { boneAREA.contains(it.tile()) }
			.nearest()
			.first()
		bones.let {
			val emptySlots = Inventory.emptySlotCount()
			if (it.interact("Take")) {
				logger.info("Looting bones.")
				Condition.wait({ Inventory.emptySlotCount() < emptySlots }, 10, 50)
			}
		}
	}

	private fun buryBones() {
		val bones: Item = Inventory.stream().id(*boneIds).firstOrNull() ?: return
		val slot = bones.inventoryIndex()
		bones.let {
			if (it.interact("Bury")) {
				logger.info("Burying bones.")
				Condition.wait({ !Inventory.itemAt(slot).valid() }, 10, 50)
			}
		}
	}

	private fun hopToRandomP2PWorld() {
		val random = Worlds.stream()
			.filtered {
				it.type() == World.Type.MEMBERS && it.population >= 15 && it.specialty() != World.Specialty.BOUNTY_HUNTER
					&& it.specialty() != World.Specialty.PVP
					&& it.specialty() != World.Specialty.TARGET_WORLD
					&& it.specialty() != World.Specialty.PVP_ARENA
					&& it.specialty() != World.Specialty.DEAD_MAN
					&& it.specialty() != World.Specialty.BETA
					&& it.specialty() != World.Specialty.HIGH_RISK
					&& it.specialty() != World.Specialty.LEAGUE
					&& it.specialty() != World.Specialty.SKILL_REQUIREMENT
					&& it.specialty() != World.Specialty.TWISTED_LEAGUE
					&& it.specialty() != World.Specialty.SPEEDRUNNING
					&& it.specialty() != World.Specialty.FRESH_START
					&& it.specialty() != World.Specialty.TRADE
			}
			.toList().random()
		logger.info("Hopping to world ${random.number}.")
		random.hop()
	}

	private fun playerNearby(): Boolean {
		val nearbyPlayer = Players.stream()
			.within(20).firstOrNull { it != Players.local() }
		return nearbyPlayer != null
	}

	private fun waitForMovement(): Boolean {
		val moved = Condition.wait({
			Players.local().tile() != lastPosition
		}, 500, 10)
		if (moved) {
			logger.info(/* p0 = */ "Bot has moved to a new position.")
		} else {
			println("Bot did not move after hopping.")
		}
		return moved
	}
}

fun main() {
	val script = BonesLooter()
	script.startScript("localhost", "", true)
}
