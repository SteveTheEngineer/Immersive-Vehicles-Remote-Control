package me.ste.ivremotecontrol.block.fuelpumpinterface

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.util.MTSUtil
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFuelPump
import minecrafttransportsimulator.entities.components.AEntityA_Base
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import minecrafttransportsimulator.entities.instances.PartEngine
import minecrafttransportsimulator.mcinterface.BuilderTileEntity
import minecrafttransportsimulator.packets.components.InterfacePacket
import minecrafttransportsimulator.packets.instances.PacketTileEntityFuelPumpConnection
import minecrafttransportsimulator.systems.ConfigSystem
import net.minecraft.block.BlockDirectional


class FuelPumpInterfaceTileEntity : PeripheralTileEntity("fuelpump") {
    private val pump: TileEntityFuelPump?
        get() {
            val decorPos = this.pos.offset(world.getBlockState(this.pos).getValue(BlockDirectional.FACING))
            val tileEntity = world.getTileEntity(decorPos)
            if (tileEntity !is BuilderTileEntity<*>) {
                return null
            }
            return tileEntity.tileEntity as? TileEntityFuelPump
        }

    private val nearestVehicle: EntityVehicleF_Physics?
        get() = this.pump?.let {
            var vehicle: EntityVehicleF_Physics? = null
            var distance = 16.0

            for (entity in AEntityA_Base.getEntities(it.world)) {
                if (entity is EntityVehicleF_Physics) {
                    val vehicleDistance = entity.position.distanceTo(it.position)
                    if (vehicleDistance < distance) {
                        distance = vehicleDistance
                        vehicle = entity
                    }
                }
            }

            vehicle
        }

    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.pump != null)

    private fun getFuel(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.pump?.let { arrayOf(it.tank.fluidLevel, it.tank.fluidLevel, it.tank.maxLevel) }

    private fun isFueling(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.pump?.let { arrayOf(it.connectedVehicle != null) }

    private fun getVehicle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.pump?.let {
            if (it.connectedVehicle != null) {
                arrayOf(
                    MTSUtil.getEntity(it.connectedVehicle)?.uniqueID?.toString() ?: "unknown",
                    it.connectedVehicle.uniqueUUID
                )
            } else {
                null
            }
        }

    private fun stop(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.pump?.let {
            if (it.connectedVehicle != null) {
                it.connectedVehicle.beingFueled = false
                it.connectedVehicle = null
                InterfacePacket.sendToAllClients(PacketTileEntityFuelPumpConnection(it, false))
            }
            null
        }

    private fun getNearestVehicle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.pump?.let {
            val vehicle = this.nearestVehicle
            if (vehicle != null) {
                arrayOf(MTSUtil.getEntity(vehicle)?.uniqueID?.toString() ?: "unknown", vehicle.uniqueUUID)
            } else {
                null
            }
        }

    private fun start(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.pump?.let {
            if (it.connectedVehicle == null) {
                val vehicle = this.nearestVehicle
                if (vehicle != null) {
                    if (it.tank.fluidLevel > 0) {
                        if (vehicle.fuelTank.fluid.isEmpty() || vehicle.fuelTank.fluid == it.tank.fluid) {
                            for (part in vehicle.parts) {
                                if (part is PartEngine && it.tank.fluid in ConfigSystem.configObject.fuel.fuels[part.definition.engine.fuelType]!!) {
                                    it.connectedVehicle = vehicle
                                    vehicle.beingFueled = true
                                    it.tank.resetAmountDispensed()
                                    InterfacePacket.sendToAllClients(PacketTileEntityFuelPumpConnection(it, true))
                                }
                            }
                            arrayOf(false, "The fuel is not accepted by any of the vehicle's engines")
                        } else {
                            arrayOf(false, "Fuel fluids cannot be mixed")
                        }
                    } else {
                        arrayOf(false, "No fuel in the pump")
                    }
                } else {
                    arrayOf(false, "No vehicle nearby")
                }
            } else {
                arrayOf(false, "A vehicle is already being fueled")
            }

            null
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