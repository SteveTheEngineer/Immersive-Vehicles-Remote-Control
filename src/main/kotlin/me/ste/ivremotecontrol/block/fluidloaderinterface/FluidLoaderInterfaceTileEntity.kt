package me.ste.ivremotecontrol.block.fluidloaderinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.util.MTSUtil
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFluidLoader
import minecrafttransportsimulator.mcinterface.BuilderTileEntity
import net.minecraft.block.BlockDirectional


class FluidLoaderInterfaceTileEntity : PeripheralTileEntity("fluidloader") {
    private val loader: TileEntityFluidLoader?
        get() {
            val decorPos = this.pos.offset(world.getBlockState(this.pos).getValue(BlockDirectional.FACING))
            val tileEntity = world.getTileEntity(decorPos)
            if (tileEntity !is BuilderTileEntity<*>) {
                return null
            }
            return tileEntity.tileEntity as? TileEntityFluidLoader
        }

    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.loader != null)

    private fun getMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.loader?.let { arrayOf(if (it.unloadMode) "unload" else "load") }

    private fun setMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? = this.loader?.let {
        val mode = ArgumentHelper.getString(args, 0)
        when (mode.toLowerCase()) {
            "load" -> it.unloadMode = false
            "unload" -> it.unloadMode = true
            else -> throw ArgumentHelper.badArgument(0, "a mode", mode)
        }
        null
    }

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

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getMode"] = this::getMode
        this.methods["setMode"] = this::setMode
        this.methods["getFluid"] = this::getFluid
        this.methods["isConnected"] = this::isConnected
        this.methods["getConnectedVehicle"] = this::getConnectedVehicle
    }
}