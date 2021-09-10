package me.mcdcs.dreambind;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import static me.mcdcs.dreambind.DreamBind.*;

public class Event194 implements Listener {
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e){
        DItem dItem;
        if (e.getMainHandItem() != null){
            dItem = new DItem(e.getMainHandItem());
            if (config.getBoolean("onAuto")){
                if (config.getBoolean("onEvent.onSwap")){
                    if (!dItem.isBind()){
                        if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(dItem.getItemStack().getType().toString())){
                            e.setMainHandItem(dItem.setBind(e.getPlayer()));
                        }
                    }
                }
            }else if (dItem.isBind("onBindGet")){
                e.setMainHandItem(dItem.setBind(e.getPlayer()));
            }
        }
        if (e.getOffHandItem() != null){
            dItem = new DItem(e.getOffHandItem());
            if (config.getBoolean("onAuto")){
                if (config.getBoolean("onEvent.onSwap")){
                    if (!dItem.isBind()){
                        if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(dItem.getItemStack().getType().toString())){
                            e.setOffHandItem(dItem.setBind(e.getPlayer()));
                        }
                    }
                }
            }else if (dItem.isBind("onBindGet")){
                e.setOffHandItem(dItem.setBind(e.getPlayer()));
            }
        }
    }
}
