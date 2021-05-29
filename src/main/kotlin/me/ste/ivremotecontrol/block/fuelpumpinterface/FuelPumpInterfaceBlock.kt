package me.ste.ivremotecontrol.block.fuelpumpinterface

import me.ste.ivremotecontrol.block.iface.InterfaceBlock
import net.minecraft.world.World

object FuelPumpInterfaceBlock : InterfaceBlock("fuel_pump_interface") {
    override fun createNewTileEntity(worldIn: World, meta: Int) = FuelPumpInterfaceTileEntity()
}