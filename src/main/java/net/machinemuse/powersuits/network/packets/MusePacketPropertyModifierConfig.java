package net.machinemuse.powersuits.network.packets;

import net.machinemuse.powersuits.item.module.PowerModuleBase;
import net.machinemuse.numina.api.module.IModule;
import net.machinemuse.numina.api.module.ModuleManager;
import net.machinemuse.numina.api.nbt.IPropertyModifier;
import net.machinemuse.numina.api.nbt.PropertyModifierFlatAdditiveDouble;
import net.machinemuse.numina.api.nbt.PropertyModifierLinearAdditiveDouble;
import net.machinemuse.numina.network.MusePackager;
import net.machinemuse.numina.network.MusePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.util.List;
import java.util.Map;

/**
 * Author: MachineMuse (Claire Semple)
 * Created: 7:23 PM, 12/05/13
 *
 * Ported to Java by lehjr on 11/14/16.
 */
public class MusePacketPropertyModifierConfig extends MusePacket {
    EntityPlayer player;
    DataInputStream data;

    public MusePacketPropertyModifierConfig(EntityPlayer player, DataInputStream data) {
        this.player= player;
        this.data = data;
    }

    @Override
    public MusePackager packager() {
        return getPackagerInstance();
    }

    @Override
    public void write() {
        writeInt(ModuleManager.getInstance().getAllModules().size());
        for (ItemStack module : ModuleManager.getInstance().getAllModules()) {
            writeString(module.getUnlocalizedName());
            writeBoolean(((IModule)module.getItem()).isAllowed());
            writeInt(((IModule)module.getItem()).getPropertyModifiers().size());
            for (Map.Entry<String, List<IPropertyModifier>> entry: ((IModule)module.getItem()).getPropertyModifiers().entrySet()) {
                writeString(entry.getKey()); // propertyName
                List<IPropertyModifier> propmodlist = entry.getValue();
                writeInt(propmodlist.size());
                for (IPropertyModifier propmod : propmodlist) {
                    if (propmod instanceof PropertyModifierFlatAdditiveDouble)
                        writeDouble(((PropertyModifierFlatAdditiveDouble) propmod).valueAdded);
                    else if (propmod instanceof PropertyModifierLinearAdditiveDouble)
                        writeDouble(((PropertyModifierLinearAdditiveDouble) propmod).multiplier);
                    else writeDouble(0);
                }
            }
        }
    }

    /**
     * Called by the network manager since it does all the packet mapping
     *
     * @param player
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void handleClient(EntityPlayer player) {
        MusePackager d = MusePacketPropertyModifierConfigPackager.getInstance();
        int numModules = d.readInt(data);
        for (int i = 0; i < numModules; i++) {
            String moduleName = d.readString(data);
            boolean allowed = d.readBoolean(data);
            ItemStack module = ModuleManager.getInstance().getModule(moduleName);
            if (module.getItem() instanceof PowerModuleBase)
                ((PowerModuleBase) module.getItem()).setIsAllowed(allowed);
            int numProps = d.readInt(data);
            for (int j = 0; j < numProps; j++ ) {
                String propName = d.readString(data);
                int numModifiers = d.readInt(data);
                List<IPropertyModifier> proplist = ((IModule)module.getItem()).getPropertyModifiers().get(propName);
                for (int m = 0; m < numModifiers; m++) {
                    IPropertyModifier propMod = proplist.get(m);
                    if (propMod instanceof PropertyModifierFlatAdditiveDouble)
                        ((PropertyModifierFlatAdditiveDouble) propMod).valueAdded = d.readDouble(data);
                    else if (propMod instanceof PropertyModifierLinearAdditiveDouble)
                        ((PropertyModifierLinearAdditiveDouble) propMod).multiplier = d.readDouble(data);
                    else d.readDouble(data);
                }
            }
        }
    }

    private static MusePacketPropertyModifierConfigPackager PACKAGERINSTANCE;
    public static MusePacketPropertyModifierConfigPackager getPackagerInstance() {
        if (PACKAGERINSTANCE == null)
            PACKAGERINSTANCE = new MusePacketPropertyModifierConfigPackager();
        return PACKAGERINSTANCE;
    }

    public static class MusePacketPropertyModifierConfigPackager extends MusePackager {
        @Override
        public MusePacket read(DataInputStream datain, EntityPlayer player) {
            return new MusePacketPropertyModifierConfig(player, datain);
        }
    }
}