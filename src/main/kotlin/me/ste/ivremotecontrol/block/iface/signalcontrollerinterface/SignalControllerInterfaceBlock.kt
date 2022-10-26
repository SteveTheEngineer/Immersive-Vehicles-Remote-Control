package me.ste.ivremotecontrol.block.iface.signalcontrollerinterface

import me.ste.ivremotecontrol.IVRCGuiHandler
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import net.minecraft.world.World

object SignalControllerInterfaceBlock : InterfaceBlock("signal_controller_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = SignalControllerInterfaceTileEntity()
    override fun getGuiId() = IVRCGuiHandler.BLOCK_INTERFACE
}