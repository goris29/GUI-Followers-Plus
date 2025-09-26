package com.natamus.guifollowers.events;

import java.util.ArrayList;
import java.util.List;

import com.natamus.guifollowers.commands.FollowerGlowCommand;
import com.natamus.guifollowers.config.ConfigHandler;
import com.natamus.guifollowers.data.Variables;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FollowerEvent {
	public static void onPlayerTick(Minecraft mc) {
		Player player = mc.player;
		if (player == null) {
			return;
		}
		
		if (player.tickCount % (20* ConfigHandler.timeBetweenChecksInSeconds) != 0) {
			return;
		}
		
		Level world = player.level();
		if (world == null) {
			return;
		}

		int dc = ConfigHandler.distanceToCheckForFollowersAround; // distancecheck
		if (dc <= 0) {
			return;
		}
		
		Vec3 pvec = player.position();
		List<Entity> entitiesaround = world.getEntities(player, new AABB(pvec.x-dc, pvec.y-dc, pvec.z-dc, pvec.x+dc, pvec.y+dc, pvec.z+dc));
		for (Entity ea : entitiesaround) {
			if (!(ea instanceof TamableAnimal)) {
				continue;
			}
			
			TamableAnimal te = (TamableAnimal)ea;
			if (!te.isTame()) {
				continue;
			}
			
			if (!te.isOwnedBy(player)) {
				continue;
			}
			
			// Check if entity is sitting
			if (te.isInSittingPose()) {
				// Add to sitting followers list if not already there
				boolean existsInSitting = false;
				for (Entity entity : Variables.sittingfollowers) {
					if (entity.getUUID().equals(ea.getUUID())) {
						existsInSitting = true;
						break;
					}
				}
				
				if (!existsInSitting) {
					Variables.sittingfollowers.add(ea);
					// Assign ID and update name
					Variables.assignFollowerId(ea);
					// Apply glow if enabled
					FollowerGlowCommand.applyGlowToNewFollower(ea);
				}
				
				// Remove from active followers if it was there
				Variables.activefollowers.removeIf(entity -> entity.getUUID().equals(ea.getUUID()));
				continue;
			}

			// Add to active followers list if not already there
			boolean existsInActive = false;
			for (Entity entity : Variables.activefollowers) {
				if (entity.getUUID().equals(ea.getUUID())) {
					existsInActive = true;
					break;
				}
			}

			if (!existsInActive) {
				Variables.activefollowers.add(ea);
				// Assign ID and update name
				Variables.assignFollowerId(ea);
				// Apply glow if enabled
				FollowerGlowCommand.applyGlowToNewFollower(ea);
			}
			
			// Remove from sitting followers if it was there (pet stood up)
			Variables.sittingfollowers.removeIf(entity -> entity.getUUID().equals(ea.getUUID()));
		}
	}
	
	public static void onPlayerLogout(Level world, Player player) {
		Variables.clearAllFollowerIds();
		Variables.activefollowers = new ArrayList<Entity>();
		Variables.sittingfollowers = new ArrayList<Entity>();
	}
	
	public static void onHotkeyPress() {
		Variables.clearAllFollowerIds();
		Variables.activefollowers = new ArrayList<Entity>();
		Variables.sittingfollowers = new ArrayList<Entity>();
	}
}
