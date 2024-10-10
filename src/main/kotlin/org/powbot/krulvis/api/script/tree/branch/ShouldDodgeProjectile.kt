package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.script.KillerScript

class ShouldDodgeProjectile<S : KillerScript>(script: S, override val failedComponent: TreeComponent<S>) :
	Branch<S>(script, "ShouldDodgeProjectile?") {
	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Dodge Projectile") {
		Movement.step(script.projectileSafespot, 0)
		sleep(600)
		script.currentTarget.interact("Attack")
	}

	override fun validate(): Boolean {
		val dest = Movement.destination()
		val tile = if (dest.valid()) dest else Players.local().tile()
		return script.projectiles.any { it.destination() == tile }
	}
}