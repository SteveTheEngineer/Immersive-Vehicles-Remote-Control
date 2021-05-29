package me.ste.ivremotecontrol.block.decorinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import me.ste.ivremotecontrol.block.decorinterface.decor.Decor
import me.ste.ivremotecontrol.block.decorinterface.decor.TileEntityDecorWrapper
import me.ste.ivremotecontrol.block.decorinterface.decor.TileEntityPoleWrapper
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole
import minecrafttransportsimulator.mcinterface.BuilderTileEntity
import minecrafttransportsimulator.packets.components.InterfacePacket
import net.minecraft.block.BlockDirectional


class DecorInterfaceTileEntity : PeripheralTileEntity("decor") {
    private val decor: Decor?
        get() {
            val decorPos = this.pos.offset(world.getBlockState(this.pos).getValue(BlockDirectional.FACING))
            val tileEntity = world.getTileEntity(decorPos)
            if (tileEntity !is BuilderTileEntity<*>) {
                return null
            }
            val base = tileEntity.tileEntity
            return when (base) {
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

            // Generate the text lines array and apply it
            decor.getTextLines(axis)?.let {
                val lines = ArrayList(it.values)
                for ((i, key) in it.keys.withIndex()) {
                    if (key.fieldName == name) {
                        lines[i] = text.substring(0, key.maxLength.coerceAtMost(text.length))
                    }
                }
                decor.setTextLines(axis, lines)
                InterfacePacket.sendToAllClients(decor.getUpdatePacket(axis, lines))
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