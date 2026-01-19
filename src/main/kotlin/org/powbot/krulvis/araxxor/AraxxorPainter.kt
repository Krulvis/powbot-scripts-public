package org.powbot.krulvis.araxxor

import org.powbot.api.Color.CYAN
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.RED
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.mobile.drawing.Rendering

class AraxxorPainter(script: Araxxor) : KrulPaint<Araxxor>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Kills") { "${script.kills}, ${script.timer.getPerHour(script.kills)}/hr" }
            .addString("Deaths") { "${script.deaths}, ${script.timer.getPerHour(script.deaths)}/hr" }
            .addString("Attack") { "tick=${script.myNextAttackTick - script.ticks}, canAttack=${script.ticks >= script.myNextAttackTick}" }
            .addString("Araxxor") {
                "Next Attack in=${script.attackTimer.getRemainder()}, AboutToAttack=${script.aboutToAttack()}"
            }
            .addCheckbox("Paint debug:", "paintDebug", false)
            .build()
    }

    override fun paintCustom(g: Rendering) {
        if (!script.debugPaint) return
        val araxxor = script.araxxor
        if (araxxor.valid()) {
            script.araxxorCenter.drawOnScreen("Anim: ${script.lastAraxAnimation}\nOri: ${araxxor.orientation()}")
        }
//        //        if (script.enrage) {
//        val borderTiles = script.borderTiles
//        borderTiles.forEach { it.drawOnScreen(outlineColor = GREEN) }
////        }
//        script.nearestOutsideTile.drawOnScreen(outlineColor = ORANGE, fillColor = ORANGE)
//
//
        val unsafe = script.unsafeTiles + script.enrageNewPools
        unsafe.forEach { it.drawOnScreen(outlineColor = RED) }

//        val eggs = script.eggs
//        eggs.forEachIndexed { i, it -> it.tile().drawOnScreen("$i", outlineColor = ORANGE) }
//
//        val myTile = me.trueTile()
//        val exploding = !script.explodingTimer.isFinished()
//        val exploRemainder = if (exploding) script.explodingTimer.getRemainder().toString() else ""
//        script.exploding.trueTile()
//            .drawOnScreen(
//                "A: ${script.exploding.animation()}, E: $exploRemainder ms",
//                outlineColor = if (exploding) RED else GREEN
//            )
//        myTile.drawOnScreen(outlineColor = CYAN)
//        script.safestTile.drawOnScreen("SAFEST", outlineColor = WHITE)
        script.exploDestination.drawOnScreen("EPLO", outlineColor = CYAN)
        script.newPosition.drawOnScreen("NEW", outlineColor = GREEN)
    }
}