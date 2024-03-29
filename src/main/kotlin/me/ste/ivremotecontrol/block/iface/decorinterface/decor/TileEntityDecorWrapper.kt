package me.ste.ivremotecontrol.block.iface.decorinterface.decor

import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor
import minecrafttransportsimulator.packets.instances.PacketEntityTextChange

class TileEntityDecorWrapper(private val decor: TileEntityDecor) : Decor() {
    override fun getTextLines(axis: ABlockBase.Axis) = this.decor.text
    override fun setTextLines(axis: ABlockBase.Axis, lines: LinkedHashMap<String, String>) = this.decor.updateText(lines)
    override fun getUpdatePacket(axis: ABlockBase.Axis, lines: LinkedHashMap<String, String>) = PacketEntityTextChange(this.decor, lines)
    override val isPole = false
}