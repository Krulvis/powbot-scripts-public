package org.powbot.krulvis.thiever.blackjack

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Utils

@ScriptManifest(
    name = "blackjacking",
    description = "Blackjack bandits",
    author = "Krulvis",
    version = "1.0.0",
//    markdownFileName = "Thiever.md",
//    scriptId = "e6043ead-e607-4385-b67a-a86dcf699204",
    category = ScriptCategory.Thieving,
    priv = true
)
class Blackjacking : KrulScript() {


    val target = "Bandit"
    var stopping = false
    val ladderTile = Tile(3364, 3003, 0)
    override val rootComponent: TreeComponent<*> = ShouldEat(this)


    override fun createPainter(): ATPaint<*> {
        return BJPainter(this)
    }

    fun eatFood() {
        val food = Food.getFirstFood()
        if (food != null) {
            val count = food.getInventoryCount()
            if (food.eat()) {
                Utils.waitFor { food.getInventoryCount() != count }
            }
        } else {
            Notifications.showNotification("Out of food stopping script!")
            stopping = true
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onMessage(e: MessageEvent) {

    }

}


class BJPainter(script: Blackjacking) : ATPaint<Blackjacking>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackInventoryItems(995)
            .trackSkill(Skill.Thieving)
            .build()
    }

}

fun main() {
    Blackjacking().startScript("127.0.0.1", "GIM", false)
}
