package me.ste.ivremotecontrol.block.decorinterface.decor;

import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.packets.instances.PacketTileEntityDecorTextChange;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TileEntityDecorWrapper implements Decor {
    private TileEntityDecor decor;

    public TileEntityDecorWrapper(TileEntityDecor decor) {
        this.decor = decor;
    }


    @Override
    public List<String> getTextLines(ABlockBase.Axis axis) {
        return new LinkedList<>(this.decor.getText().values());
    }

    @Override
    public void setTextLines(ABlockBase.Axis axis, List<String> lines) {
        int i = 0;
        if(this.decor.definition.rendering != null && this.decor.definition.rendering.textObjects != null) {
            for(JSONText text : this.decor.definition.rendering.textObjects) {
                this.decor.text.put(text, lines.get(i++));
            }
        }
    }

    @Override
    public PacketTileEntityDecorTextChange getUpdatePacket(ABlockBase.Axis axis, List<String> lines) {
        return new PacketTileEntityDecorTextChange(this.decor, lines);
    }

    @Override
    public List<JSONText> getDefinitionTextObjects(ABlockBase.Axis axis) {
        return this.decor.definition.general.textObjects;
    }

    @Override
    public boolean isPole() {
        return false;
    }
}