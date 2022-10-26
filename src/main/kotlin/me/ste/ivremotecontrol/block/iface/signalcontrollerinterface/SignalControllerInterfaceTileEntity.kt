package me.ste.ivremotecontrol.block.iface.signalcontrollerinterface

import mcinterface1122.BuilderTileEntity
import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.base.context.CallContext
import me.ste.ivremotecontrol.item.BlockSelectorItem
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntitySignalController
import minecrafttransportsimulator.mcinterface.InterfaceManager
import minecrafttransportsimulator.packets.instances.PacketTileEntitySignalControllerChange
import net.minecraft.item.ItemStack

class SignalControllerInterfaceTileEntity : InterfaceTileEntity("signalcontroller") {
    override fun isItemValid(stack: ItemStack) = stack.item is BlockSelectorItem

    private fun getController() = this.getMTSTileEntity<TileEntitySignalController>()

    private fun isAvailable(ctx: CallContext): Array<Any?> {
        return arrayOf(this.getController() != null)
    }

    private fun getMainDirection(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(controller.mainDirectionAxis.name.toLowerCase())
    }

    private fun setMainDirection(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null

        val directionString = ctx.getString(0)
        when (directionString.toLowerCase()) {
            "north" -> controller.mainDirectionAxis = ABlockBase.Axis.NORTH
            "east" -> controller.mainDirectionAxis = ABlockBase.Axis.EAST
            "northeast" -> controller.mainDirectionAxis = ABlockBase.Axis.NORTHEAST
            "northwest" -> controller.mainDirectionAxis = ABlockBase.Axis.NORTHWEST
            else -> throw ctx.badArgument(0, "a main direction", directionString)
        }

        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    private fun getMode(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(if (controller.timedMode) "timed_cycle" else "vehicle_trigger")
    }

    private fun setMode(ctx: CallContext): Array<Any?>? {
        val modeString = ctx.getString(0)

        val timedMode = when (modeString) {
            "timed_cycle" -> true
            "vehicle_trigger" -> false
            else -> throw ctx.badArgument(0, "an opmode", modeString)
        }

        val controller = this.getController() ?: return null

        controller.timedMode = timedMode

        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    private fun getGreenMainTime(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(controller.greenMainTime)
    }

    private fun getGreenCrossTime(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(controller.greenCrossTime)
    }

    private fun getYellowMainTime(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(controller.yellowMainTime)
    }

    private fun getYellowCrossTime(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(controller.yellowCrossTime)
    }

    private fun getAllRedTime(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null
        return arrayOf(controller.allRedTime)
    }

    private fun setGreenMainTime(ctx: CallContext): Array<Any?>? {
        val value = ctx.getInt(0)

        val controller = this.getController() ?: return null

        controller.greenMainTime = value
        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    private fun setGreenCrossTime(ctx: CallContext): Array<Any?>? {
        val value = ctx.getInt(0)

        val controller = this.getController() ?: return null

        controller.greenCrossTime = value
        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    private fun setYellowMainTime(ctx: CallContext): Array<Any?>? {
        val value = ctx.getInt(0)

        val controller = this.getController() ?: return null

        controller.yellowMainTime = value
        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    private fun setYellowCrossTime(ctx: CallContext): Array<Any?>? {
        val value = ctx.getInt(0)

        val controller = this.getController() ?: return null

        controller.yellowCrossTime = value
        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    private fun setAllRedTime(ctx: CallContext): Array<Any?>? {
        val value = ctx.getInt(0)

        val controller = this.getController() ?: return null

        controller.allRedTime = value
        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
    }

    @Deprecated("Removed")
    private fun getState(ctx: CallContext): Array<Any?> {
        return arrayOf(
                hashMapOf(
                        "name" to "green_main_red_cross",
                        "mainSignal" to "golight",
                        "crossSignal" to "stoplight"
                )
        )
    }

    @Deprecated("Removed")
    private fun getLights(ctx: CallContext): Array<Any?> = arrayOf(false)

    @Deprecated("Removed")
    private fun getTimeOperationStarted(ctx: CallContext): Array<Any?> = arrayOf(0)

    @Deprecated("Use getMainDirection instead")
    private fun getAxis(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null

        return arrayOf(
                when (controller.mainDirectionAxis) {
                    ABlockBase.Axis.NORTH, ABlockBase.Axis.NORTHWEST -> "z"
                    ABlockBase.Axis.EAST, ABlockBase.Axis.NORTHEAST -> "x"
                    else -> "x"
                }
        )
    }

    @Deprecated("Use setMainDirection instead")
    private fun setAxis(ctx: CallContext): Array<Any?>? {
        val controller = this.getController() ?: return null

        val axisString = ctx.getString(0)
        when (axisString.toLowerCase()) {
            "x" -> controller.mainDirectionAxis = ABlockBase.Axis.EAST
            "z" -> controller.mainDirectionAxis = ABlockBase.Axis.NORTH
            else -> throw ctx.badArgument(0, "an axis", axisString)
        }

        controller.initializeController(null)
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntitySignalControllerChange(controller))

        return null
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