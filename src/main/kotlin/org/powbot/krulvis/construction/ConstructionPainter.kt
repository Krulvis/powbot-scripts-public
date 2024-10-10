package org.powbot.krulvis.construction

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class ConstructionPainter(script: Construction) : ATPaint<Construction>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.trackSkill(Skill.Construction)
//            .addString("Tab") { Game.tab().toString() }
//            .addString("Has mats") { script.hasMats().toString() }
            .build()
    }
}