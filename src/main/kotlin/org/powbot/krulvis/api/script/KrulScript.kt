package org.powbot.krulvis.api.script

import org.powbot.api.EventFlows
import org.powbot.api.Preferences
import org.powbot.api.Random
import org.powbot.api.event.TickEvent
import org.powbot.api.script.tree.TreeScript
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.Poison
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.randoms.BondPouch
import org.powbot.krulvis.api.extensions.randoms.DeathRisk
import org.powbot.krulvis.api.extensions.randoms.EquipmentScreen
import org.powbot.krulvis.api.extensions.randoms.GrandExchangeGuide
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.mobile.drawing.FrameManager

abstract class KrulScript : TreeScript() {

    override fun onStart() {
        logger.info("Starting..")
        addPaint(painter.buildPaint(painter.paintBuilder))
        FrameManager.addListener(painter)
        val username = Preferences.getString("username")
        logger.info("Username: $username")
        EventFlows.collectTicks { onTickTimer(it) }
    }

    val painter by lazy { createPainter() }

    abstract fun createPainter(): KrulPaint<*>

    var ticks = -1

    private fun onTickTimer(e: TickEvent) {
        ticks++
        Poison.calculateDamage()
    }

    fun waitForTicks(ticks: Int = 1): Boolean {
        val endTick = this.ticks + ticks
        val startTimer = System.currentTimeMillis()
        val waited = waitFor(ticks * 700) { this.ticks >= endTick }
        logger.info("waitForTicks($ticks) took ${System.currentTimeMillis() - startTimer} ms")
        return waited
    }


    val timer = Timer()
    val oddsModifier = OddsModifier()
    val walkDelay = DelayHandler(500, 700, oddsModifier, "Walk Delay")
    var nextRun: Int = Random.nextInt(1, 6)
    val randomHandlers = mutableListOf(BondPouch(), GrandExchangeGuide(), EquipmentScreen(), DeathRisk())

    override fun poll() {
        val rh = randomHandlers.firstOrNull { it.validate() }
        if (rh != null) rh.execute() else super.poll()
    }

}
