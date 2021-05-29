package me.ste.ivremotecontrol.block.fluidloaderinterface

import me.ste.ivremotecontrol.block.iface.InterfaceBlock
import net.minecraft.world.World

object FluidLoaderInterfaceBlock : InterfaceBlock("fluid_loader_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = FluidLoaderInterfaceTileEntity()
}