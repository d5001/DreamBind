package me.mcdcs.dreambind.Api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;


public class BindEvent extends Event implements Cancellable {

    private boolean cancel;
    private String playername;
    private ItemStack itemStack;
    private ArrayList<String> lore;

    public BindEvent(ItemStack itemStack,ArrayList<String> lore,String playername){
        this.itemStack = itemStack;
        this.lore = lore;
        this.playername = playername;
    }

    public void setPlayername(String playername){
        this.playername = playername;
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

    public String getPlayername() {
        return playername;
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
