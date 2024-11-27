package org.powbot.krulvis.araxxor

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Components

object Data {

	val ARAXXOR = "Araxxor"
	val MIRROR = "Mirrorback Araxyte"
	val EXPLODING = "Ruptura Araxyte"
	val ENRAGED_ATTACK_ANIMATION = 11487
	val TOXIC_ATTACK_ANIM = 11477
	val SIZE = 6

	fun crawlingTunnel() = Components.stream(229).text("You crawl through the webbed tunnel.").first().visible()

	val webTunnelTile = Tile(3656, 9816, 0)

	val lairArea = Area(Tile(3606, 9838, 0), Tile(3649, 9796, 0))
	val spiderDungeonArea = Area(Tile(3650, 9868, 0), Tile(3713, 9794, 0))
	val outsideLairArea = Area(
		Tile(3671, 3348, 0),
		Tile(3671, 3385, 0),
		Tile(3664, 3386, 0),
		Tile(3664, 3394, 0),
		Tile(3637, 3394, 0),
		Tile(3638, 3422, 0),
		Tile(3700, 3422, 0),
		Tile(3715, 3349, 0)
	)
	val outsidePath = listOf(
		Tile(3673, 3375, 0),
		Tile(3677, 3378, 0),
		Tile(3681, 3381, 0),
		Tile(3676, 3388, 0),
		Tile(3671, 3393, 0),
		Tile(3667, 3396, 0),
		Tile(3663, 3400, 0),
		Tile(3659, 3405, 0)
	)
	val darkmeyerPath = listOf(
		Tile(3592, 3337, 0),
		Tile(3597, 3341, 0),
		Tile(3597, 3347, 0),
		Tile(3597, 3352, 0),
		Tile(3597, 3357, 0),
		Tile(3598, 3363, 0),
		Tile(3597, 3369, 0),
		Tile(3596, 3374, 0),
		Tile(3596, 3380, 0),
		Tile(3601, 3383, 0),
		Tile(3604, 3387, 0),
		Tile(3609, 3388, 0),
		Tile(3613, 3385, 0),
		Tile(3619, 3383, 0),
		Tile(3624, 3383, 0),
		Tile(3625, 3377, 0),
		Tile(3631, 3376, 0),
		Tile(3636, 3376, 0),
		Tile(3639, 3381, 0),
		Tile(3644, 3381, 0),
		Tile(3651, 3379, 0),
		Tile(3657, 3378, 0),
		Tile(3663, 3375, 0)
	)
	val wallArea = Area(Tile(3670, 3384, 0), Tile(3670, 3365, 0))
	val darkmeyerArea = Area(
		Tile(3590, 3399, 0),
		Tile(3588, 3331, 0),
		Tile(3662, 3331, 0),
		Tile(3669, 3375, 0),
		Tile(3669, 3383, 0),
		Tile(3660, 3391, 0),
		Tile(3634, 3398, 0)
	)
}