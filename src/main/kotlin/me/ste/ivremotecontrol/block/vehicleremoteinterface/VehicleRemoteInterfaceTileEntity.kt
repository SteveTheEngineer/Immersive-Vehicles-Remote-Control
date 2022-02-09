package me.ste.ivremotecontrol.block.vehicleremoteinterface

import dan200.computercraft.api.lua.ArgumentHelper
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.constants.IVRCConfiguration
import me.ste.ivremotecontrol.constants.IVRCConstants
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import me.ste.ivremotecontrol.util.*
import minecrafttransportsimulator.baseclasses.NavBeacon
import minecrafttransportsimulator.entities.components.AEntityD_Definable
import minecrafttransportsimulator.entities.components.AEntityF_Multipart
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics.*
import minecrafttransportsimulator.entities.instances.PartEngine
import minecrafttransportsimulator.entities.instances.PartGroundDevice
import minecrafttransportsimulator.entities.instances.PartGun
import minecrafttransportsimulator.mcinterface.BuilderEntityExisting
import minecrafttransportsimulator.mcinterface.InterfacePacket
import minecrafttransportsimulator.mcinterface.WrapperPlayer
import minecrafttransportsimulator.packets.instances.*
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.ItemStackHandler
import kotlin.math.abs


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
                                val vehicle = entity.mtsEntity
                                if (vehicle is EntityVehicleF_Physics) {
                                    val source: String
                                    var match: String
                                    when (IVRCConfiguration.LOOKUP_VALUE) {
                                        IVRCConfiguration.LookupValue.ENTITY -> {
                                            source = entity.uniqueID.toString()
                                            match = compound.getString("EntityUUID")
                                        }
                                        IVRCConfiguration.LookupValue.VEHICLE -> {
                                            source = vehicle.uniqueUUID.toString()
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
    private fun setAngle(
        vehicle: EntityVehicleF_Physics,
        variableName: String,
        max: Double,
        value: Double
    ) {
        val realAngle = (-max).coerceAtLeast(
            max.coerceAtMost(value)
        )

        if (variableName == RUDDER_VARIABLE) {
            vehicle.rudderAngle = value
        } else if (variableName == AILERON_VARIABLE) {
            vehicle.aileronAngle = value
        } else if (variableName == ELEVATOR_VARIABLE) {
            vehicle.elevatorAngle = value
        }

        this.setVariable(vehicle, variableName, realAngle)
    }

    private fun setVariable(vehicle: AEntityD_Definable<*>, name: String, value: Boolean) {
        if (vehicle.isVariableActive(name) != value) {
            vehicle.toggleVariable(name)
            InterfacePacket.sendToAllClients(PacketEntityVariableToggle(vehicle, name))
        }
    }

    private fun setVariable(vehicle: AEntityD_Definable<*>, name: String, value: Double) {
        vehicle.setVariable(name, value)
        InterfacePacket.sendToAllClients(PacketEntityVariableSet(vehicle, name, value))
    }

    // API methods
    private fun isAvailable(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any> =
        arrayOf(this.vehicle != null)

    private fun getLocation(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.position.x, it.position.y, it.position.z, it.world.mcWorld.provider.dimension) }

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
                    val stateName: String

                    if (engine.running) {
                        if (engine.electricStarterEngaged) {
                            stateName = "RUNNING_ES_ON"
                        } else if (engine.handStarterEngaged) {
                            stateName = "RUNNING_HS_ON"
                        } else {
                            stateName = "RUNNING"
                        }
                    } else {
                        if (engine.magnetoOn) {
                            if (engine.electricStarterEngaged) {
                                stateName = "MAGNETO_ON_ES_ON"
                            } else if (engine.handStarterEngaged) {
                                stateName = "MAGNETO_ON_HS_ON"
                            } else {
                                stateName = "MAGNETO_ON_STARTERS_OFF"
                            }
                        } else {
                            if (engine.electricStarterEngaged) {
                                stateName = "MAGNETO_OFF_ES_ON"
                            } else if (engine.handStarterEngaged) {
                                stateName = "MAGNETO_OFF_HS_ON"
                            } else {
                                stateName = "ENGINE_OFF"
                            }
                        }
                    }

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
                            "name" to stateName,
                            "electricStarter" to engine.electricStarterEngaged,
                            "handStarter" to engine.handStarterEngaged,
                            "magneto" to engine.magnetoOn,
                            "running" to engine.running
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
                    val mcEntity = entity.minecraftEntity

                    hashMapOf(
                        "uuid" to mcEntity.uniqueID.toString(),
                        "name" to mcEntity.name,
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
        this.vehicle?.let { arrayOf(it.throttle.toDouble() / MAX_THROTTLE.toDouble()) }

    private fun setThrottle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val throttle = ArgumentHelper.getFiniteDouble(args, 0)
            val normalizedThrottle =
                0.0.coerceAtLeast(
                    1.0.coerceAtMost(throttle)
                ) * MAX_THROTTLE
            this.setVariable(it, THROTTLE_VARIABLE, normalizedThrottle)
            null
        }

    private fun getBrakeLevel(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.brake / MAX_BRAKE) }

    private fun setBrakeLevel(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val brake = ArgumentHelper.getFiniteDouble(args, 0)
            val normalizedBrake =
                0.0.coerceAtLeast(
                    1.0.coerceAtMost(brake)
                ) * MAX_BRAKE
            this.setVariable(it, BRAKE_VARIABLE, normalizedBrake)
            null
        }

    private fun isParkingBrakeActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.parkingBrakeOn) }

    private fun setParkingBrakeActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val parkingBrake = ArgumentHelper.getBoolean(args, 0)
            this.setVariable(it, PARKINGBRAKE_VARIABLE, parkingBrake)
            null
        }

    private fun setMagentoActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            val id = ArgumentHelper.getInt(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            it.engines[id.toByte()]?.let {
                this.setVariable(it, PartEngine.MAGNETO_VARIABLE, value)
            }

            null
        }

    private fun setStarterActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            val id = ArgumentHelper.getInt(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            it.engines[id.toByte()]?.let {
                this.setVariable(it, PartEngine.ELECTRIC_STARTER_VARIABLE, value)
            }

            null
        }

    private fun shiftUp(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            for (engine in it.engines.values) {
                this.setVariable(engine, PartEngine.UP_SHIFT_VARIABLE, true)
            }
            null
        }

    private fun shiftDown(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            for (engine in it.engines.values) {
                this.setVariable(engine, PartEngine.DOWN_SHIFT_VARIABLE, true)
            }
            null
        }

    @Deprecated("Use getLights2 instead")
    private fun getLights(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            arrayOf(
                hashMapOf(*VehicleLight.values().map {
                    it.legacyName.toLowerCase() to vehicle.isVariableActive(it.variable)
                }.toTypedArray())
            )
        }

    private fun getLights2(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            arrayOf(
                hashMapOf(*VehicleLight.values().map {
                    it.name.toLowerCase() to vehicle.isVariableActive(it.variable)
                }.toTypedArray())
            )
        }

    private fun setLightActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val lightString = ArgumentHelper.getString(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            val light = VehicleLight.match(lightString)
                ?: throw ArgumentHelper.badArgument(0, "a vehicle light", lightString)

            this.setVariable(it, light.variable, value)

            null
        }

    private fun getInteractionVariableStates(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            vehicle.definition.collisionGroups?.let { groups ->
                val states: MutableMap<String, Boolean> = HashMap()

                for (group in groups) {
                    for (box in group.collisions) {
                        if (box.variableName == null) {
                            continue
                        }

                        states[box.variableName] = vehicle.isVariableActive(box.variableName)
                    }
                }

                arrayOf(states)
            }
        }

    private fun setInteractionVariableActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            val name = ArgumentHelper.getString(args, 0)
            val value = ArgumentHelper.getBoolean(args, 1)

            vehicle.definition.collisionGroups?.let { groups ->
                for (group in groups) {
                     for (box in group.collisions) {
                         if (box.variableName == name) {
                             this.setVariable(vehicle, box.variableName, value)
                         }
                     }
                }
            }

            null
        }

    @Deprecated("Use getAutopilotState instead")
    private fun getCruiseState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.autopilotSetting != 0.0, it.autopilotSetting) }

    private fun isHornActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.isVariableActive(HORN_VARIABLE)) }

    private fun setHornActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getBoolean(args, 0)
            this.setVariable(it, HORN_VARIABLE, value)
            null
        }

    private fun isThrustReversed(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.reverseThrust) }

    private fun setThrustReversed(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { it ->
            val state = ArgumentHelper.getBoolean(args, 0)
            if (it.definition.motorized?.isBlimp != true) {
                this.setVariable(it, REVERSE_THRUST_VARIABLE, state)
            }
            null
        }

    private fun setAutopilotActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getBoolean(args, 0)

            if (!it.definition.motorized.hasAutopilot) {
                return null
            }

            if (it.definition.motorized.isAircraft) {
                this.setVariable(it, AUTOPILOT_VARIABLE, it.position.y)
            } else {
                this.setVariable(it, AUTOPILOT_VARIABLE, it.velocity)
            }

            null
        }

    private fun getFlapAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val selected = it.definition.motorized.flapNotches?.indexOf(it.flapDesiredAngle.toFloat()) ?: -1

            arrayOf(
                it.flapDesiredAngle,
                it.flapCurrentAngle.toDouble() / IVRCConstants.FLAP_STEP.toDouble(),
                selected
            )
        }

    private fun getAileronTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.aileronTrim) }

    private fun setAileronTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)
            this.setAngle(it, AILERON_TRIM_VARIABLE, MAX_AILERON_TRIM, value)
            null
        }

    private fun getAileronAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(it.aileronAngle, 0)
        }

    private fun setAileronAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)

            this.setAngle(it, ELEVATOR_VARIABLE, MAX_ELEVATOR_ANGLE, value)

            null
        }

    private fun getElevatorTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.elevatorTrim) }

    private fun setElevatorTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)
            this.setAngle(it, ELEVATOR_TRIM_VARIABLE, MAX_ELEVATOR_TRIM, value)
            null
        }

    private fun getElevatorAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(it.elevatorAngle, 0)
        }

    private fun setElevatorAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)

            this.setAngle(it, ELEVATOR_VARIABLE, MAX_ELEVATOR_ANGLE, value)

            null
        }

    private fun getRudderTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.rudderTrim) }

    private fun setRudderTrim(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)
            this.setAngle(it, RUDDER_TRIM_VARIABLE, MAX_RUDDER_TRIM, value)
            null
        }

    private fun getRudderAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            arrayOf(it.rudderAngle, 0)
        }

    private fun setRudderAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getDouble(args, 0)

            this.setAngle(it, RUDDER_VARIABLE, MAX_RUDDER_ANGLE, value)

            null
        }

    private fun getLandingGearState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.isVariableActive(GEAR_VARIABLE), it.gearMovementTime) }

    private fun setLandingGearDeployed(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            val value = ArgumentHelper.getBoolean(args, 0)
            this.setVariable(it, GEAR_VARIABLE, value)
            null
        }

    private fun getCustomVariables(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { vehicle ->
            vehicle.definition.rendering?.customVariables?.let { variables ->
                arrayOf(hashMapOf(*variables.map { it to vehicle.isVariableActive(it) }.toTypedArray()))
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

    @Deprecated("Use setFlapNotch instead")
    private fun setFlapAngle(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            it.definition.motorized.flapNotches?.let { notches ->
                val requestedAngle = ArgumentHelper.getDouble(args, 0)
                var closestNotch = -1
                var minDifference = -1.0

                for ((notch, angle) in notches.withIndex()) {
                    val difference = abs(angle - requestedAngle)
                    if (minDifference < 0 || difference <= minDifference) {
                        closestNotch = notch
                        minDifference = difference
                    }
                }

                if (closestNotch != -1) {
                    this.setFlapNotchInternal(it, closestNotch)
                }
            }
            null
        }

    @Deprecated("Use getAutopilotState instead")
    private fun isAutopilotActive(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.isVariableActive(AUTOPILOT_VARIABLE)) }

    @Deprecated("Removed")
    private fun getTrailer(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? = null

    @Deprecated("Removed")
    private fun attachTrailer(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(false, "no_trailer_nearby") }

    @Deprecated("Removed")
    private fun detachTrailer(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? = null

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
            it.selectedBeacon = NavBeacon.getByNameFromWorld(it.world, name)

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
                                it.rotation.x,
                                it.rotation.y,
                                it.rotation.z
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
                    "aileronAngleBounds" to MAX_AILERON_ANGLE,
                    "aileronTrimBounds" to MAX_AILERON_TRIM,
                    "elevatorAngleBounds" to MAX_ELEVATOR_ANGLE,
                    "elevatorTrimBounds" to MAX_ELEVATOR_TRIM,
                    "rudderAngleBounds" to MAX_RUDDER_ANGLE,
                    "rudderTrimBounds" to MAX_RUDDER_TRIM
                )
            )
        }

    private fun getAutopilotState(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let { arrayOf(it.autopilotSetting != 0.0, it.autopilotSetting, it.autopilotSetting) }

    private fun shiftNeutral(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            for (engine in it.engines.values) {
                this.setVariable(engine, PartEngine.NEUTRAL_SHIFT_VARIABLE, true)
            }

            null
        }

    private fun setFlapNotch(pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>): Array<Any>? =
        this.vehicle?.let {
            it.definition.motorized.flapNotches?.let { flapNotches ->
                val notch = ArgumentHelper.getInt(args, 0)
                this.setFlapNotchInternal(it, notch)
            }

            null
        }

    private fun setFlapNotchInternal(vehicle: EntityVehicleF_Physics, notch: Int) {
        val target = vehicle.definition.motorized.flapNotches[notch]

        if (notch < vehicle.definition.motorized.flapNotches.size && notch > 0) {
            this.setVariable(vehicle, FLAPS_VARIABLE, target.toDouble())
        }
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
        this.methods["getInteractionVariableStates"] = this::getInteractionVariableStates
        this.methods["setInteractionVariableActive"] = this::setInteractionVariableActive
        this.methods["isHornActive"] = this::isHornActive
        this.methods["setHornActive"] = this::setHornActive
        this.methods["isThrustReversed"] = this::isThrustReversed
        this.methods["setThrustReversed"] = this::setThrustReversed
        this.methods["setAutopilotActive"] = this::setAutopilotActive
        this.methods["getFlapAngle"] = this::getFlapAngle
        this.methods["getAutopilotState"] = this::getAutopilotState
        this.methods["setFlapNotch"] = this::setFlapNotch
        this.methods["getLights"] = this::getLights
        this.methods["getLights2"] = this::getLights2
        this.methods["setLightActive"] = this::setLightActive

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

        this.methods["setCruiseActive"] = this::setAutopilotActive // Deprecated
        this.methods["getDoors"] = this::getInteractionVariableStates // Deprecated
        this.methods["setDoorOpen"] = this::setInteractionVariableActive // Deprecated
        this.methods["getCruiseState"] = this::getCruiseState
        this.methods["isAutopilotActive"] = this::isAutopilotActive
        this.methods["setFlapAngle"] = this::setFlapAngle
        this.methods["getTrailer"] = this::getTrailer
        this.methods["attachTrailer"] = this::attachTrailer
        this.methods["detachTrailer"] = this::detachTrailer
    }
}