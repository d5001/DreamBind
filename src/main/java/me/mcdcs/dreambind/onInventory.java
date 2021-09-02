package me.mcdcs.dreambind;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
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
                            ItemStack is = ((Item) entity).getItemStack();
                            if (is.getType() != Material.AIR){
                                if (isBind(is)){
                                    if (hasPlayer(getOwner(is))){
                                        addItem(is,getOwner(is));
                                        entity.remove();
                                    }
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
                if (itemStack != null){
                    if (itemStack.getType() != Material.AIR){
                        if (isBindEquip(itemStack)){
                            setBind(p,itemStack);
                        }
                    }
                }
            }
            p.getInventory().setArmorContents(isl);
            if (Version > 8){
                if (Objects.requireNonNull(p.getEquipment()).getItemInOffHand().getType() != Material.AIR){
                    ItemStack itemStack = p.getEquipment().getItemInOffHand();
                    if (isBindEquip(itemStack)){
                        setBind(p,itemStack);
                        p.getEquipment().setItemInOffHand(itemStack);
                    }
                }
            }
        }
        if (config.getBoolean("onInventory")) {
            for (Player p : OpenList) {
                ArrayList<ItemStack> il = new ArrayList<>();
                if (config.getStringList("onAccurate").contains(p.getOpenInventory().getTitle())) {
                    for (ItemStack is : p.getOpenInventory().getTopInventory().getContents()) {
                        if (is != null) {
                            if (isBind(is)) {
                                il.add(is);
                                addItem(is, getOwner(is));
                            }
                        }
                    }
                    if (il.size() > 0) {
                        for (ItemStack is : il) {
                            p.getOpenInventory().getTopInventory().remove(is);
                        }
                    }
                } else {
                    for (String s : config.getStringList("onBlurred")) {
                        if (p.getOpenInventory().getTitle().contains(s)) {
                            for (ItemStack is : p.getOpenInventory().getTopInventory().getContents()) {
                                if (is != null) {
                                    if (isBind(is)) {
                                        il.add(is);
                                        addItem(is, getOwner(is));
                                    }
                                }
                            }
                            if (il.size() > 0) {
                                for (ItemStack is : il) {
                                    p.getOpenInventory().getTopInventory().remove(is);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}