package me.mcdcs.dreambind;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static me.mcdcs.dreambind.DreamBind.*;

public class Event implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntity(PlayerInteractEntityEvent e){
        if (config.getBoolean("onEntity.onStart")){
            DItem dItem = new DItem(e.getPlayer().getItemInHand());
            if (dItem.isBind()){
                if (config.getStringList("onEntity.onList").contains(dItem.getItemStack().getType().toString())){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §c该绑定物品禁止右键实体！");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvPickup(InventoryPickupItemEvent e){
        DItem dItem = new DItem(e.getItem().getItemStack());
        if (dItem.isBind()) {
            if (hasPlayer(dItem.getOwner())) {
                e.setCancelled(true);
                addItem(dItem.getItemStack(),dItem.getOwner());
                e.getItem().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e){
        DItem dItem = new DItem(e.getPlayer().getItemInHand());
        if (dItem.isBind()){
            if (config.getBoolean("onCommand")){
                boolean b = false;
                for (String s : config.getStringList("onCmdList")){
                    if (e.getMessage().length() >= s.length()){
                        if (e.getMessage().toUpperCase().substring(0,s.length()).equals(s.toUpperCase())){
                            b = true;
                        }
                    }
                }
                if (config.getBoolean("onCmdAll")){
                    b = true;
                }
                if (b){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §c手持绑定物品禁止执行该命令!");
                }
            }
        }
        if (e.getMessage().replace("/","").length() >= Objects.requireNonNull(bindgui.getString("onInfo.command")).length()){
            if (e.getMessage().replace("/","").substring(0, Objects.requireNonNull(bindgui.getString("onInfo.command")).length()).toUpperCase().equals(Objects.requireNonNull(bindgui.getString("onInfo.command")).toUpperCase())){
                e.setCancelled(true);
                if (e.getPlayer().getItemInHand().getType() != Material.AIR){
                    Gui(e.getPlayer());
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §a已经为你打开绑定菜单");
                }else {
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §c必须手持物品才能打开绑定菜单！");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHand(PlayerItemHeldEvent e){
        DItem dItem = new DItem(e.getPlayer().getInventory().getItem(e.getNewSlot()));
        if (config.getBoolean("onAuto")){
            if (config.getBoolean("onEvent.onHand")){
                if (!dItem.isBind()){
                    if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(dItem.getItemStack().getType().toString())){
                        e.getPlayer().getInventory().setItem(e.getNewSlot(),dItem.setBind(e.getPlayer()));
                    }
                }
            }
        }else if (dItem.isBind("onBindGet")){
            e.getPlayer().getInventory().setItem(e.getNewSlot(),dItem.setBind(e.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e){
        if (bag.getConfigurationSection(e.getPlayer().getName()) != null){
            int i = Objects.requireNonNull(bag.getConfigurationSection(e.getPlayer().getName())).getKeys(true).size();
            if (i > 0){
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        e.getPlayer().sendMessage("§f[§bDreamBind§f] §a你的绑定箱内存在物品,请输入§d/bindbag §a收入背包!");
                    }
                },100);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e){ //玩家死亡保护
        if (!e.getKeepInventory()){
            boolean b = false;
            ArrayList<ItemStack> is = new ArrayList<>(e.getDrops());
            for (ItemStack itemStack : is){
                DItem dItem = new DItem(itemStack);
                if (dItem.isBind()){
                    if (config.getBoolean("onDeath")){
                        if (dItem.isOwner(e.getEntity())){
                            addBag(itemStack,e.getEntity());
                            e.getDrops().remove(itemStack);
                            b = true;
                        }else {
                            if (hasPlayer(dItem.getOwner())){
                                addItem(itemStack,dItem.getOwner());
                                e.getDrops().remove(itemStack);
                            }
                        }
                    }else if (dItem.isBind("onKeepLore")){
                        addBag(itemStack,e.getEntity());
                        e.getDrops().remove(itemStack);
                        b = true;
                    }else {
                        DeathItemList.add(itemStack);
                    }
                }else if (dItem.isBind("onKeepLore")){
                    addBag(itemStack,e.getEntity());
                    e.getDrops().remove(itemStack);
                    b = true;
                }
            }
            if (b){
                e.getEntity().sendMessage("§f[§bDreamBind§f] §a你绑定的物品由于死亡掉落放置在了绑定箱内!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        DItem dItem = new DItem(p.getItemInHand());
        if (e.hasItem()){
            if (new DItem(e.getItem()).isBind()){
                if (e.getAction() == Action.RIGHT_CLICK_AIR){
                    if (config.getBoolean("onRight.onAir")){
                        e.setCancelled(true);
                    }
                }
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if (config.getBoolean("onRight.onBlock")){
                        e.setCancelled(true);
                    }
                }
                if (e.getAction() == Action.LEFT_CLICK_AIR){
                    if (config.getBoolean("onLeft.onAir")){
                        e.setCancelled(true);
                    }
                }
                if (e.getAction() == Action.LEFT_CLICK_BLOCK){
                    if (config.getBoolean("onLeft.onBlock")){
                        e.setCancelled(true);
                    }
                }
            }
        }
        if (dItem.isBind("onBindUse")){
            p.setItemInHand(dItem.setBind(p));
        }
        if (Version > 8){
            if (p.getEquipment() != null){
                if (p.getEquipment().getItemInOffHand().getType() != Material.AIR){
                    dItem = new DItem(p.getEquipment().getItemInOffHand());
                    if (dItem.isBind("onBindUse")){
                        p.getEquipment().setItemInOffHand(dItem.setBind(p));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e){ //玩家退出保护
        Location loc = e.getPlayer().getLocation();
        for (Entity entity : loc.getWorld().getEntities()){
            if (entity instanceof Item){
                Item item = (Item) entity;
                DItem dItem = new DItem(item.getItemStack());
                if (dItem.isBind()){
                    addItem(dItem.getItemStack(),e.getPlayer());
                    entity.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(PlayerPickupItemEvent e){ //物品拾取保护
        DItem dItem = new DItem(e.getItem().getItemStack());
        if (dItem.isBind()){
            if (!dItem.isOwner(e.getPlayer())){
                if (hasPlayer(dItem.getOwner())){
                    e.setCancelled(true);
                    addItem(dItem.getItemStack(),dItem.getOwner());
                    e.getItem().remove();
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §a你所拾取的物品已经被绑定,已经将其归还为所有者!");
                }
            }
        }else {
            if (config.getBoolean("onAuto")){
                if (config.getBoolean("onEvent.onPickup")){
                    if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(dItem.getItemStack().getType().toString())){
                        e.setCancelled(true);
                        e.getPlayer().getInventory().addItem(dItem.setBind(e.getPlayer()));
                        e.getItem().remove();
                    }
                }
            }else {
                if (dItem.isBind("onBindPickup")){
                    e.setCancelled(true);
                    e.getPlayer().getInventory().addItem(dItem.setBind(e.getPlayer()));
                    e.getItem().remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent e){ //物品掉落保护
        if (!e.getPlayer().isDead()){
            DItem dItem = new DItem(e.getItemDrop().getItemStack());
            if (dItem.isBind()){
                if (config.getBoolean("onDrop")) {
                    if (dItem.isOwner(e.getPlayer())) {
                        if (e.getPlayer().getInventory().firstEmpty() == -1) {
                            DropItemList.add(e.getItemDrop());
                            e.getItemDrop().remove();
                            addItem(dItem.getItemStack(), e.getPlayer());
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§f[§bDreamBind§f] §a禁止丢弃已经绑定过的物品!");
                        }
                    } else {
                        if (hasPlayer(dItem.getOwner())) {
                            DropItemList.add(e.getItemDrop());
                            addItem(dItem.getItemStack(),dItem.getOwner());
                            e.getItemDrop().remove();
                            e.getPlayer().sendMessage("§f[§bDreamBind§f] §a你所点击的物品已经被绑定,已经将其归还为所有者!");
                        }
                    }
                }else {
                    DropItemList.add(e.getItemDrop());
                }
            }else if (dItem.isBind("onBindGet")){
                if (!e.getPlayer().isOp()){
                    e.getItemDrop().setItemStack(dItem.setBind(e.getPlayer()));
                    ItemStack[] isl = e.getPlayer().getInventory().getContents();
                    for (ItemStack itemStack : isl){
                        dItem = new DItem(itemStack);
                        if (dItem.isBind("onBindGet")){
                            dItem.setBind(e.getPlayer());
                        }
                    }
                    e.getPlayer().getInventory().setContents(isl);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(InventoryOpenEvent e){
        Player p = (Player) e.getPlayer();
        if (!p.isOp()){
            OpenList.add(p);
            ItemStack[] isl = p.getInventory().getContents();
            for (ItemStack itemStack : isl){
                DItem dItem = new DItem(itemStack);
                if (dItem.isBind("onBindGet")){
                    dItem.setBind(p);
                }
            }
            p.getInventory().setContents(isl);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e){
        if (e.getEntity() instanceof Item){
            Item item = (Item) e.getEntity();
            DItem dItem = new DItem(item.getItemStack());
            if (dItem.isBind()){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent e){ //背包点击物品保护
        ArrayList<ItemStack> il = new ArrayList<>();
        Player p = (Player) e.getWhoClicked();
        if (e.getView().getTitle().equals(bindgui.getString("onInfo.name"))){
            if (e.getSlot() != -999){
                e.setCancelled(true);
                BindGui(p,e.getSlot());
            }
        }
        if (config.getStringList("onAccurate").contains(p.getOpenInventory().getTitle())){
            for (ItemStack is : p.getOpenInventory().getTopInventory().getContents()){
                DItem dItem = new DItem(is);
                if (dItem.isBind()){
                    il.add(is);
                    addItem(is,dItem.getOwner());
                }
            }
            if (il.size() > 0){
                for (ItemStack is : il){
                    p.getOpenInventory().getTopInventory().remove(is);
                }
            }
        }else {
            for (String s : config.getStringList("onBlurred")){
                if (p.getOpenInventory().getTitle().contains(s)){
                    for (ItemStack is : p.getOpenInventory().getTopInventory().getContents()){
                        DItem dItem = new DItem(is);
                        if (dItem.isBind()){
                            il.add(is);
                            addItem(is,dItem.getOwner());
                        }
                    }
                    if (il.size() > 0){
                        for (ItemStack is : il){
                            p.getOpenInventory().getTopInventory().remove(is);
                        }
                    }
                    break;
                }
            }
        }
        if (e.getSlot() != -999){
            if (e.getCurrentItem() != null){
                if (e.getCurrentItem().getType() != Material.AIR){
                    DItem dItem = new DItem(e.getCurrentItem());
                    if (!p.isOp()){
                        if (config.getBoolean("onInventory")){
                            if (config.getStringList("onAccurate").contains(p.getOpenInventory().getTitle())){
                                if (dItem.isBind()){
                                    e.setCancelled(true);
                                    p.sendMessage("§f[§bDreamBind§f] §c绑定物品禁止放入该物品栏内!");
                                }
                            }else {
                                for (String s : config.getStringList("onBlurred")){
                                    if (p.getOpenInventory().getTitle().contains(s)){
                                        if (dItem.isBind()){
                                            e.setCancelled(true);
                                            p.sendMessage("§f[§bDreamBind§f] §c绑定物品禁止放入该物品栏内!");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (dItem.isBind()){
                        if (!dItem.isOwner(p)){
                            if (p.isOp()){
                                p.sendMessage("§f[§bDreamBind§f] §a你正在不受限制的点击绑定物品!");
                            }else {
                                if (e.getClickedInventory() == p.getInventory()){
                                    if (hasPlayer(dItem.getOwner())){
                                        addItem(dItem.getItemStack(),dItem.getOwner());
                                        e.setCurrentItem(new ItemStack(Material.AIR));
                                        p.sendMessage("§f[§bDreamBind§f] §a你所点击的物品已经被绑定,已经将其归还为所有者!");
                                    }
                                }else {
                                    e.setCancelled(true);
                                }
                            }
                        }else {
                            if (config.getBoolean("onBindStone.onStart")){
                                if (e.getCursor() != null){
                                    if (e.getCursor().getType() != Material.AIR){
                                        ItemStack cursor = e.getCursor();
                                        if (noStone(cursor)){
                                            if (cursor.getAmount() == 1){
                                                e.setCursor(new ItemStack(Material.AIR));
                                                e.setCurrentItem(dItem.unBind());
                                                e.setCancelled(true);
                                                p.sendMessage("§f[§bDreamBind§f] §c成功为该物品进行解绑!");
                                            }else {
                                                p.sendMessage("§f[§bDreamBind§f] §c解绑石只能叠加一个进行装备绑定!");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if (dItem.isBind("onBindUse")){
                            if (e.getClickedInventory() == e.getWhoClicked().getInventory() | !config.getBoolean("onBindUseMore")){
                                if (!p.isOp()){
                                    e.setCurrentItem(dItem.setBind(p));
                                }
                            }
                        }
                        if (config.getBoolean("onBindStone.onStart")){
                            if (e.getCursor() != null){
                                if (e.getCursor().getType() != Material.AIR){
                                    ItemStack cursor = e.getCursor();
                                    if (isStone(cursor)){
                                        if (cursor.getAmount() == 1){
                                            if (!config.getBoolean("onBindStone.onBind.onType") | config.getStringList("onBindStone.onBind.onBindType").contains(dItem.getItemStack().getType().toString())){
                                                if (!isStone(dItem.getItemStack())){
                                                    e.setCursor(new ItemStack(Material.AIR));;
                                                    e.setCurrentItem(dItem.setBind(p));
                                                    e.setCancelled(true);
                                                    p.sendMessage("§f[§bDreamBind§f] §c成功为该物品进行绑定!");
                                                }
                                            }else {
                                                p.sendMessage("§f[§bDreamBind§f] §c该物品无法被进行绑定!");
                                            }
                                        }else {
                                            p.sendMessage("§f[§bDreamBind§f] §c绑定石只能叠加一个进行装备绑定!");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!dItem.isBind("onKeepLore")){
                        if (e.getCursor() != null){
                            if (e.getCursor().getType() != Material.AIR){
                                ItemStack cursor = e.getCursor();
                                if (noKeep(cursor)){
                                    if (cursor.getAmount() == 1){
                                        e.setCursor(new ItemStack(Material.AIR));
                                        e.setCurrentItem(dItem.setBindAction("onKeepLore"));
                                        e.setCancelled(true);
                                        p.sendMessage("§f[§bDreamBind§f] §c成功为该物品进行死亡不掉落绑定!");
                                    }else {
                                        p.sendMessage("§f[§bDreamBind§f] §c死亡不掉落只能叠加一个进行装备绑定!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(ItemSpawnEvent e){
        if (config.getBoolean("Event.onSpawn")){
            DItem dItem = new DItem(e.getEntity().getItemStack());
            if (dItem.isBind()){
                if (DropItemList.contains(e.getEntity())) {
                    DropItemList.remove(e.getEntity());
                }else if (DeathItemList.contains(e.getEntity().getItemStack())){
                    DeathItemList.remove(e.getEntity().getItemStack());
                }else {
                    if (hasPlayer(dItem.getOwner())){
                        if (Bukkit.getPlayer(dItem.getOwner()) == null){
                            addItem(dItem.getItemStack(),dItem.getOwner());
                        }else {
                            if (Objects.requireNonNull(Bukkit.getPlayer(dItem.getOwner())).isDead()){
                                addBag(dItem.getItemStack(), Objects.requireNonNull(Bukkit.getPlayer(dItem.getOwner())));
                            }else {
                                addItem(dItem.getItemStack(),dItem.getOwner());
                            }
                        }
                        e.getEntity().remove();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(InventoryMoveItemEvent e){ //漏斗类物品栏移动
        if (config.getBoolean("onMove"))
            if (new DItem(e.getItem()).isBind()){
                e.setCancelled(true);
            }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e){ //实体伤害
        if (config.getBoolean("onDamage")){
            if (e.getEntity() instanceof Item){
                Item item = (Item) e.getEntity();
                DItem dItem = new DItem(item.getItemStack());
                if (dItem.isBind()){
                    e.setCancelled(true);
                    if (hasPlayer(dItem.getOwner())){
                        if (Bukkit.getPlayer(dItem.getOwner()) == null){
                            ArrayList<Player> il = new ArrayList<>(Bukkit.getOnlinePlayers());
                            item.teleport(il.get(random(0,il.size()) - 1).getLocation());
                        }else {
                            item.teleport(Objects.requireNonNull(Bukkit.getPlayer(dItem.getOwner())).getLocation());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent e){
        boolean z = false;
        BlockState b = e.getBlock().getState();
        Inventory i = null;
        int v = Version;
        if (b instanceof Chest){
            Chest chest = (Chest) b;
            i = chest.getInventory();
        }
        if (i == null){
            if (b instanceof Hopper){
                Hopper hopper = (Hopper) b;
                i = hopper.getInventory();
            }
        }
        if (i == null){
            if (b instanceof Furnace){
                Furnace furnace = (Furnace) b;
                i = furnace.getInventory();
            }
        }
        if (i == null){
            if (b instanceof Dispenser){
                Dispenser dispenser = (Dispenser) b;
                i = dispenser.getInventory();
            }
        }
        if (i == null){
            if (b instanceof Dropper){
                Dropper dropper = (Dropper) b;
                i = dropper.getInventory();
            }
        }
        if (i == null){
            if (b instanceof BrewingStand){
                BrewingStand brewingStand = (BrewingStand) b;
                i = brewingStand.getInventory();
            }
        }
        if (i == null){
            if (v >= 14){
                if (b instanceof Barrel){
                    Barrel barrel = (Barrel) b;
                    i = barrel.getInventory();
                }
            }
        }
        if (i == null){
            if (v >= 11){
                if (b instanceof ShulkerBox){
                    ShulkerBox shulkerBox = (ShulkerBox) b;
                    i = shulkerBox.getInventory();
                }
            }
        }
        if (i != null){
            for (ItemStack itemStack : i){
                DItem dItem = new DItem(itemStack);
                if (dItem.isBind()){
                    z = true;
                }
            }
        }
        if (z){
            e.setCancelled(true);
            e.getPlayer().sendMessage("§f[§bDreamBind§f] §c该方块内含有绑定物品,禁止被破坏!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e){  //关闭绑定箱界面
        if (e.getView().getTitle().equals("§bDreamBind §r- §d绑定箱")){
            for (ItemStack itemStack : e.getInventory().getContents()){
                DItem dItem = new DItem(itemStack);
                if (dItem.isBind()){
                    bag.set(e.getPlayer().getName() + "." + onBag((Player) e.getPlayer()),itemStack);
                }else {
                    e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(),itemStack);
                }
            }
            try {
                bag.save(bagfile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!e.getPlayer().isOp()){
            OpenList.remove(e.getPlayer());
            ItemStack[] isl = e.getPlayer().getInventory().getContents();
            for (ItemStack itemStack : isl){
                DItem dItem = new DItem(itemStack);
                if (dItem.isBind("onBindGet")){
                    dItem.setBind((Player) e.getPlayer());
                }
            }
            e.getPlayer().getInventory().setContents(isl);
        }
    }
}
