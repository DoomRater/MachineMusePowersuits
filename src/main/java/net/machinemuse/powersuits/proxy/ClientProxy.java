package net.machinemuse.powersuits.proxy;

import api.player.model.ModelPlayerAPI;
import net.machinemuse.numina.network.MusePacket;
import net.machinemuse.numina.network.MusePacketModeChangeRequest;
import net.machinemuse.numina.network.PacketSender;
import net.machinemuse.powersuits.api.constants.MPSModConstants;
import net.machinemuse.powersuits.client.event.ClientTickHandler;
import net.machinemuse.powersuits.client.event.EventRegisterRenderers;
import net.machinemuse.powersuits.client.event.ModelBakeEventHandler;
import net.machinemuse.powersuits.client.event.RenderEventHandler;
import net.machinemuse.powersuits.client.model.item.armor.SMovingArmorModel;
import net.machinemuse.powersuits.client.model.obj.OBJPlusLoader;
import net.machinemuse.powersuits.client.sound.SoundDictionary;
import net.machinemuse.powersuits.common.ModCompatibility;
import net.machinemuse.powersuits.common.config.MPSConfig;
import net.machinemuse.powersuits.control.KeybindKeyHandler;
import net.machinemuse.powersuits.control.KeybindManager;
import net.machinemuse.powersuits.event.PlayerUpdateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client side of the proxy.
 *
 * @author MachineMuse
 *
 * Ported to Java by lehjr on 11/14/16.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ModelLoaderRegistry.registerLoader(OBJPlusLoader.INSTANCE);
        OBJPlusLoader.INSTANCE.addDomain(MPSModConstants.MODID.toLowerCase());
        MPSConfig.getInstance().extractModelSpecFiles();

    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        KeybindManager.readInKeybinds();
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        MinecraftForge.EVENT_BUS.register(new KeybindKeyHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerUpdateHandler());
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new SoundDictionary());
        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
        MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.getInstance());


        MinecraftForge.EVENT_BUS.register(new EventRegisterRenderers());
    }

    @Override
    public void registerRenderers() {
        super.registerRenderers();




//        regRenderer(Item.getItemFromBlock(MPSItems.tinkerTable));
//        ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(MPSItems.tinkerTable), 0, TileEntityTinkerTable.class);
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MPSItems.luxCapacitor), 0, ModelLuxCapacitor.modelResourceLocation);

//        RenderingRegistry.registerEntityRenderingHandler(EntitySpinningBlade.class, EntityRendererSpinningBlade::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityPlasmaBolt.class, EntityRendererPlasmaBolt::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityLuxCapacitor.class, EntityRendererLuxCapacitorEntity::new);


        if (ModCompatibility.isRenderPlayerAPILoaded()) {
            ModelPlayerAPI.register(MPSModConstants.MODID, SMovingArmorModel.class);
        }
    }

    @Override
    public void sendModeChange(int dMode, String newMode) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        MusePacket modeChangePacket = new MusePacketModeChangeRequest(player, newMode, player.inventory.currentItem);
        PacketSender.sendToServer(modeChangePacket);
    }


}