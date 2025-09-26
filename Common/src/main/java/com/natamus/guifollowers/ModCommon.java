package com.natamus.guifollowers;

import com.natamus.collective.globalcallbacks.CollectiveGuiCallback;
import com.natamus.collective.services.Services;
import com.natamus.guifollowers.commands.FollowerCommand;
import com.natamus.guifollowers.config.ConfigHandler;
import com.natamus.guifollowers.events.GUIEvent;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class ModCommon {

	public static void init() {
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		if (Services.MODLOADER.isClientSide()) {
			CollectiveGuiCallback.ON_GUI_RENDER.register(((guiGraphics, deltaTracker) -> {
				GUIEvent.renderOverlay(guiGraphics, deltaTracker);
			}));
		}
	}
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		FollowerCommand.register(dispatcher);
	}
}