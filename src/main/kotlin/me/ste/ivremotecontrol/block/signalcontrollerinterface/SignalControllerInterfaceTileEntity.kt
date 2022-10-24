package me.ste.ivremotecontrol.block.signalcontrollerinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import mcinterface1122.BuilderTileEntity
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntitySignalController
import minecrafttransportsimulator.mcinterface.InterfaceManager
import minecrafttransportsimulator.packets.instances.PacketTileEntitySignalControllerChange
import net.minecraft.block.BlockDirectional

class SignalControllerInterfaceTileEntity : PeripheralTileEntity("signalcontroller") {
    private val controller: TileEntitySignalController?
        get() {
            val decorPos = this.pos.offset(world.getBlockState(this.pos).getValue(BlockDirectional.FACING))
            val tileEntity = world.getTileEntity(decorPos)
            if (tileEntity !is BuilderTileEntity<*>) {
                return null
            }
            return tileEntity.mtsTileEntity as? TileEntitySignalController
        }

    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.controller != null)

    private fun getMainDirection(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.mainDirectionAxis.name.toLowerCase()) }

    private fun setMainDirection(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            val direction = ArgumentHelper.getString(args, 0)

            when (direction.toLowerCase()) {
                "north" -> it.mainDirectionAxis = ABlockBase.Axis.NORTH
                "east" -> it.mainDirectionAxis = ABlockBase.Axis.EAST
                "northeast" -> it.mainDirectionAxis = ABlockBase.Axis.NORTHEAST
                "northwest" -> it.mainDirectionAxis = ABlockBase.Axis.NORTHWEST
                else -> throw ArgumentHelper.badArgument(0, "a main direction", direction)
            }

            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))

            null
        }

    private fun getMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(if(it.timedMode) "timed_cycle" else "vehicle_trigger") }

    private fun setMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            val modeString = ArgumentHelper.getString(args, 0)

            when (modeString) {
                "timed_cycle" -> it.timedMode = true
                "vehicle_trigger" -> it.timedMode = false
                else -> throw ArgumentHelper.badArgument(0, "an opmode", modeString)
            }

            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))

            null
        }

    private fun getGreenMainTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.greenMainTime) }

    private fun getGreenCrossTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.greenMainTime) }

    private fun getYellowMainTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.yellowMainTime) }

    private fun getYellowCrossTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.yellowCrossTime) }

    private fun getAllRedTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.allRedTime) }

    private fun setGreenMainTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.greenMainTime = ArgumentHelper.getInt(args, 0)
            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setGreenCrossTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.greenCrossTime = ArgumentHelper.getInt(args, 0)
            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setYellowMainTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.yellowMainTime = ArgumentHelper.getInt(args, 0)
            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setYellowCrossTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.yellowCrossTime = ArgumentHelper.getInt(args, 0)
            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setAllRedTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.allRedTime = ArgumentHelper.getInt(args, 0)
            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    @Deprecated("Removed")
    private fun getState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(
            hashMapOf(
                "name" to "green_main_red_cross",
                "mainSignal" to "golight",
                "crossSignal" to "stoplight"
            )
        ) }

    @Deprecated("Removed")
    private fun getLights(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(false) }

    @Deprecated("Removed")
    private fun getTimeOperationStarted(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(0) }

    @Deprecated("Use getMainDirection instead")
    private fun getAxis(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            arrayOf(
                when (it.mainDirectionAxis) {
                    ABlockBase.Axis.NORTH -> "z"
                    ABlockBase.Axis.EAST -> "x"
                    ABlockBase.Axis.NORTHEAST -> "x"
                    ABlockBase.Axis.NORTHWEST -> "z"
                    else -> "x"
                }
            )
        }

    @Deprecated("Use setMainDirection instead")
    private fun setAxis(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            val axis = ArgumentHelper.getString(args, 0)

            when (axis.toLowerCase()) {
                "x" -> it.mainDirectionAxis = ABlockBase.Axis.EAST
                "z" -> it.mainDirectionAxis = ABlockBase.Axis.NORTH
                else -> throw ArgumentHelper.badArgument(0, "an axis", axis)
            }

            it.initializeController(null)
            InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(it))

            null
        }

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getMainDirection"] = this::getMainDirection
        this.methods["setMainDirection"] = this::setMainDirection
        this.methods["getMode"] = this::getMode
        this.methods["setMode"] = this::setMode
        this.methods["getGreenMainTime"] = this::getGreenMainTime
        this.methods["getGreenCrossTime"] = this::getGreenCrossTime
        this.methods["getYellowMainTime"] = this::getYellowMainTime
        this.methods["getYellowCrossTime"] = this::getYellowCrossTime
        this.methods["getAllRedTime"] = this::getAllRedTime
        this.methods["setGreenMainTime"] = this::setGreenMainTime
        this.methods["setGreenCrossTime"] = this::setGreenCrossTime
        this.methods["setYellowMainTime"] = this::setYellowMainTime
        this.methods["setYellowCrossTime"] = this::setYellowCrossTime
        this.methods["setAllRedTime"] = this::setAllRedTime

        this.methods["getState"] = this::getState
        this.methods["getLights"] = this::getLights
        this.methods["getTimeOperationStarted"] = this::getTimeOperationStarted
        this.methods["getAxis"] = this::getAxis
        this.methods["setAxis"] = this::setAxis
    }
}