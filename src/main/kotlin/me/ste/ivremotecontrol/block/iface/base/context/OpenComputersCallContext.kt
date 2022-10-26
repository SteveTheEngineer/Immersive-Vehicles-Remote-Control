package me.ste.ivremotecontrol.block.iface.base.context

import li.cil.oc.api.machine.Arguments

class OpenComputersCallContext(private val arguments: Arguments) : CallContext {
    override fun getInt(index: Int) = this.arguments.checkInteger(index)
    override fun getString(index: Int) = this.arguments.checkString(index)
    override fun getBoolean(index: Int) = this.arguments.checkBoolean(index)
    override fun getDouble(index: Int) = this.arguments.checkDouble(index)

    override fun badArgument(index: Int, expected: String, got: String) = IllegalArgumentException("bad argument #$index ($expected expected, got $got)")
}