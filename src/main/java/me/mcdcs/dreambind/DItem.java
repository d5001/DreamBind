package me.mcdcs.dreambind;

import me.mcdcs.dreambind.Api.BindActionEvent;
import me.mcdcs.dreambind.Api.BindEvent;
import me.mcdcs.dreambind.Api.UnBindEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class DItem {

    private ItemStack itemStack;

    public DItem(ItemStack is){
        itemStack = is;
    }

    public boolean isOwner(String s){
        return getOwner().equals(s);
    }

    public boolean isOwner(Player player){
        return isOwner(player.getName());
    }

    public String getOwner(){
        if (contains(Config.getStringList("onBindLore"))){
            return getLore().get(containsWz(Config.getStringList("onBindLore")) + 1);
        }
        if (containsBindName(Config.getStringList("onBindName"))){
            return containsBindNameMz(Config.getStringList("onBindName"));
        }
        return null;
    }

    public boolean isBind(){
        boolean b = contains(Config.getStringList("onBindLore"));
        if (!b){
            b = containsBindName(Config.getStringList("onBindName"));
        }
        return b;
    }

    public boolean isBind(String s){
        return contains(Config.getStringList(s));
    }

    public ItemStack setBind(String name){
        if (isItemStack()){
            ArrayList<String> il = getLore();
            BindEvent bindEvent = new BindEvent(itemStack,il,name);
            Bukkit.getPluginManager().callEvent(bindEvent);
            if (bindEvent.isCancelled()){
                return itemStack;
            }
            itemStack = bindEvent.getItemStack();
            il = bindEvent.getLore();
            name = bindEvent.getPlayername();
            il.add(Config.getStringList("onBindLore").get(0));
            remove(il,Config.getStringList("onBindUse"));
            remove(il,Config.getStringList("onBindEquip"));
            remove(il,Config.getStringList("onBindPickup"));
            remove(il,Config.getStringList("onBindGet"));
            il.add(name);
            ItemMeta im = itemStack.getItemMeta();
            im.setLore(il);
            itemStack.setItemMeta(im);
        }
        return itemStack;
    }

    public ItemStack setBindAction(String s){
        ArrayList<String> il = getLore();
        BindActionEvent bindActionEvent = new BindActionEvent(itemStack,il,s);
        Bukkit.getPluginManager().callEvent(bindActionEvent);
        if (bindActionEvent.isCancelled()){
            return itemStack;
        }
        itemStack = bindActionEvent.getItemStack();
        il = bindActionEvent.getLore();
        s = bindActionEvent.getAction();
        remove(il,Config.getStringList(s));
        il.add(Config.getStringList(s).get(0));
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(il);
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public ItemStack setBind(Player player){
        return setBind(player.getName());
    }

    public ItemStack unBind(){
        ArrayList<String> il = getLore();
        UnBindEvent unBindEvent = new UnBindEvent(itemStack,il,getOwner());
        Bukkit.getPluginManager().callEvent(unBindEvent);
        if (unBindEvent.isCancelled()){
            return itemStack;
        }
        itemStack = unBindEvent.getItemStack();
        il = unBindEvent.getLore();
        if (contains(Config.getStringList("onBindLore"))){
            il.remove(containsWz(Config.getStringList("onBindLore")));
            il.remove(containsWz(Config.getStringList("onBindLore")));
        }else {
            for (String s : il){
                if (s.contains(containsBindNameMz(Config.getStringList("onBindName")))){
                    il.remove(s);
                    break;
                }
            }
        }
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(il);
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public String containsBindNameMz(ArrayList<String> il){
        String name = null;
        if (hasLore()){
            for (String s : getLore()){
                if (name != null){
                    break;
                }
                for (String z : il){
                    if (s.contains(z)){
                        name = s.replace(z,"");
                        break;
                    }
                }
            }
        }
        return name;
    }

    public boolean containsBindName(ArrayList<String> il){
        boolean b = false;
        if (hasLore()){
            for (String s : getLore()){
                if (b){
                    break;
                }
                for (String z : il){
                    if (s.contains(z)){
                        b = true;
                        break;
                    }
                }
            }
        }
        return b;
    }

    public int containsWz(ArrayList<String> il){
        int i = 0;
        if (hasLore()){
            for (String s : getLore()){
                if (il.contains(s)){
                    break;
                }else {
                    i++;
                }
            }
        }
        return i;
    }

    public boolean contains(ArrayList<String> il){
        boolean b = false;
        if (hasLore()){
            for (String s : getLore()){
                if (il.contains(s)){
                    b = true;
                    break;
                }
            }
        }
        return b;
    }

    public ArrayList<String> remove(ArrayList<String> il,ArrayList<String> rem){
        if (contains(rem)){
            for (String s : rem){
                il.remove(s);
            }
        }
        return il;
    }

    public ArrayList<String> getLore(){
        if (hasLore()){
            return (ArrayList<String>) itemStack.getItemMeta().getLore();
        }
        return new ArrayList<>();
    }

    public boolean hasLore(){
        if (isItemStack()){
            if (itemStack.hasItemMeta()){
                if (itemStack.getItemMeta().hasLore()){
                    return itemStack.getItemMeta().hasLore();
                }
            }
        }
        return false;
    }

    public boolean isItemStack(){
        if (itemStack != null){
            return itemStack.getType() != Material.AIR;
        }
        return false;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
