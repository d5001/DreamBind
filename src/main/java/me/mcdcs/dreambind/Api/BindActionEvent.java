package me.mcdcs.dreambind.Api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BindActionEvent extends Event implements Cancellable {

    private boolean cancel;
    private ItemStack itemStack;
    private ArrayList<String> lore;
    private String action;

    public BindActionEvent(ItemStack itemStack,ArrayList<String> lore,String action){
        this.itemStack = itemStack;
        this.lore = lore;
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action){
        this.action = action;
    }

    public void setLore(ArrayList<String> lore){
        this.lore = lore;
    }

    public void setItemStack(ItemStack itemStack){
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ArrayList<String> getLore() {
        return lore;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }
}

