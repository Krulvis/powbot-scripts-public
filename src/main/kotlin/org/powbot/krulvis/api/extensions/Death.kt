package org.powbot.krulvis.api.extensions

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.slf4j.LoggerFactory

object Death {

	private val logger = LoggerFactory.getLogger(javaClass.simpleName)

	private const val RETRIEVAL_ROOT = 669

	enum class Entrance(val objName: String, val objAction: String, val tile: Tile) {
		LUMBRIDGE("Death's Domain", "Enter", Tile(3238, 3193, 0)),
		EDGEVILLE("Death's Domain", "Enter", Tile(3097, 3477, 0)),
		SEERS("Death's Domain", "Enter", Tile(2714, 3466, 0)),
		FALADOR("Death's Domain", "Enter", Tile(2978, 9738, 0)),
		FEROX("Death's Domain", "Enter", Tile(3126, 3631, 0)),
		;

		fun findObject() = Objects.stream(tile, GameObject.Type.INTERACTIVE).name(objName).action(objAction).first()

		companion object {
			fun nearest(): Entrance = values().minByOrNull { it.tile.distance() } ?: LUMBRIDGE
		}
	}

	val options = arrayOf(
		"Can I collect the items from that gravestone now?",
		"Bring my items here now; I'll pay your fee."
	)

	fun handleConversation(): Boolean {
		var chatting = Chat.chatting()
		val death = Npcs.stream().name("Death").action("Talk-to").first()
		if (!chatting || !gravestoneActive()) {
			if (!death.valid()) return false
			if (!gravestoneActive() && death.actions().contains("Collect")) {
				logger.info("Gravestone no longer active, opening collection")
				if (death.interact("Collect")) {
					return waitForDistance(death, extraWait = 1200) { itemRetrievalComp().visible() }
				}
			} else if (death.interact("Talk-to")) {
				logger.info("Talking to death to ask if items can be brought here.")
				chatting = waitForDistance(death, extraWait = 1200) { Chat.chatting() }
			}
		}
		if (chatting) {
			val chatTimer = Timer(5000)
			do {
				if (Chat.canContinue()) {
					if (Chat.clickContinue()) {
						waitFor { Chat.stream().text(*options).first().valid() }
					}

				} else {
					val option = Chat.stream().text(*options).first()
					if (option.valid()) {
						option.select()
						sleep(600)
					} else {
						death.interact("Talk-to")
					}
					waitFor { Chat.stream().text(*options).first() != option }
				}
			} while (!itemRetrievalComp().visible() && !chatTimer.isFinished())
		}
		return itemRetrievalComp().visible()
	}

	fun retrievalItems(): List<Pair<Component, Int>> =
		Components.stream(RETRIEVAL_ROOT, 3)
			.filtered { it.itemId() > 0 && it.itemStackSize() > 0 && it.visible() }
			.toList()
			.map { it to it.itemId() }

	fun close(): Boolean {
		val closeComp = Components.stream(RETRIEVAL_ROOT).action("Close").first()
		return !closeComp.valid() || closeComp.click()
	}

	fun itemRetrievalComp() = Components.stream(RETRIEVAL_ROOT, 10).action("Take-All").first()

	const val GRAVESTONE_TILE_VARP = 3916

	//	const val GRAVESTONE_ACTIVE_VARP = 843
	const val GRAVESTONE_TIMER_VARP = 1697

	fun timeRemaining() = Varpbits.varpbit(GRAVESTONE_TIMER_VARP) / 427.844
	fun gravestoneActive() = Varpbits.varpbit(GRAVESTONE_TIMER_VARP) > 0

	fun gravestoneTile(): Tile {
		val v = Varpbits.varpbit(GRAVESTONE_TILE_VARP)
		val x = (v.shr(14) and 0x3FFF)
		val y = (v and 0x3FFF)
		val z = v.shr(28) and 0x3
		return Tile(x, y, z)
	}
}