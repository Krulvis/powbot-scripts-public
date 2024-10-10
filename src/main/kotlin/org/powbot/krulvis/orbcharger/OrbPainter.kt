package org.powbot.krulvis.orbcharger

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class OrbPainter(script: OrbCrafter) : ATPaint<OrbCrafter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.trackSkill(Skill.Magic)
            .trackInventoryItems(
                *Orb.values().map {
                    it.id
                }.toIntArray()
            )
        return paintBuilder.build()
    }

}