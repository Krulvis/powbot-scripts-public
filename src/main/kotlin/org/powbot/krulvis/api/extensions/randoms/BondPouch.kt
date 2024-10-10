package org.powbot.krulvis.api.extensions.randoms

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components

class BondPouch : RandomHandler() {
    override fun validate(): Boolean {
        return getBondPouch() != null
    }

    fun getBondPouch(): Component? =
        Components.stream(65).filtered { it.visible() && it.text().contains("Bond Pouch") }.firstOrNull()

    override fun execute(): Boolean {
        val bPouch = getBondPouch()
        val close = bPouch?.parent()?.firstOrNull { it != null && it.visible() && it.actions().contains("Close") }
        return close?.click() == true
    }
}
