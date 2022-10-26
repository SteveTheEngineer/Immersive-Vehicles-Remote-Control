package me.ste.ivremotecontrol.block.iface.vehicleremoteinterface

import me.ste.ivremotecontrol.IVRCGuiHandler
import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.items.CapabilityItemHandler

object VehicleRemoteInterfaceBlock : InterfaceBlock("vehicle_remote_controller"), ITileEntityProvider {
    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity = VehicleRemoteInterfaceTileEntity()
    override fun getGuiId() = IVRCGuiHandler.VEHICLE_INTERFACE
}