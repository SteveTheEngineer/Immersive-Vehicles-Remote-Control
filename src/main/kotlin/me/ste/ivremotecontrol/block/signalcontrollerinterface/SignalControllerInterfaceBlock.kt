package me.ste.ivremotecontrol.block.signalcontrollerinterface

import me.ste.ivremotecontrol.block.iface.InterfaceBlock
import net.minecraft.world.World

object SignalControllerInterfaceBlock : InterfaceBlock("signal_controller_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = SignalControllerInterfaceTileEntity()
}