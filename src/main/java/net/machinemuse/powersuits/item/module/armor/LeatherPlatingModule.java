package net.machinemuse.powersuits.item.module.armor;

import net.machinemuse.powersuits.item.module.PowerModuleBase;
import net.machinemuse.numina.api.module.EnumModuleTarget;
import net.machinemuse.powersuits.api.constants.MPSModuleConstants;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.utils.MuseItemUtils;

public class LeatherPlatingModule extends PowerModuleBase {
    public LeatherPlatingModule(String resourceDommain, String UnlocalizedName) {
        super(EnumModuleTarget.ALLITEMS, resourceDommain, UnlocalizedName);
        addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.ironPlating, 1));
        addTradeoffPropertyDouble("Plating Thickness", MPSModuleConstants.ARMOR_VALUE_PHYSICAL, 3, " Points");
        addTradeoffPropertyDouble("Plating Thickness", MPSModuleConstants.WEIGHT, 10000, "g");



    }

    @Override
    public String getCategory() {
        return MPSModuleConstants.CATEGORY_ARMOR;
    }
}