package org.powbot.krulvis.giantsfoundry.data

import org.powbot.api.rt4.Component

enum class BonusType {
    Broad,
    Flat,
    Heavy,
    Light,
    Narrow,
    Spiked;

    companion object {
        fun isBonus(component: Component) = values().any { it.name == component.text() }
        fun forComp(component: Component) = values().firstOrNull { it.name == component.text() }
        fun forText(text: String) = values().firstOrNull { it.name == text }
    }
}