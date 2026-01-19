package org.powbot.krulvis.miner

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.items.Item.Companion.BLESSED_BONE_SHARD
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.miner.Data.getMotherlodeCapacity
import org.powbot.krulvis.miner.Data.getMotherlodeCount


class MinerPainter(script: Miner) : KrulPaint<Miner>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder
            .addString("Sack count") { getMotherlodeCount().toString() }
            .addString("Sack cap") { getMotherlodeCapacity().toString() }
            .trackSkill(Skill.Mining)
            .trackInventoryItems(
                12012,
                BLESSED_BONE_SHARD,
                *Ore.values().filter { it != Ore.PAY_DIRT }.flatMap { it.ids.toList() }.toIntArray()
            )
            .withTotalLoot(true)
        return paintBuilder.build()
    }
}