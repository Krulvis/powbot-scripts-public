package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.Patch.Companion.index
import org.powbot.krulvis.tithe.Patch.Companion.refresh
import org.powbot.krulvis.tithe.TitheFarmer

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {

	override fun execute() {
//        if (!waitFor(long()) { !ctx.movement.moving() }) {
//            debug("Not interacting yet because still moving")
//            return
//        }

		script.getPatchTiles()
		closeOpenHUD()
		val hasEnoughWater = script.getWaterCount() > 0
		val ms = System.currentTimeMillis()
		debug("Waiting for handling of patchIndex=${script.patches.index(script.lastPatch)}")
		if (Condition.wait({ script.lastPatch?.needsAction(true) == false }, 100, 30)) {
			debug("Last patch: ${script.lastPatch?.index} = handled in ${System.currentTimeMillis() - ms}ms")
			script.lastPatch = null
		} else {
			debug("Failed to handle last patch=${script.lastPatch?.index}")
		}

		val patches = script.patches.refresh().sortedWith(compareBy(Patch::id, Patch::index))
		val patch = patches.firstOrNull {
			it.needsAction() && (hasEnoughWater || it.isDone())
		} ?: script.lastPatch ?: return
		debug("Refreshed and selected next patch! INTERACTING")

		if (patch.handle(script.patches)) {
			val currentPatchIndex = script.patches.index(patch)
			val nextPatchIndex = currentPatchIndex + 1
			val nextPatch =
				if (nextPatchIndex == script.patches.size) script.patches[0] else script.patches[nextPatchIndex]
			debug("Handled patch=${currentPatchIndex}, nextPatchIndex=$nextPatchIndex, nextPatch at tile=${nextPatch.tile}")
			script.nextPatch = nextPatch
		}
	}

}