package com.gmail.scyntrus.fmob;

import net.minecraft.server.v1_7_R1.Entity;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.gmail.scyntrus.fmob.mobs.Archer;
import com.gmail.scyntrus.fmob.mobs.Mage;
import com.gmail.scyntrus.fmob.mobs.Swordsman;
import com.gmail.scyntrus.fmob.mobs.Titan;
import com.gmail.scyntrus.ifactions.Faction;
import com.gmail.scyntrus.ifactions.Factions;
import com.gmail.scyntrus.ifactions.UPlayer;

public class FmcCommand implements CommandExecutor {

	FactionMobs plugin;
	
	public FmcCommand(FactionMobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (sender instanceof Player) {
			if (!sender.hasPermission("fmob.fmc")) {
				sender.sendMessage("You do not have permission");
				return true;
			}
		}
		
		if (FactionMobs.mobList.size() >= FactionMobs.spawnLimit) {
			sender.sendMessage("There are too many faction mobs");
			return true;
		}
		
		if (split.length < 6) {
			sender.sendMessage("Not enough arguments");
			return false;
		}
		
		org.bukkit.World craftWorld = plugin.getServer().getWorld(split[2]);
		if (craftWorld==null) {
			sender.sendMessage("World not found");
			return false;
		}
		net.minecraft.server.v1_7_R1.World world = ((CraftWorld)craftWorld).getHandle();
		
		Location loc = null;
		try {
			loc = new Location(craftWorld, Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
		} catch (Exception e) {
			sender.sendMessage("Invalid coordinates");
    	    //if (!FactionMobs.silentErrors) e.printStackTrace();
			return false;
		}
		
		String factionName = split[1];
		Faction faction = null;
		if (sender instanceof BlockCommandSender) {
			Location blockLoc = ((BlockCommandSender) sender).getBlock().getLocation();
			if (factionName.equalsIgnoreCase("%land%")) {
				faction = Factions.getFactionAt(blockLoc);
			} else if (factionName.equalsIgnoreCase("%near%")) {
				double minDist = 16;
				Player pNear = null;
				for (Player p : blockLoc.getWorld().getPlayers()) {
					if (p.getLocation().distanceSquared(blockLoc) < minDist) {
						pNear = p;
						minDist = p.getLocation().distanceSquared(blockLoc);
					}
				}
				if (pNear == null) return true;
				faction = UPlayer.getPlayerFaction(pNear);
			} else {
				faction = Factions.getFactionByName(split[2],factionName);
			}
		} else {
			faction = Factions.getFactionByName(split[2],factionName);
		}
		if (faction == null || faction.isNone()) {
			sender.sendMessage("Faction not found");
			return false;
		}
		
		FactionMob newMob = null;
		if (split[0].equalsIgnoreCase("Archer") || split[0].equalsIgnoreCase("Ranger")) {
			newMob = new Archer(loc, faction);
		} else if (split[0].equalsIgnoreCase("Swordsman")) {
			newMob = new Swordsman(loc, faction);
		} else if (split[0].equalsIgnoreCase("Titan") || split[0].equalsIgnoreCase("Golem")) {
			newMob = new Titan(loc, faction);
		} else if (split[0].equalsIgnoreCase("Mage") || split[0].equalsIgnoreCase("Witch")) {
			newMob = new Mage(loc, faction);
		} else {
			sender.sendMessage("Unrecognized mob name");
			return true;
		}
		
		if (!newMob.getEnabled()) {
			sender.sendMessage(String.format("Spawning %s has been disabled", newMob.getTypeName()));
			newMob.forceDie();
			return true;
		}
				
		world.addEntity((Entity) newMob, SpawnReason.CUSTOM);
		newMob.getEntity().dead = false;
		FactionMobs.mobList.add(newMob);
		
		if (split.length > 6) {
			if (split[6].equalsIgnoreCase("moveToPoint") || split[6].equalsIgnoreCase("move") || split[6].equalsIgnoreCase("point")) {
				if (split.length < 10) {
					sender.sendMessage("Not enough arguments for move order");
					return false;
				}
				try {
					newMob.setPoi(Double.parseDouble(split[7]), Double.parseDouble(split[8]), Double.parseDouble(split[9]));
					newMob.setOrder("poi");
				} catch (Exception e) {
					sender.sendMessage("Invalid move coordinates");
		    	    //if (!FactionMobs.silentErrors) e.printStackTrace();
					return false;
				}
				return true;
			} else if (split[6].equalsIgnoreCase("patrolHere") || split[6].equalsIgnoreCase("patrol")) {
				if (split.length < 10) {
					sender.sendMessage("Not enough arguments for patrol order");
					return false;
				}
				try {
					newMob.setPoi(Double.parseDouble(split[7]), Double.parseDouble(split[8]), Double.parseDouble(split[9]));
					newMob.setOrder("ppoi");
				} catch (Exception e) {
					sender.sendMessage("Invalid patrol coordinates");
		    	    //if (!FactionMobs.silentErrors) e.printStackTrace();
					return false;
				}
				return true;
			} else if (split[6].equalsIgnoreCase("path")) {
				if (split.length < 13) {
					sender.sendMessage("Not enough arguments for path order");
					return false;
				}
				try {
					newMob.setOrder("path");
					newMob.setPoi(Double.parseDouble(split[7]), Double.parseDouble(split[8]), Double.parseDouble(split[9]));
					Location spawnLoc = newMob.getSpawn();
					spawnLoc.setX(Double.parseDouble(split[10]));
					spawnLoc.setY(Double.parseDouble(split[11]));
					spawnLoc.setZ(Double.parseDouble(split[12]));
				} catch (Exception e) {
					sender.sendMessage("Invalid path coordinates");
		    	    //if (!FactionMobs.silentErrors) e.printStackTrace();
					return false;
				}
				return true;
			} else if (split[6].equalsIgnoreCase("wander")) {
				newMob.setOrder("wander");
				return true;
			}
		}
		return true;
	}
}
