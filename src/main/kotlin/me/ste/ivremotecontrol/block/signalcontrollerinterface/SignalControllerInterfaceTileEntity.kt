package me.ste.ivremotecontrol.block.signalcontrollerinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.constants.IVRCConstants
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntitySignalController
import minecrafttransportsimulator.mcinterface.BuilderTileEntity
import minecrafttransportsimulator.packets.components.InterfacePacket
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
            return tileEntity.tileEntity as? TileEntitySignalController
        }

    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.controller != null)

    private fun getAxis(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(if (it.mainDirectionXAxis) "x" else "z") }

    private fun setAxis(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            val axis = ArgumentHelper.getString(args, 0)
            when (axis.toLowerCase()) {
                "x" -> it.mainDirectionXAxis = false
                "z" -> it.mainDirectionXAxis = true
                else -> throw ArgumentHelper.badArgument(0, "an axis", axis)
            }

            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))

            null
        }

    private fun getMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.currentOpMode.name.toLowerCase()) }

    private fun setMode(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            val modeString = ArgumentHelper.getString(args, 0)
            val mode: TileEntitySignalController.OpMode
            try {
                mode = TileEntitySignalController.OpMode.valueOf(modeString.toUpperCase())
            } catch (e: Exception) {
                throw ArgumentHelper.badArgument(0, "an opmode", modeString)
            }

            it.currentOpMode = mode
            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))

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
            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setGreenCrossTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.greenCrossTime = ArgumentHelper.getInt(args, 0)
            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setYellowMainTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.yellowMainTime = ArgumentHelper.getInt(args, 0)
            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setYellowCrossTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.yellowCrossTime = ArgumentHelper.getInt(args, 0)
            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun setAllRedTime(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            it.allRedTime = ArgumentHelper.getInt(args, 0)
            it.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true)
            InterfacePacket.sendToAllClients(PacketTileEntitySignalControllerChange(it))
            null
        }

    private fun getState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let {
            val state: MutableMap<String, Any> = HashMap()
            state["name"] = it.currentOpState.name.toLowerCase()
            state["mainSignal"] = it.currentOpState.mainLight.name.toLowerCase()
            state["crossSignal"] = it.currentOpState.crossLight.name.toLowerCase()
            arrayOf(state)
        }

    private fun getLights(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.lightsOn) }

    private fun getTimeOperationStarted(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.controller?.let { arrayOf(it.timeOperationStarted / IVRCConstants.TICKS_PER_SECOND) }

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getAxis"] = this::getAxis
        this.methods["setAxis"] = this::setAxis
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
        this.methods["getLights"] = this::getLights
        this.methods["getTimeOperationStarted"] = this::getTimeOperationStarted
    }
}