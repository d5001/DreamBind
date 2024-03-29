package me.mcdcs.dreambind;

import me.mcdcs.dreambind.Api.DItem;
import me.mcdcs.dreambind.Cmd.*;
import me.mcdcs.dreambind.Listener.Event;
import me.mcdcs.dreambind.Listener.Event1710;
import me.mcdcs.dreambind.Listener.Event194;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class DreamBind extends JavaPlugin {

    public static FileConfiguration vault;
    public static FileConfiguration bindgui;
    public static File bindfile;
    public static File vaultfile;
    public static FileConfiguration config;
    public static FileConfiguration bag;
    public static File bagfile;
    public static ArrayList<Item> DropItemList = new ArrayList<>();
    public static ArrayList<ItemStack> DeathItemList = new ArrayList<>();
    public static ArrayList<Player> OpenList = new ArrayList<>();
    public static int onTime = 0;
    public static Economy econ = null;
    public static PlayerPoints playerPoints = null;
    public static int Version;
    public static ArrayList<String> sound = new ArrayList<>();
    public static String color = "";

    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        String ver = Bukkit.getVersion();
        ver = ver.substring(ver.indexOf(".") + 1,ver.length() - 1);
        if (ver.contains(" ")){
            ver = ver.substring(0,ver.indexOf(" "));
        }
        if (ver.contains(".")){
            ver = ver.substring(0,ver.indexOf("."));
        }
        if (ver.contains("-")){
            ver = ver.substring(0,ver.indexOf("-"));
        }
        Version = Integer.parseInt(ver);

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
        color = config.getString("bindColor");
        if (color != null){
            color = color.replace("&","§");
            if (!tihuan(color).equals("")){
                color = "";
                System.out.println("[DreamBind]颜色符号加载错误！");
            }else {
                System.out.println("[DreamBind]颜色符号成功加载完毕！");
            }
        }

        if (Version >= 8){
            getServer().getPluginManager().registerEvents(new Event1710(),this);
        }
        if (Version >= 9){
            getServer().getPluginManager().registerEvents(new Event194(),this);
        }
        getServer().getPluginManager().registerEvents(new Event(),this);

        BukkitTask task = new me.mcdcs.dreambind.Runnable.Runnable().runTaskTimer(this,0,12000);
        BukkitTask onInventory = new me.mcdcs.dreambind.Runnable.onInventory().runTaskTimer(this,0,10);

        Objects.requireNonNull(getCommand("bind")).setExecutor(new Bind());
        Objects.requireNonNull(getCommand("unbind")).setExecutor(new unBind());
        Objects.requireNonNull(getCommand("bindreload")).setExecutor(new bindReload());
        Objects.requireNonNull(getCommand("bindbag")).setExecutor(new bindBag());
        Objects.requireNonNull(getCommand("bindall")).setExecutor(new bindAll());
        Objects.requireNonNull(getCommand("bindonequip")).setExecutor(new bindOnEquip());
        Objects.requireNonNull(getCommand("bindonpickup")).setExecutor(new bindOnPick());
        Objects.requireNonNull(getCommand("bindonget")).setExecutor(new bindOnGet());
        Objects.requireNonNull(getCommand("bindtype")).setExecutor(new bindType());
        Objects.requireNonNull(getCommand("bindonuse")).setExecutor(new bindOnUse());
        Objects.requireNonNull(getCommand("bindstone")).setExecutor(new bindStone());
        Objects.requireNonNull(getCommand("unbindall")).setExecutor(new unBindAll());
        Objects.requireNonNull(getCommand("bindondeath")).setExecutor(new bindOnDeath());

        if (!setupEconomy()) {
            System.out.println("[DreamBind]未检测到Vault插件,Vault货币失效!");
        }
        if (!loadPlayerPoints()){
            System.out.println("[DreamBind]未检测到PlayerPoints插件,PlayerPoints货币失效!");
        }

        for (Sound s : Sound.values()){
            sound.add(s.name());
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void addItem(ItemStack is,String s){
        Player p = Bukkit.getPlayer(s);
        int i = 0;
        if (p == null){
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()){
                if (Objects.equals(player.getName(), s)){
                    i = 1;
                    break;
                }else if (player.getUniqueId().toString().equals(s)){
                    i = 1;
                    s = player.getName();
                    break;
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

    public static void addBag(ItemStack is,Player p){
        bag.set(p.getName() + "." + onBag(p),is);
        try {
            bag.save(bagfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addItem(ItemStack is,Player p){
        if (p.getInventory().firstEmpty() == -1){
            if (p.getEnderChest().firstEmpty() == -1){
                bag.set(p.getName() + "." + onBag(p),is);
                try {
                    bag.save(bagfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                p.getEnderChest().addItem(is);
            }
        }else {
            p.getInventory().addItem(is);
            p.sendMessage("§f[§bDreamBind§f] §a您的绑定物品由于某种原因归还至背包！");
        }
    }

    public static String tihuan(String s){
        for (String z : colorlist()){
            s = s.replace(z,"");
        }
        return s;
    }

    public static ArrayList<String> colorlist(){
        ArrayList<String> il = new ArrayList<>();
        il.add("§0");
        il.add("§1");
        il.add("§2");
        il.add("§3");
        il.add("§4");
        il.add("§5");
        il.add("§6");
        il.add("§7");
        il.add("§8");
        il.add("§9");
        il.add("§a");
        il.add("§b");
        il.add("§c");
        il.add("§d");
        il.add("§e");
        il.add("§f");
        il.add("§r");
        il.add("§l");
        il.add("§n");
        il.add("§m");
        il.add(" ");
        il.addAll(config.getStringList("moreColor"));
        return il;
    }

    public static String onBag(Player p){
        return onBag(p.getName());
    }

    public static String onBag(String s){
        ArrayList<String> il = new ArrayList<>();
        il.add("e");
        il.add("f");
        il.add("g");
        il.add("h");
        il.add("a");
        il.add("b");
        il.add("c");
        il.add("d");
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

    public static void openBag(Player p,String owner){
        Inventory inv = Bukkit.createInventory(p,27,"§bDreamBind §r- §d绑定箱 " + owner);

        int i = 0;
        for (String s : Objects.requireNonNull(bag.getConfigurationSection(owner)).getKeys(true)){
            inv.addItem(bag.getItemStack(owner + "." + s));
            bag.set(owner + "." + s,null);
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
                    break;
                }else if (p.getUniqueId().toString().equals(s)){
                    b = true;
                    break;
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
            if (!(Version >= 16)){
                is.setDurability((short) config.getInt("onBindStone.onItem.date"));
            }
            is.setItemMeta(im);
        }else if (b == 2){
            is = new ItemStack(Material.valueOf(Objects.requireNonNull(config.getString("onBindStone.unItem.type")).toUpperCase()));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(config.getString("onBindStone.unItem.name"));
            im.setLore(config.getStringList("onBindStone.unItem.lore"));
            if (!(Version >= 16)){
                is.setDurability((short) config.getInt("onBindStone.unItem.date"));
            }
            is.setItemMeta(im);
        }else {
            is = new ItemStack(Material.valueOf(Objects.requireNonNull(config.getString("onKeepStone.type")).toUpperCase()));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(config.getString("onKeepStone.name"));
            im.setLore(config.getStringList("onKeepStone.lore"));
            if (!(Version >= 16)){
                is.setDurability((short) config.getInt("onKeepStone.date"));
            }
            is.setItemMeta(im);
        }
        is.setAmount(i);
        return is;
    }

    public static void Gui(Player p){
        Inventory inv = Bukkit.createInventory(p,bindgui.getInt("onInfo.size") * 9, Objects.requireNonNull(bindgui.getString("onInfo.name")));

        if (bindgui.getString("onInfo.music") != null){
            if (sound.contains(bindgui.getString("onInfo.music"))){
                p.playSound(p.getLocation(),Sound.valueOf(Objects.requireNonNull(bindgui.getString("onInfo.music")).toUpperCase()),50,50);
            }else {
                System.out.println("[DreamBind] 警告！bind.yml内的music配置错误！");
                System.out.println("[DreamBind] 警告！绑定GUI的声音播放失败");
                System.out.println("[DreamBind] 警告！请自行查阅Wiki网站获取音效！");
            }
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

    public static boolean number(String number) {
        if(number==null) return false;
        return number.matches("[+-]?[1-9]+[0-9]*(\\.[0-9]+)?");
    }

    public static int random(int i, int i1) {
        if (i > i1) {
            int i2 = i;
            i = i1;
            i1 = i2;
        }
        return (int) Math.floor(Math.random() * (i1 - i + 1) + i);
    }

    public boolean loadPlayerPoints(){
        if (getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPoints = (PlayerPoints)getServer().getPluginManager().getPlugin("PlayerPoints");
            return playerPoints != null;
        }
        return false;
    }

    public boolean setupEconomy() {
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
                            DItem dItem = new DItem(p.getItemInHand());
                            if (Objects.requireNonNull(bindgui.getString("onMenu." + s + ".bind")).toUpperCase().equals("TRUE")){
                                if (dItem.isBind()){
                                    p.sendMessage("§f[§bDreamBind§f] §c你手上的物品已经被绑定过了!");
                                }else {
                                    p.setItemInHand(dItem.setBind(p));
                                    p.sendMessage("§f[§bDreamBind§f] §a手上物品绑定成功!");
                                    for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                        DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                    }
                                }
                            }else {
                                if (dItem.isBind()){
                                    p.setItemInHand(dItem.unBind());
                                    p.sendMessage("§f[§bDreamBind§f] §a手上物品解绑成功!");
                                    for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                        DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                    }
                                }else {
                                    p.sendMessage("§f[§bDreamBind§f] §c你手上的物品还没有绑定过!");
                                }
                            }
                        }else {
                            p.sendMessage("§f[§bDreamBind§f] §c你的钱不足以进行该操作！");
                        }
                    }else {
                        DItem dItem = new DItem(p.getItemInHand());
                        if (Objects.requireNonNull(bindgui.getString("onMenu." + s + ".bind")).toUpperCase().equals("TRUE")){
                            if (dItem.isBind()){
                                p.sendMessage("§f[§bDreamBind§f] §c你手上的物品已经被绑定过了!");
                            }else {
                                p.setItemInHand(dItem.setBind(p));
                                p.sendMessage("§f[§bDreamBind§f] §a手上物品绑定成功!");
                                for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                    DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                }
                            }
                        }else {
                            if (dItem.isBind()){
                                p.setItemInHand(dItem.unBind());
                                p.sendMessage("§f[§bDreamBind§f] §a手上物品解绑成功!");
                                for (String v : Objects.requireNonNull(bindgui.getConfigurationSection("onMenu." + s + ".money")).getKeys(false)){
                                    DreamBind.getPlugin(DreamBind.class).takeMoney(v,p,bindgui.getDouble("onMenu." + s + ".money." + v));
                                }
                            }else {
                                p.sendMessage("§f[§bDreamBind§f] §c你手上的物品还没有绑定过!");
                            }
                        }
                    }
                }
            }
        }
    }

    public double getMoney(String s, Player p){
        double i = 0;
        if (Objects.requireNonNull(vault.getString(s + ".type")).toUpperCase().equals("VAULT")) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                if (getServer().getServicesManager().getRegistration(Economy.class) != null) {
                    i = getVault(p);
                }
            }
        }else if (Objects.requireNonNull(vault.getString(s + ".type")).equalsIgnoreCase("points")){
            if (getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
                i = playerPoints.getAPI().look(p.getUniqueId());
            }
        }else {
            i = getCustom(p,vault.getString( s + ".get"),vault.getString(s + ".folder"));
        }
        return i;
    }

    public void takeMoney(String s, Player p, double d){
        if (Objects.requireNonNull(vault.getString(s + ".type")).toUpperCase().equals("VAULT")) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                if (getServer().getServicesManager().getRegistration(Economy.class) != null) {
                    takeVault(p, d);
                }
            }
        }else if (Objects.requireNonNull(vault.getString(s + ".type")).equalsIgnoreCase("points")){
            if (getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
                playerPoints.getAPI().take(p.getUniqueId(), (int) d);
            }
        }else {
            takeCustom(p, vault.getString(s + ".get"), vault.getString(s + ".folder"), d);
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

    public void takeCustom(Player p, String l, String f, double d){
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
