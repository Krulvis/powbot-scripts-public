package org.powbot.krulvis.api.extensions

import org.powbot.api.Condition
import org.powbot.api.Input
import org.powbot.api.Point
import org.powbot.api.Random
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Menu
import org.powbot.mobile.input.DragType
import org.powbot.mobile.input.TouchGestureSession
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.roundToInt


object ScrollHelper {

    const val TimeoutAfter = 15000

    val logger = LoggerFactory.getLogger("ScrollHelper")

    @JvmStatic
    fun scrollTo(
        getItem: () -> Component,
        getPane: () -> Component,
        getScrollComp: () -> Component
    ): Boolean {
        val startPos = getScrollComp().component(1).screenPoint()
        val timeoutAt = System.currentTimeMillis() + TimeoutAfter

        (0 until 4).forEach { attempt ->
            val item = getItem()
            val pane = getPane()
            val scrollComp = getScrollComp()
            if (!pane.visible()) {
                logger.warn("Pane no longer visible")
                return false
            } else if (!scrollComp.visible()) {
                logger.warn("Scroll comp no longer visible")
                return false
            } else if (System.currentTimeMillis() > timeoutAt) {
                logger.warn("Timeout reached")
                return false
            }

            if (scrollTo(item, pane, scrollComp, attempt, startPos, timeoutAt) {
                    isVisible(
                        getItem(),
                        getPane()
                    )
                }) {
                return true
            } else {
                logger.warn("Scrollto failed on attempt=$attempt")
            }
        }

        return false
    }

    @JvmStatic
    @Deprecated("use scrollTo(() -> Component, () -> Component(), () -> Component())")
    fun scrollTo(
        item: Component,
        pane: Component,
        scrollComp: Component,
    ): Boolean {
        return scrollTo(item, pane, scrollComp, 0, scrollComp.component(1).screenPoint()) {
            isVisible(
                item,
                pane
            )
        }
    }

    @JvmStatic
    fun scrollTo(
        item: Component,
        pane: Component,
        scrollComp: Component,
        attempt: Int,
        startPos: Point,
        isVisible: () -> Boolean,
    ): Boolean {
        return scrollTo(item, pane, scrollComp, attempt, startPos, Long.MAX_VALUE, isVisible)
    }

    @JvmStatic
    private fun scrollTo(
        item: Component,
        pane: Component,
        scrollComp: Component,
        attempt: Int,
        startPos: Point,
        timeoutAt: Long,
        isVisible: () -> Boolean,
    ): Boolean {
        if (!scrollComp.visible() || !pane.visible() || System.currentTimeMillis() > timeoutAt) {
            return false
        }
        if (Menu.opened()) {
            Menu.click(Menu.filter("Cancel"))
            Condition.waitInternal { !Menu.opened() }
        }

        if (isVisible()) {
            return true
        }

        val scrollBar = scrollComp.component(0)
        val thumb = scrollComp.component(1)
        val up = scrollComp.component(4)
        val down = scrollComp.component(5)

        val paneScrollHeight = pane.scrollHeight()
        val scrollBarHeight = scrollBar.height()
        val thumbWidth = thumb.width()
        val thumbHeight = thumb.height()
        val paneHeight = pane.height()

        if (scrollBarHeight > 0) {
            val panePxPerScrollBarPx = paneScrollHeight / scrollBarHeight

            val paneY = pane.centerPoint().y
            val itemY = item.centerPoint().y
            val itemDiffPanePx = (itemY - paneY)

            logger.warn(
                "ScrollbarHeight=$scrollBarHeight, panePxPerScrollBarPx=$panePxPerScrollBarPx, paneY=$paneY, itemY=$itemY, " +
                        "itemDiffPanePx=$itemDiffPanePx, timeoutAt=$timeoutAt, currentTimeMS=${System.currentTimeMillis()}," +
                        " timeleft=${timeoutAt - System.currentTimeMillis()}, scrollbarHeight=$scrollBarHeight"
            )
            if (abs(itemDiffPanePx) <= 700 || (attempt > 2 && startPos.distance(thumb.screenPoint()) <= 5.0)) {
                val p = if (itemDiffPanePx > 0) down.nextPoint() else up.nextPoint()
                val touchGestureSession = TouchGestureSession()
                logger.warn(
                    "abs(itemDiffPanePx)=${abs(itemDiffPanePx)} <= 700=${abs(itemDiffPanePx) <= 700}, (attempt > 2 && startPos.distance(thumb.screenPoint()) <= 5.0)=${
                        (attempt > 2 && startPos.distance(
                            thumb.screenPoint()
                        ) <= 5.0)
                    }"
                )
                touchGestureSession.sendPress(p)
                val wait = Condition.waitInternal({
                    isVisible() || !scrollComp.visible() || !pane.visible() || System.currentTimeMillis() > timeoutAt
                }, 100, 20)
                logger.warn("wait=$wait, isVisble=${isVisible()}, scrollComp=${scrollComp.visible()}, pane=${pane.visible()}, timeoutAt=${System.currentTimeMillis() > timeoutAt}")
                touchGestureSession.sendRelease(p)
            } else if (panePxPerScrollBarPx > 0) {
                val pxToScroll = (itemDiffPanePx - (paneHeight / 2)) / panePxPerScrollBarPx

                val thumbCenter = thumb.centerPoint()
                Input.drag(
                    thumbCenter.derive(
                        Random.nextInt(
                            (thumbWidth * -0.01).roundToInt(),
                            (thumbWidth * 0.01).roundToInt()
                        ),
                        Random.nextInt(
                            (thumbHeight * -0.01).roundToInt(),
                            (thumbHeight * 0.01).roundToInt()
                        )
                    ),
                    thumbCenter.derive(
                        Random.nextInt(0, (thumbWidth * -0.01).roundToInt()),
                        pxToScroll
                    ),
                    DragType.LINE,
                )
            }
        }
        Condition.sleep(Random.nextInt(100, 200))

        return isVisible()
    }

    private fun isVisible(item: Component, pane: Component): Boolean {
        val paneRect = pane.boundingRect()
        return item.visible() && paneRect.contains(item.screenPoint()) && paneRect.contains(
            item.screenPoint().derive(item.width() / 2, item.height())
        )
    }


}