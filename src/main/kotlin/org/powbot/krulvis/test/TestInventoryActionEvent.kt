package org.powbot.krulvis.test

import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript


@ScriptManifest(name = "Test InventoryItemActionEvent", version = "6.6.6", description = "", priv = true)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "inventory_actions",
            description = "Inventory with something in inventory",
            optionType = OptionType.GAME_ACTIONS,
            defaultValue = "[{\"id\":946,\"interaction\":\"Use\",\"mouseX\":869,\"mouseY\":238,\"rawEntityName\":\"<col=ff9040>Knife</col>\",\"rawOpcode\":25,\"var0\":0,\"widgetId\":9764864,\"name\":\"Knife\",\"strippedName\":\"Knife\"},{\"id\":1517,\"interaction\":\"Use\",\"mouseX\":915,\"mouseY\":230,\"rawEntityName\":\"<col=ff9040>Knife</col><col=ffffff> -> <col=ff9040>Maple logs</col>\",\"rawOpcode\":58,\"var0\":1,\"widgetId\":9764864,\"name\":\"Maple logs\",\"strippedName\":\"Knife -> Maple logs\"},{\"id\":1,\"interaction\":\"Make\",\"mouseX\":291,\"mouseY\":85,\"rawEntityName\":\"<col=ff9040>Maple longbow</col>\",\"rawOpcode\":57,\"var0\":-1,\"widgetId\":17694736,\"componentIndex\":-1,\"widgetIndex\":-1,\"name\":\"Maple longbow\",\"strippedName\":\"Maple longbow\"}]"
        )
    ]
)
class TestInventoryActionEventScript : TreeScript() {

    val combineActions by lazy { getOption<List<GameActionEvent>>("inventory_actions") }

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "Test") {
        combineActions.forEachIndexed { i, event ->
            when (event) {
                is InventoryItemActionEvent -> {
                    val item = event.item()
                    logger.info("Event=$event, item=$item")
                    if (item != null) {
                        logger.info("Interacted successfully=${item.interact(event.interaction)}")
                    }
                }
                else -> {
                    logger.info("Not testing this kind of event")
                }
            }

        }
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        logger.info("$e")
    }

}

fun main() {
    TestInventoryActionEventScript().startScript("127.0.0.1", "GIM", false)
}