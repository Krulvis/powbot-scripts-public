package org.powbot.krulvis.fletching

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.KrulPaint

class ValeTotemPainter(script: AuburnvaleFletcher) : KrulPaint<AuburnvaleFletcher>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Site") { script.current.key.name }
            .addString("TotemTimer") { script.totemTimer.getRemainderString() }
//            .addString("Varp") { script.current.key.varpValue().toString(2) }
//            .addString("Build") { script.current.key.built().toString() }
//            .addString("Logs") { script.current.key.decorations().toString() }
//            .addString("Animals") { script.current.key.layers().joinToString { "$it: ${animalNameForIndex(it - 10)}" } }
            .trackSkill(Skill.Fletching)
            .trackInventoryItemQ(OFFERINGS)
            .build()
    }
}