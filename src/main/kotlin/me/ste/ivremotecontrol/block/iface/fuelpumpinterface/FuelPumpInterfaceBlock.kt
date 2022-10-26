package me.ste.ivremotecontrol.block.iface.fuelpumpinterface

import me.ste.ivremotecontrol.IVRCGuiHandler
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import net.minecraft.world.World

object FuelPumpInterfaceBlock : InterfaceBlock("fuel_pump_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = FuelPumpInterfaceTileEntity()
    override fun getGuiId() = IVRCGuiHandler.BLOCK_INTERFACE
}