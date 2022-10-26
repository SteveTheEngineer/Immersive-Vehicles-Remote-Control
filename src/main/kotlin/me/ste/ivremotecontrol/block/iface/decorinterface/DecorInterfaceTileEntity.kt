package me.ste.ivremotecontrol.block.iface.decorinterface

import me.ste.ivremotecontrol.block.iface.decorinterface.decor.Decor
import me.ste.ivremotecontrol.block.iface.decorinterface.decor.TileEntityDecorWrapper
import me.ste.ivremotecontrol.block.iface.decorinterface.decor.TileEntityPoleWrapper
import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.base.context.CallContext
import me.ste.ivremotecontrol.item.BlockSelectorItem
import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole
import minecrafttransportsimulator.mcinterface.InterfaceManager
import net.minecraft.item.ItemStack
import kotlin.math.min


class DecorInterfaceTileEntity : InterfaceTileEntity("decor") {
    override fun isItemValid(stack: ItemStack) = stack.item is BlockSelectorItem

    private fun getDecor(): Decor? {
        val tile = this.getMTSTileEntity<ATileEntityBase<*>>() ?: return null

        return when (tile) {
            is TileEntityDecor -> TileEntityDecorWrapper(tile)
            is TileEntityPole -> TileEntityPoleWrapper(tile)
            else -> null
        }
    }

    private fun isAvailable(ctx: CallContext): Array<Any?> = arrayOf(this.getDecor() != null)

    private fun isPole(ctx: CallContext): Array<Any?>? {
        val decor = this.getDecor() ?: return null
        return arrayOf(decor.isPole)
    }

    private fun getText(ctx: CallContext): Array<Any?>? {
        val axisString = ctx.getString(0)

        val decor = this.getDecor() ?: return null

        val axis: ABlockBase.Axis
        try {
            axis = ABlockBase.Axis.valueOf(axisString.toUpperCase())
        } catch (e: IllegalArgumentException) {
            throw ctx.badArgument(0, "an axis", axisString)
        }

        return arrayOf(decor.getTextLines(axis)?.mapKeys { it.key.fieldName } ?: emptyMap<String, String>())
    }

    private fun setText(ctx: CallContext): Array<Any?>? {
        val axisString = ctx.getString(0)
        val name = ctx.getString(1)
        val text = ctx.getString(2)

        val decor = this.getDecor() ?: return null

        val axis: ABlockBase.Axis
        try {
            axis = ABlockBase.Axis.valueOf(axisString.toUpperCase())
        } catch (e: IllegalArgumentException) {
            throw ctx.badArgument(0, "an axis", axisString)
        }

        // Generate the text lines map and apply it
        val currentLines = decor.getTextLines(axis) ?: return null
        val newLines = currentLines
                .mapValues { (key, value) ->
                    if (key.fieldName == name) {
                        text.substring(0, min(key.maxLength, text.length))
                    } else {
                        value
                    }
                }
                .mapKeys { (key, _) -> key.fieldName }
        val newLinesMap = LinkedHashMap(newLines)

        decor.setTextLines(axis, newLinesMap)
        InterfaceManager.packetInterface.sendToAllClients(decor.getUpdatePacket(axis, newLinesMap))

        return null
    }

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["isPole"] = this::isPole
        this.methods["getText"] = this::getText
        this.methods["setText"] = this::setText
    }
}