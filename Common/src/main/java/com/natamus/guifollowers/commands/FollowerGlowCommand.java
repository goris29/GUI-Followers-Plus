package com.natamus.guifollowers.commands;

import com.natamus.guifollowers.data.Variables;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.ChatFormatting;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

public class FollowerGlowCommand {
    private static final String TEAM_NAME = "followers_glow";
    private static boolean glowEnabled = false;
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("follower")
            .then(Commands.literal("glow")
                .then(Commands.literal("toggle")
                    .requires(source -> source.hasPermission(2))
                    .executes(FollowerGlowCommand::toggleGlow))));
    }
    
    private static int toggleGlow(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (source.getLevel() == null) {
            source.sendFailure(Component.literal("Command must be executed in a world!"));
            return 0;
        }
        
        ServerLevel level = source.getLevel();
        Scoreboard scoreboard = level.getScoreboard();
        
        // Toggle the glow state
        glowEnabled = !glowEnabled;
        
        if (glowEnabled) {
            // Enable glow
            enableFollowerGlow(level, scoreboard);
            source.sendSuccess(() -> Component.literal("Follower glow enabled!").withStyle(ChatFormatting.GREEN), true);
        } else {
            // Disable glow
            disableFollowerGlow(level, scoreboard);
            source.sendSuccess(() -> Component.literal("Follower glow disabled!").withStyle(ChatFormatting.RED), true);
        }
        
        return 1;
    }
    
    private static void enableFollowerGlow(ServerLevel level, Scoreboard scoreboard) {
        // Create or get the team
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.addPlayerTeam(TEAM_NAME);
            team.setColor(ChatFormatting.AQUA);
            team.setDisplayName(Component.literal("Glowing Followers"));
        }
        
        // Apply glow to all followers
        applyGlowToFollowers(level, scoreboard, team, true);
    }
    
    private static void disableFollowerGlow(ServerLevel level, Scoreboard scoreboard) {
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        
        // Remove glow from all followers
        applyGlowToFollowers(level, scoreboard, team, false);
        
        // Optionally remove the team (commented out to preserve team for future use)
        // if (team != null) {
        //     scoreboard.removePlayerTeam(team);
        // }
    }
    
    private static void applyGlowToFollowers(ServerLevel level, Scoreboard scoreboard, PlayerTeam team, boolean enable) {
        // Apply to active followers
        for (Entity follower : Variables.activefollowers) {
            if (follower instanceof LivingEntity livingEntity && follower.level() == level) {
                applyGlowToEntity(livingEntity, scoreboard, team, enable);
            }
        }
        
        // Apply to sitting followers
        for (Entity follower : Variables.sittingfollowers) {
            if (follower instanceof LivingEntity livingEntity && follower.level() == level) {
                applyGlowToEntity(livingEntity, scoreboard, team, enable);
            }
        }
    }
    
    private static void applyGlowToEntity(LivingEntity entity, Scoreboard scoreboard, PlayerTeam team, boolean enable) {
        if (enable) {
            // Add to team if it exists
            if (team != null) {
                String entityName = entity.getScoreboardName();
                scoreboard.addPlayerToTeam(entityName, team);
            }
            
            // Apply glowing effect (10 minutes duration)
            MobEffectInstance glowEffect = new MobEffectInstance(MobEffects.GLOWING, 12000, 0, false, false, true);
            entity.addEffect(glowEffect);
        } else {
            // Remove from team
            if (team != null) {
                String entityName = entity.getScoreboardName();
                scoreboard.removePlayerFromTeam(entityName, team);
            }
            
            // Remove glowing effect
            entity.removeEffect(MobEffects.GLOWING);
        }
    }
    
    public static boolean isGlowEnabled() {
        return glowEnabled;
    }
    
    public static void setGlowEnabled(boolean enabled) {
        glowEnabled = enabled;
    }
    
    /**
     * Applies glow to a single follower if glow is currently enabled
     */
    public static void applyGlowToNewFollower(Entity follower) {
        if (!glowEnabled || !(follower instanceof LivingEntity livingEntity)) {
            return;
        }
        
        if (follower.level() instanceof ServerLevel serverLevel) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
            
            if (team == null) {
                team = scoreboard.addPlayerTeam(TEAM_NAME);
                team.setColor(ChatFormatting.AQUA);
                team.setDisplayName(Component.literal("Glowing Followers"));
            }
            
            applyGlowToEntity(livingEntity, scoreboard, team, true);
        }
    }
}
