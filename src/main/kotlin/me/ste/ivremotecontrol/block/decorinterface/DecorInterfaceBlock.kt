package me.ste.ivremotecontrol.block.decorinterface

import me.ste.ivremotecontrol.block.iface.InterfaceBlock
import net.minecraft.world.World

object DecorInterfaceBlock : InterfaceBlock("decor_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = DecorInterfaceTileEntity()
}