package org.powbot.krulvis.giantsfoundry.data

import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.giantsfoundry.JOB_VARP
import org.powbot.krulvis.giantsfoundry.mouldWidget

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
            return MouldType.entries.first { it.openPageBit == varp }
        }

        fun selectedAll(): Boolean {
            val varp = Varpbits.varpbit(JOB_VARP)
            return MouldType.entries.all {
                varp.shr(it.shiftAmount).and(optionsMask) > 0
            }
        }
    }
}