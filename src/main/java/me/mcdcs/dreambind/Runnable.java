package me.mcdcs.dreambind;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.*;

public class Runnable extends BukkitRunnable {
    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()){
            if (!p.isOp()){
                ArrayList<ItemStack> il = new ArrayList<>();
                for (ItemStack itemStack : p.getInventory().getContents()){
                    if (itemStack != null){
                        if (isBind(itemStack)){
                            if (!isOwner(itemStack,p)){
                                il.add(itemStack);
                                addItem(itemStack,getOwner(itemStack));
                            }
                        }
                    }
                }
                for (ItemStack itemStack : il){
                    p.getInventory().remove(itemStack);
                }
            }
            if (bag.getConfigurationSection(p.getName()) != null){
                int i = Objects.requireNonNull(bag.getConfigurationSection(p.getName())).getKeys(true).size();
                if (i > 0){
                    p.sendMessage("§f[§bDreamBind§f] §a你的绑定箱内存在物品,请输入§d/bindbag §a收入背包!");
                }
            }
        }
    }
}