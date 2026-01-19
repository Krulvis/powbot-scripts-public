package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.requirement.Requirement

class QuestRequirement(val quest: Int, val finished: Boolean) : Requirement {

    override fun meets(): Boolean {
//        return if (finished) ctx.quests.isCompleted(quest) else ctx.quests.isStarted(quest)
        TODO("Not implemented yet")
    }
}