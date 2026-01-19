package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.powbot.api.EventFlows
import org.powbot.api.event.TickEvent
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

class Test {

	fun onSomething(e: String) {}

	@Subscribe
	fun onInt(e: Int) {

	}

	@Subscribe
	fun onTick(e: TickEvent) {

	}
}

fun main() {
	val test = Test()
	val tickListeners = test::class.memberFunctions.filter {
		it.findAnnotation<Subscribe>() != null && it.valueParameters.size == 1 &&
			it.valueParameters[0].type.classifier == TickEvent::class
	}
	println(tickListeners)

	tickListeners.forEach { tickListener ->
		tickListener.isAccessible = true

		CoroutineScope(Dispatchers.Default).launch {
			EventFlows.ticks().collect { tickListener.call(test, it) }
		}
	}
}

