package me.ste.ivremotecontrol.block.vehicleremoteinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.peripheral.IComputerAccess
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.constants.IVRCConfiguration
import me.ste.ivremotecontrol.constants.IVRCConstants
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import me.ste.ivremotecontrol.util.MTSUtil
import me.ste.ivremotecontrol.util.SerializationUtil
import minecrafttransportsimulator.baseclasses.NavBeacon
import minecrafttransportsimulator.entities.components.AEntityA_Base
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import minecrafttransportsimulator.entities.instances.PartGroundDevice
import minecrafttransportsimulator.entities.instances.PartGun
import minecrafttransportsimulator.mcinterface.BuilderEntityExisting
import minecrafttransportsimulator.mcinterface.WrapperPlayer
import minecrafttransportsimulator.mcinterface.WrapperWorld
import minecrafttransportsimulator.packets.components.InterfacePacket
import minecrafttransportsimulator.packets.instances.*
import minecrafttransportsimulator.systems.NavBeaconSystem
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.ItemStackHandler

class VehicleRemoteInterfaceTileEntity : PeripheralTileEntity("vehicle") {
    private val inventory = ItemStackHandler(1)

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("Inventory", this.inventory.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        this.inventory.deserializeNBT(compound.getCompoundTag("Inventory"))
        super.readFromNBT(compound)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing)
    }

    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            this.inventory as T
        } else {
            null
        }

    val vehicle: EntityVehicleF_Physics?
        get() {
            val stack = this.inventory.getStackInSlot(0)
            if (stack.item == VehicleSelectorItem && stack.hasTagCompound()) {
                val compound = stack.tagCompound!!
                if (compound.hasKey("EntityUUID") && compound.hasKey("VehicleUUID")) {
                    for (world in this.world.minecraftServer!!.worlds) {
                        for (entity in world.loadedEntityList) {
                            if (entity is BuilderEntityExisting) {
                                val vehicle = entity.entity
                                if (vehicle is EntityVehicleF_Physics) {
                                    val source: String
                                    var match: String
                                    when (IVRCConfiguration.LOOKUP_VALUE) {
                                        IVRCConfiguration.LookupValue.ENTITY -> {
                                            source = entity.uniqueID.toString()
                                            match = compound.getString("EntityUUID")
                                        }
                                        IVRCConfiguration.LookupValue.VEHICLE -> {
                                            source = vehicle.uniqueUUID
                                            match = compound.getString("VehicleUUID")
                                        }
                                    }
                                    if (source == match) {
                                        return vehicle
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null
        }

    // Utility methods
    private fun setTrim(
        vehicle: EntityVehicleF_Physics,
        packet: PacketVehicleControlDigital.Controls,
        set: (value: Short) -> Unit,
        get: () -> Short,
        step: Short,
        min: Short,
        max: Short,
        value: Int
    ) {
        val realValue = -(min / step).coerceAtLeast(
            (max / step).coerceAtMost(value)
        ) * step

        while (get() < realValue) {
            set((vehicle.flapDesiredAngle + step).toShort())
            InterfacePacket.sendToAllClients(
                PacketVehicleControlDigital(
                    vehicle,
                    packet,
                    true
                )
            )
        }
        while (get() > realValue) {
            set((vehicle.flapDesiredAngle - step).toShort())
            InterfacePacket.sendToAllClients(
                PacketVehicleControlDigital(
                    vehicle,
                    packet,
                    false
                )
            )
        }
    }

    private fun setAngle(
        vehicle: EntityVehicleF_Physics,
        packet: PacketVehicleControlAnalog.Controls,
        currentValue: Short,
        setValue: (value: Short) -> Unit,
        setCooldown: (value: Byte) -> Unit,
        max: Short,
        step: Short,
        value: Double,
        cooldown: Double
    ) {
        val realAngle = (-max).coerceAtLeast(
            max.toDouble().coerceAtMost(
                value * step
            ).toInt()
        ).toShort()
        val realCooldown = 0.coerceAtLeast(
            Byte.MAX_VALUE.toDouble().coerceAtMost(
                cooldown * IVRCConstants.TICKS_PER_SECOND
            ).toInt()
        ).toByte()
        setValue(realAngle)
        setCooldown(realCooldown)
        InterfacePacket.sendToAllClients(
            PacketVehicleControlAnalog(
                vehicle,
                packet,
                if (realCooldown != Byte.MAX_VALUE) (realAngle - currentValue).toShort() else currentValue,
                realCooldown
            )
        )
    }

    private fun setVariable(vehicle: EntityVehicleF_Physics, name: String, value: Boolean) {
        if (name !in vehicle.variablesOn && value) {
            vehicle.variablesOn += name
            InterfacePacket.sendToAllClients(PacketEntityVariableToggle(vehicle, name))
        } else if (name in vehicle.variablesOn && !value) {
            vehicle.variablesOn -= name
            InterfacePacket.sendToAllClients(PacketEntityVariableToggle(vehicle, name))
        }
    }

    // API methods
    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.vehicle != null)

    private fun getLocation(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.position.x, it.position.y, it.position.z, it.world.world.provider.dimension) }

    private fun getVelocity(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.motion.x, it.motion.y, it.motion.z, it.velocity) }

    private fun getRotation(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.angles.x, it.angles.y, it.angles.z) }

    private fun getUniqueIds(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(MTSUtil.getEntity(it)?.uniqueID?.toString() ?: "unknown", it.uniqueUUID) }

    private fun getFuel(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.fuelTank.fluid, it.fuelTank.fluidLevel, it.fuelTank.maxLevel) }

    private fun isBeingFueled(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.beingFueled) }

    private fun getDefinition(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(SerializationUtil.objectToTree(it.definition)) }

    private fun getOwner(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.ownerUUID) }

    private fun getMass(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.currentMass) }

    private fun getElectricPower(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.electricPower) }

    private fun getEngines(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(
                it.engines.mapValues { (_, engine) ->
                    hashMapOf(
                        "definition" to SerializationUtil.objectToTree(engine.definition),
                        "hasBrokenStarter" to engine.brokenStarter,
                        "currentGear" to engine.currentGear,
                        "fuelFlow" to engine.fuelFlow,
                        "hasFuelLeak" to engine.fuelLeak,
                        "hasBackfired" to engine.backfired,
                        "isBadShift" to engine.badShift,
                        "downshiftCountdown" to engine.downshiftCountdown,
                        "hours" to engine.hours,
                        "isCreative" to engine.isCreative,
                        "hasLinkedEngine" to (engine.linkedEngine != null),
                        "hasOilLeak" to engine.oilLeak,
                        "pressure" to engine.pressure,
                        "propellerGearboxRatio" to engine.propellerGearboxRatio,
                        "reverseGears" to engine.reverseGears,
                        "rpm" to engine.rpm,
                        "position" to arrayOf(
                            engine.position.x,
                            engine.position.y,
                            engine.position.z
                        ),
                        "state" to hashMapOf(
                            "name" to engine.state.name,
                            "electricStarter" to engine.state.esOn,
                            "handStarter" to engine.state.hsOn,
                            "magneto" to engine.state.magnetoOn,
                            "running" to engine.state.running
                        )
                    )
                }
            )
        }

    private fun getWheels(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            arrayOf(
                it.parts.filterIsInstance(PartGroundDevice::class.java).map {
                    hashMapOf(
                        "definition" to SerializationUtil.objectToTree(it.definition),
                        "angularPosition" to it.angularPosition,
                        "angularVelocity" to it.angularVelocity,
                        "position" to arrayOf(it.position.x, it.position.y, it.position.z)
                    )
                }
            )
        }

    private fun getPassengers(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            arrayOf(
                vehicle.locationRiderMap.entries.map { (_, entity) ->
                    hashMapOf(
                        "uuid" to entity.entity.uniqueID.toString(),
                        "name" to entity.entity.name,
                        "controller" to (entity is WrapperPlayer && vehicle.controller == entity)
                    )
                }
            )
        }

    private fun isLocked(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.locked) }

    private fun setLocked(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            it.locked = ArgumentHelper.getBoolean(args, 0)
            null
        }

    private fun getThrottle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.throttle.toDouble() / EntityVehicleF_Physics.MAX_THROTTLE.toDouble()) }

    private fun setThrottle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val throttle = ArgumentHelper.getFiniteDouble(args, 0)
            it.throttle =
                (0.0.coerceAtLeast(1.0.coerceAtMost(throttle)) * EntityVehicleF_Physics.MAX_THROTTLE).toInt().toByte()
            InterfacePacket.sendToAllClients(
                PacketVehicleControlAnalog(
                    vehicle,
                    PacketVehicleControlAnalog.Controls.THROTTLE,
                    it.throttle.toShort(),
                    Byte.MAX_VALUE
                )
            )
            null
        }

    private fun getBrakeLevel(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.brake.toDouble() / EntityVehicleF_Physics.MAX_BRAKE.toDouble()) }

    private fun setBrakeLevel(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val brake = ArgumentHelper.getFiniteDouble(args, 0)
            it.brake = (0.0.coerceAtLeast(1.0.coerceAtMost(brake)) * EntityVehicleF_Physics.MAX_BRAKE).toInt().toByte()
            InterfacePacket.sendToAllClients(
                PacketVehicleControlAnalog(
                    vehicle,
                    PacketVehicleControlAnalog.Controls.BRAKE,
                    it.brake.toShort(),
                    Byte.MAX_VALUE
                )
            )
            null
        }

    private fun isParkingBrakeActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.parkingBrakeOn) }

    private fun setParkingBrakeActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val parkingBrake = ArgumentHelper.getBoolean(args, 0)
            it.parkingBrakeOn = parkingBrake
            InterfacePacket.sendToAllClients(
                PacketVehicleControlDigital(
                    vehicle,
                    PacketVehicleControlDigital.Controls.P_BRAKE,
                    it.parkingBrakeOn
                )
            )
            null
        }

    private fun setMagentoActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            val id = ArgumentHelper.getInt(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            it.engines[id.toByte()]?.let {
                it.setMagnetoStatus(value)
                InterfacePacket.sendToAllClients(
                    PacketPartEngine(
                        it,
                        if (value) PacketPartEngine.Signal.MAGNETO_ON else PacketPartEngine.Signal.MAGNETO_OFF
                    )
                )
            }

            null
        }

    private fun setStarterActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            val id = ArgumentHelper.getInt(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            it.engines[id.toByte()]?.let {
                it.setElectricStarterStatus(value)
                InterfacePacket.sendToAllClients(
                    PacketPartEngine(
                        it,
                        if (value) PacketPartEngine.Signal.ES_ON else PacketPartEngine.Signal.ES_OFF
                    )
                )
            }

            null
        }

    private fun shiftUp(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            for (engine in it.engines.values) {
                if (engine.shiftUp(false)) {
                    InterfacePacket.sendToAllClients(PacketPartEngine(engine, PacketPartEngine.Signal.SHIFT_UP_MANUAL))
                }
            }
            null
        }

    private fun shiftDown(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            for (engine in it.engines.values) {
                if (engine.shiftDown(false)) {
                    InterfacePacket.sendToAllClients(PacketPartEngine(engine, PacketPartEngine.Signal.SHIFT_UP_MANUAL))
                }
            }
            null
        }

    @Deprecated("Removed")
    private fun getLights(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: getLights") }

    @Deprecated("Removed")
    private fun setLightActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: setLightActive") }

    private fun getDoors(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            vehicle.definition.doors?.let { door ->
                arrayOf(
                    hashMapOf(*(door.map { it.name to (it.name in vehicle.variablesOn) }).toTypedArray())
                )
            }
        }

    private fun setDoorOpen(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            val name = ArgumentHelper.getString(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            vehicle.definition.doors?.let {
                for (door in it) {
                    if (door.name == name) {
                        this.setVariable(vehicle, name, value)
                        break
                    }
                }
            }

            null
        }

    @Deprecated("Removed")
    private fun getCruiseState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: getCruiseState") }

    @Deprecated("Removed")
    private fun setCruiseActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: setCruiseActive") }

    private fun isHornActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.hornOn) }

    private fun setHornActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getBoolean(args, 0)

            it.hornOn = value
            InterfacePacket.sendToAllClients(
                PacketVehicleControlDigital(
                    vehicle,
                    PacketVehicleControlDigital.Controls.HORN,
                    value
                )
            )

            null
        }

    private fun isThrustReversed(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.reverseThrust) }

    private fun setThrustReversed(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            val state = ArgumentHelper.getBoolean(args, 0)
            if (it.definition.motorized?.isBlimp != true) {
                it.reverseThrust = state
                InterfacePacket.sendToAllClients(
                    PacketVehicleControlDigital(
                        it,
                        PacketVehicleControlDigital.Controls.REVERSE,
                        state
                    )
                )
            }
            null
        }

    private fun isAutopilotActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.autopilot) }

    private fun setAutopilotActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getBoolean(args, 0)

            it.autopilot = value
            if (value) {
                it.altitudeSetting = it.position.y
                it.speedSetting = it.velocity
            }

            InterfacePacket.sendToAllClients(
                PacketVehicleControlDigital(
                    it,
                    PacketVehicleControlDigital.Controls.AUTOPILOT,
                    value
                )
            )

            null
        }

    private fun getFlapAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(
                it.flapDesiredAngle / IVRCConstants.FLAP_STEP,
                it.flapCurrentAngle.toDouble() / IVRCConstants.FLAP_STEP.toDouble(),
                it.flapNotchSelected
            )
        }

    @Deprecated("Removed")
    private fun setFlapAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: setFlapAngle") }

    private fun getAileronTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.aileronTrim) }

    private fun setAileronTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getInt(args, 0)
            this.setTrim(
                it,
                PacketVehicleControlDigital.Controls.TRIM_ROLL,
                it::aileronTrim::set,
                it::aileronTrim::get,
                1,
                (-EntityVehicleF_Physics.MAX_AILERON_TRIM).toShort(),
                EntityVehicleF_Physics.MAX_AILERON_TRIM,
                value
            )
            null
        }

    private fun getAileronAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(
                it.aileronAngle / IVRCConstants.ANGLE_STEP,
                it.aileronCooldown / IVRCConstants.TICKS_PER_SECOND
            )
        }

    private fun setAileronAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)
            val cooldown = ArgumentHelper.getDouble(args, 1)

            this.setAngle(
                it,
                PacketVehicleControlAnalog.Controls.AILERON,
                it.aileronAngle,
                it::aileronAngle::set,
                it::aileronCooldown::set,
                EntityVehicleF_Physics.MAX_AILERON_ANGLE,
                IVRCConstants.ANGLE_STEP,
                value,
                cooldown
            )

            null
        }

    private fun getElevatorTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.elevatorTrim) }

    private fun setElevatorTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getInt(args, 0)
            this.setTrim(
                it,
                PacketVehicleControlDigital.Controls.TRIM_PITCH,
                it::elevatorTrim::set,
                it::elevatorTrim::get,
                1,
                (-EntityVehicleF_Physics.MAX_ELEVATOR_TRIM).toShort(),
                EntityVehicleF_Physics.MAX_ELEVATOR_TRIM,
                value
            )
            null
        }

    private fun getElevatorAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(
                it.elevatorAngle / IVRCConstants.ANGLE_STEP,
                it.elevatorCooldown / IVRCConstants.TICKS_PER_SECOND
            )
        }

    private fun setElevatorAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)
            val cooldown = ArgumentHelper.getDouble(args, 1)

            this.setAngle(
                it,
                PacketVehicleControlAnalog.Controls.ELEVATOR,
                it.elevatorAngle,
                it::elevatorAngle::set,
                it::elevatorCooldown::set,
                EntityVehicleF_Physics.MAX_ELEVATOR_ANGLE,
                IVRCConstants.ANGLE_STEP,
                value,
                cooldown
            )

            null
        }

    private fun getRudderTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.rudderTrim) }

    private fun setRudderTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getInt(args, 0)
            this.setTrim(
                it,
                PacketVehicleControlDigital.Controls.TRIM_YAW,
                it::rudderTrim::set,
                it::rudderTrim::get,
                1,
                (-EntityVehicleF_Physics.MAX_RUDDER_TRIM).toShort(),
                EntityVehicleF_Physics.MAX_RUDDER_TRIM,
                value
            )
            null
        }

    private fun getRudderAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(
                it.rudderAngle / IVRCConstants.ANGLE_STEP,
                it.rudderCooldown / IVRCConstants.TICKS_PER_SECOND
            )
        }

    private fun setRudderAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)
            val cooldown = ArgumentHelper.getDouble(args, 1)

            this.setAngle(
                it,
                PacketVehicleControlAnalog.Controls.RUDDER,
                it.rudderAngle,
                it::rudderAngle::set,
                it::rudderCooldown::set,
                EntityVehicleF_Physics.MAX_RUDDER_ANGLE,
                IVRCConstants.ANGLE_STEP,
                value,
                cooldown
            )

            null
        }

    private fun getLandingGearState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.gearUpCommand, it.gearMovementTime) }

    private fun setLandingGearDeployed(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getBoolean(args, 0)

            it.gearUpCommand = value
            InterfacePacket.sendToAllClients(
                PacketVehicleControlDigital(
                    it,
                    PacketVehicleControlDigital.Controls.GEAR,
                    value
                )
            )

            null
        }

    private fun getCustomVariables(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            vehicle.definition.rendering?.customVariables?.let { variables ->
                arrayOf(hashMapOf(*variables.map { it to (it in vehicle.variablesOn) }.toTypedArray()))
            }
        }

    private fun setCustomVariableActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            val name = ArgumentHelper.getString(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            vehicle.definition.rendering?.customVariables?.let { it ->
                if (name in it) {
                    this.setVariable(vehicle, name, value)
                }
            }

            null
        }

    @Deprecated("Removed")
    private fun getTrailer(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: getTrailer") }

    @Deprecated("Removed")
    private fun attachTrailer(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: attachTrailer") }

    @Deprecated("Removed")
    private fun detachTrailer(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { throw LuaException("removed method: detachTrailer") }

    private fun getTextEntries(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            val map: MutableMap<String, String> = HashMap()

            vehicle.definition.rendering?.textObjects?.let {
                for (text in it) {
                    map[text.fieldName] = vehicle.text[text]!!
                }
            }

            for (part in vehicle.parts) {
                part.definition.rendering?.textObjects?.let {
                    for (text in it) {
                        map[text.fieldName] = part.text[text]!!
                    }
                }
            }

            arrayOf(map)
        }

    private fun setTextEntry(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            val name = ArgumentHelper.getString(args, 0)
            val value = ArgumentHelper.getString(args, 1)

            val lines: MutableList<String> = ArrayList()

            vehicle.definition.rendering?.textObjects?.let {
                for (text in it) {
                    lines += if (text.fieldName == name) {
                        value.substring(0, value.length.coerceAtMost(text.maxLength))
                    } else {
                        vehicle.text[text]!!
                    }
                }
            }

            for (part in vehicle.parts) {
                part.definition.rendering?.textObjects?.let {
                    for (text in it) {
                        lines += if (text.fieldName == name) {
                            value.substring(0, value.length.coerceAtMost(text.maxLength))
                        } else {
                            part.text[text]!!
                        }
                    }
                }
            }

            vehicle.updateText(lines)
            InterfacePacket.sendToAllClients(PacketEntityTextChange(vehicle, lines))

            null
        }

    private fun getSelectedBeacon(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.selectedBeaconName) }

    private fun setSelectedBeacon(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val name = ArgumentHelper.getString(args, 0)

            it.selectedBeaconName = name
            it.selectedBeacon = NavBeaconSystem.getBeacon(it.world, name)

            InterfacePacket.sendToAllClients(PacketVehicleBeaconChange(it, name))

            null
        }

    private fun getGuns(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            arrayOf(
                hashMapOf(
                    *(vehicle.parts.filterIsInstance<PartGun>().mapIndexed { i, it ->
                        i to hashMapOf(
                            "definition" to SerializationUtil.objectToTree(it.definition),
                            "bulletsLeft" to it.bulletsLeft,
                            "firing" to it.ticksFiring,
                            "rotation" to arrayOf(
                                it.currentOrientation.x,
                                it.currentOrientation.y,
                                it.currentOrientation.z
                            ),
                            "position" to arrayOf(it.position.x, it.position.y, it.position.z)
                        )
                    }).toTypedArray()
                )
            )
        }



    private fun getConstants(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(
                hashMapOf(
                    "aileronAngleBounds" to EntityVehicleF_Physics.MAX_AILERON_ANGLE / IVRCConstants.ANGLE_STEP,
                    "aileronTrimBounds" to EntityVehicleF_Physics.MAX_AILERON_TRIM,
                    "elevatorAngleBounds" to EntityVehicleF_Physics.MAX_ELEVATOR_ANGLE / IVRCConstants.ANGLE_STEP,
                    "elevatorTrimBounds" to EntityVehicleF_Physics.MAX_ELEVATOR_TRIM,
                    "rudderAngleBounds" to EntityVehicleF_Physics.MAX_RUDDER_ANGLE / IVRCConstants.ANGLE_STEP,
                    "rudderTrimBounds" to EntityVehicleF_Physics.MAX_RUDDER_TRIM
                )
            )
        }

    private fun getAutopilotState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.autopilot, it.altitudeSetting, it.speedSetting) }

    private fun shiftNeutral(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            for (engine in it.engines.values) {
                engine.currentGear = 0
            }
            InterfacePacket.sendToAllClients(PacketVehicleControlDigital(it, PacketVehicleControlDigital.Controls.SHIFT_NEUTRAL, true))
            null
        }

    private fun setFlapNotch(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val notch = ArgumentHelper.getInt(args, 0).coerceAtLeast(0).coerceAtMost(it.definition.motorized.flapNotches.size - 1)

            while (it.flapNotchSelected < notch) {
                it.flapNotchSelected++
                InterfacePacket.sendToAllClients(PacketVehicleControlDigital(it, PacketVehicleControlDigital.Controls.FLAPS, true))
            }
            while (it.flapNotchSelected > notch) {
                it.flapNotchSelected--
                InterfacePacket.sendToAllClients(PacketVehicleControlDigital(it, PacketVehicleControlDigital.Controls.FLAPS, false))
            }

            null
        }

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getLocation"] = this::getLocation
        this.methods["getVelocity"] = this::getVelocity
        this.methods["getRotation"] = this::getRotation
        this.methods["getUniqueIds"] = this::getUniqueIds
        this.methods["getFuel"] = this::getFuel
        this.methods["isBeingFueled"] = this::isBeingFueled
        this.methods["getDefinition"] = this::getDefinition
        this.methods["getOwner"] = this::getOwner
        this.methods["getMass"] = this::getMass
        this.methods["getElectricPower"] = this::getElectricPower
        this.methods["getEngines"] = this::getEngines
        this.methods["getWheels"] = this::getWheels
        this.methods["getPassengers"] = this::getPassengers
        this.methods["isLocked"] = this::isLocked
        this.methods["setLocked"] = this::setLocked
        this.methods["getThrottle"] = this::getThrottle
        this.methods["setThrottle"] = this::setThrottle
        this.methods["getBrakeLevel"] = this::getBrakeLevel
        this.methods["setBrakeLevel"] = this::setBrakeLevel
        this.methods["isParkingBrakeActive"] = this::isParkingBrakeActive
        this.methods["setParkingBrakeActive"] = this::setParkingBrakeActive
        this.methods["setMagnetoActive"] = this::setMagentoActive
        this.methods["setStarterActive"] = this::setStarterActive
        this.methods["shiftUp"] = this::shiftUp
        this.methods["shiftNeutral"] = this::shiftNeutral
        this.methods["shiftDown"] = this::shiftDown
        this.methods["getDoors"] = this::getDoors
        this.methods["setDoorOpen"] = this::setDoorOpen
        this.methods["isHornActive"] = this::isHornActive
        this.methods["setHornActive"] = this::setHornActive
        this.methods["isThrustReversed"] = this::isThrustReversed
        this.methods["setThrustReversed"] = this::setThrustReversed
        this.methods["isAutopilotActive"] = this::isAutopilotActive
        this.methods["setAutopilotActive"] = this::setAutopilotActive
        this.methods["getFlapAngle"] = this::getFlapAngle
        this.methods["getAutopilotState"] = this::getAutopilotState
        this.methods["setFlapNotch"] = this::setFlapNotch

        this.methods["getAileronTrim"] = this::getAileronTrim
        this.methods["setAileronTrim"] = this::setAileronTrim
        this.methods["getAileronAngle"] = this::getAileronAngle
        this.methods["setAileronAngle"] = this::setAileronAngle

        this.methods["getElevatorTrim"] = this::getElevatorTrim
        this.methods["setElevatorTrim"] = this::setElevatorTrim
        this.methods["getElevatorAngle"] = this::getElevatorAngle
        this.methods["setElevatorAngle"] = this::setElevatorAngle

        this.methods["getRudderTrim"] = this::getRudderTrim
        this.methods["setRudderTrim"] = this::setRudderTrim
        this.methods["getRudderAngle"] = this::getRudderAngle
        this.methods["setRudderAngle"] = this::setRudderAngle

        this.methods["getLandingGearState"] = this::getLandingGearState
        this.methods["setLandingGearDeployed"] = this::setLandingGearDeployed
        this.methods["getCustomVariables"] = this::getCustomVariables
        this.methods["setCustomVariableActive"] = this::setCustomVariableActive

        this.methods["getTextEntries"] = this::getTextEntries
        this.methods["setTextEntry"] = this::setTextEntry
        this.methods["getSelectedBeacon"] = this::getSelectedBeacon
        this.methods["setSelectedBeacon"] = this::setSelectedBeacon

        this.methods["getGuns"] = this::getGuns

        this.methods["getConstants"] = this::getConstants

        this.methods["getTrailer"] = this::getTrailer
        this.methods["attachTrailer"] = this::attachTrailer
        this.methods["detachTrailer"] = this::detachTrailer
        this.methods["getCruiseState"] = this::getCruiseState
        this.methods["getLights"] = this::getLights
        this.methods["setLightActive"] = this::setLightActive
        this.methods["setCruiseActive"] = this::setCruiseActive
        this.methods["setFlapAngle"] = this::setFlapAngle
    }
}