package org.powbot.krulvis.api.extensions

import org.powbot.api.Locatable
import org.powbot.api.Random
import org.powbot.api.rt4.Movement
import kotlin.math.min


object Utils {

	private val OSRS_BOX_URL = "https://api.osrsbox.com/equipment/"

	fun sleep(delay: Int) {
		Thread.sleep(delay.toLong())
	}

	fun sleep(min: Int, max: Int) {
		Thread.sleep(Random.nextInt(min, max).toLong())
	}

	fun short(): Int = Random.nextInt(600, 1000)

	fun mid(): Int = Random.nextInt(1000, 2500)

	fun long(): Int = Random.nextInt(5000, 8000)

	fun waitFor(min: Int, max: Int, condition: () -> Boolean): Boolean {
		return waitFor(
			Random.nextInt(
				min,
				max
			), condition
		)
	}

	fun waitForWhile(timeOut: Int = mid(), condition: () -> Boolean, whileWaiting: () -> Any = {}): Boolean {
		val totalDelay = System.currentTimeMillis() + timeOut
		do {
			whileWaiting()
			if (condition.invoke()) {
				return true
			}
			sleep(
				Random.nextInt(
					50, 100
				)
			)
		} while (totalDelay > System.currentTimeMillis())
		return false
	}

	fun waitFor(timeOut: Int = mid(), condition: () -> Boolean): Boolean = waitForWhile(timeOut, condition)

	fun waitForDistanceWhile(
		locatable: Locatable,
		extraWait: Int = 1200,
		maxWait: Int = 20000,
		condition: () -> Boolean,
		whileWaiting: () -> Any = {}
	): Boolean {
		val distanceWaitingTime = locatable.distance().toInt() * if (Movement.running()) 300 else 600
		return waitForWhile(
			min(distanceWaitingTime + extraWait, maxWait),
			condition,
			whileWaiting
		)
	}


	fun waitForDistance(locatable: Locatable, extraWait: Int = 1200, maxWait: Int = 20000, condition: () -> Boolean) =
		waitForDistanceWhile(locatable, extraWait, maxWait, condition)
}