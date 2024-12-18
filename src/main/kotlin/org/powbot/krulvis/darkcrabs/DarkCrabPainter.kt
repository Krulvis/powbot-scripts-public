package org.powbot.krulvis.darkcrabs

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.KrulPaint

class DarkCrabPainter(script: DarkCrabs) : KrulPaint<DarkCrabs>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Dark crabs") { perHourText(script.caught) }
            .trackSkill(Skill.Fishing).build()
    }
}