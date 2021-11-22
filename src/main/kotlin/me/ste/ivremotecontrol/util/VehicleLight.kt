package me.ste.ivremotecontrol.util

import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics.*

enum class VehicleLight(
    val variable: String,
    val legacyName: String
) {
    RUNNING_LIGHT(RUNNINGLIGHT_VARIABLE, "runninglight"),
    HEADLIGHT(HEADLIGHT_VARIABLE, "headlight"),
    NAVIGATION_LIGHT(NAVIGATIONLIGHT_VARIABLE, "navigationlight"),
    STROBE_LIGHT(STROBELIGHT_VARIABLE, "strobelight"),
    TAXI_LIGHT(TAXILIGHT_VARIABLE, "taxilight"),
    LANDING_LIGHT(LANDINGLIGHT_VARIABLE, "landinglight"),
    LEFT_TURN_SIGNAL(LEFTTURNLIGHT_VARIABLE, "leftturnlight"),
    RIGHT_TURN_SIGNAL(RIGHTTURNLIGHT_VARIABLE, "rightturnlight");

    companion object {
        fun match(value: String) = values().find {
            it.name.equals(value, ignoreCase = true) || it.legacyName.equals(value, ignoreCase = true)
        }
    }
}