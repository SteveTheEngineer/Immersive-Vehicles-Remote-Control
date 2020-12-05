package me.ste.ivremotecontrol.block.vehicleremotecontroller;

import me.ste.ivremotecontrol.IVRemoteControl;
import me.ste.ivremotecontrol.ModCreativeTab;
import me.ste.ivremotecontrol.ModGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public class VehicleRemoteControllerBlock extends Block implements ITileEntityProvider {
    public static final VehicleRemoteControllerBlock INSTANCE = new VehicleRemoteControllerBlock();
    public static final Item ITEM_INSTANCE = new ItemBlock(VehicleRemoteControllerBlock.INSTANCE).setRegistryName(VehicleRemoteControllerBlock.INSTANCE.getRegistryName()).setUnlocalizedName(VehicleRemoteControllerBlock.INSTANCE.getUnlocalizedName()).setCreativeTab(VehicleRemoteControllerBlock.INSTANCE.getCreativeTabToDisplayOn());

    private VehicleRemoteControllerBlock() {
        super(Material.IRON, MapColor.CYAN);
        this.setRegistryName(new ResourceLocation("ivremotecontrol", "vehicle_remote_controller"));
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setHarvestLevel("pickaxe", 1);
        this.setSoundType(SoundType.METAL);
        this.setCreativeTab(ModCreativeTab.INSTANCE);
        this.setHardness(3F);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new VehicleRemoteControllerTileEntity();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            playerIn.openGui(IVRemoteControl.INSTANCE, ModGuiHandler.VEHICLE_REMOTE_CONTROLLER, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        VehicleRemoteControllerTileEntity tile = (VehicleRemoteControllerTileEntity) world.getTileEntity(pos);
        ItemStack stack = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
        if (!stack.isEmpty()) {
            world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
        super.breakBlock(world, pos, state);
    }
}
