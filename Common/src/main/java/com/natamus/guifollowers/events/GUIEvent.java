package com.natamus.guifollowers.events;

import com.mojang.blaze3d.platform.Window;
import org.joml.Matrix3x2fStack;
import com.natamus.collective.functions.GUIFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.guifollowers.config.ConfigHandler;
import com.natamus.guifollowers.data.Variables;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUIEvent {
	private static final Minecraft mc = Minecraft.getInstance();

	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (GUIFunctions.shouldHideGUI()) {
			return;
		}

		Font font = mc.font;
		Window scaled = mc.getWindow();
		Matrix3x2fStack matrixStack = guiGraphics.pose();
		matrixStack.pushMatrix();
		
		LocalPlayer player = mc.player;
		if (player == null) {
			matrixStack.popMatrix();
			return;
		}
		
		String playerdimension = WorldFunctions.getWorldDimensionName(player.level());
		
		// Check if we have any followers to display
		if (Variables.activefollowers.size() > 0 || Variables.sittingfollowers.size() > 0) {
			int width = scaled.getGuiScaledWidth();
			Color colour = new Color(ConfigHandler.RGB_R, ConfigHandler.RGB_G, ConfigHandler.RGB_B, 255);
			
			int xoffset = 5;
			int heightoffset = ConfigHandler.followerListHeightOffset;
			
			// Render Active Followers section
			if (Variables.activefollowers.size() > 0) {
				heightoffset = renderFollowerSection(guiGraphics, font, width, colour, xoffset, heightoffset, 
					Variables.activefollowers, "Active Followers:", playerdimension, player, false);
			}
			
			// Add spacing between sections
			if (Variables.activefollowers.size() > 0 && Variables.sittingfollowers.size() > 0 && ConfigHandler.showSittingFollowers) {
				heightoffset += 10;
			}
			
			// Render Sitting Followers section
			if (Variables.sittingfollowers.size() > 0 && ConfigHandler.showSittingFollowers) {
				heightoffset = renderFollowerSection(guiGraphics, font, width, colour, xoffset, heightoffset, 
					Variables.sittingfollowers, "Sitting Followers:", playerdimension, player, true);
			}
		}
		
		matrixStack.popMatrix();
	}
	
	private static int renderFollowerSection(GuiGraphics guiGraphics, Font font, int width, Color colour, 
											int xoffset, int startHeight, List<Entity> followerList, 
											String sectionTitle, String playerdimension, LocalPlayer player, boolean isSittingSection) {
		
		int heightoffset = startHeight;
		
		// Calculate position for section header
		int headerWidth = font.width(sectionTitle);
		int xcoord = calculateXCoordinate(width, headerWidth, 0);
		
		// Draw section header
		drawText(font, guiGraphics, sectionTitle, xcoord, heightoffset, colour.getRGB(), ConfigHandler.drawTextShadow);
		heightoffset += 10;
		
		// Track entities to remove
		List<Entity> toremove = new ArrayList<Entity>();
		
		// Render each follower in this section
		for (Entity follower : new ArrayList<Entity>(followerList)) {
			String followerdimension = WorldFunctions.getWorldDimensionName(follower.level());
			if (!playerdimension.equals(followerdimension)) {
				toremove.add(follower);
				continue;
			}

			if (!follower.isAlive() || !(follower instanceof TamableAnimal)) {
				toremove.add(follower);
				continue;
			}

			TamableAnimal te = (TamableAnimal) follower;
			
			// For sitting section, remove if no longer sitting
			// For active section, remove if now sitting
			if (isSittingSection && !te.isInSittingPose()) {
				toremove.add(follower);
				continue;
			} else if (!isSittingSection && te.isInSittingPose()) {
				toremove.add(follower);
				continue;
			}

			String follower_string = follower.getName().getString();
			
			// Add health information
			if (ConfigHandler.showFollowerHealth) {
				LivingEntity le = (LivingEntity) follower;
				float currenthealth = le.getHealth();
				float maxhealth = le.getMaxHealth();

				int percenthealth = (int) ((100 / maxhealth) * currenthealth);
				if (percenthealth <= 0) {
					toremove.add(follower);
					continue;
				}

				String healthformat = ConfigHandler.followerHealthFormat;
				follower_string = follower_string + healthformat.replaceAll("<health>", percenthealth + "");
			}

			// Add distance information
			if (ConfigHandler.showFollowerDistance) {
				Vec3 pvec = player.position();
				Vec3 fvec = follower.position();

				double distance = pvec.distanceTo(fvec);
				String distanceformat = ConfigHandler.followerDistanceFormat;
				follower_string = follower_string + distanceformat.replaceAll("<distance>", String.format("%.2f", distance));
			}

			// Calculate position for follower entry
			int follower_stringWidth = font.width(follower_string);
			int followerXCoord = calculateXCoordinate(width, follower_stringWidth, xoffset);

			// Draw follower entry
			drawText(font, guiGraphics, follower_string, followerXCoord, heightoffset, colour.getRGB(), ConfigHandler.drawTextShadow);
			heightoffset += 10;
		}
		
		// Remove dead/invalid entities
		if (toremove.size() > 0) {
			for (Entity etr : toremove) {
				followerList.remove(etr);
				// Remove ID when entity is removed from lists
				Variables.removeFollowerId(etr);
			}
		}
		
		return heightoffset;
	}
	
	private static int calculateXCoordinate(int width, int textWidth, int offset) {
		if (ConfigHandler.followerListPositionIsLeft) {
			return 5 + offset;
		} else if (ConfigHandler.followerListPositionIsCenter) {
			return (width / 2) - (textWidth / 2);
		} else {
			return width - textWidth - 5 - offset;
		}
	}

	private static void drawText(Font font, GuiGraphics guiGraphics, String content, int x, int y, int rgb, boolean drawShadow) {
		guiGraphics.drawString(font, Component.literal(content), x, y, rgb, drawShadow);
	}
}