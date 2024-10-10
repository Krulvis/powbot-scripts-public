package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.GameActionEvent

val tagFilter = Regex("<[^>]+>([^<]+)</[^>]+>")

object GameActions {

	fun GameActionEvent.isUsingItem(): Boolean {
		return rawEntityName.contains("->")
	}

	fun GameActionEvent.itemUsing() = tagFilter.findAll(rawEntityName).first().destructured.component1()
}

fun main() {
	val rawEntityName = "<col=ff9040>Knife</col><col=ffffff> -> <col=ff9040>Maple logs</col>"
	println(tagFilter.findAll(rawEntityName).first().destructured.component1())
}
