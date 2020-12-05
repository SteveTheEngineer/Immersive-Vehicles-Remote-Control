package me.ste.ivremotecontrol.network;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.vehicles.main.AEntityBase;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.nio.charset.StandardCharsets;

public class VehicleDoorPacket implements IMessage {
    private int lookupId;
    private String doorName;

    public VehicleDoorPacket() {}

    public VehicleDoorPacket(EntityVehicleF_Physics vehicle, String doorName) {
        this.lookupId = vehicle.lookupID;
        this.doorName = doorName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.lookupId = buf.readInt();
        this.doorName = buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.lookupId);

        byte[] bytes = this.doorName.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public int getLookupId() {
        return this.lookupId;
    }

    public EntityVehicleF_Physics getVehicle(Side side) {
        for(AEntityBase entity : (side == Side.CLIENT ? AEntityBase.createdClientEntities : AEntityBase.createdServerEntities)) {
            if(entity.lookupID == lookupId && entity instanceof EntityVehicleF_Physics) {
                return (EntityVehicleF_Physics) entity;
            }
        }
        return null;
    }

    public String getDoorName() {
        return this.doorName;
    }

    public static class MessageHandler implements IMessageHandler<VehicleDoorPacket, IMessage> {
        @Override
        public IMessage onMessage(VehicleDoorPacket packet, MessageContext ctx) {
            if(ctx.side == Side.CLIENT) {
                EntityVehicleF_Physics vehicle = packet.getVehicle(ctx.side);
                if(vehicle != null) {
                    if(!vehicle.doorsOpen.contains(packet.getDoorName())) {
                        vehicle.doorsOpen.add(packet.getDoorName());
                    } else {
                        vehicle.doorsOpen.remove(packet.getDoorName());
                    }
                }
            }
            return null;
        }
    }
}
