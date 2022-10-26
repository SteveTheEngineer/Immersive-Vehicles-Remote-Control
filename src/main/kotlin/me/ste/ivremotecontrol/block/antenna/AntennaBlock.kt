package me.ste.ivremotecontrol.block.antenna

import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import me.ste.ivremotecontrol.IVRCConfiguration
import me.ste.ivremotecontrol.tab.IVRCCreativeTab
import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.EnumMap
import java.util.function.Supplier

class AntennaBlock private constructor(
        private val tierName: String,
        private val rangeSupplier: Supplier<Double>
) : Block(Material.ROCK) {
    companion object {
        val BASIC = AntennaBlock("basic", Supplier { IVRCConfiguration.BASIC_ANTENNA_RANGE })
        val ADVANCED = AntennaBlock("advanced", Supplier { IVRCConfiguration.ADVANCED_ANTENNA_RANGE })
        val ELITE = AntennaBlock("elite", Supplier { IVRCConfiguration.ELITE_ANTENNA_RANGE })
        val ULTIMATE = AntennaBlock("ultimate", Supplier { IVRCConfiguration.ULTIMATE_ANTENNA_RANGE })
        val CREATIVE = AntennaBlock("creative", Supplier { IVRCConfiguration.CREATIVE_ANTENNA_RANGE })

        val SHAPES = EnumMap(
                mapOf(
                        EnumFacing.UP to AxisAlignedBB(6.0 / 16.0, 0.0, 6.0 / 16.0, 10.0 / 16.0, 2.0, 10.0 / 16.0),
                        EnumFacing.DOWN to AxisAlignedBB(6.0 / 16.0, -1.0, 6.0 / 16.0, 10.0 / 16.0, 1.0, 10.0 / 16.0),
                        EnumFacing.NORTH to AxisAlignedBB(5.0 / 16.0, 5.0 / 16.0, -1.0, 11.0 / 16.0, 11.0 / 16.0, 1.0),
                        EnumFacing.SOUTH to AxisAlignedBB(5.0 / 16.0, 5.0 / 16.0, 0.0, 11.0 / 16.0, 11.0 / 16.0, 2.0),
                        EnumFacing.EAST to AxisAlignedBB(0.0, 5.0 / 16.0, 5.0 / 16.0, 2.0, 11.0 / 16.0, 11.0 / 16.0),
                        EnumFacing.WEST to AxisAlignedBB(-1.0, 5.0 / 16.0, 5.0 / 16.0, 1.0, 11.0 / 16.0, 11.0 / 16.0)
                )
        )
    }

    // Block setup
    init {
        this.registryName = IVRemoteControl.resourceLocation("${this.tierName}_antenna")
        this.unlocalizedName = this.registryName.toString()
        this.setHarvestLevel("pickaxe", 2)
        this.soundType = SoundType.STONE
        this.setCreativeTab(IVRCCreativeTab)
        this.blockHardness = 3F
        this.defaultState = this.blockState.baseState
                .withProperty(FACING, EnumFacing.UP)
    }

    val item = ItemBlock(this).apply {
        this.registryName = this@AntennaBlock.registryName
        this.unlocalizedName = this@AntennaBlock.unlocalizedName
        this.creativeTab = this@AntennaBlock.creativeTabToDisplayOn
    }

    override fun createBlockState() = BlockStateContainer(this, FACING)

    override fun getMetaFromState(state: IBlockState) = state.getValue(FACING).ordinal

    override fun getStateFromMeta(meta: Int) =
            this.defaultState.withProperty(FACING, EnumFacing.values()[meta])

    override fun canPlaceBlockOnSide(world: World, pos: BlockPos, side: EnumFacing): Boolean {
        val targetedBlockState = world.getBlockState(pos.offset(side.opposite))
        val targetedBlock = targetedBlockState.block
        if (targetedBlock !is InterfaceBlock) {
            return false
        }

        val targetedBlockFacing = targetedBlockState.getValue(FACING)
        if (targetedBlockFacing != side) {
            return false
        }

        return true
    }

    override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos)

        val selfFacing = state.getValue(FACING)
        if (!this.canPlaceBlockOnSide(world, pos, selfFacing)) {
            world.destroyBlock(pos, true)
        }
    }

    override fun getStateForPlacement(worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState {
        val state = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
        return state.withProperty(FACING, facing)
    }

    override fun isOpaqueCube(state: IBlockState) = false
    override fun isFullCube(state: IBlockState) = false
    override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing) = true

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB {
        val facing = state.getValue(FACING)
        return SHAPES[facing]!!
    }

    override fun addInformation(stack: ItemStack, world: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        val range = this.rangeSupplier.get()

        tooltip += I18n.format("ivremotecontrol.antenna.tooltip.help0")
        tooltip += I18n.format("ivremotecontrol.antenna.tooltip.range", if (range == -1.0) "∞" else range.toString())
    }

    // Utility
    fun getRange(state: IBlockState, world: World, pos: BlockPos) = this.rangeSupplier.get()
}