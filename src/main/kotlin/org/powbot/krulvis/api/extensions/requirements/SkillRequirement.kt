package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Skills

class SkillRequirement(val skill: Int, val level: Int) : Requirement {
    override fun meets(): Boolean {
        return Skills.level(skill) >= level
    }
}