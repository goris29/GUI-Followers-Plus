package com.natamus.guifollowers.data;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Variables {
	public static final Minecraft mc = Minecraft.getInstance();
	public static KeyMapping clearlist_hotkey;

	public static List<Entity> activefollowers = new ArrayList<Entity>();
	public static List<Entity> sittingfollowers = new ArrayList<Entity>();
	
	// Follower ID system
	public static Map<UUID, Integer> followerIds = new HashMap<>();
	public static int nextAvailableId = 1;
	
	/**
	 * Assigns a unique ID to a follower and sets their custom name
	 */
	public static int assignFollowerId(Entity follower) {
		// Check if ID system is enabled
		if (!com.natamus.guifollowers.config.ConfigHandler.enableFollowerIDs) {
			return -1;
		}
		
		UUID entityUUID = follower.getUUID();
		
		// If already has an ID, return it
		if (followerIds.containsKey(entityUUID)) {
			return followerIds.get(entityUUID);
		}
		
		// Assign new ID
		int assignedId = nextAvailableId++;
		followerIds.put(entityUUID, assignedId);
		
		// Set custom name
		updateFollowerName(follower, assignedId);
		
		return assignedId;
	}
	
	/**
	 * Updates the custom name of a follower with their ID
	 */
	public static void updateFollowerName(Entity follower, int id) {
		if (!com.natamus.guifollowers.config.ConfigHandler.enableFollowerIDs) {
			return;
		}
		
		String entityType = follower.getType().getDescription().getString();
		String customName = entityType + " [" + id + "]";
		follower.setCustomName(net.minecraft.network.chat.Component.literal(customName));
		follower.setCustomNameVisible(true);
	}
	
	/**
	 * Removes a follower's ID and clears their custom name
	 */
	public static void removeFollowerId(Entity follower) {
		UUID entityUUID = follower.getUUID();
		if (followerIds.containsKey(entityUUID)) {
			followerIds.remove(entityUUID);
			// Clear custom name
			follower.setCustomName(null);
			follower.setCustomNameVisible(false);
		}
	}
	
	/**
	 * Gets the ID of a follower, or -1 if not assigned
	 */
	public static int getFollowerId(Entity follower) {
		return followerIds.getOrDefault(follower.getUUID(), -1);
	}
	
	/**
	 * Clears all follower IDs and resets the counter
	 */
	public static void clearAllFollowerIds() {
		// Clear custom names for all tracked followers
		for (Entity follower : activefollowers) {
			follower.setCustomName(null);
			follower.setCustomNameVisible(false);
		}
		for (Entity follower : sittingfollowers) {
			follower.setCustomName(null);
			follower.setCustomNameVisible(false);
		}
		
		followerIds.clear();
		nextAvailableId = 1;
	}
}
