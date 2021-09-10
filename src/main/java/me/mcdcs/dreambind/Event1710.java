package me.mcdcs.dreambind;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class Event1710 implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmor(PlayerArmorStandManipulateEvent e){
        DItem dItem = new DItem(e.getPlayerItem());
        if (dItem.isBind()){
            e.setCancelled(true);
            e.getPlayer().sendMessage("§f[§bDreamBind§f] §a被绑定的物品无法放置在盔甲架上!");
        }
    }
}