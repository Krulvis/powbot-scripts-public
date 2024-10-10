package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.Notifications
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Skills
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.tithe.TitheFarmer
import org.powbot.mobile.script.ScriptManager

class Start(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Starting") {

	override fun execute() {
		if (script.lastRound) {
			Notifications.showNotification("Stopped because it was last round")
			script.logger.info("Stopped because it was last round")
			ScriptManager.stop()
			return
		}
		if (!script.hasSeeds()) {
			script.logger.info("Getting seeds")
			if (!Chat.chatting()) {
				val table = Objects.stream(25).name("Seed table").findFirst()
				script.logger.info("Interacting with seed table=${table.isPresent}")
				table.ifPresent {
					if (walkAndInteract(it, "Search", useMenu = false)) {
						Condition.wait({ Chat.chatting() }, 250, 10)
					}
				}
			}
			val seed = getSeedInput()
			script.logger.info("Selecting seed: $seed")
			if (!Chat.pendingInput() && Chat.chatting() && Chat.continueChat(seed)) {
				Condition.wait({ Chat.pendingInput() }, 100, 20)
			}
			if (Chat.pendingInput() && Chat.sendInput(10000)) {
				Condition.wait({ script.hasSeeds() }, 100, 20)
			}

		} else {
			script.logger.info("Interacting with door")
			Objects.stream(25).name("Farm door").findFirst().ifPresent {
				if (walkAndInteract(it, "Open", useMenu = false)) {
					Condition.wait({ script.getPoints() >= 0 }, 250, 40)
				}
			}
		}
	}

	fun getSeedInput(): String {
		val farming = Skills.level(Constants.SKILLS_FARMING)
		return if (farming >= 74) {
			"Logavano"
		} else if (farming >= 54) {
			"Bologano"
		} else {
			"Golovanova"
		}
	}
}