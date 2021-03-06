package net.machinemuse.powersuits.client.gui.tinker.frame;

import net.machinemuse.numina.api.constants.NuminaNBTConstants;
import net.machinemuse.powersuits.item.module.PowerModuleBase;
import net.machinemuse.numina.api.module.ModuleManager;
import net.machinemuse.numina.math.geometry.Colour;
import net.machinemuse.numina.math.geometry.MusePoint2D;
import net.machinemuse.numina.utils.module.helpers.WeightHelper;
import net.machinemuse.numina.utils.render.MuseRenderer;
import net.machinemuse.numina.utils.string.MuseStringUtils;
import net.machinemuse.powersuits.api.constants.MPSModuleConstants;
import net.machinemuse.powersuits.common.config.MPSConfig;
import net.machinemuse.powersuits.utils.MuseItemUtils;
import net.machinemuse.numina.utils.energy.ElectricItemUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class DetailedSummaryFrame extends ScrollableFrame {
    public static final double SCALEFACTOR = 1;
    protected EntityPlayer player;
    protected double weight;
    protected double energy;
    protected double armor;

    public DetailedSummaryFrame(EntityPlayer player, MusePoint2D topleft,
                                MusePoint2D bottomright,
                                Colour borderColour, Colour insideColour) {
        super(topleft.times(1.0 / SCALEFACTOR), bottomright.times(1.0 / SCALEFACTOR), borderColour, insideColour);
        this.player = player;
    }

    @Override
    public void update(double mousex, double mousey) {
        weight = WeightHelper.getPlayerWeight(player);
        energy = 0;
        armor = 0;
        for(ItemStack stack : MuseItemUtils.modularItemsEquipped(player)) {
            energy += ElectricItemUtils.getItemEnergy(stack);
            armor += ModuleManager.getInstance().computeModularPropertyDouble(stack, MPSModuleConstants.ARMOR_VALUE_PHYSICAL);
            armor += ModuleManager.getInstance().computeModularPropertyDouble(stack, MPSModuleConstants.ARMOR_VALUE_ENERGY);
        }
    }

    @Override
    public void draw() {
        if (player != null) {
            GL11.glPushMatrix();
            GL11.glScaled(SCALEFACTOR, SCALEFACTOR, SCALEFACTOR);
            super.draw();
            int margin = 4;
            int nexty = (int) border.top() + margin;
            MuseRenderer.drawCenteredString(I18n.format("gui.equippedTotals"), (border.left() + border.right())/2, nexty);
            nexty += 10;

            String formattedValue = MuseStringUtils.formatNumberFromUnits(energy, PowerModuleBase.getUnit(NuminaNBTConstants.MAXIMUM_ENERGY));
            String name = I18n.format("gui.energyStorage");
            double valueWidth = MuseRenderer.getStringWidth(formattedValue);
            double allowedNameWidth = border.width() - valueWidth - margin * 2;
            List<String> namesList = MuseStringUtils.wrapStringToVisualLength(name, allowedNameWidth);
            for(int i=0; i<namesList.size();i++) {
                MuseRenderer.drawString(namesList.get(i), border.left() + margin, nexty + 9*i);
            }
            MuseRenderer.drawRightAlignedString(formattedValue, border.right() - margin, nexty + 9 * (namesList.size() - 1) / 2);
            nexty += 10*namesList.size()+1;

            formattedValue = MuseStringUtils.wrapFormatTags(MuseStringUtils.formatNumberFromUnits(weight, PowerModuleBase.getUnit(MPSModuleConstants.WEIGHT)), weight > MPSConfig.getInstance().getWeightCapacity() ? MuseStringUtils.FormatCodes.Red : MuseStringUtils.FormatCodes.BrightGreen);
            name = I18n.format("gui.weight");
            valueWidth = MuseRenderer.getStringWidth(formattedValue);
            allowedNameWidth = border.width() - valueWidth - margin * 2;
            namesList = MuseStringUtils.wrapStringToVisualLength(name, allowedNameWidth);
            assert namesList != null;
            for(int i = 0; i<namesList.size(); i++) {
                MuseRenderer.drawString(namesList.get(i), border.left() + margin, nexty + 9*i);
            }
            MuseRenderer.drawRightAlignedString(formattedValue, border.right() - margin, nexty + 9 * (namesList.size() - 1) / 2);
            nexty += 10*namesList.size()+1;

            formattedValue = MuseStringUtils.formatNumberFromUnits(armor, "pts");
            name = I18n.format("gui.armor");
            valueWidth = MuseRenderer.getStringWidth(formattedValue);
            allowedNameWidth = border.width() - valueWidth - margin * 2;
            namesList = MuseStringUtils.wrapStringToVisualLength(name, allowedNameWidth);
            assert namesList != null;
            for(int i = 0; i<namesList.size(); i++) {
                MuseRenderer.drawString(namesList.get(i), border.left() + margin, nexty + 9*i);
            }
            MuseRenderer.drawRightAlignedString(formattedValue, border.right() - margin, nexty + 9 * (namesList.size() - 1) / 2);

            GL11.glPopMatrix();
        }
    }

    @Override
    public void onMouseDown(double x, double y, int button) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseUp(double x, double y, int button) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getToolTip(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }
}
