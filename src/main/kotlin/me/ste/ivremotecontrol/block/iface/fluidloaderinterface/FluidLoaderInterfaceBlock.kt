package me.ste.ivremotecontrol.block.iface.fluidloaderinterface

import me.ste.ivremotecontrol.IVRCGuiHandler
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import net.minecraft.world.World

object FluidLoaderInterfaceBlock : InterfaceBlock("fluid_loader_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = FluidLoaderInterfaceTileEntity()
    override fun getGuiId() = IVRCGuiHandler.BLOCK_INTERFACE
}