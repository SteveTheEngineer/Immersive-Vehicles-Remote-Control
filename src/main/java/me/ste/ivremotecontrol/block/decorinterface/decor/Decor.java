package me.ste.ivremotecontrol.block.decorinterface.decor;

import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.packets.components.APacketEntity;

import java.util.Collection;
import java.util.List;

public interface Decor {
    List<String> getTextLines(ABlockBase.Axis axis);
    void setTextLines(ABlockBase.Axis axis, List<String> lines);
    APacketEntity<?> getUpdatePacket(ABlockBase.Axis axis, List<String> lines);
    List<JSONText> getDefinitionTextObjects(ABlockBase.Axis axis);
    boolean isPole();
}