package org.powbot.krulvis.smelter

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.script.painter.ATPaint

class SmelterPainter(script: Smelter) : ATPaint<Smelter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.trackSkill(Skill.Smithing)
            .trackInventoryItem(if (script.cannonballs) Item.CANNONBALL else script.bar.id).build()
    }
}