package net.machinemuse.powersuits.item.module.tool;

import net.machinemuse.powersuits.item.module.PowerModuleBase;
import net.machinemuse.numina.api.module.EnumModuleTarget;
import net.machinemuse.numina.api.module.IPlayerTickModule;
import net.machinemuse.numina.api.module.IRightClickModule;
import net.machinemuse.powersuits.utils.module.helpers.PersonalShrinkingModuleHelper;
import net.machinemuse.powersuits.api.constants.MPSModuleConstants;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.utils.MuseItemUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by User: Korynkai
 * 5:41 PM 2014-11-19
 */

/*
    TODO: the mechanics have changed a bit. This module will require a fluid handler
 */
public class PersonalShrinkingModule extends PowerModuleBase implements IRightClickModule, IPlayerTickModule {

    private final ItemStack cpmPSD = new ItemStack( Item.REGISTRY.getObject(new ResourceLocation("cm2", "psd")), 1);
    public PersonalShrinkingModule(String resourceDommain, String UnlocalizedName) {
        super(EnumModuleTarget.TOOLONLY, resourceDommain, UnlocalizedName);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("fluid", 4000);
        cpmPSD.setTagCompound(nbt);
        addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.controlCircuit, 4));
        addInstallCost(cpmPSD);
    }

    @Override
    public String getCategory() {
        return MPSModuleConstants.CATEGORY_TOOL;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return new ActionResult(EnumActionResult.FAIL, itemStackIn);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack itemStackIn, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        return cpmPSD.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public void onPlayerTickActive(EntityPlayer player, ItemStack item) {
        if (!PersonalShrinkingModuleHelper.getCanShrink(item)) {
            PersonalShrinkingModuleHelper.setCanShrink(item, true);
        }
    }

    @Override
    public void onPlayerTickInactive(EntityPlayer player, ItemStack item) {
        if (PersonalShrinkingModuleHelper.getCanShrink(item)) {
            PersonalShrinkingModuleHelper.setCanShrink(item, false);
        }
    }

    public float minF(float a, float b) {
        return a < b ? a : b;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {

    }
}