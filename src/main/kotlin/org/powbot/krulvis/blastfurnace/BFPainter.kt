package org.powbot.krulvis.blastfurnace

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class BFPainter(script: BlastFurnace) : ATPaint<BlastFurnace>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.addString("Bars:") { script.bar.blastFurnaceCount.toString() }
        paintBuilder.addString(
                { "${script.bar.primary.itemName} Ore: " },
                { script.bar.primary.blastFurnaceCount.toString() })
        if (script.bar.secondaryMultiplier > 0) {
            paintBuilder.addString(
                    { "${script.bar.secondary.itemName} Ore: " },
                    { script.bar.secondary.blastFurnaceCount.toString() })
        }
        paintBuilder.trackSkill(Skill.Smithing)
        paintBuilder.trackInventoryItem(script.bar.id)

        if (ATContext.debugComponents) {
            paintBuilder.addString("CofferCount") { script.cofferCount().toString() }
            if (script.bar == Bar.GOLD) {
                paintBuilder.addString("WaitForXP") { script.waitForBars.toString() }
            }
        }
        return paintBuilder.build()
    }

    override fun paintCustom(g: Rendering) {
        var y = 100
        val x = 800
        if (ATContext.debugComponents) {
//            g.drawString("Coffer count: ${script.cofferCount()}", x, y)
//            y += yy
        }
    }
}