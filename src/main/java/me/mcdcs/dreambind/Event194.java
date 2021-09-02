package me.mcdcs.dreambind;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import static me.mcdcs.dreambind.DreamBind.*;

public class Event194 implements Listener {
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e){
        ItemStack itemStack;
        if (e.getMainHandItem() != null){
            itemStack = e.getMainHandItem();
            if (config.getBoolean("onAuto")){
                if (config.getBoolean("onEvent.onSwap")){
                    assert itemStack != null;
                    if (!isBind(itemStack)){
                        if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(itemStack.getType().toString())){
                            setBind(itemStack,e.getPlayer());
                            e.setMainHandItem(itemStack);
                        }
                    }
                }
            }
        }
        if (e.getOffHandItem() != null){
            itemStack = e.getOffHandItem();
            if (config.getBoolean("onAuto")){
                if (config.getBoolean("onEvent.onSwap")){
                    assert itemStack != null;
                    if (!isBind(itemStack)){
                        if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(itemStack.getType().toString())){
                            setBind(itemStack,e.getPlayer());
                            e.setOffHandItem(itemStack);
                        }
                    }
                }
            }
        }
    }
}
