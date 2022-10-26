package me.ste.ivremotecontrol.block.iface.base.context

interface CallContext {
    fun getInt(index: Int): Int
    fun getString(index: Int): String
    fun getBoolean(index: Int): Boolean
    fun getDouble(index: Int): Double

    fun badArgument(index: Int, expected: String, got: String): Throwable
}