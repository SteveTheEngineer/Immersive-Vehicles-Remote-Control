package me.ste.ivremotecontrol.block.decorinterface.decor;

import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.packets.components.APacketBase;
import minecrafttransportsimulator.packets.components.APacketTileEntity;

import java.util.List;

public interface Decor {
    List<String> getTextLines(ABlockBase.Axis axis);
    void setTextLines(ABlockBase.Axis axis, List<String> lines);
    APacketTileEntity<?> getUpdatePacket(ABlockBase.Axis axis, List<String> lines);
    List<JSONText> getDefinitionTextObjects(ABlockBase.Axis axis);
    boolean isPole();
}