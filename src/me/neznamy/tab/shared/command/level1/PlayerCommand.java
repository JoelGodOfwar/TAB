package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class PlayerCommand extends SubCommand {
	
	public PlayerCommand() {
		super("player", null);
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		//<name> <property> [value...]
		if (args.length > 1) {
			String player = args[0];
			String type = args[1].toLowerCase();
			String value = "";
			for (int i=2; i<args.length; i++){
				if (i>2) value += " ";
				value += args[i];
			}
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					Configs.config.set("Users." + player, null);
					Configs.config.save();
					ITabPlayer pl = Shared.getPlayer(player);
					if (pl != null) {
						pl.updateAll();
						pl.forceRefresh();
					}
					sendMessage(sender, Configs.data_removed.replace("%category%", "player").replace("%value%", player));
				}
				return;
			}
			for (String property : usualProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, player, type, value);
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
			for (String property : extraProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, player, type, value);
						if (!Shared.features.containsKey("nametagx")) {
							sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
						}
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
		}
		sendMessage(sender, "&cSyntax&8: &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
		sendMessage(sender, "&7Valid Properties are:");
		sendMessage(sender, " - &9tabprefix&3/&9tabsuffix&3/&9customtabname");
		sendMessage(sender, " - &9tagprefix&3/&9tagsuffix&3/&9customtagname");
		sendMessage(sender, " - &9belowname&3/&9abovename");
	}
	public void savePlayer(ITabPlayer sender, String player, String type, String value){
		ITabPlayer pl = Shared.getPlayer(player);
		if (value.length() == 0) value = null;
		Configs.config.set("Users." + player + "." + type, value);
		Configs.config.save();
		Placeholders.checkForRegistration(value);
		if (pl != null) {
			pl.updateAll();
			pl.forceRefresh();
		}
		if (value != null){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", player).replace("%category%", "player"));
		}
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getPlayers(arguments[0]);
		List<String> suggestions = new ArrayList<String>();
		if (arguments.length == 2) {
			for (String property : usualProperties) {
				if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
			}
			for (String property : extraProperties) {
				if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
			}
		}
		return suggestions;
	}
}