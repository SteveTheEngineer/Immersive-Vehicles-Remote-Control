package me.ste.ivremotecontrol.block.iface.base

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

data class WorldAndPos(
        val world: World,
        val pos: BlockPos
)
