package me.ste.ivremotecontrol.block.signalcontrollerinterface;

import me.ste.ivremotecontrol.ModCreativeTab;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalControllerInterfaceBlock extends BlockDirectional implements ITileEntityProvider {
    public static final SignalControllerInterfaceBlock INSTANCE = new SignalControllerInterfaceBlock();
    public static final Item ITEM_INSTANCE = new ItemBlock(SignalControllerInterfaceBlock.INSTANCE).setRegistryName(SignalControllerInterfaceBlock.INSTANCE.getRegistryName()).setUnlocalizedName(SignalControllerInterfaceBlock.INSTANCE.getUnlocalizedName()).setCreativeTab(SignalControllerInterfaceBlock.INSTANCE.getCreativeTabToDisplayOn());

    private SignalControllerInterfaceBlock() {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation("ivremotecontrol", "signal_controller_interface"));
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setHarvestLevel("pickaxe", 1);
        this.setSoundType(SoundType.METAL);
        this.setCreativeTab(ModCreativeTab.INSTANCE);
        this.setHardness(3F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockDirectional.FACING, EnumFacing.UP));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockDirectional.FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockDirectional.FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.blockState.getBaseState().withProperty(BlockDirectional.FACING, EnumFacing.values()[meta]);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SignalControllerInterfaceTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(BlockDirectional.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)));
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }
}