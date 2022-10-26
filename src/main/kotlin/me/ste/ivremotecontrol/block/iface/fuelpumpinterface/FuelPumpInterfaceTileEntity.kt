package me.ste.ivremotecontrol.block.iface.fuelpumpinterface

import mcinterface1122.BuilderEntityExisting
import mcinterface1122.BuilderTileEntity
import mcinterface1122.WrapperWorld
import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.base.context.CallContext
import me.ste.ivremotecontrol.item.BlockSelectorItem
import me.ste.ivremotecontrol.util.MTSUtil
import me.ste.ivremotecontrol.util.mcWorld
import me.ste.ivremotecontrol.util.mtsEntity
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFuelPump
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import minecrafttransportsimulator.entities.instances.PartEngine
import minecrafttransportsimulator.mcinterface.InterfaceManager
import minecrafttransportsimulator.packets.instances.PacketTileEntityFuelPumpConnection
import minecrafttransportsimulator.systems.ConfigSystem
import net.minecraft.item.ItemStack


class FuelPumpInterfaceTileEntity : InterfaceTileEntity("fuelpump") {
    override fun isItemValid(stack: ItemStack) = stack.item is BlockSelectorItem

    private fun getPump() = this.getMTSTileEntity<TileEntityFuelPump>()

    private fun getNearestVehicle(): EntityVehicleF_Physics? {
        val pump = this.getPump() ?: return null
        val world = (pump.world as WrapperWorld).mcWorld

        return world.loadedEntityList
                .filterIsInstance<BuilderEntityExisting>()
                .map { it.mtsEntity }
                .filterIsInstance<EntityVehicleF_Physics>()
                .map { it to it.position.distanceTo(pump.position) }
                .filter { (_, distance) -> distance < 16.0 }
                .minBy { (_, distance) -> distance }
                ?.first
    }

    private fun isAvailable(ctx: CallContext): Array<Any?> {
        return arrayOf(this.getPump() != null)
    }

    private fun getFuel(ctx: CallContext): Array<Any?>? {
        val pump = this.getPump() ?: return null
        val tank = pump.tank

        return arrayOf(tank.fluidLevel, tank.fluidLevel, tank.maxLevel)
    }

    private fun isFueling(ctx: CallContext): Array<Any?>? {
        val pump = this.getPump() ?: return null
        return arrayOf(pump.connectedVehicle != null)
    }

    private fun getVehicle(ctx: CallContext): Array<Any?>? {
        val pump = this.getPump() ?: return null
        val vehicle = pump.connectedVehicle ?: return null
        val entity = MTSUtil.getEntity(vehicle)

        return arrayOf(entity?.uniqueID?.toString() ?: "unknown", vehicle.uniqueUUID)
    }

    private fun stop(ctx: CallContext): Array<Any?>? {
        val pump = this.getPump() ?: return null
        val vehicle = pump.connectedVehicle ?: return null

        vehicle.beingFueled = false
        pump.connectedVehicle = null
        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntityFuelPumpConnection(pump, null))

        return null
    }

    private fun getNearestVehicle(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getNearestVehicle() ?: return null
        val entity = MTSUtil.getEntity(vehicle)

        return arrayOf(entity?.uniqueID?.toString() ?: "unknown", vehicle.uniqueUUID)
    }

    private fun start(ctx: CallContext): Array<Any?>? {
        val pump = this.getPump() ?: return null

        if (pump.connectedVehicle != null) {
            return arrayOf(false, "A vehicle is already being fueled")
        }

        if (pump.tank.fluidLevel <= 0) {
            return arrayOf(false, "No fuel in the pump")
        }

        val vehicle = this.getNearestVehicle() ?: return arrayOf(false, "No vehicle nearby")

        if (vehicle.fuelTank.fluid.isNotEmpty() && vehicle.fuelTank.fluid != pump.tank.fluid) {
            return arrayOf(false, "Fuel fluids cannot be mixed")
        }

        if (vehicle.engines.none { engine -> pump.tank.fluid in ConfigSystem.settings.fuel.fuels[engine.definition.engine.fuelType]!! }) {
            return arrayOf(false, "The fuel is not accepted by any of the vehicle's engines")
        }

        pump.connectedVehicle = vehicle
        vehicle.beingFueled = true
        pump.tank.resetAmountDispensed()

        InterfaceManager.packetInterface.sendToAllClients(PacketTileEntityFuelPumpConnection(pump, vehicle))

        return arrayOf(true)
    }

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getFuel"] = this::getFuel
        this.methods["isFueling"] = this::isFueling
        this.methods["getVehicle"] = this::getVehicle
        this.methods["stop"] = this::stop
        this.methods["getNearestVehicle"] = this::getNearestVehicle
        this.methods["start"] = this::start
    }
}