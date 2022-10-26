package me.ste.ivremotecontrol.block.iface.decorinterface

import me.ste.ivremotecontrol.IVRCGuiHandler
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import net.minecraft.world.World

object DecorInterfaceBlock : InterfaceBlock("decor_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = DecorInterfaceTileEntity()
    override fun getGuiId() = IVRCGuiHandler.BLOCK_INTERFACE
}