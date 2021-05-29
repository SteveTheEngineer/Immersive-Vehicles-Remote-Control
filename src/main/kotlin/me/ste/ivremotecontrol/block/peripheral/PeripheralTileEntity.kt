package me.ste.ivremotecontrol.block.peripheral

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.tileentity.TileEntity

open class PeripheralTileEntity(
    private val type: String
) : TileEntity(), IPeripheral {
    protected val methods: MutableMap<String, (pc: IComputerAccess, ctx: ILuaContext, args: Array<Any>) -> Array<Any>?> =
        LinkedHashMap()

    override fun equals(other: IPeripheral?) = other === this
    override fun getType() = this.type
    override fun getMethodNames() = this.methods.keys.toTypedArray()
    override fun callMethod(
        computer: IComputerAccess,
        context: ILuaContext,
        id: Int,
        arguments: Array<Any>
    ): Array<Any>? {
        var index = 0
        for (entry in this.methods) {
            if (index++ == id) {
                return entry.value(computer, context, arguments)
            }
        }
        return null
    }
}