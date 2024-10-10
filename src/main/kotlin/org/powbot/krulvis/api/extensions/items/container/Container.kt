package org.powbot.krulvis.api.extensions.items.container

import com.google.common.eventbus.Subscribe
import org.powbot.api.Events
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.MessageType
import org.powbot.krulvis.api.ATContext.uppercaseFirst
import org.powbot.krulvis.api.extensions.items.Item
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Container")

enum class Container(override val CLOSED_ID: Int, override val OPEN_ID: Int) : Item, Openable {
    HERB_SACK(13226, 24478),
    COAL_BAG(12019, 24480),
    GEM_BAG(12020, 24481),
    SEED_BOX(13639, 24482),
    FISH_BARREL(25582, 25584),
    ;

    init {
        Events.register(this)
    }

    override val itemName: String = name.uppercaseFirst().replace("_", " ")
    override val stackable: Boolean = false
    override val ids: IntArray = intArrayOf(CLOSED_ID, OPEN_ID)

    var emptied = false
    override fun hasWith(): Boolean = getInventoryCount() > 0

    override fun getCount(countNoted: Boolean): Int = getInventoryCount()

    fun empty(): Boolean {
        if (emptied) return true
        val invItem = getInvItem() ?: return true
        if (invItem.interact("Empty")) {
            emptied = true
            return true
        } else if (invItem.actions().none { it == "Empty" }) {
            emptied = true
            return true
        }
        return false
    }

    val smallName = name.split("_")[1].lowercase()

    val regex = Regex("(?=.*empty)(?=.*$smallName).*")

    @Subscribe
    fun messageEvent(evt: MessageEvent) {
        if (evt.messageType != MessageType.Game) return
        if (regex.matches(evt.message)) {
            logger.info("The $itemName has been emptied.")
            emptied = true
        }
    }


    companion object {
        fun requireEmpty() {
            values().forEach { it.emptied = false }
        }

        fun emptyAll() = values().all { it.empty() }

        fun openAll() = values().all { it.open() }
    }
}