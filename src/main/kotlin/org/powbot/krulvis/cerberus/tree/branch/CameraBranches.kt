package org.powbot.krulvis.cerberus.tree.branch

import org.powbot.api.Random
import org.powbot.api.rt4.Camera
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.cerberus.Cerberus

class ShouldSetCamera(script: Cerberus) : Branch<Cerberus>(script, "ShouldSetCamera?") {
    override val failedComponent: TreeComponent<Cerberus>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "Setting Camera") {
        Camera.pitch(Random.nextInt(30, maxPitch - 1))
        Camera.angle(Random.nextInt(angleRange[0], angleRange[1]))
    }

    val maxPitch = 40
    val angleRange = arrayOf(150, 195)
    override fun validate(): Boolean {
        return Camera.pitch() > maxPitch || angleRange[0] > Camera.yaw() || Camera.yaw() > angleRange[1]
    }
}