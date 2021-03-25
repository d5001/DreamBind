package me.mcdcs.dreambind;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class DreamBind extends JavaPlugin {

    private static FileConfiguration vault;
    private static FileConfiguration bindgui;
    private static File bindfile;
    private static File vaultfile;
    private static FileConfiguration config;
    private static FileConfiguration bag;
    private static File bagfile;
    private static ArrayList<Item> DropItemList = new ArrayList<>();
    private static ArrayList<ItemStack> DeathItemList = new ArrayList<>();
    private static ArrayList<Player> OpenList = new ArrayList<>();
    private static int onTime = 0;
    private static Economy econ = null;
    private static int Version = Integer.parseInt(Bukkit.getBukkitVersion().replace("1.","").substring(0,Bukkit.getBukkitVersion().replace("1.","").indexOf(".")));

    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        bagfile = new File(getDataFolder(),"bag.yml");
        if (!bagfile.exists()){
            saveResource("bag.yml",true);
            System.out.println("[DreamBind]正在加载bag.yml文件");
        }

        vaultfile = new File(getDataFolder(),"vault.yml");
        if (!vaultfile.exists()){
            saveResource("vault.yml",true);
            System.out.println("[DreamBind]正在加载vault.yml文件");
        }
        vault = YamlConfiguration.loadConfiguration(vaultfile);

        bindfile = new File(getDataFolder(),"bind.yml");
        if (!bindfile.exists()){
            saveResource("bind.yml",true);
            System.out.println("[DreamBind]正在加载bind.yml文件");
        }
        bindgui = YamlConfiguration.loadConfiguration(bindfile);

        System.out.println("[DreamBind]插件交流群: 362221212");

        bag = YamlConfiguration.loadConfiguration(bagfile);
        config = getConfig();

        if (Version >= 8){
            getServer().getPluginManager().registerEvents(new Event1710(),this);
        }
        if (Version >= 9){
            getServer().getPluginManager().registerEvents(new Event194(),this);
        }
        getServer().getPluginManager().registerEvents(new Event(),this);

        BukkitTask task = new me.mcdcs.dreambind.DreamBind.Runnable().runTaskTimer(this,0,12000);
        BukkitTask onInventory = new me.mcdcs.dreambind.DreamBind.onInventory().runTaskTimer(this,0,5);

        Objects.requireNonNull(getCommand("bind")).setExecutor(new Command());

        if (!setupEconomy() ) {
            System.out.println("[DreamBind]未检测到Vault插件,Vault货币失效!");
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public class Command implements CommandExecutor{
        @Override
        public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            switch (label.toLowerCase()) {
                case "bindreload":
                    if (sender.isOp()) {
                        DreamBind pl = DreamBind.getPlugin(DreamBind.class);
                        pl.reloadConfig();
                        config = pl.getConfig();
                        bagfile = new File(pl.getDataFolder(), "bag.yml");
                        if (!bagfile.exists()){
                            saveResource("bag.yml",true);
                            System.out.println("[DreamBind]正在加载bag.yml文件");
                        }
                        bag = YamlConfiguration.loadConfiguration(bagfile);
                        vaultfile = new File(getDataFolder(),"vault.yml");
                        if (!vaultfile.exists()){
                            saveResource("vault.yml",true);
                            System.out.println("[DreamBind]正在加载vault.yml文件");
                        }
                        vault = YamlConfiguration.loadConfiguration(vaultfile);
                        bindfile = new File(getDataFolder(),"bind.yml");
                        if (!bindfile.exists()){
                            saveResource("bind.yml",true);
                            System.out.println("[DreamBind]正在加载bind.yml文件");
                        }
                        bindgui = YamlConfiguration.loadConfiguration(bindfile);
                        sender.sendMessage("§r[§bDreamBind§r] §a插件已经成功重载完毕!");
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有OP才能使用该命令!");
                    }
                    break;
                case "bindtype":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        p.sendMessage(p.getItemInHand().getType().toString());
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有玩家才能进行该命令！");
                    }
                    break;
                case "bindonpickup":
                    if (sender.isOp()) {
                        if (sender instanceof Player) {
                            if (((Player) sender).getItemInHand().getType() != Material.AIR) {
                                if (isBindPickup(((Player) sender).getItemInHand())) {
                                    sender.sendMessage("§r[§bDreamBind§r] §c已经有了该类型的绑定！");
                                } else {
                                    if (isBind(((Player) sender).getItemInHand())) {
                                        sender.sendMessage("§r[§bDreamBind§r] §c已经绑定过的物品无法进行该操作！");
                                    } else {
                                        setBind((Player) sender, "onBindPickup");
                                        sender.sendMessage("§r[§bDreamBind§r] §a成功为手上物品附加拾取绑定！");
                                    }
                                }
                            } else {
                                sender.sendMessage("§r[§bDreamBind§r] §c必须持有物品才能进行该命令！");
                            }
                        } else {
                            sender.sendMessage("§r[§bDreamBind§r] §c只有玩家才能进行该命令！");
                        }
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有OP才能执行该命令！");
                    }
                    break;
                case "bindonuse":
                    if (sender.isOp()) {
                        if (sender instanceof Player) {
                            if (((Player) sender).getItemInHand().getType() != Material.AIR) {
                                if (isBindUse(((Player) sender).getItemInHand())) {
                                    sender.sendMessage("§r[§bDreamBind§r] §c已经有了该类型的绑定！");
                                } else {
                                    if (isBind(((Player) sender).getItemInHand())) {
                                        sender.sendMessage("§r[§bDreamBind§r] §c已经绑定过的物品无法进行该操作！");
                                    } else {
                                        setBind((Player) sender, "onBindUse");
                                        sender.sendMessage("§r[§bDreamBind§r] §a成功为手上物品附加使用绑定！");
                                    }
                                }
                            } else {
                                sender.sendMessage("§r[§bDreamBind§r] §c必须持有物品才能进行该命令！");
                            }
                        } else {
                            sender.sendMessage("§r[§bDreamBind§r] §c只有玩家才能进行该命令！");
                        }
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有OP才能执行该命令！");
                    }
                    break;
                case "bindonequip":
                    if (sender.isOp()) {
                        if (sender instanceof Player) {
                            if (((Player) sender).getItemInHand().getType() != Material.AIR) {
                                if (isBindEquip(((Player) sender).getItemInHand())) {
                                    sender.sendMessage("§r[§bDreamBind§r] §c已经有了该类型的绑定！");
                                } else {
                                    if (isBind(((Player) sender).getItemInHand())) {
                                        sender.sendMessage("§r[§bDreamBind§r] §c已经绑定过的物品无法进行该操作！");
                                    } else {
                                        setBind((Player) sender, "onBindEquip");
                                        sender.sendMessage("§r[§bDreamBind§r] §a成功为手上物品附加装备绑定！");
                                    }
                                }
                            } else {
                                sender.sendMessage("§r[§bDreamBind§r] §c必须持有物品才能进行该命令！");
                            }
                        } else {
                            sender.sendMessage("§r[§bDreamBind§r] §c只有玩家才能进行该命令！");
                        }
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有OP才能执行该命令！");
                    }
                    break;
                case "bindstone":
                    if (sender.isOp()) {
                        if (args.length >= 3) {
                            if (number(args[0])) {
                                if (number(args[2])) {
                                    if (Bukkit.getPlayer(args[1]) == null) {
                                        sender.sendMessage("§r[§bDreamBind§r] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                                    } else {
                                        sender.sendMessage("§r[§bDreamBind§r] §a已经成功将绑定石给予对应玩家!");
                                        Objects.requireNonNull(Bukkit.getPlayer(args[1])).getInventory().addItem(getStone(Integer.parseInt(args[2]), Integer.parseInt(args[0])));
                                        if (args[0].equals("1")) {
                                            Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage("§r[§bDreamBind§r] §a你收到了一些绑定石");
                                        } else if (args[0].equals("2")) {
                                            Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage("§r[§bDreamBind§r] §a你收到了一些解绑石");
                                        } else {
                                            Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage("§r[§bDreamBind§r] §a你收到了一些死亡不掉落石");
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§r[§bDreamBind§r] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                                }
                            } else {
                                sender.sendMessage("§r[§bDreamBind§r] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                            }
                        } else {
                            sender.sendMessage("§r[§bDreamBind§r] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                        }
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有OP才能使用该命令!");
                    }
                    break;
                default:
                    if (sender instanceof Player) {
                        switch (label.toLowerCase()) {
                            case "bind":
                                if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand", "DreamBind.use")))) {
                                    if (setBind((Player) sender)) {
                                        sender.sendMessage("§r[§bDreamBind§r] §a手上物品成功被绑定!");
                                    } else {
                                        sender.sendMessage("§r[§bDreamBind§r] §a手上物品无法被绑定!");
                                    }
                                } else {
                                    sender.sendMessage("§r[§bDreamBind§r] §c你没有权限使用该命令!");
                                }
                                break;
                            case "bindbag":
                                if (bag.getConfigurationSection(sender.getName()) != null) {
                                    int i = Objects.requireNonNull(bag.getConfigurationSection(sender.getName())).getKeys(true).size();
                                    if (i > 0) {
                                        sender.sendMessage("§r[§bDreamBind§r] §a已经为你自动打开绑定箱,请拿走绑定箱内物品!");
                                        openBag((Player) sender);
                                    } else {
                                        sender.sendMessage("§r[§bDreamBind§r] §a你并未存在任何物品在绑定箱内!");
                                    }
                                } else {
                                    sender.sendMessage("§r[§bDreamBind§r] §a你并未存在任何物品在绑定箱内!");
                                }
                                break;
                            case "unbind":
                                if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand", "DreamBind.use")))) {
                                    if (unBind((Player) sender)) {
                                        sender.sendMessage("§r[§bDreamBind§r] §a手上物品成功解除绑定!");
                                    } else {
                                        sender.sendMessage("§r[§bDreamBind§r] §a手上物品无法被解除绑定!");
                                    }
                                } else {
                                    sender.sendMessage("§r[§bDreamBind§r] §c你没有权限使用该命令!");
                                }
                                break;
                        }
                    } else {
                        sender.sendMessage("§r[§bDreamBind§r] §c只有玩家才能执行该命令！");
                    }
                    break;
            }
            return false;
        }
    }

    public static class onInventory extends BukkitRunnable{
        @Override
        public void run() {
            if (config.getBoolean("onServer.onStart")){
                onTime++;
                if (onTime/4 == config.getInt("onServer.onTime")){
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
                ItemStack itemStack;
                if (p.getEquipment() != null){
                    if (p.getEquipment().getLeggings() != null){
                        itemStack = p.getEquipment().getLeggings();
                        if (itemStack.getType() != Material.AIR){
                            if (isBindEquip(itemStack)){
                                setBind(p,itemStack);
                                p.getEquipment().setLeggings(itemStack);
                            }
                        }
                    }
                    if (p.getEquipment().getChestplate() != null){
                        itemStack = p.getEquipment().getChestplate();
                        if (itemStack.getType() != Material.AIR){
                            if (isBindEquip(itemStack)){
                                setBind(p,itemStack);
                                p.getEquipment().setChestplate(itemStack);
                            }
                        }
                    }
                    if (p.getEquipment().getBoots() != null){
                        itemStack = p.getEquipment().getBoots();
                        if (itemStack.getType() != Material.AIR){
                            if (isBindEquip(itemStack)){
                                setBind(p,itemStack);
                                p.getEquipment().setBoots(itemStack);
                            }
                        }
                    }
                    if (p.getEquipment().getHelmet() != null){
                        itemStack = p.getEquipment().getHelmet();
                        if (itemStack.getType() != Material.AIR){
                            if (isBindEquip(itemStack)){
                                setBind(p,itemStack);
                                p.getEquipment().setHelmet(itemStack);
                            }
                        }
                    }
                    if (Version > 8){
                        itemStack = p.getEquipment().getItemInOffHand();
                        if (itemStack.getType() != Material.AIR){
                            if (isBindEquip(itemStack)){
                                setBind(p,itemStack);
                                p.getEquipment().setItemInOffHand(itemStack);
                            }
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

    public static class Runnable extends BukkitRunnable{
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
                        p.sendMessage("§r[§bDreamBind§r] §a你的绑定箱内存在物品,请输入§d/bindbag §a收入背包!");
                    }
                }
            }
        }
    }

    public static class Event194 implements Listener{
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

    public static class Event1710 implements Listener{
        @EventHandler(priority = EventPriority.MONITOR)
        public void onArmor(PlayerArmorStandManipulateEvent e){
            if (e.getPlayerItem().getType() != Material.AIR){
                if (isBind(e.getPlayerItem())){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§r[§bDreamBind§r] §a被绑定的物品无法放置在盔甲架上!");
                }
            }
        }
    }

    public static class Event implements Listener{
        @EventHandler(priority = EventPriority.MONITOR)
        public void onEntity(PlayerInteractEntityEvent e){
            if (config.getBoolean("onEntity.onStart")){
                if (e.getPlayer().getItemInHand().getType() != Material.AIR){
                    if (config.getStringList("onEntity.onList").contains(e.getPlayer().getItemInHand().getType().toString())){
                        e.setCancelled(true);
                        e.getPlayer().sendMessage("§r[§bDreamBind§r] §c该绑定物品禁止右键实体！");
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
                            e.getPlayer().sendMessage("§r[§bDreamBind§r] §c手持绑定物品禁止执行该命令!");
                        }
                    }
                }
            }
            if (e.getMessage().replace("/","").length() >= Objects.requireNonNull(bindgui.getString("onInfo.command")).length()){
                if (e.getMessage().replace("/","").substring(0, Objects.requireNonNull(bindgui.getString("onInfo.command")).length()).toUpperCase().equals(Objects.requireNonNull(bindgui.getString("onInfo.command")).toUpperCase())){
                    e.setCancelled(true);
                    if (e.getPlayer().getItemInHand().getType() != Material.AIR){
                        Gui(e.getPlayer());
                        e.getPlayer().sendMessage("§r[§bDreamBind§r] §a已经为你打开绑定菜单");
                    }else {
                        e.getPlayer().sendMessage("§r[§bDreamBind§r] §c必须手持物品才能打开绑定菜单！");
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
            if (config.getConfigurationSection(e.getPlayer().getName()) != null){
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        e.getPlayer().sendMessage("§r[§bDreamBind§r] §a你的绑定箱内存在物品,请输入§d/bindbag §a收入背包!");
                    }
                },100);
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
                        e.getEntity().sendMessage("§r[§bDreamBind§r] §a你绑定的物品由于死亡掉落放置在了绑定箱内!");
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
                        e.getPlayer().sendMessage("§r[§bDreamBind§r] §a你所拾取的物品已经被绑定,已经将其归还为所有者!");
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
                                e.getPlayer().sendMessage("§r[§bDreamBind§r] §a禁止丢弃已经绑定过的物品!");
                            }
                        } else {
                            if (hasPlayer(getOwner(itemStack))) {
                                DropItemList.add(e.getItemDrop());
                                addItem(itemStack, getOwner(itemStack));
                                e.getItemDrop().remove();
                                e.getPlayer().sendMessage("§r[§bDreamBind§r] §a你所点击的物品已经被绑定,已经将其归还为所有者!");
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
                                        p.sendMessage("§r[§bDreamBind§r] §c绑定物品禁止放入该物品栏内!");
                                    }
                                }else {
                                    for (String s : config.getStringList("onBlurred")){
                                        if (p.getOpenInventory().getTitle().contains(s)){
                                            if (isBind(itemStack)){
                                                e.setCancelled(true);
                                                p.sendMessage("§r[§bDreamBind§r] §c绑定物品禁止放入该物品栏内!");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (isBind(itemStack)){
                            if (!isOwner(itemStack,p)){
                                if (p.isOp()){
                                    p.sendMessage("§r[§bDreamBind§r] §a你正在不受限制的点击绑定物品!");
                                }else {
                                    if (e.getClickedInventory() == p.getInventory()){
                                        if (hasPlayer(getOwner(itemStack))){
                                            addItem(itemStack,getOwner(itemStack));
                                            e.setCurrentItem(new ItemStack(Material.AIR));
                                            p.sendMessage("§r[§bDreamBind§r] §a你所点击的物品已经被绑定,已经将其归还为所有者!");
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
                                                    p.sendMessage("§r[§bDreamBind§r] §c成功为该物品进行解绑!");
                                                }else {
                                                    p.sendMessage("§r[§bDreamBind§r] §c解绑石只能叠加一个进行装备绑定!");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else {
                            if (isBindUse(itemStack)){
                                setBind(p,itemStack);
                                e.setCurrentItem(itemStack);
                            }
                            if (config.getBoolean("onBindStone.onStart")){
                                if (e.getCursor() != null){
                                    if (e.getCursor().getType() != Material.AIR){
                                        ItemStack cursor = e.getCursor();
                                        if (isStone(cursor)){
                                            if (cursor.getAmount() == 1){
                                                if (!config.getBoolean("onBindStone.onBind.onType") | config.getStringList("onBindStone.onBind.onBindType").contains(itemStack.getType().toString())){
                                                    e.setCursor(new ItemStack(Material.AIR));
                                                    setBind(itemStack,p);
                                                    e.setCurrentItem(itemStack);
                                                    e.setCancelled(true);
                                                    p.sendMessage("§r[§bDreamBind§r] §c成功为该物品进行绑定!");
                                                }else {
                                                    p.sendMessage("§r[§bDreamBind§r] §c该物品无法被进行绑定!");
                                                }
                                            }else {
                                                p.sendMessage("§r[§bDreamBind§r] §c绑定石只能叠加一个进行装备绑定!");
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
                                            p.sendMessage("§r[§bDreamBind§r] §c成功为该物品进行死亡不掉落绑定!");
                                        }else {
                                            p.sendMessage("§r[§bDreamBind§r] §c死亡不掉落只能叠加一个进行装备绑定!");
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
            boolean b = false;
            Inventory inv = null;
            BlockState state = e.getBlock().getState();
            if (state instanceof Chest) {
                Chest chest = (Chest) state;
                inv = chest.getInventory();
            }
            if (state instanceof DoubleChest){
                DoubleChest doubleChest = (DoubleChest) state;
                inv = doubleChest.getInventory();
            }
            if (state instanceof Furnace){
                Furnace furnace = (Furnace) state;
                inv = furnace.getInventory();
            }
            if (state instanceof Dropper){
                Dropper dropper = (Dropper) state;
                inv = dropper.getInventory();
            }
            if (state instanceof Dispenser){
                Dispenser dispenser = (Dispenser) state;
                inv = dispenser.getInventory();
            }
            if (state instanceof Hopper){
                Hopper hopper = (Hopper) state;
                inv = hopper.getInventory();
            }
            if (state instanceof BrewingStand){
                BrewingStand brewingStand = (BrewingStand) state;
                inv = brewingStand.getInventory();
            }
            if (inv != null){
                for (ItemStack itemStack : inv){
                    if (itemStack != null){
                        if (isBind(itemStack)){
                            b = true;
                        }
                    }
                }
            }
            if (b){
                e.setCancelled(true);
                e.getPlayer().sendMessage("§r[§bDreamBind§r] §c该方块内含有绑定物品,禁止被破坏!");
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

    private static void addItem(ItemStack is,String s){
        Player p = Bukkit.getPlayer(s);
        int i = 0;
        if (p == null){
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()){
                if (Objects.equals(player.getName(), s)){
                    i = 1;
                }
            }
        }
        if (i == 1){
            bag.set(s + "." + onBag(s),is);
            try {
                bag.save(bagfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            if (p != null){
                addItem(is, p);
            }
        }
    }

    private static void addBag(ItemStack is,Player p){
        bag.set(p.getName() + "." + onBag(p),is);
        try {
            bag.save(bagfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addItem(ItemStack is,Player p){
        if (p.getInventory().firstEmpty() == -1){
            if (p.getEnderChest().firstEmpty() == -1){
                bag.set(p.getName() + "." + onBag(p),is);
                try {
                    bag.save(bagfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                p.sendMessage("§r[§bDreamBind§r] §a您的绑定物品由于某种原因归还至绑定箱！");
            }else {
                p.getEnderChest().addItem(is);
                p.sendMessage("§r[§bDreamBind§r] §a您的绑定物品由于某种原因归还至末影箱！");
            }
        }else {
            p.getInventory().addItem(is);
            p.sendMessage("§r[§bDreamBind§r] §a您的绑定物品由于某种原因归还至背包！");
        }
    }

    private static boolean isKeep(ItemStack is){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasLore()){
                    for (String s : Objects.requireNonNull(is.getItemMeta().getLore())){
                        if (config.getStringList("onKeepLore").contains(s)){
                            b = true;
                        }
                    }
                }
            }
        }
        return b;
    }

    private static boolean isBindUse(ItemStack is){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasLore()){
                    for (String s : Objects.requireNonNull(is.getItemMeta().getLore())){
                        if (config.getStringList("onBindUse").contains(s)){
                            b = true;
                        }
                    }
                }
            }
        }
        return b;
    }

    private static boolean isBindEquip(ItemStack is){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasLore()){
                    for (String s : Objects.requireNonNull(is.getItemMeta().getLore())){
                        if (config.getStringList("onBindEquip").contains(s)){
                            b = true;
                        }
                    }
                }
            }
        }
        return b;
    }

    private static boolean isBindPickup(ItemStack is){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasLore()){
                    for (String s : Objects.requireNonNull(is.getItemMeta().getLore())){
                        if (config.getStringList("onBindPickup").contains(s)){
                            b = true;
                        }
                    }
                }
            }
        }
        return b;
    }

    private static boolean isBind(ItemStack is){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasLore()){
                    for (String s : Objects.requireNonNull(is.getItemMeta().getLore())){
                        if (config.getStringList("onBindLore").contains(s)){
                            b = true;
                        }
                        for (String w : config.getStringList("onBindName")){
                            if (s.contains(w)){
                                if (s.substring(0,w.length()).equals(w)){
                                    b = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return b;
    }

    private static boolean isOwner(ItemStack is, Player p){
        boolean b = false;
        if (isBind(is)){
            for (String s : Objects.requireNonNull(is.getItemMeta().getLore())){
                if (s.equals(p.getName())){
                    b = true;
                }else if (s.contains(p.getName())){
                    for (String w : config.getStringList("onBindName")){
                        if (s.contains(w)){
                            if (s.substring(0,w.length()).equals(w)){
                                if (s.replace(w,"").equals(p.getName())){
                                    b = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return b;
    }

    private static String getOwner(ItemStack is){
        String s = "wu";
        if (isBind(is)){
            int i = 0;
            ArrayList<String> il = (ArrayList<String>) is.getItemMeta().getLore();
            assert il != null;
            for (String wb : config.getStringList("onBindLore")){
                if (il.indexOf(wb) >= 0){
                    i = il.indexOf(wb);
                }
            }
            if (i <= il.size() - 2){
                s = il.get(i + 1);
            }
            if (!s.equals("wu")){
                for (String b : il){
                    for (String w : config.getStringList("onBindName")){
                        if (b.contains(w)){
                            if (b.substring(0,w.length()).equals(w)){
                                if (hasPlayer(b.replace(w,""))){
                                    s = b.replace(w,"");
                                }
                            }
                        }
                    }
                }
            }
        }
        return s;
    }

    public static boolean setKeep(ItemStack is,Player p){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (!isKeep(is)){
                ArrayList<String> il = new ArrayList<>();
                if (is.hasItemMeta()){
                    if (is.getItemMeta().hasLore()){
                        il = (ArrayList<String>) is.getItemMeta().getLore();
                    }
                }
                assert il != null;
                il.add(config.getStringList("onKeepLore").get(0));
                b = true;
                ItemMeta im = is.getItemMeta();
                im.setLore(il);
                is.setItemMeta(im);
            }
        }
        return b;
    }

    public static boolean setBind(ItemStack is,Player p){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (!isBind(is)){
                ArrayList<String> il = new ArrayList<>();
                if (is.hasItemMeta()){
                    if (is.getItemMeta().hasLore()){
                        il = (ArrayList<String>) is.getItemMeta().getLore();
                    }
                }
                assert il != null;
                il.add(config.getStringList("onBindLore").get(0));
                il.add(p.getName());
                b = true;
                ItemMeta im = is.getItemMeta();
                im.setLore(il);
                is.setItemMeta(im);
            }
        }
        return b;
    }

    public static boolean setBind(Player p){
        boolean b = false;
        ItemStack itemStack = p.getItemInHand();
        if (itemStack.getType() != Material.AIR){
            if (!isBind(itemStack)){
                ArrayList<String> il = new ArrayList<>();
                if (itemStack.hasItemMeta()){
                    if (itemStack.getItemMeta().hasLore()){
                        il = (ArrayList<String>) itemStack.getItemMeta().getLore();
                    }
                }
                assert il != null;
                il.add(config.getStringList("onBindLore").get(0));
                il.add(p.getName());
                b = true;
                ItemMeta im = itemStack.getItemMeta();
                im.setLore(il);
                itemStack.setItemMeta(im);
                p.setItemInHand(itemStack);
            }
        }
        return b;
    }

    public static void delBind(ArrayList<String> list){
        for (String s : config.getStringList("onBindPickup")){
            list.remove(s);
        }
        for (String s : config.getStringList("onBindUse")){
            list.remove(s);
        }
        for (String s : config.getStringList("onBindEquip")){
            list.remove(s);
        }
    }

    public static boolean setBind(Player p,ItemStack is){
        boolean b = false;
        if (is.getType() != Material.AIR){
            if (!isBind(is)){
                ArrayList<String> il = new ArrayList<>();
                if (is.hasItemMeta()){
                    if (is.getItemMeta().hasLore()){
                        il = (ArrayList<String>) is.getItemMeta().getLore();
                    }
                }
                assert il != null;
                delBind(il);
                il.add(config.getStringList("onBindLore").get(0));
                il.add(p.getName());
                b = true;
                ItemMeta im = is.getItemMeta();
                im.setLore(il);
                is.setItemMeta(im);
            }
        }
        return b;
    }

    public static void setBind(Player p,String s){
        ItemStack itemStack = p.getItemInHand();
        ArrayList<String> il = new ArrayList<>();
        if (itemStack.hasItemMeta()){
            if (itemStack.getItemMeta().hasLore()){
                il = (ArrayList<String>) itemStack.getItemMeta().getLore();
            }
        }
        assert il != null;
        il.add(config.getStringList(s).get(0));
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(il);
        itemStack.setItemMeta(im);
        p.setItemInHand(itemStack);
    }

    public static void unBind(ItemStack itemStack,Player p){
        if (itemStack.getType() != Material.AIR){
            if (isBind(itemStack)){
                ItemMeta im = itemStack.getItemMeta();
                ArrayList<String> il = new ArrayList<>();
                if (im.getLore() != null){
                    if (p.isOp()){
                        for (String s : im.getLore()){
                            if (config.getStringList("onBindLore").contains(s)){
                            }else if (s.equals(getOwner(itemStack))){
                            }else {
                                il.add(s);
                            }
                        }
                    }else {
                        if (isOwner(itemStack,p)){
                            for (String s : im.getLore()){
                                if (config.getStringList("onBindLore").contains(s)){
                                }else if (s.equals(getOwner(itemStack))){
                                }else {
                                    il.add(s);
                                }
                            }
                        }
                    }
                }
                im.setLore(il);
                itemStack.setItemMeta(im);
            }
        }
    }

    public static boolean unBind(Player p){
        boolean b = false;
        ItemStack itemStack = p.getItemInHand();
        if (itemStack.getType() != Material.AIR){
            if (isBind(itemStack)){
                ItemMeta im = itemStack.getItemMeta();
                ArrayList<String> il = new ArrayList<>();
                if (im.getLore() != null){
                    if (p.isOp()){
                        for (String s : im.getLore()){
                            if (config.getStringList("onBindLore").contains(s)){
                                b = true;
                            }else if (s.equals(getOwner(itemStack))){
                                b = true;
                            }else {
                                il.add(s);
                            }
                        }
                    }else {
                        if (isOwner(itemStack,p)){
                            for (String s : im.getLore()){
                                if (config.getStringList("onBindLore").contains(s)){
                                    b = true;
                                }else if (s.equals(getOwner(itemStack))){
                                    b = true;
                                }else {
                                    il.add(s);
                                }
                            }
                        }
                    }
                }
                im.setLore(il);
                itemStack.setItemMeta(im);
                p.setItemInHand(itemStack);
            }
        }
        return b;
    }

    private static String onBag(Player p){
        ArrayList<String> il = new ArrayList<>();
        il.add("a");
        il.add("b");
        il.add("c");
        il.add("d");
        String s = il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3))  + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3));
        if (bag.getConfigurationSection(p.getName()) == null){
            return s;
        }
        if (Objects.requireNonNull(bag.getConfigurationSection(p.getName())).getKeys(true).contains(s)){
            return onBag(p);
        }else {
            return s;
        }
    }

    private static String onBag(String s){
        ArrayList<String> il = new ArrayList<>();
        il.add("e");
        il.add("f");
        il.add("g");
        il.add("h");
        String wb = il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3))  + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3)) + il.get(random(0,3));
        if (bag.getConfigurationSection(s) == null){
            return wb;
        }
        if (Objects.requireNonNull(bag.getConfigurationSection(s)).getKeys(true).contains(wb)){
            return onBag(s);
        }else {
            return wb;
        }
    }

    private static void openBag(Player p){
        Inventory inv = Bukkit.createInventory(p,27,"§bDreamBind §r- §d绑定箱");

        int i = 0;
        for (String s : Objects.requireNonNull(bag.getConfigurationSection(p.getName())).getKeys(true)){
            inv.addItem(bag.getItemStack(p.getName() + "." + s));
            bag.set(p.getName() + "." + s,null);
            i++;
            if (i == 27){
                break;
            }
        }

        try {
            bag.save(bagfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        p.openInventory(inv);
    }

    public static boolean hasPlayer(String s){
        boolean b = false;
        if (Bukkit.getPlayer(s) == null){
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()){
                if (Objects.equals(p.getName(), s)){
                    b = true;
                }
            }
        }else {
            b = true;
        }
        return b;
    }

    public static boolean noStone(ItemStack is){
        boolean b = false;
        if (is.getType().toString().equals(Objects.requireNonNull(config.getString("onBindStone.unItem.type", "PAPER")).toUpperCase())){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasDisplayName() & is.getItemMeta().hasLore()){
                    if (is.getItemMeta().getDisplayName().equals(config.getString("onBindStone.unItem.name"))){
                        if (Objects.requireNonNull(is.getItemMeta().getLore()).size() == config.getStringList("onBindStone.unItem.lore").size()){
                            boolean z = true;
                            int i = 0;
                            for (String s : config.getStringList("onBindStone.unItem.lore")){
                                if (!s.equals(Objects.requireNonNull(is.getItemMeta().getLore()).get(i))){
                                    z = false;
                                }
                                i++;
                            }
                            b = z;
                        }
                    }
                }
            }
        }
        return b;
    }

    public static boolean noKeep(ItemStack is){
        boolean b = false;
        if (is.getType().toString().equals(Objects.requireNonNull(config.getString("onKeepStone.type", "PAPER")).toUpperCase())){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasDisplayName() & is.getItemMeta().hasLore()){
                    if (is.getItemMeta().getDisplayName().equals(config.getString("onKeepStone.name"))){
                        if (Objects.requireNonNull(is.getItemMeta().getLore()).size() == config.getStringList("onKeepStone.lore").size()){
                            boolean z = true;
                            int i = 0;
                            for (String s : config.getStringList("onKeepStone.lore")){
                                if (!s.equals(Objects.requireNonNull(is.getItemMeta().getLore()).get(i))){
                                    z = false;
                                }
                                i++;
                            }
                            b = z;
                        }
                    }
                }
            }
        }
        return b;
    }

    public static boolean isStone(ItemStack is){
        boolean b = false;
        if (is.getType().toString().equals(Objects.requireNonNull(config.getString("onBindStone.onItem.type", "PAPER")).toUpperCase())){
            if (is.hasItemMeta()){
                if (is.getItemMeta().hasDisplayName() & is.getItemMeta().hasLore()){
                    if (is.getItemMeta().getDisplayName().equals(config.getString("onBindStone.onItem.name"))){
                        if (Objects.requireNonNull(is.getItemMeta().getLore()).size() == config.getStringList("onBindStone.onItem.lore").size()){
                            boolean z = true;
                            int i = 0;
                            for (String s : config.getStringList("onBindStone.onItem.lore")){
                                if (!s.equals(Objects.requireNonNull(is.getItemMeta().getLore()).get(i))){
                                    z = false;
                                }
                                i++;
                            }
                            b = z;
                        }
                    }
                }
            }
        }
        return b;
    }

    public static ItemStack getStone(int i,int b){
        ItemStack is;
        if (b == 1){
            is = new ItemStack(Material.valueOf(Objects.requireNonNull(config.getString("onBindStone.onItem.type")).toUpperCase()));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(config.getString("onBindStone.onItem.name"));
            im.setLore(config.getStringList("onBindStone.onItem.lore"));
            if (!DreamBind.getPlugin(DreamBind.class).getServer().getVersion().contains("1.16")){
                is.setDurability((short) config.getInt("onBindStone.onItem.date"));
            }
            is.setItemMeta(im);
        }else if (b == 2){
            is = new ItemStack(Material.valueOf(Objects.requireNonNull(config.getString("onBindStone.unItem.type")).toUpperCase()));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(config.getString("onBindStone.unItem.name"));
            im.setLore(config.getStringList("onBindStone.unItem.lore"));
            if (!DreamBind.getPlugin(DreamBind.class).getServer().getVersion().contains("1.16")){
                is.setDurability((short) config.getInt("onBindStone.unItem.date"));
            }
            is.setItemMeta(im);
        }else {
            is = new ItemStack(Material.valueOf(Objects.requireNonNull(config.getString("onKeepStone.type")).toUpperCase()));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(config.getString("onKeepStone.name"));
            im.setLore(config.getStringList("onKeepStone.lore"));
            if (!DreamBind.getPlugin(DreamBind.class).getServer().getVersion().contains("1.16")){
                is.setDurability((short) config.getInt("onKeepStone.date"));
            }
            is.setItemMeta(im);
        }
        is.setAmount(i);
        return is;
    }

    private static void Gui(Player p){
        Inventory inv = Bukkit.createInventory(p,bindgui.getInt("onInfo.size") * 9, Objects.requireNonNull(bindgui.getString("onInfo.name")));

        if (bindgui.getString("onInfo.music") != null){
            p.playSound(p.getLocation(),Sound.valueOf(Objects.requireNonNull(bindgui.getString("onInfo.music")).toUpperCase()),50,50);
        }
        for (String s : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu")).getKeys(false)){
            ItemStack is = new ItemStack(Material.valueOf(bindgui.getString("onMenu." + s + ".type")));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(bindgui.getString("onMenu." + s + ".name"));
            im.setLore(bindgui.getStringList("onMenu." + s + ".lore"));
            if (Version < 16){
                is.setDurability((short) bindgui.getInt("onMenu." + s + ".date"));
            }
            is.setItemMeta(im);
            for (Integer i : bindgui.getIntegerList("onMenu." + s + ".slots")){
                inv.setItem(i - 1,is);
            }
        }

        p.openInventory(inv);
    }

    private static boolean number(String number) {
        if(number==null) return false;
        return number.matches("[+-]?[1-9]+[0-9]*(\\.[0-9]+)?");
    }

    private static int random(int i, int i1) {
        if (i > i1) {
            int i2 = i;
            i = i1;
            i1 = i2;
        }
        return (int) Math.floor(Math.random() * (i1 - i + 1) + i);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }


    public static Economy getEconomy() {
        return econ;
    }

    public static void BindGui(Player p,int slot){
        slot++;
        for (String s : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu")).getKeys(false)){
            if (bindgui.getIntegerList("onMenu." + s + ".slots").contains(slot)){
                if (bindgui.getString("onMenu." + s + ".bind", null) != null){
                    if (bindgui.getConfigurationSection("onMenu." + s + ".money") != null){
                        p.closeInventory();
                        boolean b = true;
                        for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                            if (DreamBind.getPlugin(DreamBind.class).getMoney(v,p) < bindgui.getDouble("onMenu." + s + ".money." + v)){
                                b = false;
                            }
                        }
                        if (b){
                            if (Objects.requireNonNull(bindgui.getString("onMenu." + s + ".bind")).toUpperCase().equals("TRUE")){
                                if (isBind(p.getItemInHand())){
                                    p.sendMessage("§r[§bDreamBind§r] §c你手上的物品已经被绑定过了!");
                                }else {
                                    setBind(p);
                                    p.sendMessage("§r[§bDreamBind§r] §a手上物品绑定成功!");
                                    for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                        DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                    }
                                }
                            }else {
                                if (isBind(p.getItemInHand())){
                                    unBind(p);
                                    p.sendMessage("§r[§bDreamBind§r] §a手上物品解绑成功!");
                                    for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                        DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                    }
                                }else {
                                    p.sendMessage("§r[§bDreamBind§r] §c你手上的物品还没有绑定过!");
                                }
                            }
                        }else {
                            p.sendMessage("§r[§bDreamBind§r] §c你的钱不足以进行该操作！");
                        }
                    }else {
                        if (Objects.requireNonNull(bindgui.getString("onMenu." + s + ".bind")).toUpperCase().equals("TRUE")){
                            if (isBind(p.getItemInHand())){
                                p.sendMessage("§r[§bDreamBind§r] §c你手上的物品已经被绑定过了!");
                            }else {
                                setBind(p);
                                p.sendMessage("§r[§bDreamBind§r] §a手上物品绑定成功!");
                                for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                    DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                }
                            }
                        }else {
                            if (isBind(p.getItemInHand())){
                                unBind(p);
                                p.sendMessage("§r[§bDreamBind§r] §a手上物品解绑成功!");
                                for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                    DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                }
                            }else {
                                p.sendMessage("§r[§bDreamBind§r] §c你手上的物品还没有绑定过!");
                            }
                        }
                    }
                }
            }
        }
    }

    public double getMoney(String s, Player p){
        double i = 0;
        if (Objects.requireNonNull(vault.getString(s + ".type")).toUpperCase().equals("VAULT")){
            if (getServer().getPluginManager().getPlugin("Vault") != null){
                if (getServer().getServicesManager().getRegistration(Economy.class) != null){
                    i = getVault(p);
                }
            }
        }else {
            i = getCustom(p,vault.getString( s + ".get"),vault.getString(s + ".folder"));
        }
        return i;
    }

    public void takeMoney(String s, Player p, double d){
        if (Objects.requireNonNull(vault.getString(s + ".type")).toUpperCase().equals("VAULT")){
            if (getServer().getPluginManager().getPlugin("Vault") != null){
                if (getServer().getServicesManager().getRegistration(Economy.class) != null){
                    takeVault(p, d);
                }
            }
        }else {
            takeCustom(s, p, vault.getString(s + ".get"), vault.getString(s + ".folder"), d);
        }
    }

    public static double getVault(Player p){
        return getEconomy().getBalance(p);
    }

    public double getCustom(Player p, String l, String f){
        File file = new File(getDataFolder().getPath().replace("DreamBind",f));
        if (file.exists()){
            return YamlConfiguration.loadConfiguration(file).getDouble(l.replace("name",p.getName()).replace("UUID",p.getUniqueId().toString()));
        }else {
            return 0;
        }
    }

    public static void takeVault(Player p, double d){
        if (getVault(p) >= d){
            getEconomy().withdrawPlayer(p,d);
        }
    }

    public void takeCustom(String s, Player p, String l, String f, double d){
        if (getCustom(p,l,f) >= d){
            File file = new File(getDataFolder().getAbsolutePath().replace("DreamBind",f));
            FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
            fc.set(l.replace("name",p.getName()).replace("UUID",p.getUniqueId().toString()),getCustom(p,l,f) - d);
            Plugin plugin = getServer().getPluginManager().getPlugin(f.substring(0, f.indexOf("/")));
            try {
                fc.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (plugin != null){
                getServer().getPluginManager().disablePlugin(plugin);
                getServer().getPluginManager().enablePlugin(plugin);
            }
        }
    }
}
