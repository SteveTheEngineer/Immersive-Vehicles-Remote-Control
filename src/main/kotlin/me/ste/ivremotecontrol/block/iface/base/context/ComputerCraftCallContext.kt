package me.ste.ivremotecontrol.block.iface.base.context

import dan200.computercraft.api.lua.ArgumentHelper

class ComputerCraftCallContext(private val arguments: Array<Any?>) : CallContext {
    override fun getInt(index: Int) = ArgumentHelper.getInt(this.arguments, index)
    override fun getString(index: Int) = ArgumentHelper.getString(this.arguments, index)
    override fun getBoolean(index: Int) = ArgumentHelper.getBoolean(this.arguments, index)
    override fun getDouble(index: Int) = ArgumentHelper.getDouble(this.arguments, index)

    override fun badArgument(index: Int, expected: String, got: String) = ArgumentHelper.badArgument(index, expected, got)
}