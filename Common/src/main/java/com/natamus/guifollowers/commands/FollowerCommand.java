package com.natamus.guifollowers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.guifollowers.data.Variables;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class FollowerCommand {
	private static final String GLOW_TEAM_NAME = "followers_glow";
	private static boolean glowEnabled = false;
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("follower")
			.then(Commands.literal("glow")
				.then(Commands.literal("toggle")
					.requires(source -> source.hasPermission(2))  // Operator level permission
					.executes(FollowerCommand::executeGlowToggle)
				)
			)
		);
	}
	
	private static int executeGlowToggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		
		glowEnabled = !glowEnabled;
		
		if (glowEnabled) {
			enableFollowerGlow(level);
			source.sendSuccess(() -> Component.literal("Follower glow enabled.")
				.withStyle(ChatFormatting.GREEN), true);
		} else {
			disableFollowerGlow(level);
			source.sendSuccess(() -> Component.literal("Follower glow disabled.")
				.withStyle(ChatFormatting.RED), true);
		}
		
		return 1;
	}
	
	private static void enableFollowerGlow(ServerLevel level) {
		Scoreboard scoreboard = level.getScoreboard();
		
		// Create or get the glow team
		PlayerTeam glowTeam = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
		if (glowTeam == null) {
			glowTeam = scoreboard.addPlayerTeam(GLOW_TEAM_NAME);
			glowTeam.setColor(ChatFormatting.AQUA);
		}
		
		// Apply glow to all active followers
		for (Entity follower : Variables.activefollowers) {
			if (follower instanceof LivingEntity) {
				LivingEntity livingFollower = (LivingEntity) follower;
				
				// Add to scoreboard team
				String entityName = follower.getStringUUID();
				scoreboard.addPlayerToTeam(entityName, glowTeam);
				
				// Apply glowing effect (duration: 20 minutes = 24000 ticks)
				MobEffectInstance glowEffect = new MobEffectInstance(MobEffects.GLOWING, 24000, 0, false, false);
				livingFollower.addEffect(glowEffect);
			}
		}
		
		// Apply glow to all sitting followers
		for (Entity follower : Variables.sittingfollowers) {
			if (follower instanceof LivingEntity) {
				LivingEntity livingFollower = (LivingEntity) follower;
				
				// Add to scoreboard team
				String entityName = follower.getStringUUID();
				scoreboard.addPlayerToTeam(entityName, glowTeam);
				
				// Apply glowing effect (duration: 20 minutes = 24000 ticks)
				MobEffectInstance glowEffect = new MobEffectInstance(MobEffects.GLOWING, 24000, 0, false, false);
				livingFollower.addEffect(glowEffect);
			}
		}
	}
	
	private static void disableFollowerGlow(ServerLevel level) {
		Scoreboard scoreboard = level.getScoreboard();
		PlayerTeam glowTeam = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
		
		// Remove glow from all active followers
		for (Entity follower : Variables.activefollowers) {
			if (follower instanceof LivingEntity) {
				LivingEntity livingFollower = (LivingEntity) follower;
				
				// Remove from scoreboard team
				String entityName = follower.getStringUUID();
				if (glowTeam != null) {
					scoreboard.removePlayerFromTeam(entityName, glowTeam);
				}
				
				// Remove glowing effect
				livingFollower.removeEffect(MobEffects.GLOWING);
			}
		}
		
		// Remove glow from all sitting followers
		for (Entity follower : Variables.sittingfollowers) {
			if (follower instanceof LivingEntity) {
				LivingEntity livingFollower = (LivingEntity) follower;
				
				// Remove from scoreboard team
				String entityName = follower.getStringUUID();
				if (glowTeam != null) {
					scoreboard.removePlayerFromTeam(entityName, glowTeam);
				}
				
				// Remove glowing effect
				livingFollower.removeEffect(MobEffects.GLOWING);
			}
		}
	}
	
	/**
	 * Called when a new follower is added and glow is enabled
	 */
	public static void applyGlowToNewFollower(Entity follower, ServerLevel level) {
		if (!glowEnabled || !(follower instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity livingFollower = (LivingEntity) follower;
		Scoreboard scoreboard = level.getScoreboard();
		
		// Get or create the glow team
		PlayerTeam glowTeam = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
		if (glowTeam == null) {
			glowTeam = scoreboard.addPlayerTeam(GLOW_TEAM_NAME);
			glowTeam.setColor(ChatFormatting.AQUA);
		}
		
		// Add to scoreboard team
		String entityName = follower.getStringUUID();
		scoreboard.addPlayerToTeam(entityName, glowTeam);
		
		// Apply glowing effect
		MobEffectInstance glowEffect = new MobEffectInstance(MobEffects.GLOWING, 24000, 0, false, false);
		livingFollower.addEffect(glowEffect);
	}
	
	/**
	 * Called when a follower is removed and glow is enabled
	 */
	public static void removeGlowFromFollower(Entity follower, ServerLevel level) {
		if (!glowEnabled || !(follower instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity livingFollower = (LivingEntity) follower;
		Scoreboard scoreboard = level.getScoreboard();
		PlayerTeam glowTeam = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
		
		// Remove from scoreboard team
		String entityName = follower.getStringUUID();
		if (glowTeam != null) {
			scoreboard.removePlayerFromTeam(entityName, glowTeam);
		}
		
		// Remove glowing effect
		livingFollower.removeEffect(MobEffects.GLOWING);
	}
	
	/**
	 * Returns whether glow is currently enabled
	 */
	public static boolean isGlowEnabled() {
		return glowEnabled;
	}
}
