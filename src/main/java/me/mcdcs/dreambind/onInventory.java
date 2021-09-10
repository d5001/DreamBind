package me.mcdcs.dreambind;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.*;

public class onInventory extends BukkitRunnable {
    @Override
    public void run() {
        if (config.getBoolean("onServer.onStart")){
            onTime++;
            if (onTime/2 == config.getInt("onServer.onTime")){
                for (World w : Bukkit.getWorlds()){
                    for (Entity entity : w.getEntities()){
                        if (entity instanceof Item){
                            DItem dItem = new DItem(((Item) entity).getItemStack());
                            if (dItem.isBind()){
                                if (hasPlayer(dItem.getOwner())){
                                    addItem(dItem.getItemStack(),dItem.getOwner());
                                    entity.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Player p : Bukkit.getOnlinePlayers()){
            ItemStack[] isl = p.getInventory().getArmorContents();
            for (ItemStack itemStack : isl){
                DItem dItem = new DItem(itemStack);
                if (dItem.isBind("onBindEquip")){
                    dItem.setBind(p);
                }
            }
            p.getInventory().setArmorContents(isl);
            if (Version > 8){
                if (Objects.requireNonNull(p.getEquipment()).getItemInOffHand().getType() != Material.AIR){
                    DItem dItem = new DItem(p.getEquipment().getItemInOffHand());
                    if (dItem.isBind("onBindEquip")){
                        p.getEquipment().setItemInOffHand(dItem.setBind(p));
                    }
                }
            }
        }
        if (config.getBoolean("onInventory")) {
            for (Player p : OpenList) {
                ItemStack[] iss = p.getOpenInventory().getTopInventory().getContents();
                if (config.getStringList("onAccurate").contains(p.getOpenInventory().getTitle())) {
                    for (ItemStack is : iss) {
                        DItem dItem = new DItem(is);
                        if (dItem.isBind()) {
                            is.setType(Material.AIR);
                            addItem(is,dItem.getOwner());
                        }
                    }
                    p.getOpenInventory().getTopInventory().setContents(iss);
                } else {
                    for (String s : config.getStringList("onBlurred")) {
                        if (p.getOpenInventory().getTitle().contains(s)) {
                            for (ItemStack is : iss) {
                                DItem dItem = new DItem(is);
                                if (dItem.isBind()) {
                                    is.setType(Material.AIR);
                                    addItem(is,dItem.getOwner());
                                }
                            }
                            p.getOpenInventory().getTopInventory().setContents(iss);
                            break;
                        }
                    }
                }
            }
        }
    }
}