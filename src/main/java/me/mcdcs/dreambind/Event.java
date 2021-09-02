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
            if (e.getPlayer().getItemInHand().getType() != Material.AIR){
                if (config.getStringList("onEntity.onList").contains(e.getPlayer().getItemInHand().getType().toString())){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §c该绑定物品禁止右键实体！");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvPickup(InventoryPickupItemEvent e){
        ItemStack itemStack = e.getItem().getItemStack();
        if (isBind(itemStack)) {
            if (hasPlayer(getOwner(itemStack))) {
                e.setCancelled(true);
                addItem(itemStack, getOwner(itemStack));
                e.getItem().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e){
        if (e.getPlayer().getItemInHand().getType() != Material.AIR){
            if (isBind(e.getPlayer().getItemInHand())){
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
        if (e.getPlayer().getInventory().getItem(e.getNewSlot()) != null){
            if (Objects.requireNonNull(e.getPlayer().getInventory().getItem(e.getNewSlot())).getType() != Material.AIR){
                ItemStack itemStack = e.getPlayer().getInventory().getItem(e.getNewSlot());
                if (config.getBoolean("onAuto")){
                    if (config.getBoolean("onEvent.onHand")){
                        assert itemStack != null;
                        if (!isBind(itemStack)){
                            if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(itemStack.getType().toString())){
                                setBind(itemStack,e.getPlayer());
                                e.getPlayer().getInventory().setItem(e.getNewSlot(),itemStack);
                            }
                        }
                    }
                }
            }
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
                if (isBind(itemStack)){
                    if (config.getBoolean("onDeath")){
                        if (isOwner(itemStack,e.getEntity())){
                            addBag(itemStack,e.getEntity());
                            e.getDrops().remove(itemStack);
                            b = true;
                        }else {
                            if (hasPlayer(getOwner(itemStack))){
                                addItem(itemStack,getOwner(itemStack));
                                e.getDrops().remove(itemStack);
                            }
                        }
                    }else if (isKeep(itemStack)){
                        addBag(itemStack,e.getEntity());
                        e.getDrops().remove(itemStack);
                        b = true;
                    }else {
                        DeathItemList.add(itemStack);
                    }
                }else if (isKeep(itemStack)){
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
        ItemStack is = p.getItemInHand();
        if (e.hasItem()){
            if (isBind(Objects.requireNonNull(e.getItem()))){
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
        if (is.getType() != Material.AIR){
            if (isBindUse(is)){
                setBind(p,is);
                p.setItemInHand(is);
            }
        }
        if (Version > 8){
            if (p.getEquipment() != null){
                if (p.getEquipment().getItemInOffHand().getType() != Material.AIR){
                    is = p.getEquipment().getItemInOffHand();
                    if (isBindUse(is)){
                        setBind(p,is);
                        p.getEquipment().setItemInOffHand(is);
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
                ItemStack itemStack = item.getItemStack();
                if (isBind(itemStack)){
                    addItem(itemStack,e.getPlayer());
                    entity.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(PlayerPickupItemEvent e){ //物品拾取保护
        ItemStack itemStack = e.getItem().getItemStack();
        if (isBind(itemStack)){
            if (!isOwner(itemStack,e.getPlayer())){
                if (hasPlayer(getOwner(itemStack))){
                    e.setCancelled(true);
                    addItem(itemStack,getOwner(itemStack));
                    e.getItem().remove();
                    e.getPlayer().sendMessage("§f[§bDreamBind§f] §a你所拾取的物品已经被绑定,已经将其归还为所有者!");
                }
            }
        }else {
            if (config.getBoolean("onAuto")){
                if (config.getBoolean("onEvent.onPickup")){
                    if (!config.getBoolean("onType") | config.getStringList("onBindType").contains(itemStack.getType().toString())){
                        setBind(e.getPlayer(),itemStack);
                        e.setCancelled(true);
                        e.getPlayer().getInventory().addItem(itemStack);
                        e.getItem().remove();
                    }
                }
            }else {
                if (isBindPickup(itemStack)){
                    setBind(e.getPlayer(),itemStack);
                    e.setCancelled(true);
                    e.getPlayer().getInventory().addItem(itemStack);
                    e.getItem().remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent e){ //物品掉落保护
        if (!e.getPlayer().isDead()){
            ItemStack itemStack = e.getItemDrop().getItemStack();
            if (isBind(itemStack)){
                if (config.getBoolean("onDrop")) {
                    if (isOwner(itemStack, e.getPlayer())) {
                        if (e.getPlayer().getInventory().firstEmpty() == -1) {
                            DropItemList.add(e.getItemDrop());
                            e.getItemDrop().remove();
                            addItem(itemStack, e.getPlayer());
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§f[§bDreamBind§f] §a禁止丢弃已经绑定过的物品!");
                        }
                    } else {
                        if (hasPlayer(getOwner(itemStack))) {
                            DropItemList.add(e.getItemDrop());
                            addItem(itemStack, getOwner(itemStack));
                            e.getItemDrop().remove();
                            e.getPlayer().sendMessage("§f[§bDreamBind§f] §a你所点击的物品已经被绑定,已经将其归还为所有者!");
                        }
                    }
                }else {
                    DropItemList.add(e.getItemDrop());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(InventoryOpenEvent e){
        if (!e.getPlayer().isOp()){
            OpenList.add((Player) e.getPlayer());
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e){
        if (e.getEntity() instanceof ItemStack){
            if (isBind((ItemStack) e.getEntity())){
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
                if (is != null){
                    if (isBind(is)){
                        il.add(is);
                        addItem(is,getOwner(is));
                    }
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
                        if (is != null){
                            if (isBind(is)){
                                il.add(is);
                                addItem(is,getOwner(is));
                            }
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
                    ItemStack itemStack = e.getCurrentItem();
                    if (!p.isOp()){
                        if (config.getBoolean("onInventory")){
                            if (config.getStringList("onAccurate").contains(p.getOpenInventory().getTitle())){
                                if (isBind(itemStack)){
                                    e.setCancelled(true);
                                    p.sendMessage("§f[§bDreamBind§f] §c绑定物品禁止放入该物品栏内!");
                                }
                            }else {
                                for (String s : config.getStringList("onBlurred")){
                                    if (p.getOpenInventory().getTitle().contains(s)){
                                        if (isBind(itemStack)){
                                            e.setCancelled(true);
                                            p.sendMessage("§f[§bDreamBind§f] §c绑定物品禁止放入该物品栏内!");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (isBind(itemStack)){
                        if (!isOwner(itemStack,p)){
                            if (p.isOp()){
                                p.sendMessage("§f[§bDreamBind§f] §a你正在不受限制的点击绑定物品!");
                            }else {
                                if (e.getClickedInventory() == p.getInventory()){
                                    if (hasPlayer(getOwner(itemStack))){
                                        addItem(itemStack,getOwner(itemStack));
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
                                                unBind(itemStack,p);
                                                e.setCurrentItem(itemStack);
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
                        if (isBindUse(itemStack)){
                            if (e.getClickedInventory() == e.getWhoClicked().getInventory() | !config.getBoolean("onBindUseMore")){
                                if (!p.isOp()){
                                    setBind(p,itemStack);
                                    e.setCurrentItem(itemStack);
                                }
                            }
                        }
                        if (config.getBoolean("onBindStone.onStart")){
                            if (e.getCursor() != null){
                                if (e.getCursor().getType() != Material.AIR){
                                    ItemStack cursor = e.getCursor();
                                    if (isStone(cursor)){
                                        if (cursor.getAmount() == 1){
                                            if (!config.getBoolean("onBindStone.onBind.onType") | config.getStringList("onBindStone.onBind.onBindType").contains(itemStack.getType().toString())){
                                                if (!isStone(itemStack)){
                                                    e.setCursor(new ItemStack(Material.AIR));
                                                    setBind(itemStack,p);
                                                    e.setCurrentItem(itemStack);
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
                    if (!isKeep(itemStack)){
                        if (e.getCursor() != null){
                            if (e.getCursor().getType() != Material.AIR){
                                ItemStack cursor = e.getCursor();
                                if (noKeep(cursor)){
                                    if (cursor.getAmount() == 1){
                                        e.setCursor(new ItemStack(Material.AIR));
                                        setKeep(itemStack,p);
                                        e.setCurrentItem(itemStack);
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
            ItemStack itemStack = e.getEntity().getItemStack();
            if (isBind(itemStack)){
                if (DropItemList.contains(e.getEntity())) {
                    DropItemList.remove(e.getEntity());
                }else if (DeathItemList.contains(e.getEntity().getItemStack())){
                    DeathItemList.remove(e.getEntity().getItemStack());
                }else {
                    if (hasPlayer(getOwner(itemStack))){
                        if (Bukkit.getPlayer(getOwner(itemStack)) == null){
                            addItem(itemStack,getOwner(itemStack));
                        }else {
                            if (Objects.requireNonNull(Bukkit.getPlayer(getOwner(itemStack))).isDead()){
                                addBag(itemStack, Objects.requireNonNull(Bukkit.getPlayer(getOwner(itemStack))));
                            }else {
                                addItem(itemStack,getOwner(itemStack));
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
            if (isBind(e.getItem())){
                e.setCancelled(true);
            }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e){ //实体伤害
        if (config.getBoolean("onDamage")){
            if (e.getEntity() instanceof Item){
                Item item = (Item) e.getEntity();
                ItemStack itemStack = item.getItemStack();
                if (isBind(itemStack)){
                    e.setCancelled(true);
                    if (hasPlayer(getOwner(itemStack))){
                        if (Bukkit.getPlayer(getOwner(itemStack)) == null){
                            ArrayList<Player> il = new ArrayList<>(Bukkit.getOnlinePlayers());
                            item.teleport(il.get(random(1,il.size()) - 1).getLocation());
                        }else {
                            item.teleport(Objects.requireNonNull(Bukkit.getPlayer(getOwner(itemStack))).getLocation());
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
                if (itemStack != null){
                    if (isBind(itemStack)){
                        z = true;
                    }
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
                bag.set(e.getPlayer().getName() + "." + onBag((Player) e.getPlayer()),itemStack);
            }
            try {
                bag.save(bagfile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!e.getPlayer().isOp()){
            OpenList.remove(e.getPlayer());
        }
    }
}
