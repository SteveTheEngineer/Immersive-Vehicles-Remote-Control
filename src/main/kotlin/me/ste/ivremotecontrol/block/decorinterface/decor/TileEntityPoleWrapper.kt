package me.ste.ivremotecontrol.block.decorinterface.decor

import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole
import minecrafttransportsimulator.packets.instances.PacketEntityTextChange

class TileEntityPoleWrapper(private val pole: TileEntityPole) : Decor() {
    override fun getTextLines(axis: ABlockBase.Axis) = this.pole.components[axis]?.text
    override val isPole = true

    override fun setTextLines(axis: ABlockBase.Axis, lines: LinkedHashMap<String, String>) {
        this.pole.components[axis]?.updateText(lines)
    }

    override fun getUpdatePacket(axis: ABlockBase.Axis, lines: LinkedHashMap<String, String>) =
        this.pole.components[axis]?.let { PacketEntityTextChange(it, lines) }
}