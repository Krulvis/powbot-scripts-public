import org.powbot.api.rt4.ScrollHelper
import org.powbot.api.rt4.Widgets
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.RunePouch
import org.powbot.krulvis.api.extensions.items.RunePouchLoadout
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.RunePouchScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldSetRunePouchRunes

@ScriptManifest("test runepouch", "Tests runepouch", "Krulvis", priv = true)
class TestRunePouch : KrulScript(), RunePouchScript {
    override fun createPainter(): KrulPaint<*> = TestRunePouchPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldSetRunePouchRunes(this, SimpleLeaf(this, "Chilling") {
        val loadout = RunePouchLoadout.J
        val loadoutRunes = loadout.getLoadout()
        val runepouchRunes = RunePouch.runes()

    })
    override val runePouchRunes: List<Pair<Rune, Int>> =
        listOf(Pair(Rune.BLOOD, 5), Pair(Rune.WATER, 3), Pair(Rune.AIR, 1))

}

class TestRunePouchPainter(script: TestRunePouch) : KrulPaint<TestRunePouch>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder
            .addString("Runes") { RunePouch.runes().joinToString() }

        RunePouchLoadout.entries.forEach { loadout ->
            paintBuilder.addString(loadout.name) {
                loadout.getLoadout().joinToString { "${it.first.name}(${it.second})" }
            }
        }
        return paintBuilder.build()
    }
}

fun main() {
    TestRunePouch().startScript("127.0.0.1", "GIM", true)
}