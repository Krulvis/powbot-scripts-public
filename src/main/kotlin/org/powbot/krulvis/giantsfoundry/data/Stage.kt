package org.powbot.krulvis.giantsfoundry.data

import org.powbot.api.rt4.Widgets
import org.powbot.api.script.paint.PaintFormatters.round
import org.powbot.krulvis.giantsfoundry.ACTIVITIES_CHILD_INDEX
import org.powbot.krulvis.giantsfoundry.ROOT
import org.powbot.krulvis.giantsfoundry.currentProgress
import org.slf4j.LoggerFactory
import kotlin.math.ceil

data class Stage(val action: Action, val start: Int, val end: Int) {

    fun isActive(progress: Int) = progress in start..end
    fun isActive() = isActive(currentProgress())
    fun pointsLeft() = end - currentProgress()

    companion object {
        private val logger = LoggerFactory.getLogger(Stage::class.java.simpleName)
        val Nil = Stage(Action.HAMMER, -1, -1)

        fun parseStages(): Array<Stage> {
            val stagesBar = Widgets.component(ROOT, ACTIVITIES_CHILD_INDEX)
            val children = stagesBar.components()
            val stageIdentifiers = children.filter { Action.forTexture(it.textureId()) != null }
            val stagesCount = stageIdentifiers.size
            val stageSize = 1000.0 / stagesCount
            logger.warn("Parsing stages: stageCount=${stagesCount}, stageSize=${stageSize.round(2)}")
            val stages = stageIdentifiers.mapIndexed { i, stage ->
                //The texture is one child above the boundingComp
                Stage(
                    Action.forTexture(stage.textureId())!!,
                    ceil(stageSize * i).toInt(),
                    (stageSize * i + stageSize).toInt()
                )
            }.toTypedArray()
            stages.forEachIndexed { i, s ->
                logger.warn("Stage[$i]=${s.action}, start=${s.start}, end=${s.end}")
            }

            return stages
        }
    }
}