package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.event.WidgetActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner
import org.powbot.krulvis.combiner.interactions.InventoryActions.getItem
import org.powbot.krulvis.combiner.interactions.ObjectActions.getObject
import org.powbot.krulvis.combiner.interactions.ObjectActions.isClimb
import org.powbot.krulvis.combiner.interactions.TabInteraction.getTabOpening
import org.powbot.krulvis.combiner.interactions.WidgetActions.isHomeTeleport
import kotlin.random.Random

class Combine(script: Combiner) : Leaf<Combiner>(script, "Start combining") {
    override fun execute() {
        Bank.close()
        for (i in 0 until script.combineActions.size) {
            val tile = me.tile()
            val event = script.combineActions[i]
            val useMenu = !event.rawEntityName.contains("->")
            val prev = if (i > 0) script.combineActions[i - 1] else null
            val next = if (script.combineActions.size > i + 1) script.combineActions[i + 1] else null
            if (next is WidgetActionEvent
                && next.widget().visible() && next.widget().actions().contains(next.interaction)
            ) {
                script.logger.info("Next widget=$next is already visible")
                continue
            } else if (event is GameObjectActionEvent
                && event.interaction.contains("climb", true) && event.tile.floor != tile.floor()
            ) {
                script.logger.info("Already on different floor for climb interaction=${event}")
                continue
            }
            val interaction = when (event) {
                is InventoryItemActionEvent -> {
                    val item = event.getItem()
                    debug("Event item=${item}")
                    Inventory.open() && item
                        ?.interact(event.interaction, useMenu && item.actions().indexOf("Use") != 0)
                            ?: false
                }

                is GameObjectActionEvent -> {
                    val obj = event.getObject()
                    if (obj == null) {
                        script.logger.info("Cannot find gameobject with name=${event.name} and action=${event.interaction}")
                        Movement.walkTo(event.tile)
                    }
                    walkAndInteract(
                        event.getObject(),
                        event.interaction,
                        selectItem = if (prev is InventoryItemActionEvent && prev.interaction == "Use") prev.id else -1
                    )
                }

                is NpcActionEvent -> {
                    walkAndInteract(
                        Npcs.stream().name(event.name).nearest().firstOrNull(),
                        event.interaction,
                        selectItem = if (prev is InventoryItemActionEvent && prev.interaction == "Use") prev.id else -1
                    )
                }

                is WidgetActionEvent -> {
                    val tabOpening = event.getTabOpening()
                    if (tabOpening != null) {
                        Game.tab(tabOpening)
                    } else if (event.isHomeTeleport() && House.isInside()) {
                        true
                    } else
                        event.widget().interact(event.interaction, false)
                }

                else -> {
                    script.logger.info("None of the handled types: $event")
                    false
                }
            }
            if (next is WidgetActionEvent && next.widget().visible() || interaction) {
                script.logger.info("----Interaction for event=$event successfull, next=$next")
                if (next == null) {
                    val sleepDelay = script.calculateSpamDelay()
                    script.logger.info("Waiting for items to be made... sleeping=${sleepDelay}")
                    sleep(sleepDelay)
                    waitFor(long()) { script.spamClick || !script.stoppedUsing() }
                    if (event is WidgetActionEvent && event.interaction == "Cast" && script.shouldBank()) {
                        script.logger.info("Casting spell as last action")
                        val randomSleep = Random.nextInt(600, 1200)
                        sleep(randomSleep)
                        script.logger.info("Slept for $randomSleep")
                    }
                } else {
                    val wait = waitFor(long()) {
                        if (next.name.contains("->")) {
                            Inventory.selectedItem().id == event.id
                        } else if (event.isClimb()) {
                            me.floor() != (event as GameObjectActionEvent).tile.floor
                        } else {
                            when (next) {
                                is WidgetActionEvent -> {
                                    if (next.isHomeTeleport()) {
                                        House.isInside()
                                    } else {
                                        val nextWidget = next.widget()
                                        nextWidget.visible() && nextWidget.actions().contains(next.interaction)
                                    }
                                }

                                else -> {
                                    sleep(if (script.spamClick) Random.nextInt(script.calculateSpamDelay()) else 600)
                                    true
                                }
                            }
                        }
                    }
                    script.logger.info("waitFor next=$next ${if (wait) "success" else "failed"}")
                }
            } else {
                script.logger.info("FAILED interaction for event=$event, next=$next")
            }
        }
    }


}