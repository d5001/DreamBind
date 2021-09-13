package me.mcdcs.dreambind.Runnable;

import me.mcdcs.dreambind.Api.DItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.*;

public class Runnable extends BukkitRunnable {
    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()){
            if (!p.isOp()){
                ItemStack[] is = p.getInventory().getContents();
                for (ItemStack itemStack : is){
                    DItem dItem = new DItem(itemStack);
                    if (dItem.isBind()){
                        if (!dItem.isOwner(p)){
                            itemStack.setType(Material.AIR);
                            addItem(itemStack,dItem.getOwner());
                        }
                    }
                }
                p.getInventory().setContents(is);
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