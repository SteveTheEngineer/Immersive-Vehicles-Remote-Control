package me.ste.ivremotecontrol.block.fluidloaderinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.peripheral.IComputerAccess
import mcinterface1122.BuilderTileEntity
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.util.MTSUtil
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFluidLoader
import net.minecraft.block.BlockDirectional


class FluidLoaderInterfaceTileEntity : PeripheralTileEntity("fluidloader") {
    private val loader: TileEntityFluidLoader?
        get() {
            val decorPos = this.pos.offset(world.getBlockState(this.pos).getValue(BlockDirectional.FACING))
            val tileEntity = world.getTileEntity(decorPos)
            if (tileEntity !is BuilderTileEntity<*>) {
                return null
            }
            return tileEntity.mtsTileEntity as? TileEntityFluidLoader
        }

    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.loader != null)

    private fun getFluid(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.loader?.let { arrayOf(it.tank.fluid, it.tank.fluidLevel, it.tank.maxLevel) }

    private fun isConnected(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.loader?.let { arrayOf(it.connectedPart != null) }

    private fun getConnectedVehicle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.loader?.let {
            if (it.connectedPart != null) {
                arrayOf(
                    MTSUtil.getEntity(it.connectedPart.linkedVehicle)?.uniqueID?.toString() ?: "unknown",
                    it.connectedPart.linkedVehicle.uniqueUUID
                )
            } else {
                null
            }
        }

    @Deprecated("Use getType instead")
    private fun getMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.loader?.let { arrayOf(if (it.isUnloader) "unload" else "load") }

    private fun getType(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.loader?.let { arrayOf(if (it.isUnloader) "unloader" else "loader") }

    @Deprecated("Removed")
    private fun setMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? = null

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getFluid"] = this::getFluid
        this.methods["isConnected"] = this::isConnected
        this.methods["getConnectedVehicle"] = this::getConnectedVehicle
        this.methods["getType"] = this::getType

        this.methods["getMode"] = this::getMode
        this.methods["setMode"] = this::setMode
    }
}