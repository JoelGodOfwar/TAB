package me.neznamy.tab.platforms.bukkit.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

@SuppressWarnings({"deprecation", "unchecked"})
public class PerWorldPlayerlist implements Loadable, JoinEventListener, WorldChangeListener, PlayerInfoPacketListener{

	private boolean allowBypass;
	private List<String> ignoredWorlds;
	private Map<String, List<String>> sharedWorlds;

	@Override
	public void load(){
		allowBypass = Configs.advancedconfig.getBoolean("per-world-playerlist.allow-bypass-permission", false);
		ignoredWorlds = Configs.advancedconfig.getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
		sharedWorlds = Configs.advancedconfig.getConfigurationSection("per-world-playerlist.shared-playerlist-world-groups");
		for (Player p : Main.instance.getOnlinePlayers()){
			hidePlayer(p);
			showInSameWorldGroup(p);
		}
	}
	@Override
	public void unload(){
		for (Player p : Main.instance.getOnlinePlayers()) for (Player pl : Main.instance.getOnlinePlayers()) p.showPlayer(pl);
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		hidePlayer(connectedPlayer.getBukkitEntity());
		showInSameWorldGroup(connectedPlayer.getBukkitEntity());
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		hidePlayer(p.getBukkitEntity());
		showInSameWorldGroup(p.getBukkitEntity());
	}
	private void showInSameWorldGroup(Player shown){
		Bukkit.getScheduler().runTask(Main.instance, new Runnable() {

			@Override
			public void run() {
				for (Player everyone : Main.instance.getOnlinePlayers()){
					if (everyone == shown) continue;
					if (shouldSee(shown, everyone)) shown.showPlayer(everyone);
					if (shouldSee(everyone, shown)) everyone.showPlayer(shown);
				}
			}
		});
	}
	private boolean shouldSee(Player viewer, Player displayed) {
		if (displayed == viewer) return true;
		String player1WorldGroup = null;
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group).contains(viewer.getWorld().getName())) player1WorldGroup = group;
		}
		String player2WorldGroup = null;
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group).contains(displayed.getWorld().getName())) player2WorldGroup = group;
		}
		if (viewer.getWorld() == displayed.getWorld() || (player1WorldGroup != null && player2WorldGroup != null && player1WorldGroup.equals(player2WorldGroup))) {
			return true;
		}
		if ((allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(viewer.getWorld().getName())) {
			return true;
		}
		return false;
	}
	public void hidePlayer(Player hidden){
		Bukkit.getScheduler().runTask(Main.instance, new Runnable() {

			@Override
			public void run() {
				for (Player everyone : Main.instance.getOnlinePlayers()){
					if (everyone == hidden) continue;
					hidden.hidePlayer(everyone);
					everyone.hidePlayer(hidden);
				}
			}
		});
	}
	//fixing bukkit api bug making players not hide when hidePlayer is called too early
	@Override
	public PacketPlayOutPlayerInfo onPacketSend(ITabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.action != EnumPlayerInfoAction.ADD_PLAYER) return info;
		List<PlayerInfoData> toRemove = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData data : info.entries) {
			ITabPlayer added = Shared.getPlayerByTablistUUID(data.uniqueId);
			if (added != null && !shouldSee(receiver.getBukkitEntity(), added.getBukkitEntity())) {
				toRemove.add(data);
			}
		}
		List<PlayerInfoData> newList = new ArrayList<PlayerInfoData>();
		Arrays.asList(info.entries).forEach(d -> newList.add(d));
		newList.removeAll(toRemove);
		info.entries = newList.toArray(new PlayerInfoData[0]);
		if (info.entries.length == 0) return null;
		return info;
	}
	@Override
	public CPUFeature getCPUName() {
		return CPUFeature.PER_WORLD_PLAYERLIST;
	}
}