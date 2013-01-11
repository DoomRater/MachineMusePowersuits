/**
 * 
 */
package net.machinemuse.powersuits.tick;

import java.util.EnumSet;
import java.util.List;

import net.machinemuse.powersuits.common.MuseLogger;
import net.machinemuse.powersuits.event.MovementManager;
import net.machinemuse.powersuits.item.IModularItem;
import net.machinemuse.powersuits.item.ItemUtils;
import net.machinemuse.powersuits.item.ModularCommon;
import net.machinemuse.powersuits.network.MusePacket;
import net.machinemuse.powersuits.network.MusePacketEnergyAdjustment;
import net.machinemuse.powersuits.network.MusePacketFallDistance;
import net.machinemuse.powersuits.powermodule.ModuleManager;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;

/**
 * Tick handler for Player update step. tickStart() is queued before the entity
 * is updated, and tickEnd() is queued afterwards.
 * 
 * Player update step: "Called to update the entity's position/logic."
 * 
 * tickData: EntityPlayer of the entity being updated.
 * 
 * @author MachineMuse
 */
public class PlayerTickHandlerClient implements ITickHandler {
	public int playerJumpTicks = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		EntityPlayer rawPlayer = toPlayer(tickData[0]);
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT && rawPlayer instanceof EntityClientPlayerMP) {
			EntityClientPlayerMP player = (EntityClientPlayerMP) rawPlayer;

			ItemStack helmet = player.getCurrentArmor(3);
			ItemStack torso = player.getCurrentArmor(2);
			ItemStack pants = player.getCurrentArmor(1);
			ItemStack boots = player.getCurrentArmor(0);
			ItemStack tool = player.getCurrentEquippedItem();

			double totalEnergy = ItemUtils.getPlayerEnergy(player);
			double totalEnergyDrain = 0;
			double totalWeight = ItemUtils.getPlayerWeight(player);
			double weightCapacity = 25000;

			double landMovementFactor = 0.1;
			double jumpMovementFactor = 0.02;
			double horzMovement = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
			double exhaustion = Math.round(horzMovement * 100.0F) * 0.01;

			Vec3 playerHorzFacing = player.getLookVec();
			playerHorzFacing.yCoord = 0;
			playerHorzFacing.normalize();

			boolean jumpkey = player.movementInput.jump;
			float forwardkey = player.movementInput.moveForward;

			boolean hasSprintAssist = false;
			boolean hasGlider = false;
			boolean hasParachute = false;
			boolean hasJetpack = false;
			boolean hasJetboots = false;

			if (pants != null && pants.getItem() instanceof IModularItem) {
				hasSprintAssist = ItemUtils.itemHasModule(pants, ModularCommon.MODULE_SPRINT_ASSIST);
			}
			if (boots != null && boots.getItem() instanceof IModularItem) {
				hasJetboots = ItemUtils.itemHasModule(boots, ModularCommon.MODULE_JETBOOTS);
			}
			if (torso != null && torso.getItem() instanceof IModularItem) {
				hasJetpack = ItemUtils.itemHasModule(torso, ModularCommon.MODULE_JETPACK);
				hasGlider = ItemUtils.itemHasModule(torso, ModularCommon.MODULE_GLIDER);
				hasParachute = ItemUtils.itemHasModule(torso, ModularCommon.MODULE_PARACHUTE);
			}

			// Jetpack & jetboots
			if ((hasJetpack || hasJetboots) && jumpkey) {
				double jetEnergy = ModuleManager.computeModularProperty(boots, ModularCommon.JET_ENERGY_CONSUMPTION)
						+ ModuleManager.computeModularProperty(torso, ModularCommon.JET_ENERGY_CONSUMPTION);
				double thrust = ModuleManager.computeModularProperty(boots, ModularCommon.JET_THRUST)
						+ ModuleManager.computeModularProperty(torso, ModularCommon.JET_THRUST);
				if (jetEnergy + totalEnergyDrain < totalEnergy) {
					totalEnergyDrain += jetEnergy;
					if (forwardkey == 0) {
						player.motionY += thrust;
					} else {
						player.motionY += thrust / 2;
						player.motionX += playerHorzFacing.xCoord * thrust / 2 * Math.signum(forwardkey);
						player.motionZ += playerHorzFacing.zCoord * thrust / 2 * Math.signum(forwardkey);
					}
				}

			}

			// Glider
			if (hasGlider && jumpkey && player.motionY < -0.1 && (!hasParachute || forwardkey > 0)) {
				if (player.motionY < -0.1) {
					double motionYchange = Math.min(0.08, -0.1 - player.motionY);
					player.motionY += motionYchange;
					player.motionX += playerHorzFacing.xCoord * motionYchange;
					player.motionZ += playerHorzFacing.zCoord * motionYchange;

					// sprinting speed
					player.jumpMovementFactor += 0.03f;
				}
			}

			// Parachute
			if (hasParachute && jumpkey && player.motionY < -0.1 && (!hasGlider || forwardkey <= 0)) {
				double totalVelocity = Math.sqrt(horzMovement * horzMovement + player.motionY * player.motionY);
				player.motionX = player.motionX * 0.1 / totalVelocity;
				player.motionY = player.motionY * 0.1 / totalVelocity;
				player.motionZ = player.motionZ * 0.1 / totalVelocity;
			}

			// Sprint assist
			if (hasSprintAssist && player.isSprinting()) {
				double sprintCost = ModuleManager.computeModularProperty(pants, ModularCommon.SPRINT_ENERGY_CONSUMPTION);
				if (sprintCost + totalEnergyDrain < totalEnergy) {
					double sprintMultiplier = ModuleManager.computeModularProperty(pants, ModularCommon.SPRINT_SPEED_MULTIPLIER);
					double exhaustionComp = ModuleManager.computeModularProperty(pants, ModularCommon.SPRINT_FOOD_COMPENSATION);
					totalEnergyDrain += sprintCost;
					player.landMovementFactor *= sprintMultiplier;
					player.addExhaustion((float) (-0.01 * exhaustion * exhaustionComp));
				}
			}

			// Update fall distance for damage
			double fallDistance = MovementManager.computeFallHeightFromVelocity(player.motionY);
			MusePacket packet = new MusePacketFallDistance(player, fallDistance);
			player.sendQueue.addToSendQueue(packet.getPacket250());

			// Update energy drain
			if (totalEnergyDrain > 0) {
				ItemUtils.drainPlayerEnergy(player, totalEnergyDrain);
				packet = new MusePacketEnergyAdjustment(player, -totalEnergyDrain);
				player.sendQueue.addToSendQueue(packet.getPacket250());
			}

			// Weight movement penalty
			if (totalWeight > weightCapacity) {
				player.motionX *= weightCapacity / totalWeight;
				player.motionZ *= weightCapacity / totalWeight;
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		EntityPlayer player = toPlayer(tickData[0]);
		List<ItemStack> stacks = ItemUtils
				.getModularItemsInInventory(player.inventory);

	}

	public static World toWorld(Object data) {
		World world = null;
		try {
			world = (World) data;
		} catch (ClassCastException e) {
			MuseLogger.logDebug(
					"MMMPS: Player tick handler received invalid World object");
			e.printStackTrace();
		}
		return world;
	}

	public static EntityPlayer toPlayer(Object data) {
		EntityPlayer player = null;
		try {
			player = (EntityPlayer) data;
		} catch (ClassCastException e) {
			MuseLogger
					.logDebug(
					"MMMPS: Player tick handler received invalid Player object");
			e.printStackTrace();
		}
		return player;
	}

	/**
	 * Type of tick handled by this handler
	 */
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.PLAYER);
	}

	/**
	 * Profiling label for this handler
	 */
	@Override
	public String getLabel() {
		return "MMMPS: Player Tick";
	}

}
