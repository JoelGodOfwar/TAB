package me.neznamy.tab.shared;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.shared.config.Configs;

public class PluginHooks {

	public static boolean libsDisguises;
	public static boolean placeholderAPI;
	public static boolean viaversion;
	public static boolean protocolsupport;
	public static Object idisguise;

	public static String setPlaceholders(UUID player, String placeholder) {
		Player p = (player == null ? null : Bukkit.getPlayer(player));
		return setPlaceholders(p, placeholder);
	}
	public static String setPlaceholders(Player player, String placeholder) {
		if (!placeholderAPI) return placeholder;
		try {
			return PlaceholderAPI.setPlaceholders(player, placeholder);
		} catch (Throwable t) {
			String playername = (player == null ? "null" : player.getName());
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.errorManager.printError("PlaceholderAPI v" + papi.getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, Configs.papiErrorFile);
			} else {
				placeholderAPI = false;
			}
			return "ERROR";
		}
	}
	public static String setRelationalPlaceholders(ITabPlayer viewer, ITabPlayer target, String placeholder) {
		if (!placeholderAPI) return placeholder;
		try {
			return PlaceholderAPI.setRelationalPlaceholders(viewer.getBukkitEntity(), target.getBukkitEntity(), placeholder);
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.errorManager.printError("PlaceholderAPI v" + papi.getDescription().getVersion() + " generated an error when setting relational placeholder " + placeholder + " for viewer " + viewer.getName() + " and target " + target.getName(), t, false, Configs.papiErrorFile);
			} else {
				placeholderAPI = false;
			}
		}
		return placeholder;
	}
}