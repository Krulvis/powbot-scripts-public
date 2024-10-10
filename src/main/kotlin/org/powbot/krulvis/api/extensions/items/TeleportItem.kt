package org.powbot.krulvis.api.extensions.items

import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TeleportItem")

interface ITeleportItem : Item {
	fun teleport(destination: String): Boolean

	fun getCharges(): Int

	companion object {
		fun isTeleportItem(id: Int) = getTeleportItem(id) != null

		fun getTeleportItem(id: Int): ITeleportItem? =
			TeleportItem.getTeleportItem(id) ?: TeleportEquipment.getTeleportEquipment(id)

	}
}

enum class TeleportItem(override val itemName: String, override val ids: IntArray) : ITeleportItem {
	ROYAL_SEED_POD("Royal seed pod", intArrayOf(19564)),
	;

	override val stackable: Boolean = false

	override fun hasWith(): Boolean = inInventory()

	override fun getCharges(): Int = Int.MAX_VALUE

	override fun getCount(countNoted: Boolean): Int = getInventoryCount(false)
	override fun teleport(destination: String): Boolean {
		logger.info("$itemName Teleport to: $destination")
		Bank.close()
		val w = if (Widgets.widget(300).componentCount() >= 91) Widgets.widget(300).component(91) else null
		if (w != null && w.interact("Close")) {
			sleep(Random.nextInt(200, 500))
		}
		return Inventory.stream().id(*ids).firstOrNull()?.interact(destination) == true
	}

	companion object {
		fun isTeleportItem(id: Int) = getTeleportItem(id) != null

		fun getTeleportItem(id: Int): TeleportEquipment? {
			for (ti in TeleportEquipment.values()) {
				for (i in ti.ids) {
					if (i == id) {
						return ti
					}
				}
			}
			return null
		}
	}
}

enum class TeleportEquipment(
	override val itemName: String,
	override val slot: Equipment.Slot,
	override vararg val ids: Int,
) : IEquipmentItem, ITeleportItem {
	GLORY("Amulet of Glory", Equipment.Slot.NECK, 11978, 11976, 1712, 1710, 1708, 1706),
	GAMES("Games necklace", Equipment.Slot.NECK, 3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867),
	ROD("Ring of dueling", Equipment.Slot.RING, 2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566),
	ROW("Ring of wealth", Equipment.Slot.RING, 11980, 11982, 11984, 11986, 11988),
	SKILLS("Skills Necklace", Equipment.Slot.NECK, 11968, 11970, 11105, 11107, 11109, 11111),
	WARRIORS("Warriors Bracelet", Equipment.Slot.HANDS, 11972, 11974, 11118, 11120, 11122, 11124),
	BURNING("Burning Amulet", Equipment.Slot.NECK, 21166, 21169, 21171, 21173, 21175),
	COMBAT("Combat Bracelet", Equipment.Slot.HANDS, 11972, 11974, 11118, 11120, 11122, 11124),
	SLAYER("Slayer ring", Equipment.Slot.RING, 21268, 11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873),
	RADAS_BLESSING("Rada's Blessing", Equipment.Slot.QUIVER, 22945, 22947),
	GHOMMAL_HILT("Ghommal's Hilt", Equipment.Slot.OFF_HAND, 25936, 25934, 25932, 25930, 25928),
	ARD_CLOAK("Ardougne cloak", Equipment.Slot.CAPE, 13121),
	KARAMJA_GLOVES("Karamja gloves", Equipment.Slot.HANDS, 13103, 11140),
	ACHIEVEMENT_DIARY_CAPE("Achievement diary Cape", Equipment.Slot.CAPE, 13069, 19476),
	CRAFTING_CAPE("Crafting cape", Equipment.Slot.CAPE, 9780, 9781),
	MYTH_CAPE("Mythical cape", Equipment.Slot.CAPE, 22114),
	;

	val bestId: Int = ids[0]

	val worseId: Int = ids[ids.size - 1]

	override val stackable: Boolean = false
	fun isPartOfSet(id: Int): Boolean {
		return ids.contains(id)
	}

	private fun org.powbot.api.rt4.Item.getCharges(): Int {
		if (ids.size == 1) return Int.MAX_VALUE
		for (i in ids.indices.reversed()) {
			if (ids[i] == id) {
				return ids.size - i
			}
		}
		return 0
	}

	fun getEquipmentCharges(): Int {
		val item = Equipment.stream().id(*ids).firstOrNull() ?: return 0
		return item.getCharges()
	}

	override fun getCharges(): Int {
		val equipmentCharges = getEquipmentCharges()
		return equipmentCharges + Inventory.get { it.id in ids }.sumOf { it.getCharges() }
	}

	fun getNotedCharges(): Int {
		var tps = 0
		ids.reversed().forEachIndexed { i, id ->
			tps += Inventory.getCount(true, id, id + 1) * (i + 1)
		}
		return tps
	}

	fun canTeleport(): Boolean {
		return when (this) {
			GLORY, ROW -> Combat.wildernessLevel() <= 30
			else -> Combat.wildernessLevel() <= 20
		}
	}

	override fun teleport(place: String): Boolean {
		logger.info("$name Teleport to: $place")
		Bank.close()
		val w = if (Widgets.widget(300).componentCount() >= 91) Widgets.widget(300).component(91) else null
		if (w != null && w.interact("Close")) {
			sleep(Random.nextInt(200, 500))
		}

		if (equip()) {
			if (this == SLAYER) {
				return teleportSlayerRing(place)
			} else if (this == BURNING) {
				return teleportBurning(place)
			}
			var teleport = place
			when (place.lowercase()) {
				"ferox", "enclave" -> teleport = "Ferox Enclave"
				"barb", "barbarian" -> teleport = "Barbarian Outpost"
				"corp" -> teleport = "Corporeal Beast"
				"ge" -> teleport = "Grand Exchange"
				"duel" -> teleport = "Duel Arena"
				"castle", "cw" -> teleport = "Castle Wars"
				"clan" -> teleport = "Clan Wars"
				"edge" -> teleport = "Edgeville"
				"draynor" -> teleport = "Draynor"
				"monastery" -> teleport = "Monastery"
				"crafting" -> teleport = "Crafting Guild"
				"warrior" -> teleport = "Warriors' Guild"
				"champion" -> teleport = "Champions' Guild"
				"ranging" -> teleport = "Ranging Guild"
				"motherlode" -> teleport = "Motherlode Mine"
				"fishing" -> teleport = "Fishing Guild"
				"cooking" -> teleport = "Cooking Guild"
			}
			return Equipment.stream().id(*ids).firstOrNull()?.interact(teleport) == true
		}
		return false
	}


	private fun teleportBurning(location: String): Boolean {
		var okayTeleport = burningComponent()
		if (!okayTeleport.valid()) {
			if (Equipment.stream().id(*ids).first().interact(location)) {
				waitFor {
					okayTeleport = burningComponent()
					okayTeleport.visible()
				}
			}
		}
		if (okayTeleport.valid() && okayTeleport.visible()) {
			return okayTeleport.click()
		}
		return false
	}

	private fun burningComponent() = Components.stream(219, 1).textContains("Okay, teleport").first()

	private fun teleportSlayerRing(location: String): Boolean {
		var teleportWidget = slayerRingTeleportWidget(location)
		logger.info("SlayerRing Teleport to location=$location, componentVisible=${teleportWidget.visible()}")
		if (!teleportWidget.visible()) {
			if (Equipment.stream().id(*ids).first().interact("Teleport")) {
				waitFor {
					teleportWidget = slayerRingTeleportWidget(location)
					teleportWidget.visible()
				}
			}
		}

		if (teleportWidget.valid() && teleportWidget.visible()) {
			return teleportWidget.click()
		}
		return false
	}

	private fun slayerRingTeleportWidget(location: String) =
		Components.stream(219, 1).textContains(location).first()

	override fun withdrawExact(amount: Int, worse: Boolean, wait: Boolean): Boolean {
		val charges = getCharges()
		if (charges >= amount) return true
		val bankItemForCharges = getBankItemForCharges(amount - charges) ?: return false

		if (Bank.withdrawModeNoted(false) && inBank()
			&& Bank.withdraw(bankItemForCharges, 1)
		) {
			return !wait || waitFor(5000) { getCharges() >= amount }
		}
		return false
	}

	private fun getBankItemForCharges(minCharges: Int): org.powbot.api.rt4.Item? {
		val bankItems = Bank.get { it.id in ids }.sortedBy { it.getCharges() }
		return bankItems.firstOrNull { it.getCharges() >= minCharges }
	}


	fun withdraw(): Boolean {
		if (!inEquipment() && !inInventory()) {
			if (Bank.withdrawModeNoted(false) && inBank()
				&& Bank.withdraw(1, getBankId(true))
			) {
				waitFor(5000) { inInventory() }
			}
		}
		return inInventory() || inEquipment()
	}


	companion object {
		fun isTeleportEquipment(id: Int) = getTeleportEquipment(id) != null

		fun getTeleportEquipment(id: Int): TeleportEquipment? {
			for (ti in values()) {
				for (i in ti.ids) {
					if (i == id) {
						return ti
					}
				}
			}
			return null
		}
	}

}


