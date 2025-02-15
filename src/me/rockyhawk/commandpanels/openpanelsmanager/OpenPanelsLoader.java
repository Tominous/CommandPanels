package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.ioclasses.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OpenPanelsLoader {
    CommandPanels plugin;
    public OpenPanelsLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    /*
    This is used as a less laggy and non title reliant way to determine which panels are open for specific players
    The configuration section is opened directly
    into the correct panel, so there is no need for the panel name
    */
    public HashMap<String, Panel> openPanels = new HashMap<>(); //player name and panel

    //this will return the panel CF based on the player, if it isn't there it returns null
    public Panel getOpenPanel(String playerName){
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue();
            }
        }
        return null;
    }

    //this will return the panel CF based on the player, if it isn't there it returns null
    public String getOpenPanelName(String playerName){
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue().getName();
            }
        }
        return null;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName, String panelName){
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName) && entry.getValue().getName().equals(panelName)){
                return true;
            }
        }
        return false;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName) {
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return true;
            }
        }
        return false;
    }

    //tell loader that a panel has been opened
    public void openPanelForLoader(String playerName, Panel panel){
        openPanels.put(playerName, panel);
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).trim().equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Opened " + panel.getName());
            }
        }
    }

    //close all of the panels for a player currently open
    public void closePanelForLoader(String playerName){
        if(!openPanels.containsKey(playerName)){
            return;
        }
        panelCloseCommands(playerName,openPanels.get(playerName));
        checkNBTItems(Bukkit.getPlayer(playerName));
        plugin.customCommand.removeCCP(openPanels.get(playerName).getName(), playerName);
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Closed " + openPanels.get(playerName).getName());
            }
        }
        openPanels.remove(playerName);
    }

    public void panelCloseCommands(String playerName, Panel panel){
        if (panel.getConfig().contains("commands-on-close")) {
            //execute commands on panel close
            try {
                List<String> commands = panel.getConfig().getStringList("commands-on-close");
                for (String command : commands) {
                    int val = plugin.commandTags.commandPayWall(Bukkit.getPlayer(playerName),command);
                    if(val == 0){
                        break;
                    }
                    if(val == 2){
                        plugin.commandTags.commandTags(Bukkit.getPlayer(playerName), plugin.papi(Bukkit.getPlayer(playerName),command), command);
                    }
                }
            }catch(Exception s){
                plugin.debug(s);
            }
        }
    }

    //ensure the player has not duplicated items
    public void checkNBTItems(Player p){
        try {
            for(ItemStack playerItem : p.getInventory().getContents()){
                //ensure the item is not a panel item
                try {
                    if (NBTEditor.getString(playerItem, "plugin").equalsIgnoreCase("CommandPanels")) {
                        p.getInventory().removeItem(playerItem);
                    }
                }catch(Exception ignore){}
            }
        }catch(Exception e){
            //oof
        }
    }
}
