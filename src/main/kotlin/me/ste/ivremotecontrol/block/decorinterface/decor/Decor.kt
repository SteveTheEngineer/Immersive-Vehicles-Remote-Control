package me.ste.ivremotecontrol.block.decorinterface.decor

import minecrafttransportsimulator.blocks.components.ABlockBase
import minecrafttransportsimulator.jsondefs.JSONText
import minecrafttransportsimulator.packets.components.APacketEntity

abstract class Decor {
    abstract fun getTextLines(axis: ABlockBase.Axis): Map<JSONText, String>?
    abstract fun setTextLines(axis: ABlockBase.Axis, lines: List<String>)
    abstract fun getUpdatePacket(axis: ABlockBase.Axis, lines: List<String>): APacketEntity<*>?
    abstract val isPole: Boolean
}