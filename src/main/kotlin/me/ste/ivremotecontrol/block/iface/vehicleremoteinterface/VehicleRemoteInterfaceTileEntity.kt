package me.ste.ivremotecontrol.block.iface.vehicleremoteinterface

import dan200.computercraft.api.lua.ArgumentHelper
import mcinterface1122.WrapperEntity
import mcinterface1122.WrapperWorld
import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.base.context.CallContext
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import me.ste.ivremotecontrol.util.*
import minecrafttransportsimulator.baseclasses.NavBeacon
import minecrafttransportsimulator.entities.components.AEntityD_Definable
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics.*
import minecrafttransportsimulator.entities.instances.PartEngine
import minecrafttransportsimulator.entities.instances.PartGroundDevice
import minecrafttransportsimulator.entities.instances.PartGun
import minecrafttransportsimulator.jsondefs.JSONText
import minecrafttransportsimulator.mcinterface.InterfaceManager
import minecrafttransportsimulator.packets.instances.*
import net.minecraft.item.ItemStack
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min


class VehicleRemoteInterfaceTileEntity : InterfaceTileEntity("vehicle") {
    override fun isItemValid(stack: ItemStack) = stack.item is VehicleSelectorItem

    // Utility methods
    private fun setAngle(
        vehicle: EntityVehicleF_Physics,
        variableName: String,
        max: Double,
        value: Double
    ) {
        val limitedAngle = max(-max, min(max, value))

        if (variableName == RUDDER_VARIABLE) {
            vehicle.rudderAngle = value
        } else if (variableName == AILERON_VARIABLE) {
            vehicle.aileronAngle = value
        } else if (variableName == ELEVATOR_VARIABLE) {
            vehicle.elevatorAngle = value
        }

        this.setVariable(vehicle, variableName, limitedAngle)
    }

    private fun setVariable(vehicle: AEntityD_Definable<*>, name: String, value: Boolean) {
        if (vehicle.isVariableActive(name) != value) {
            vehicle.toggleVariable(name)
            InterfaceManager.packetInterface.sendToAllClients(PacketEntityVariableToggle(vehicle, name))
        }
    }

    private fun setVariable(vehicle: AEntityD_Definable<*>, name: String, value: Double) {
        vehicle.setVariable(name, value)
        InterfaceManager.packetInterface.sendToAllClients(PacketEntityVariableSet(vehicle, name, value))
    }

    // API methods
    private fun isAvailable(ctx: CallContext): Array<Any?> {
        return arrayOf(this.getVehicle() != null)
    }

    private fun getLocation(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val position = vehicle.position
        val world = (vehicle.world as WrapperWorld).mcWorld

        return arrayOf(position.x, position.y, position.z, world.provider.dimension)
    }

    private fun getVelocity(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val motion = vehicle.motion

        return arrayOf(motion.x, motion.y, motion.z, vehicle.velocity)
    }

    private fun getRotation(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val angles = vehicle.orientation.angles

        return arrayOf(angles.x, angles.y, angles.z)
    }

    private fun getUniqueIds(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val entity = MTSUtil.getEntity(vehicle)

        return arrayOf(entity?.uniqueID?.toString() ?: "unknown", vehicle.uniqueUUID)
    }

    private fun getFuel(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val tank = vehicle.fuelTank

        return arrayOf(tank.fluid, tank.fluidLevel, tank.maxLevel)
    }

    private fun isBeingFueled(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.beingFueled)
    }

    private fun getDefinition(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(SerializationUtil.objectToTree(vehicle.definition))
    }

    private fun getOwner(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.ownerUUID)
    }

    private fun getMass(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.currentMass)
    }

    private fun getElectricPower(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.electricPower)
    }

    private fun getEngines(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val engines = vehicle.engines.mapIndexed { id, engine ->
            // Determine the legacy state name
            var stateName = ""

            stateName += if (engine.running) {
                "RUNNING"
            } else if (engine.magnetoOn) {
                "MAGNETO_ON"
            } else {
                "MAGNETO_OFF"
            }

            if (engine.electricStarterEngaged) {
                stateName += "_ES_ON"
            } else if (engine.handStarterEngaged) {
                stateName += "_HS_ON"
            }

            if (stateName == "MAGNETO_ON") {
                stateName += "_STARTERS_OFF"
            }
            if (stateName == "MAGNETO_OFF") {
                stateName = "ENGINE_OFF"
            }

            // Create the entry
            id to hashMapOf(
                "definition" to SerializationUtil.objectToTree(engine.definition),
                "hasBrokenStarter" to false, // Probably completely removed from the mod
                "currentGear" to engine.currentGear,
                "fuelFlow" to engine.fuelFlow,
                "hasFuelLeak" to engine.fuelLeak,
                "hasBackfired" to engine.backfired,
                "isBadShift" to engine.badShift,
                "downshiftCountdown" to engine.downshiftCountdown,
                "hours" to engine.hours,
                "isCreative" to vehicle.isCreative,
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
        }.toMap()

        return arrayOf(engines)
    }

    private fun getWheels(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val wheels = vehicle.parts.filterIsInstance<PartGroundDevice>().map { wheel ->
            hashMapOf(
                    "definition" to SerializationUtil.objectToTree(wheel.definition),
                    "angularPosition" to wheel.angularPosition,
                    "angularVelocity" to wheel.angularVelocity,
                    "position" to arrayOf(wheel.position.x, wheel.position.y, wheel.position.z)
            )
        }

        return arrayOf(wheels)
    }

    private fun getPassengers(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val passengers = vehicle.parts
                .filter { it.rider != null }
                .map { part ->
                    val mcEntity = (part.rider as WrapperEntity).minecraftEntity

                    hashMapOf(
                            "uuid" to mcEntity.uniqueID.toString(),
                            "name" to mcEntity.name,
                            "controller" to part.placementDefinition.isController
                    )
                }

        return arrayOf(passengers)
    }

    private fun isLocked(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.locked)
    }

    private fun setLocked(ctx: CallContext): Array<Any?>? {
        val value = ctx.getBoolean(0)

        val vehicle = this.getVehicle() ?: return null

        if (vehicle.locked != value) {
            vehicle.toggleLock()
        }

        return null
    }

    private fun getThrottle(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.throttle / MAX_THROTTLE)
    }

    private fun setThrottle(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        val limitedValue = max(0.0, min(1.0, value))
        val mappedValue = MAX_THROTTLE * limitedValue

        this.setVariable(vehicle, THROTTLE_VARIABLE, mappedValue)
        return null
    }

    private fun getBrakeLevel(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.brake / MAX_BRAKE)
    }

    private fun setBrakeLevel(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        val limitedValue = max(0.0, min(1.0, value))
        val mappedValue = MAX_THROTTLE * limitedValue

        this.setVariable(vehicle, BRAKE_VARIABLE, mappedValue)
        return null
    }

    private fun isParkingBrakeActive(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.parkingBrakeOn)
    }

    private fun setParkingBrakeActive(ctx: CallContext): Array<Any?>? {
        val value = ctx.getBoolean(0)
        val vehicle = this.getVehicle() ?: return null

        this.setVariable(vehicle, PARKINGBRAKE_VARIABLE, value)
        return null
    }

    private fun setMagentoActive(ctx: CallContext): Array<Any?>? {
        val id = ctx.getInt(0)
        val value = ctx.getBoolean(1)

        val vehicle = this.getVehicle() ?: return null

        val engine = vehicle.engines[id] ?: return null

        this.setVariable(engine, PartEngine.MAGNETO_VARIABLE, value)
        return null
    }

    private fun setStarterActive(ctx: CallContext): Array<Any?>? {
        val id = ctx.getInt(0)
        val value = ctx.getBoolean(1)

        val vehicle = this.getVehicle() ?: return null

        val engine = vehicle.engines[id] ?: return null

        this.setVariable(engine, PartEngine.ELECTRIC_STARTER_VARIABLE, value)
        return null
    }

    private fun shiftUp(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        for (engine in vehicle.engines) {
            this.setVariable(engine, PartEngine.UP_SHIFT_VARIABLE, true)
        }

        return null
    }

    private fun shiftDown(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        for (engine in vehicle.engines) {
            this.setVariable(engine, PartEngine.DOWN_SHIFT_VARIABLE, true)
        }

        return null
    }

    @Deprecated("Use getLights2 instead")
    private fun getLights(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val lights = VehicleLight.values().associate { light ->
            light.legacyName.toLowerCase() to vehicle.isVariableActive(light.variable)
        }

        return arrayOf(lights)
    }

    private fun getLights2(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val lights = VehicleLight.values().associate { light ->
            light.name.toLowerCase() to vehicle.isVariableActive(light.variable)
        }

        return arrayOf(lights)
    }

    private fun setLightActive(ctx: CallContext): Array<Any?>? {
        val lightString = ctx.getString(0)
        val value = ctx.getBoolean(1)

        val vehicle = this.getVehicle() ?: return null

        val light = VehicleLight.match(lightString) ?: throw ctx.badArgument(0, "a vehicle light", lightString)

        this.setVariable(vehicle, light.variable, value)
        return null
    }

    private fun getInteractionVariableStates(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val collisionGroups = vehicle.definition.collisionGroups ?: return null

        val states = collisionGroups
                .flatMap { it.collisions }
                .filter { it.variableName != null }
                .associate { it.variableName to vehicle.isVariableActive(it.variableName) }

        return arrayOf(states)
    }

    private fun setInteractionVariableActive(ctx: CallContext): Array<Any?>? {
        val name = ctx.getString(0)
        val value = ctx.getBoolean(1)

        val vehicle = this.getVehicle() ?: return null
        val collisionGroups = vehicle.definition.collisionGroups ?: return null

        if (collisionGroups.flatMap { it.collisions }.none { it.variableName == name }) {
            return null
        }

        this.setVariable(vehicle, name, value)
        return null
    }

    @Deprecated("Use getAutopilotState instead")
    private fun getCruiseState(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.autopilotSetting != 0.0, vehicle.autopilotSetting)
    }

    private fun isHornActive(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.isVariableActive(HORN_VARIABLE))
    }

    private fun setHornActive(ctx: CallContext): Array<Any?>? {
        val value = ctx.getBoolean(0)
        val vehicle = this.getVehicle() ?: return null

        this.setVariable(vehicle, HORN_VARIABLE, value)
        return null
    }

    private fun isThrustReversed(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.reverseThrust)
    }

    private fun setThrustReversed(ctx: CallContext): Array<Any?>? {
        val state = ctx.getBoolean(0)

        val vehicle = this.getVehicle() ?: return null
        val motorized = vehicle.definition.motorized ?: return null

        if (motorized.isBlimp) {
            return null
        }

        this.setVariable(vehicle, REVERSE_THRUST_VARIABLE, state)
        return null
    }

    private fun setAutopilotActive(ctx: CallContext): Array<Any?>? {
        val value = ctx.getBoolean(0)

        val vehicle = this.getVehicle() ?: return null
        val motorized = vehicle.definition.motorized ?: return null

        if (!motorized.hasAutopilot) {
            return null
        }

        if (value) {
            this.setVariable(vehicle, AUTOPILOT_VARIABLE, if (motorized.isAircraft) vehicle.position.y else vehicle.velocity)
        } else {
            this.setVariable(vehicle, AUTOPILOT_VARIABLE, 0.0)
        }

        return null
    }

    private fun getFlapAngle(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val selected = vehicle.definition.motorized?.flapNotches?.indexOf(vehicle.flapDesiredAngle.toFloat()) ?: -1

        return arrayOf(
                vehicle.flapDesiredAngle,
                vehicle.flapCurrentAngle.toDouble() / IVRemoteControl.FLAP_STEP.toDouble(),
                selected
        )
    }

    private fun getAileronTrim(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.aileronTrim)
    }

    private fun setAileronTrim(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        this.setAngle(vehicle, AILERON_TRIM_VARIABLE, MAX_AILERON_TRIM, value)
        return null
    }

    private fun getAileronAngle(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.aileronAngle)
    }

    private fun setAileronAngle(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        this.setAngle(vehicle, AILERON_VARIABLE, MAX_AILERON_ANGLE, value)
        return null
    }

    private fun getElevatorTrim(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.elevatorTrim)
    }

    private fun setElevatorTrim(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        this.setAngle(vehicle, ELEVATOR_TRIM_VARIABLE, MAX_ELEVATOR_TRIM, value)
        return null
    }

    private fun getElevatorAngle(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.elevatorAngle)
    }

    private fun setElevatorAngle(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        this.setAngle(vehicle, ELEVATOR_VARIABLE, MAX_ELEVATOR_ANGLE, value)
        return null
    }

    private fun getRudderTrim(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.rudderTrim)
    }

    private fun setRudderTrim(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        this.setAngle(vehicle, RUDDER_TRIM_VARIABLE, MAX_RUDDER_TRIM, value)
        return null
    }

    private fun getRudderAngle(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.rudderAngle)
    }

    private fun setRudderAngle(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null

        this.setAngle(vehicle, RUDDER_VARIABLE, MAX_RUDDER_ANGLE, value)
        return null
    }

    private fun getLandingGearState(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.isVariableActive(GEAR_VARIABLE), vehicle.gearMovementTime)
    }

    private fun setLandingGearDeployed(ctx: CallContext): Array<Any?>? {
        val value = ctx.getBoolean(0)

        val vehicle = this.getVehicle() ?: return null

        this.setVariable(vehicle, GEAR_VARIABLE, value)
        return null
    }

    private fun getCustomVariables(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val customVariables = vehicle.definition.rendering?.customVariables ?: return arrayOf(hashMapOf<String, Boolean>())

        val variables = customVariables.associateWith { vehicle.isVariableActive(it) }

        return arrayOf(variables)
    }

    private fun setCustomVariableActive(ctx: CallContext): Array<Any?>? {
        val name = ctx.getString(0)
        val value = ctx.getBoolean(1)

        val vehicle = this.getVehicle() ?: return null
        val customVariables = vehicle.definition.rendering?.customVariables ?: return null

        if (name !in customVariables) {
            return null
        }

        this.setVariable(vehicle, name, value)
        return null
    }

    @Deprecated("Use setFlapNotch instead")
    private fun setFlapAngle(ctx: CallContext): Array<Any?>? {
        val value = ctx.getDouble(0)

        val vehicle = this.getVehicle() ?: return null
        val flapNotches = vehicle.definition.motorized?.flapNotches ?: return null

        val closestNotch = flapNotches
                .mapIndexed { index, notchValue -> index to (notchValue - value).absoluteValue }
                .minBy { (_, difference) -> difference }
                ?.first
                ?: return null

        this.setFlapNotchInternal(vehicle, flapNotches, closestNotch)
        return null
    }

    @Deprecated("Use getAutopilotState instead")
    private fun isAutopilotActive(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.isVariableActive(AUTOPILOT_VARIABLE))
    }

    @Deprecated("Removed")
    private fun getTrailer(ctx: CallContext): Array<Any?>? = null

    @Deprecated("Removed")
    private fun attachTrailer(ctx: CallContext): Array<Any?> = arrayOf(false, "no_trailer_nearby")

    @Deprecated("Removed")
    private fun detachTrailer(ctx: CallContext): Array<Any?>? = null

    private fun getTextEntries(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        val entries = this.getTextEntriesInternal(vehicle).mapKeys { (text, _) -> text.fieldName }
        return arrayOf(entries)
    }

    private fun setTextEntry(ctx: CallContext): Array<Any?>? {
        val name = ctx.getString(0)
        val value = ctx.getString(1)

        val vehicle = this.getVehicle() ?: return null
        val entries = this.getTextEntriesInternal(vehicle)

        val text = entries.keys.find { it.fieldName == name } ?: return null
        entries[text] = value

        val entriesMap = LinkedHashMap(entries.mapKeys { (text, _) -> text.fieldName })

        vehicle.updateText(entriesMap)
        InterfaceManager.packetInterface.sendToAllClients(PacketEntityTextChange(vehicle, entriesMap))

        return null
    }

    private fun getSelectedBeacon(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.selectedBeaconName)
    }

    private fun setSelectedBeacon(ctx: CallContext): Array<Any?>? {
        val name = ctx.getString(0)

        val vehicle = this.getVehicle() ?: return null

        vehicle.selectedBeaconName = name
        vehicle.selectedBeacon = NavBeacon.getByNameFromWorld(vehicle.world, name)

        InterfaceManager.packetInterface.sendToAllClients(PacketVehicleBeaconChange(vehicle, name))

        return null
    }

    private fun getGuns(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        val guns = vehicle.parts.filterIsInstance<PartGun>()
                .mapIndexed { id, gun -> id to gun }
                .associate { (id, gun) ->
                    id to hashMapOf(
                            "definition" to SerializationUtil.objectToTree(gun.definition),
                            "bulletsLeft" to gun.getRawVariableValue("gun_ammo_count", 0.0F),
                            "firing" to 0,
                            "firedLastCheck" to gun.firedThisCheck,
                            "rotation" to arrayOf(
                                    gun.orientation.angles.x,
                                    gun.orientation.angles.y,
                                    gun.orientation.angles.z
                            ),
                            "position" to arrayOf(gun.position.x, gun.position.y, gun.position.z)
                    )
                }

        return arrayOf(guns)
    }

    private fun getConstants(ctx: CallContext): Array<Any?>? {
        return arrayOf(
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

    private fun getAutopilotState(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null
        return arrayOf(vehicle.autopilotSetting != 0.0, vehicle.autopilotSetting, vehicle.autopilotSetting)
    }

    private fun shiftNeutral(ctx: CallContext): Array<Any?>? {
        val vehicle = this.getVehicle() ?: return null

        for (engine in vehicle.engines) {
            this.setVariable(engine, PartEngine.NEUTRAL_SHIFT_VARIABLE, true)
        }

        return null
    }

    private fun setFlapNotch(ctx: CallContext): Array<Any?>? {
        val notch = ctx.getInt(0)

        val vehicle = this.getVehicle() ?: return null
        val flapNotches = vehicle.definition.motorized?.flapNotches ?: return null

        this.setFlapNotchInternal(vehicle, flapNotches, notch)
        return null
    }

    private fun getTextEntriesInternal(vehicle: EntityVehicleF_Physics): HashMap<JSONText, String> {
        val entries = hashMapOf<JSONText, String>()

        // Vehicle
        val vehicleTextObjects = vehicle.definition.rendering?.textObjects
        if (vehicleTextObjects != null) {
            entries += vehicleTextObjects.associateWith { text -> (vehicle.text[text] ?: "") }
        }

        // Parts
        for (part in vehicle.parts) {
            val partTextObjects = part.definition.rendering?.textObjects ?: continue
            entries += partTextObjects.associateWith { text -> (part.text[text] ?: "") }
        }

        return entries
    }

    private fun setFlapNotchInternal(vehicle: EntityVehicleF_Physics, flapNotches: List<Float>, notch: Int) {
        val limitedNotch = max(0, min(flapNotches.size - 1, notch))
        val flapAngle = flapNotches[limitedNotch]

        this.setVariable(vehicle, FLAPS_VARIABLE, flapAngle.toDouble())
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