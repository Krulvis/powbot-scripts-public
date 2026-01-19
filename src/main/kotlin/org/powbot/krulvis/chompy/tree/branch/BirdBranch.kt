package org.powbot.krulvis.chompy.tree.branch

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.chompy.ChompyBird
import org.powbot.krulvis.chompy.isBirdValid
import org.powbot.krulvis.chompy.tree.leaf.KillBird

class BirdSpawned(script: ChompyBird) : Branch<ChompyBird>(script, "BirdSpawned?") {
	override val failedComponent: TreeComponent<ChompyBird> = HasToad(script)
	override val successComponent: TreeComponent<ChompyBird> = KillBird(script)

	override fun validate(): Boolean {
		if (script.currentTarget.refresh().isBirdValid())
			return true
		script.currentTarget = Npcs.stream().name("Chompy bird").filtered { it.isBirdValid() }.nearest().first()
		return script.currentTarget.valid()
	}


}
