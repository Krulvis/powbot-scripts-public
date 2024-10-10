package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getHighAlchValue
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.items.Item.Companion.ABYSSAL_DAGGER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ABYSSAL_WHIP
import org.powbot.krulvis.api.extensions.items.Item.Companion.BASILISK_JAW
import org.powbot.krulvis.api.extensions.items.Item.Companion.BLACK_MASK_10
import org.powbot.krulvis.api.extensions.items.Item.Companion.BLOOD_SHARD
import org.powbot.krulvis.api.extensions.items.Item.Companion.DARK_BOW
import org.powbot.krulvis.api.extensions.items.Item.Companion.DARK_KEY
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRACONIC_VISAGE
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAGON_BOOTS
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAGON_CHAINBODY
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAGON_MED_HELM
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAGON_SPEAR
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAGON_SWORD
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAGON_WARHAMMER
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAKES_CLAW
import org.powbot.krulvis.api.extensions.items.Item.Companion.DRAKES_TOOTH
import org.powbot.krulvis.api.extensions.items.Item.Companion.ETERNAL_CRYSTAL
import org.powbot.krulvis.api.extensions.items.Item.Companion.GRANITE_BOOTS
import org.powbot.krulvis.api.extensions.items.Item.Companion.HYDRA_CLAW
import org.powbot.krulvis.api.extensions.items.Item.Companion.HYDRA_EYE
import org.powbot.krulvis.api.extensions.items.Item.Companion.HYDRA_FANG
import org.powbot.krulvis.api.extensions.items.Item.Companion.HYDRA_HEART
import org.powbot.krulvis.api.extensions.items.Item.Companion.HYDRA_LEATHER
import org.powbot.krulvis.api.extensions.items.Item.Companion.HYDRA_TAIL
import org.powbot.krulvis.api.extensions.items.Item.Companion.KRAKEN_TENTACLE
import org.powbot.krulvis.api.extensions.items.Item.Companion.LEAF_BLADED_BATTLEAXE
import org.powbot.krulvis.api.extensions.items.Item.Companion.OCCULT_NECKLACE
import org.powbot.krulvis.api.extensions.items.Item.Companion.PEGASIAN_CRYSTAL
import org.powbot.krulvis.api.extensions.items.Item.Companion.PRIMORDIAL_CRYSTAL
import org.powbot.krulvis.api.extensions.items.Item.Companion.SHIELD_LEFT_HALF
import org.powbot.krulvis.api.extensions.items.Item.Companion.SMOULDERING_STONE
import org.powbot.krulvis.api.extensions.items.Item.Companion.TRIDENT_FULL
import org.powbot.krulvis.api.extensions.items.Item.Companion.TRIDENT_UNCHARGED
import org.powbot.krulvis.api.extensions.items.Item.Companion.WARPED_SCEPTRE_CHARGED
import org.powbot.krulvis.api.extensions.items.Item.Companion.WARPED_SCEPTRE_UNCHARGED
import org.powbot.krulvis.api.extensions.items.Item.Companion.WYVERN_VISAGE
import org.powbot.krulvis.api.script.KrulScript

class ShouldHighAlch<S : KrulScript>(script: S, override val failedComponent: TreeComponent<S>) :
	Branch<S>(script, "Should high alch?") {
	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "High alching") {
		val alchable = alchable ?: return@SimpleLeaf
		if (!spell.casting()) {
			if (spell.cast()) {
				Utils.waitFor { spell.casting() }
				sleep(150)
			}
		}
		val price = alchable.getPrice().toDouble()
		script.logger.info("Alching alchable=${alchable.name()}, with id=${alchable.id}, HA=${alchable.getHighAlchValue()}, GE=${price}, ${alchable.getHighAlchValue() / price * 100}%")
		if (spell.casting()) {
			val count = Inventory.stream().id(alchable.id).count()
			if (alchable.interact("Cast")) {
				Utils.waitFor { Inventory.stream().id(alchable.id).count() != count }
			}
		}
	}


	val spell get() = Magic.Spell.HIGH_ALCHEMY
	var alchable: Item? = null
	fun alchable(): Item? {
		val lootIds = script.painter.paintBuilder.items
			.filter { row -> row.any { it is InventoryItemPaintItem } }
			.map { row -> (row.first { it is InventoryItemPaintItem } as InventoryItemPaintItem).itemId }
			.toIntArray()
		return Inventory.stream().id(*lootIds).firstOrNull {
			val value = it.getHighAlchValue()
			it.id !in skip && value > 250 && !it.stackable() && value / it.getPrice().toDouble() > .9
		}
	}

	val skip =
		intArrayOf(
			DARK_KEY, LEAF_BLADED_BATTLEAXE, SHIELD_LEFT_HALF,
			DRAGON_MED_HELM, DRAGON_SPEAR, DRAGON_WARHAMMER,
			DRAKES_TOOTH, DRAKES_CLAW, TRIDENT_FULL, TRIDENT_UNCHARGED,
			KRAKEN_TENTACLE, ABYSSAL_WHIP, DRAGON_BOOTS, BLOOD_SHARD,
			WARPED_SCEPTRE_CHARGED, WARPED_SCEPTRE_UNCHARGED,
			BLACK_MASK_10, BASILISK_JAW, DRAGON_SWORD, DRAGON_CHAINBODY,
			GRANITE_BOOTS, WYVERN_VISAGE, DRACONIC_VISAGE, ABYSSAL_DAGGER,
			OCCULT_NECKLACE, DARK_BOW, PEGASIAN_CRYSTAL, PRIMORDIAL_CRYSTAL,
			ETERNAL_CRYSTAL, SMOULDERING_STONE, HYDRA_EYE, HYDRA_CLAW, HYDRA_FANG, HYDRA_TAIL,
			HYDRA_HEART, HYDRA_LEATHER
		)

	override fun validate(): Boolean {
		alchable = alchable()
		return alchable != null && spell.canCast()
	}
}