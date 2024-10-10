package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Varpbits
import org.powbot.api.rt4.Widgets

fun mouldWidget() = Widgets.widget(MOULD_WIDGET_ROOT)
const val MOULD_SELECTION_CONTAINER = 9
const val MOULD_EMPTY_TEXTURE = 3593
const val MOULD_SELECTED_COLOR = 16777215

fun getSelectedMouldComp() : Component {
    val comp = mouldWidget().component(MOULD_SELECTION_CONTAINER)
    return comp.firstOrNull { it?.textColor() == MOULD_SELECTED_COLOR } ?: return Component.Nil
}

enum class MouldType(val shiftAmount: Int, val openPageBit: Int, val selectedCompIndex: Int) {
    Forte(8, 0, 21),
    Blades(13, 1, 22),
    Tips(18, 2, 23);

    fun selected() = Varpbits.varpbit(JOB_VARP, shiftAmount, optionsMask)

    fun selectedTexture(): Int = mouldWidget().component(selectedCompIndex).textureId()

    fun hasSelectedAny() = selected() > 0

    fun open() = Varpbits.varpbit(JOB_VARP, 6, 3) == openPageBit

    companion object {
        const val optionsMask = 31

        fun openPage(): MouldType {
            val varp = Varpbits.varpbit(JOB_VARP, 6, 3)
            return values().first { it.openPageBit == varp }
        }

        fun selectedAll(): Boolean {
            val varp = Varpbits.varpbit(JOB_VARP)
            return values().all {
                varp.shr(it.shiftAmount).and(optionsMask) > 0
            }
        }
    }
}