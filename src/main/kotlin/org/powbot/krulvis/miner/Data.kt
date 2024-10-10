package org.powbot.krulvis.miner

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.container.Container

object Data {

	val WATERSKINS = intArrayOf(1823, 1825, 1827, 1829)

	val SPECIAL_ATTACK_PICKS = intArrayOf(
		11920, 12797, 23677, 25376, 23680, 23682, 13243, 13244, 25063, 25369
	)

	val TOOLS = intArrayOf(
		1265,
		1267,
		1269,
		1271,
		1273,
		1275,
		12297,
		Item.HAMMER,
		1823,
		Item.RAW_KARAMBWANJI,
		3150,
		*SPECIAL_ATTACK_PICKS,
		*Container.GEM_BAG.ids,
		*Container.COAL_BAG.ids,
		*WATERSKINS,
	)

	@JvmStatic
	fun main(args: Array<String>) {
		OUTSIDE_TILES.forEach {
			println("$it, inside=${TOP_POLY.contains(it)}")
		}
	}

	val OUTSIDE_TILES = arrayOf(
		Tile(3750, 5677),
		Tile(3751, 5675),
		Tile(3753, 5674),
		Tile(3754, 5673),
		Tile(3756, 5672),
		Tile(3759, 5670),
		Tile(3760, 5667),
		Tile(3760, 5665),
		Tile(3759, 5663),
	)

	var TOP_POLY = Area(
		Tile(3748, 5685),
		Tile(3766, 5685),
		Tile(3766, 5678),
		Tile(3765, 5657),
		Tile(3760, 5657),
		Tile(3761, 5666),
		Tile(3761, 5667),
		Tile(3760, 5672),
		Tile(3758, 5672),
		Tile(3753, 5675),
		Tile(3752, 5675)
	)
	val EMPTY_WATERSKIN = 1831

	val GEM_BAG_GEMS = intArrayOf(1623, 1621, 1619, 1617)
	val SANDSTONE = intArrayOf(6971, 6973, 6975, 6977)

	fun hasWaterSkins() = Inventory.stream().id(*WATERSKINS).isNotEmpty()

	fun hasEmptyWaterSkin() = Inventory.stream().id(EMPTY_WATERSKIN).isNotEmpty()

}
