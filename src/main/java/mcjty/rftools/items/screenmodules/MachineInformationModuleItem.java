package mcjty.rftools.items.screenmodules;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.MachineInformationScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.MachineInformationClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class MachineInformationModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public MachineInformationModuleItem() {
        super("machineinformation_module");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<MachineInformationScreenModule> getServerScreenModule() {
        return MachineInformationScreenModule.class;
    }

    @Override
    public Class<MachineInformationClientScreenModule> getClientScreenModule() {
        return MachineInformationClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Info";
    }

    private static final IModuleGuiBuilder.Choice[] EMPTY_CHOICES = new IModuleGuiBuilder.Choice[0];

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        World world = guiBuilder.getWorld();
        NBTTagCompound currentData = guiBuilder.getCurrentData();
        IModuleGuiBuilder.Choice[] choices = EMPTY_CHOICES;
        if((currentData.hasKey("monitordim") ? currentData.getInteger("monitordim") : currentData.getInteger("dim")) == world.provider.getDimension()) {
	        TileEntity tileEntity = world.getTileEntity(new BlockPos(currentData.getInteger("monitorx"), currentData.getInteger("monitory"), currentData.getInteger("monitorz")));
	        if (tileEntity instanceof MachineInformation) {
	            MachineInformation information = (MachineInformation)tileEntity;
	            int count = information.getTagCount();
	            choices = new IModuleGuiBuilder.Choice[count];
	            for (int i = 0; i < count; ++i) {
	                choices[i] = new IModuleGuiBuilder.Choice(information.getTagName(i), information.getTagDescription(i));
	            }
	        }
        }

        guiBuilder
                .label("L:").color("color", "Color for the label").label("Txt:").color("txtcolor", "Color for the text").nl()
                .choices("monitorTag", choices).nl()
                .block("monitor").nl();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.MACHINEINFO_RFPERTICK + " RF/tick");
        boolean hasTarget = false;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            if (tagCompound.hasKey("monitorx")) {
                int monitorx = tagCompound.getInteger("monitorx");
                int monitory = tagCompound.getInteger("monitory");
                int monitorz = tagCompound.getInteger("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")");
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(TextFormatting.YELLOW + "Sneak right-click on a supported machine");
            list.add(TextFormatting.YELLOW + "to set the target for this module");
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (te instanceof MachineInformation) {
            tagCompound.setInteger("monitordim", world.provider.getDimension());
            tagCompound.setInteger("monitorx", pos.getX());
            tagCompound.setInteger("monitory", pos.getY());
            tagCompound.setInteger("monitorz", pos.getZ());
            IBlockState state = player.getEntityWorld().getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockTools.getReadableName(world, pos);
            }
            tagCompound.setString("monitorname", name);
            if (world.isRemote) {
                Logging.message(player, "Machine Information module is set to block '" + name + "'");
            }
        } else {
            tagCompound.removeTag("monitordim");
            tagCompound.removeTag("monitorx");
            tagCompound.removeTag("monitory");
            tagCompound.removeTag("monitorz");
            tagCompound.removeTag("monitorname");
            if (world.isRemote) {
                Logging.message(player, "Machine Information module is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return EnumActionResult.SUCCESS;
    }
}