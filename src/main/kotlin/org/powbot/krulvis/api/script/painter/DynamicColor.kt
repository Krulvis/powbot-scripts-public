package org.powbot.krulvis.api.script.painter

class DynamicColor(val min: Float, val max: Float, val step: Float = 0.01f, val interval: Int = 100) {

    private var lastColorUpdate: Long = 0
    private var bgHue = min
    private var hueUp = true
    private var custom: Int = HSBtoRGB(bgHue, 1.0f, 1.0f, 0xff)

    init {
        bgHue = min
        hueUp = true
        updateColor()
    }

    @JvmOverloads
    operator fun get(update: Boolean = true): Int {
        if (update) {
            updateColor()
        }
        return custom
    }

    fun updateColor() {
        if (lastColorUpdate + interval > System.currentTimeMillis()) {
            return
        }
        lastColorUpdate = System.currentTimeMillis()
        custom = HSBtoRGB(bgHue, 1.0f, 1.0f, 0xff)
        bgHue += if (hueUp) step else -step
        if (hueUp && bgHue >= max || !hueUp && bgHue <= min) {
            hueUp = !hueUp
        }
    }

    private fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float, alpha: Int): Int {
        var r = 0
        var g = 0
        var b = 0
        if (saturation == 0f) {
            b = (brightness * 255.0f + 0.5f).toInt()
            g = b
            r = g
        } else {
            val h = (hue - Math.floor(hue.toDouble()).toFloat()) * 6.0f
            val f = h - Math.floor(h.toDouble()).toFloat()
            val p = brightness * (1.0f - saturation)
            val q = brightness * (1.0f - saturation * f)
            val t = brightness * (1.0f - saturation * (1.0f - f))
            when (h.toInt()) {
                0 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (t * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }
                1 -> {
                    r = (q * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }
                2 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (t * 255.0f + 0.5f).toInt()
                }
                3 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (q * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }
                4 -> {
                    r = (t * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }
                5 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (q * 255.0f + 0.5f).toInt()
                }
            }
        }
        return alpha shl 24 or (r shl 16) or (g shl 8) or b
    }
}
