package org.powbot.krulvis.darkcrabs

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.ScriptState
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.darkcrabs.tree.branch.ShouldHop
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
    "krul DarkCrabs",
    "Fishes dark crabs in wilderness resource area.",
    version = "1.0.0",
    category = ScriptCategory.Fishing,
)
class DarkCrabs : KrulScript() {

    var forcedBanking = false
    val startXp: Int by lazy { Skills.experience(Skill.Fishing) }
    var currentXp: Int = -1
    var caught = 0
    var died = false

    @Subscribe
    fun onMsg(e: MessageEvent){
        if(e.message.contains("dead", true)){
            died = true
        }
    }
    @Subscribe
    fun onTick(e: TickEvent) {
        if (ScriptManager.state() != ScriptState.Running || !Game.loggedIn()) return
        currentXp = Skills.experience(Skill.Fishing)
        caught = (currentXp - startXp) / 130
    }

    override fun createPainter(): ATPaint<*> = DarkCrabPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldHop(this)
}

fun main() {
    DarkCrabs().startScript("127.0.0.1", "GIM", false)
}