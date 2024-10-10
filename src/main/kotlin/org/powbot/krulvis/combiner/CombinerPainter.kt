package org.powbot.krulvis.combiner

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class CombinerPainter(script: Combiner) : ATPaint<Combiner>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackSkill(Skill.Crafting)
            .trackSkill(Skill.Smithing)
            .trackSkill(Skill.Cooking)
            .trackSkill(Skill.Fletching)
            .trackSkill(Skill.Herblore)
            .build()
    }
}