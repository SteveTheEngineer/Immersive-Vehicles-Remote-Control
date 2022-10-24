package me.ste.ivremotecontrol.block.decorinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import mcinterface1122.BuilderTileEntity
import me.ste.ivremotecontrol.block.decorinterface.decor.Decor
import me.ste.ivremotecontrol.block.decorinterface.decor.TileEntityDecorWrapper
import me.ste.ivremotecontrol.block.decorinterface.decor.TileEntityPoleWrapper
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole
import minecrafttransportsimulator.mcinterface.InterfaceManager
import net.minecraft.block.BlockDirectional
import kotlin.math.min


class DecorInterfaceTileEntity : PeripheralTileEntity("decor") {
    private val decor: Decor?
        get() {
            val decorPos = this.pos.offset(world.getBlockState(this.pos).getValue(BlockDirectional.FACING))
            val tileEntity = world.getTileEntity(decorPos)
            if (tileEntity !is BuilderTileEntity<*>) {
                return null
            }

            return when (val base = tileEntity.mtsTileEntity) {
                is TileEntityDecor -> TileEntityDecorWrapper(base)
                is TileEntityPole -> TileEntityPoleWrapper(base)
                else -> null
            }
        }

    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.decor != null)

    private fun isPole(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.decor?.let { arrayOf(it.isPole) }

    private fun getText(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? = this.decor?.let { it ->
        val axisString = ArgumentHelper.getString(args, 0)
        val axis: ABlockBase.Axis
        try {
            axis = ABlockBase.Axis.valueOf(axisString.toUpperCase())
        } catch (e: IllegalArgumentException) {
            throw ArgumentHelper.badArgument(0, "an axis", axisString)
        }

        arrayOf(it.getTextLines(axis)?.mapKeys { it.key.fieldName } ?: emptyMap<String, String>())
    }

    private fun setText(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.decor?.let { decor ->
            val axisString = ArgumentHelper.getString(args, 0)
            val name = ArgumentHelper.getString(args, 1)
            val text = ArgumentHelper.getString(args, 2)
            val axis: ABlockBase.Axis
            try {
                axis = ABlockBase.Axis.valueOf(axisString.toUpperCase())
            } catch (e: IllegalArgumentException) {
                throw ArgumentHelper.badArgument(0, "an axis", axisString)
            }

            // Generate the text lines map and apply it
            decor.getTextLines(axis)?.let {
                val lines = LinkedHashMap<String, String>()

                for ((key, value) in it) {
                    lines[key.fieldName] = if (key.fieldName == name) {
                        text.substring(0, min(key.maxLength, text.length))
                    } else {
                        value
                    }
                }

                decor.setTextLines(axis, lines)
                InterfaceManager.packetInterface.sendToAllClients(decor.getUpdatePacket(axis, lines))
            }

            null
        }

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["isPole"] = this::isPole
        this.methods["getText"] = this::getText
        this.methods["setText"] = this::setText
    }
}