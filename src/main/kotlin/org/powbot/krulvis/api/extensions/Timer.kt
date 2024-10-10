package org.powbot.krulvis.api.extensions

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

class Timer(var time: Number) {
	companion object {
		fun formatTime(time: Long, addMilis: Boolean = false): String {
			if (time <= 0L)
				return "--:--:--"
			val t = StringBuilder()
			val totalSec = time / 1000
			val totalMin = totalSec / 60
			val totalHour = totalMin / 60
			val totalDay = totalHour / 24
			val second = totalSec.toInt() % 60
			val minute = totalMin.toInt() % 60
			val hour = totalHour.toInt() % 24
			val day = totalDay.toInt()
			if (day > 0) {
				t.append(day)
				t.append(":")
			}
			if (hour > 0) {
				if (hour < 10)
					t.append("0")
				t.append(hour)
				t.append(":")
			}
			if (minute > 0) {
				if (minute < 10)
					t.append("0")
				t.append(minute)
				t.append(":")
			}
			if (second < 10)
				t.append("0")
			t.append(second)
			if (time < 1000 && addMilis) {
				t.append(":")
				t.append(time)
			}
			return t.toString()
		}
	}

	constructor() : this(0L)

	private var start = 0L
	var end = 0L

	init {
		start = System.currentTimeMillis()
		if (time.toLong() > 0) {
			end = (start + time.toLong())
		}
	}

	fun getRemainder(): Long {
		return end - System.currentTimeMillis()
	}

	fun getRemainderString(): String {
		return formatTime(getRemainder())
	}

	/**
	 * Returns actual elapsed time since start of running, not till end
	 */
	fun getElapsedTime(): Long {
		return System.currentTimeMillis() - start
	}

	fun isFinished(): Boolean {
		return System.currentTimeMillis() > end
	}

	fun reset() {
		reset(time)
	}

	fun reset(newTime: Number) {
		time = newTime
		start = System.currentTimeMillis()
		end = start + time.toLong()
	}

	/**
	 * Use this if you actually want to time something
	 */
	fun stop() {
		end = System.currentTimeMillis()
	}

	fun getStartToEnd(): Long {
		return end - start
	}

	fun getXPForLevel(level: Int): Int {
		var points = 0
		var output = 0
		for (lvl in 1..level) {
			points += floor(lvl + 300.0 * 2.0.pow(lvl / 7.0)).toInt()
			if (lvl >= level) {
				return output
			}
			output = floor((points / 4).toDouble()).toInt()
		}
		return 0
	}

	fun getTimeToNextLevel(xpPerHour: Int, level: Int, xp: Long): String {
		val elapsed: Long
		if (xpPerHour < 1) {
			elapsed = 0
		} else {
			elapsed = ((getXPForLevel(level + 1) - xp) * 3600000.0 / xpPerHour).toLong()
		}
		return formatTime(elapsed)
	}

	fun getPerHour(gained: Int): Int {
		if (gained == 0) {
			return 0
		}
		return ceil(gained * 3600000.0 / (System.currentTimeMillis() - start)).toInt()
	}


	/**
	 * Displays remaining time if timer is not infinite, otherwise displaying the elapsed time
	 */
	override fun toString(): String {
		return if (end == 0L) formatTime(getElapsedTime()) else getRemainderString()
	}
}