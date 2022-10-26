package me.ste.ivremotecontrol.block.iface.base

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.*
import li.cil.oc.server.network.Network
import mcinterface1122.BuilderEntityExisting
import mcinterface1122.BuilderTileEntity
import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.block.antenna.AntennaBlock
import me.ste.ivremotecontrol.block.iface.base.context.CallContext
import me.ste.ivremotecontrol.block.iface.base.context.ComputerCraftCallContext
import me.ste.ivremotecontrol.block.iface.base.context.OpenComputersCallContext
import me.ste.ivremotecontrol.IVRCConfiguration
import me.ste.ivremotecontrol.item.BlockSelectorItem
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import me.ste.ivremotecontrol.util.mtsEntity
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.ItemStackHandler
import kotlin.math.pow
import kotlin.reflect.KClass

@Optional.InterfaceList(
        Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "opencomputers"),
        Optional.Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "opencomputers"),
        Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "opencomputers"),
        Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "computercraft")
)
abstract class InterfaceTileEntity(
    private val type: String
) : TileEntity(), IPeripheral, Environment, SidedEnvironment, ManagedPeripheral {
    // Shared
    companion object {
        private const val TAG_NODE = "oc:node"
    }

    @Optional.Method(modid = "computercraft")
    override fun equals(other: IPeripheral?) = other === this

    protected val methods: MutableMap<String, (ctx: CallContext) -> Array<Any?>?> =
        LinkedHashMap()

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        if (IVRemoteControl.OPENCOMPUTERS_ENABLED) {
            val data = NBTTagCompound()
            this.node?.save(data)
            compound.setTag(TAG_NODE, data)
        }

        compound.setTag("Inventory", this.inventory.serializeNBT())
        compound.setInteger("CurrentSlot", this.slot)

        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        if (IVRemoteControl.OPENCOMPUTERS_ENABLED) {
            this.node?.load(compound.getCompoundTag(TAG_NODE))
        }

        this.inventory.deserializeNBT(compound.getCompoundTag("Inventory"))

        // Force the size
        val oldStacks = (0 until this.inventory.slots).map(this.inventory::getStackInSlot)
        this.inventory.setSize(9)
        for ((index, stack) in oldStacks.withIndex()) {
            if (index >= this.inventory.slots) {
                continue
            }

            this.inventory.setStackInSlot(index, stack)
        }

        this.slot = compound.getInteger("CurrentSlot")

        super.readFromNBT(compound)
    }

    override fun onLoad() {
        if (IVRemoteControl.OPENCOMPUTERS_ENABLED) {
            Network.joinOrCreateNetwork(this)
        }

        super.onLoad()
    }

    override fun onChunkUnload() {
        if (IVRemoteControl.OPENCOMPUTERS_ENABLED) {
            this.node?.remove()
        }

        super.onChunkUnload()
    }

    override fun invalidate() {
        if (IVRemoteControl.OPENCOMPUTERS_ENABLED) {
            this.node?.remove()
        }

        super.invalidate()
    }

    // Inventory
    private val inventory = object : ItemStackHandler(9) {
        override fun isItemValid(slot: Int, stack: ItemStack) = this@InterfaceTileEntity.isItemValid(stack)
    }
    private var slot = 0

    abstract fun isItemValid(stack: ItemStack): Boolean

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
            capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                    || super.hasCapability(capability, facing)

    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                this.inventory as T
            } else {
                super.getCapability(capability, facing)
            }

    // OpenComputers
    private val node: ComponentConnector? by lazy { Network.newNode(this, Visibility.Network).withConnector().withComponent(this.type).create() }

    @Optional.Method(modid = "opencomputers")
    override fun node() = this.node

    @Optional.Method(modid = "opencomputers")
    override fun onConnect(node: Node) {}

    @Optional.Method(modid = "opencomputers")
    override fun onDisconnect(node: Node) {}

    @Optional.Method(modid = "opencomputers")
    override fun onMessage(message: Message) {}

    @Optional.Method(modid = "opencomputers")
    override fun sidedNode(facing: EnumFacing): Node? {
        if (!this.canConnect(facing)) {
            return null
        }

        return this.node
    }

    @Optional.Method(modid = "opencomputers")
    override fun canConnect(facing: EnumFacing): Boolean {
        val state = this.world.getBlockState(this.pos)
        val selfFacing = state.getValue(FACING)

        return facing == selfFacing.opposite
    }

    @Optional.Method(modid = "opencomputers")
    override fun methods() = this.methods.keys.toTypedArray()

    @Optional.Method(modid = "opencomputers")
    override fun invoke(name: String, context: Context, arguments: Arguments): Array<Any?>? {
        val callback = this.methods[name] ?: throw NoSuchMethodException()
        val ctx = OpenComputersCallContext(arguments)
        return callback.invoke(ctx)
    }

    // ComputerCraft
    @Optional.Method(modid = "computercraft")
    override fun getType() = this.type
    
    @Optional.Method(modid = "computercraft")
    override fun getMethodNames() = this.methods.keys.toTypedArray()
    
    @Optional.Method(modid = "computercraft")
    override fun callMethod(
        computer: IComputerAccess,
        context: ILuaContext,
        id: Int,
        arguments: Array<Any?>
    ): Array<Any?>? {
        val method = this.methods.toList().getOrNull(id)
                ?: throw LuaException("Invalid method index: $id.")

        val ctx = ComputerCraftCallContext(arguments)
        return method.second(ctx)
    }

    // Utility
    private fun getRange(): Double {
        val state = this.world.getBlockState(this.pos)
        val block = state.block as? InterfaceBlock ?: return 0.0

        return block.getRange(state, this.world, this.pos)
    }

    private fun isInRange(range: Double, targetPos: Vec3d): Boolean {
        // Check for infinite range
        if (range == -1.0) {
            return true
        }

        // Check if distance is inside the range
        val rangeSquared = range.pow(2)
        val source = Vec3d(this.pos).addVector(0.5, 0.5, 0.5)
        return targetPos.squareDistanceTo(source) <= rangeSquared
    }

    private fun isInRange(range: Double, targetWorld: World, targetPos: Vec3d): Boolean {
        // Check for infinite range
        if (range == -1.0) {
            return true
        }

        // If the range isn't infinite, the target has to be in the same dimension
        if (targetWorld != this.world) {
            return false
        }

        // Check if distance is inside the range
        return this.isInRange(range, targetPos)
    }

    private fun isInRange(targetWorld: World, targetPos: Vec3d) = this.isInRange(this.getRange(), targetWorld, targetPos)

    private fun getCurrentSelector() = this.inventory.getStackInSlot(this.slot)

    protected fun <T : TileEntity> getTileEntity(clazz: KClass<T>): T? {
        val pos = this.getBlock() ?: return null
        val tile = pos.world.getTileEntity(pos.pos)

        if (!clazz.isInstance(tile)) {
            return null
        }

        return tile as T
    }

    protected inline fun <reified T : TileEntity> getTileEntity() = this.getTileEntity(T::class)

    protected fun <T : ATileEntityBase<*>> getMTSTileEntity(clazz: KClass<T>): T? {
        val tile = this.getTileEntity<BuilderTileEntity<*>>() ?: return null
        val mtsTile = tile.mtsTileEntity

        if (!clazz.isInstance(mtsTile)) {
            return null
        }

        return mtsTile as T
    }

    protected inline fun <reified T : ATileEntityBase<*>> getMTSTileEntity() = this.getMTSTileEntity(T::class)

    private fun getBlock(): WorldAndPos? {
        val stack = this.getCurrentSelector()
        if (stack.item != BlockSelectorItem || !stack.hasTagCompound()) {
            return null
        }

        val compound = stack.tagCompound!!
        if (!compound.hasKey("BlockX") || !compound.hasKey("BlockY") || !compound.hasKey("BlockZ") || !compound.hasKey("BlockDimension")) {
            return null
        }

        val world = this.world.minecraftServer?.getWorld(compound.getInteger("BlockDimension")) ?: return null
        val pos = BlockPos(compound.getInteger("BlockX"), compound.getInteger("BlockY"), compound.getInteger("BlockZ"))

        if (!this.isInRange(world, Vec3d(pos).addVector(0.5, 0.5, 0.5))) {
            return null
        }

        return WorldAndPos(world, pos)
    }

    protected fun getVehicle(): EntityVehicleF_Physics? {
        val stack = this.getCurrentSelector()
        if (stack.item != VehicleSelectorItem || !stack.hasTagCompound()) {
            return null
        }

        val compound = stack.tagCompound!!
        if (!compound.hasKey("EntityUUID") || !compound.hasKey("VehicleUUID")) {
            return null
        }

        val entityUUID = compound.getString("EntityUUID")
        val vehicleUUID = compound.getString("VehicleUUID")

        val range = this.getRange()

        val worlds = if (range != -1.0) this.world.minecraftServer!!.worlds else arrayOf(this.world)
        val vehiclePair = worlds
                .flatMap { it.loadedEntityList }
                .filterIsInstance<BuilderEntityExisting>()
                .map { it to it.mtsEntity!! }
                .filter { (_, mtsEntity) -> mtsEntity is EntityVehicleF_Physics }
                .find { (entity, vehicle) ->
                    when (IVRCConfiguration.LOOKUP_VALUE!!) {
                        IVRCConfiguration.LookupValue.ENTITY -> entity.uniqueID.toString() == entityUUID
                        IVRCConfiguration.LookupValue.VEHICLE -> vehicle.uniqueUUID.toString() == vehicleUUID
                    }
                }
                ?: return null

        val entity = vehiclePair.first
        val vehicle = vehiclePair.second as EntityVehicleF_Physics

        if (!this.isInRange(range, entity.positionVector)) {
            return null
        }

        if (
                !IVRCConfiguration.BYPASS_ANTENNA_CHECK
                && "${vehicle.definition.packID}:${vehicle.definition.systemName}" !in IVRemoteControl.ANTENNA_VEHICLES
                && vehicle.parts.none { "${it.definition.packID}:${it.definition.systemName}" in IVRemoteControl.ANTENNA_PARTS }
        ) {
            return null
        }

        return vehicle
    }

    // Default methods
    private fun getSlot(ctx: CallContext): Array<Any?> = arrayOf(this.slot)
    private fun setSlot(ctx: CallContext): Array<Any?> {
        val slot = ctx.getInt(0)

        if (slot < 0 || slot >= this.inventory.slots) {
            throw ctx.badArgument(0, "an integer in range from 0 to ${this.inventory.slots - 1}", slot.toString())
        }

        this.slot = slot

        return emptyArray()
    }
    private fun getUsedSlots(ctx: CallContext): Array<Any?> {
        val slots = mutableSetOf<Int>()

        for (slot in 0 until this.inventory.slots) {
            if (this.inventory.getStackInSlot(slot).isEmpty) {
                continue
            }

            slots += slot
        }

        return arrayOf(slots.toIntArray())
    }

    private fun getSelectedVehicleIds(ctx: CallContext): Array<Any?> {
        val stack = this.getCurrentSelector()
        if (stack.item != VehicleSelectorItem || !stack.hasTagCompound()) {
            return arrayOf("", "")
        }

        val compound = stack.tagCompound!!
        if (!compound.hasKey("EntityUUID") || !compound.hasKey("VehicleUUID")) {
            return arrayOf("", "")
        }

        return arrayOf(compound.getString("EntityUUID"), compound.getString("VehicleUUID"))
    }
    private fun getSelectedBlockPos(ctx: CallContext): Array<Any?> {
        val stack = this.getCurrentSelector()
        if (stack.item != BlockSelectorItem || !stack.hasTagCompound()) {
            return arrayOf()
        }

        val compound = stack.tagCompound!!
        if (!compound.hasKey("BlockX") || !compound.hasKey("BlockY") || !compound.hasKey("BlockZ") || !compound.hasKey("BlockDimension")) {
            return arrayOf()
        }

        return arrayOf(compound.getInteger("BlockX"), compound.getInteger("BlockY"), compound.getInteger("BlockZ"), compound.getInteger("BlockDimension"))
    }

    private fun getMaxRange(ctx: CallContext): Array<Any?> = arrayOf(this.getRange())
    private fun getAntennaType(ctx: CallContext): Array<Any?> {
        val selfState = this.world.getBlockState(this.pos)
        if (selfState.block !is InterfaceBlock) {
            return arrayOf("")
        }

        val selfFacing = selfState.getValue(FACING)
        val antennaState = this.world.getBlockState(this.pos.offset(selfFacing))
        if (antennaState.block !is AntennaBlock) {
            return arrayOf("")
        }

        return arrayOf(antennaState.block.registryName?.toString() ?: "unknown")
    }

    init {
        this.methods["getSlot"] = this::getSlot
        this.methods["setSlot"] = this::setSlot
        this.methods["getUsedSlots"] = this::getUsedSlots

        this.methods["getSelectedVehicleIds"] = this::getSelectedVehicleIds
        this.methods["getSelectedBlockPos"] = this::getSelectedBlockPos

        this.methods["getMaxRange"] = this::getMaxRange
        this.methods["getAntennaType"] = this::getAntennaType
    }
}