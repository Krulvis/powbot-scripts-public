package org.powbot.krulvis.mta

import org.powbot.api.Notifications
import org.powbot.api.script.*
import org.powbot.api.script.tree.SimpleBranch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.mta.rooms.*
import org.powbot.krulvis.mta.tree.leafs.GoInside
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
    name = "krul MagicTrainingArena",
    description = "Does MTA for points, make sure to have required runes + staff equipped.",
    scriptId = "3cc11fa3-daac-4ca9-b740-04d380056fd1",
    version = "1.0.1",
    category = ScriptCategory.Magic
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Method",
            description = "Which room to gather points at?",
            allowedValues = arrayOf(ALCHEMY_METHOD, ENCHANTING_METHOD, GRAVEYARD_METHOD, TELEKINETIC_METHOD),
            defaultValue = TELEKINETIC_METHOD
        ),
        ScriptConfiguration(
            name = "Stop at points",
            description = "Stop at points cap?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "Points to collect",
            description = "How many points to stop?",
            optionType = OptionType.INTEGER,
            defaultValue = "500",
            visible = false
        ),
    ]
)
class MTA : KrulScript() {

    val method by lazy { getOption<String>("Method") }
    val stopAtPoints by lazy { getOption<Boolean>("Stop at points") }
    val pointsCap by lazy { getOption<Int>("Points to collect") }

    lateinit var room: MTARoom
    var started = false

    var gainedPoints = 0
    var startPoints = -1
    var currentPoints = -1

    override fun createPainter(): ATPaint<*> = MTAPainter(this)

    private lateinit var methodComp: TreeComponent<MTA>

    override val rootComponent: TreeComponent<*> = SimpleBranch(
        this, "Inside room",
        successComponent = object : TreeComponent<MTA>(this, "RoomStart") {
            override fun execute() {
                currentPoints = room.points()
                if (startPoints == -1) startPoints = currentPoints
                gainedPoints = currentPoints - startPoints
                if (stopAtPoints && currentPoints >= pointsCap) {
                    ScriptManager.stop()
                    Notifications.showNotification("Reached $pointsCap points, stopping script")
                    return
                }
                methodComp.execute()
            }
        },
        failedComponent = GoInside(this)
    ) {
        room.inside()
    }


    override fun onStart() {
        super.onStart()
        room = rooms[method]!!
        TelekineticRoom.resetRoom()
        methodComp = room.rootComponent(this)
        started = true
    }

    @ValueChanged("Stop at points")
    fun onStopAtPointsChange(stopAtPoints: Boolean) {
        updateVisibility("Points to collect", stopAtPoints)
    }

}

fun main() {
    MTA().startScript("127.0.0.1", "GIM", true)
}