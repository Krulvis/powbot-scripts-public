package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.script.KillerScript

class ShouldDodgeProjectile<S : KillerScript>(script: S, override val failedComponent: TreeComponent<S>) :
    Branch<S>(script, "ShouldDodgeProjectile?") {
    override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Dodge Projectile") {
        try {
            script.findSafeSpotFromProjectile(script.currentTarget)
            val targetTile =
                script.ironmanLoot.firstOrNull { loot -> loot.valid() && loot.tile.valid() && loot.tile != myTile }?.tile
                    ?: script.projectileSafespot
            val startTime = System.currentTimeMillis()
            script.logger.warn("Dangerous projectile spawned on tile=${myTile}, moving to=$targetTile")
            val walk = Movement.stepNoConfirm(targetTile)
            script.logger.warn("Moving took ${System.currentTimeMillis() - startTime} ms and completed=${walk}")
        } catch (e: Exception) {
            script.logger.error(e.stackTraceToString() + "\n" + e.message + "\n" + e.cause?.message, e.cause ?: e)
        }
        sleep(600)
        script.currentTarget.interact("Attack")
    }

    var myTile: Tile = Tile.Nil

    override fun validate(): Boolean {
        val dest = Movement.destination()
        myTile = if (dest.valid()) dest else Players.local().tile()
        return script.projectiles.any { it.destination() == myTile }
    }
}