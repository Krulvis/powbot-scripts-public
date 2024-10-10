package org.powbot.krulvis.chompy.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.chompy.ChompyBird

class KillBird(script: ChompyBird) : Leaf<ChompyBird>(script, "Kill Bird") {
	override fun execute() {
		if (me.interacting() != script.currentTarget && walkAndInteract(script.currentTarget, "Attack")) {
			waitFor(600) { me.interacting() == script.currentTarget }
		}
	}
}