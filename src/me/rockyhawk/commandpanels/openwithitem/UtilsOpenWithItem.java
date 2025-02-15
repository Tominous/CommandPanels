package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.ioclasses.GetItemInHand;
import me.rockyhawk.commandpanels.ioclasses.GetItemInHand_Legacy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class UtilsOpenWithItem implements Listener {
    CommandPanels plugin;
    public UtilsOpenWithItem(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {
        //on a click when in any inventory
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = (Player)e.getWhoClicked();
        //get the item clicked, then loop through panel names after action isn't nothing
        if(e.getAction() == InventoryAction.NOTHING){return;}
        if(e.getSlot() == -999){return;}
        if(e.getClickedInventory().getType() != InventoryType.PLAYER){return;}
        if(plugin.hotbar.stationaryExecute(e.getSlot(),p,true)){
            e.setCancelled(true);
            p.updateInventory();
            return;
        }
        if (plugin.hotbar.itemCheckExecute(e.getCurrentItem(), p, false,true) || plugin.hotbar.itemCheckExecute(e.getCursor(), p, false,true) || plugin.hotbar.stationaryExecute(e.getHotbarButton(), p, false)) {
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e){
        //item right clicked only (not left because that causes issues when things are interacted with)
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        try {
            if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK || Objects.requireNonNull(e.getItem()).getType() == Material.AIR){
                return;
            }
        }catch(Exception b){
            return;
        }
        Player p = e.getPlayer();
        if(plugin.hotbar.itemCheckExecute(e.getItem(),p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        /*
        This world change event is added so if the player is using disabled-worlds
        and they change worlds, it will check if the player can have the item
        and if they can, it gives the item. This is because onRespawn doesn't
        give the item to the player in all the worlds that it could automatically.

        The player will of course need a plugin to split inventories between worlds
        for this to take effect. I don't want to delete the item on the wrong world
        because then it might overwrite one of their actual slots upon rejoining the enabled world.
         */
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();

        for(Panel panel : plugin.panelList) { //will loop through all the files in folder
            if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                continue;
            }
            if (p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) && panel.hasHotbarItem()) {
                ItemStack s = panel.getHotbarItem(p);
                if(panel.getConfig().contains("open-with-item.stationary")) {
                    p.getInventory().setItem(panel.getConfig().getInt("open-with-item.stationary"),s);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        for(Panel panel : plugin.panelList) { //will loop through all the files in folder
            if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                continue;
            }
            if (p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) && panel.hasHotbarItem()) {
                ItemStack s = panel.getHotbarItem(p);
                if(panel.getConfig().contains("open-with-item.stationary")){
                    p.getInventory().setItem(panel.getConfig().getInt("open-with-item.stationary"), s);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getEntity();
        for(Panel panel : plugin.panelList) { //will loop through all the files in folder
            if (p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) && panel.hasHotbarItem()) {
                if(panel.getConfig().contains("open-with-item.stationary")){
                    ItemStack s = panel.getHotbarItem(p);
                    e.getDrops().remove(s);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        for(Panel panel : plugin.panelList) { //will loop through all the files in folder
            if(!panel.getConfig().contains("open-with-item.stationary")){
                continue;
            }
            if (p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))){
                if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                    continue;
                }
                ItemStack s = panel.getHotbarItem(p);
                p.getInventory().setItem(panel.getConfig().getInt("open-with-item.stationary"), s);
            }else{
                //if the player has an item that they have no permission for, remove it
                ItemStack s;
                s = panel.getHotbarItem(p);
                if (p.getInventory().getItem(panel.getConfig().getInt("open-with-item.stationary")).isSimilar(s)) {
                    p.getInventory().setItem(panel.getConfig().getInt("open-with-item.stationary"), null);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //if item dropped
        Player p = e.getPlayer();
        if(plugin.hotbar.itemCheckExecute(e.getItemDrop().getItemStack(),p,false,true)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //cancel everything if holding item (item frames eg)
        Player p = e.getPlayer();
        ItemStack clicked;
        if(Bukkit.getVersion().contains("1.8")){
            clicked =  new GetItemInHand_Legacy(plugin).itemInHand(p);
        }else{
            clicked = new GetItemInHand(plugin).itemInHand(p);
        }
        if(plugin.hotbar.itemCheckExecute(clicked,p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
}
