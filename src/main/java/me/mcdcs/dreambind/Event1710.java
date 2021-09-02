package me.mcdcs.dreambind;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import static me.mcdcs.dreambind.DreamBind.*;

public class Event1710 implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmor(PlayerArmorStandManipulateEvent e){
        if (e.getPlayerItem().getType() != Material.AIR){
            if (isBind(e.getPlayerItem())){
                e.setCancelled(true);
                e.getPlayer().sendMessage("§f[§bDreamBind§f] §a被绑定的物品无法放置在盔甲架上!");
            }
        }
    }
}