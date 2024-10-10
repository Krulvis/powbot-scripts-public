package org.powbot.krulvis.api.extensions

import org.powbot.api.Color
import org.powbot.api.Events
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Widgets
import org.slf4j.LoggerFactory

object StatusIcons {
	private val scriptlogger = LoggerFactory.getLogger(javaClass.simpleName)

	private val WIDGET_ID = 651
	private val COMPONENT_ID = 4

	private val ITEM_COMPONENT_ID = 6
	private val TEXT_COMPONENT_ID = 8
	private val TOPTEXT_COMPONENT_ID = 9
	private val BACKGROUND_COMPONENT_ID = 1

	private val componentCount = 11

	val BACKGROUND_COLOR_RED = 14483456
	private val REGEX_POISON = "([0-9]*)([a-z])".toRegex()

	init {
		Events.register(this)
	}

	var activeStatus: Array<Status> = arrayOf()

	@Suppress
	fun onTickEvent(tickEvent: TickEvent) {
		activeStatus = getItems()
	}

	private fun getItems(): Array<Status> {

		val items = Widgets.component(WIDGET_ID, COMPONENT_ID)
		if (!items.valid()) {
			return arrayOf()
		}

		val components = items.components()
		val loops = components.size / componentCount
		val statusItems = mutableListOf<Status>()
		for (i in 0..loops) {
			val modelComponent = components[(i * componentCount) + ITEM_COMPONENT_ID]
			val modelId = if (modelComponent.itemId() != -1) modelComponent.itemId() else modelComponent.textureId()
			val textComponent = components[(i * componentCount) + TEXT_COMPONENT_ID]
			val topTextComponent = components[(i * componentCount) + TOPTEXT_COMPONENT_ID]
			val backgroundComponent = components[(i * componentCount) + BACKGROUND_COMPONENT_ID]

			val textColor = if (textComponent.text().startsWith("<")) Color.RED else Color.WHITE

			statusItems.add(
				Status(
					modelId,
					textComponent.text(),
					topTextComponent.text(),
					textColor,
					backgroundComponent.textColor()
				)
			)
		}

		return statusItems.toTypedArray()
	}

	fun getPoisonImmuneTime(): Int {
		val status = activeStatus.firstOrNull { it.itemId == 2446 } ?: return -1

		val text = REGEX_POISON.find(status.text)
		if (text?.groupValues?.size == 3) {
			val numberText = text.groupValues[1]
			if (numberText.isEmpty()) {
				return -1
			}
			val number = numberText.toInt()
			val time = if (text.groupValues[2] == "m") 60 else 1
			return number * time
		}
		return -1
	}

	fun getPoisonDamage(): Poison {
		val status = activeStatus.firstOrNull { it.itemId == 1632 || it.itemId == 1360 } ?: return Poison(false, -1, -1)
		val timerText = status.text
		val time = if (timerText.contains("m")) timerText.replace("m", "").toInt() * 60
		else if (timerText.contains("s")) timerText.replace("s", "").toInt() else -1
		return if (status.topText.isEmpty() || status.topText.any { !it.isDigit() }) {
			Poison(false, -1, time)
		} else Poison(status.itemId == 1632, status.topText.toInt(), time)
	}

}

data class Poison(val venom: Boolean, val damage: Int, val timeTillHit: Int)

data class Status(val itemId: Int, val text: String, val topText: String, val textColor: Int, val backgroundColor: Int)