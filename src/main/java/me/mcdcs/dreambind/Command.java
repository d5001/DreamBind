package me.mcdcs.dreambind;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.*;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "unbindall":
                if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand.unbindall", "DreamBind.unbindall")))){
                    Player bp = null;
                    if (args.length >= 1){
                        if (Bukkit.getPlayer(args[0]) != null){
                            bp = Bukkit.getPlayer(args[0]);
                        }
                    }
                    if (bp == null){
                        if (sender instanceof Player){
                            bp = (Player) sender;
                        }
                    }
                    if (bp != null){
                        ItemStack[] is = bp.getInventory().getContents();
                        for (ItemStack i : is){
                            if (i != null){
                                if (i.getType() != Material.AIR){
                                    if (isBind(i)){
                                        unBind(i,bp);
                                    }
                                }
                            }
                        }
                        bp.getInventory().setContents(is);
                        bp.sendMessage("§f[§bDreamBind§f] §a成功解绑完你背包所有的物品！");
                        if (sender != bp){
                            sender.sendMessage("§f[§bDreamBind§f] §a已经解绑成功对应目标玩家的背包物品！");
                        }
                    }else {
                        sender.sendMessage("§f[§bDreamBind§f] §c无法锁定绑定背包的玩家!");
                    }
                }else {
                    sender.sendMessage("§f[§bDreamBind§f] §c你没有使用这个命令的权限!");
                }
                break;
            case "bindall":
                if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand.bindall", "DreamBind.bindall")))){
                    Player bp = null;
                    if (args.length >= 1){
                        if (Bukkit.getPlayer(args[0]) != null){
                            bp = Bukkit.getPlayer(args[0]);
                        }
                    }
                    if (bp == null){
                        if (sender instanceof Player){
                            bp = (Player) sender;
                        }
                    }
                    if (bp != null){
                        ItemStack[] is = bp.getInventory().getContents();
                        for (ItemStack i : is){
                            if (i != null){
                                if (i.getType() != Material.AIR){
                                    if (!isBind(i)){
                                        setBind(i,bp);
                                    }
                                }
                            }
                        }
                        bp.getInventory().setContents(is);
                        bp.sendMessage("§f[§bDreamBind§f] §a成功绑定完你背包所有的物品！");
                        if (sender != bp){
                            sender.sendMessage("§f[§bDreamBind§f] §a已经绑定成功对应目标玩家的背包物品！");
                        }
                    }else {
                        sender.sendMessage("§f[§bDreamBind§f] §c无法锁定绑定背包的玩家!");
                    }
                }else {
                    sender.sendMessage("§f[§bDreamBind§f] §c你没有使用这个命令的权限!");
                }
                break;
            case "bindreload":
                if (sender.isOp()) {
                    DreamBind pl = DreamBind.getPlugin(DreamBind.class);
                    pl.reloadConfig();
                    config = pl.getConfig();
                    bagfile = new File(pl.getDataFolder(), "bag.yml");
                    if (!bagfile.exists()){
                        pl.saveResource("bag.yml",true);
                        System.out.println("[DreamBind]正在加载bag.yml文件");
                    }
                    bag = YamlConfiguration.loadConfiguration(bagfile);
                    vaultfile = new File(pl.getDataFolder(),"vault.yml");
                    if (!vaultfile.exists()){
                        pl.saveResource("vault.yml",true);
                        System.out.println("[DreamBind]正在加载vault.yml文件");
                    }
                    vault = YamlConfiguration.loadConfiguration(vaultfile);
                    bindfile = new File(pl.getDataFolder(),"bind.yml");
                    if (!bindfile.exists()){
                        pl.saveResource("bind.yml",true);
                        System.out.println("[DreamBind]正在加载bind.yml文件");
                    }
                    bindgui = YamlConfiguration.loadConfiguration(bindfile);
                    sender.sendMessage("§f[§bDreamBind§f] §a插件已经成功重载完毕!");
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能使用该命令!");
                }
                break;
            case "bindtype":
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    p.sendMessage(p.getItemInHand().getType().toString());
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能进行该命令！");
                }
                break;
            case "bindonpickup":
                if (sender.isOp()) {
                    if (sender instanceof Player) {
                        if (((Player) sender).getItemInHand().getType() != Material.AIR) {
                            if (isBindPickup(((Player) sender).getItemInHand())) {
                                sender.sendMessage("§f[§bDreamBind§f] §c已经有了该类型的绑定！");
                            } else {
                                if (isBind(((Player) sender).getItemInHand())) {
                                    sender.sendMessage("§f[§bDreamBind§f] §c已经绑定过的物品无法进行该操作！");
                                } else {
                                    setBind((Player) sender, "onBindPickup");
                                    sender.sendMessage("§f[§bDreamBind§f] §a成功为手上物品附加拾取绑定！");
                                }
                            }
                        } else {
                            sender.sendMessage("§f[§bDreamBind§f] §c必须持有物品才能进行该命令！");
                        }
                    } else {
                        sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能进行该命令！");
                    }
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能执行该命令！");
                }
                break;
            case "bindonuse":
                if (sender.isOp()) {
                    if (sender instanceof Player) {
                        if (((Player) sender).getItemInHand().getType() != Material.AIR) {
                            if (isBindUse(((Player) sender).getItemInHand())) {
                                sender.sendMessage("§f[§bDreamBind§f] §c已经有了该类型的绑定！");
                            } else {
                                if (isBind(((Player) sender).getItemInHand())) {
                                    sender.sendMessage("§f[§bDreamBind§f] §c已经绑定过的物品无法进行该操作！");
                                } else {
                                    setBind((Player) sender, "onBindUse");
                                    sender.sendMessage("§f[§bDreamBind§f] §a成功为手上物品附加使用绑定！");
                                }
                            }
                        } else {
                            sender.sendMessage("§f[§bDreamBind§f] §c必须持有物品才能进行该命令！");
                        }
                    } else {
                        sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能进行该命令！");
                    }
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能执行该命令！");
                }
                break;
            case "bindonequip":
                if (sender.isOp()) {
                    if (sender instanceof Player) {
                        if (((Player) sender).getItemInHand().getType() != Material.AIR) {
                            if (isBindEquip(((Player) sender).getItemInHand())) {
                                sender.sendMessage("§f[§bDreamBind§f] §c已经有了该类型的绑定！");
                            } else {
                                if (isBind(((Player) sender).getItemInHand())) {
                                    sender.sendMessage("§f[§bDreamBind§f] §c已经绑定过的物品无法进行该操作！");
                                } else {
                                    setBind((Player) sender, "onBindEquip");
                                    sender.sendMessage("§f[§bDreamBind§f] §a成功为手上物品附加装备绑定！");
                                }
                            }
                        } else {
                            sender.sendMessage("§f[§bDreamBind§f] §c必须持有物品才能进行该命令！");
                        }
                    } else {
                        sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能进行该命令！");
                    }
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能执行该命令！");
                }
                break;
            case "bindstone":
                if (sender.isOp()) {
                    if (args.length >= 3) {
                        if (number(args[0])) {
                            if (number(args[2])) {
                                if (Bukkit.getPlayer(args[1]) == null) {
                                    sender.sendMessage("§f[§bDreamBind§f] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                                } else {
                                    sender.sendMessage("§f[§bDreamBind§f] §a已经成功将绑定石给予对应玩家!");
                                    Objects.requireNonNull(Bukkit.getPlayer(args[1])).getInventory().addItem(getStone(Integer.parseInt(args[2]), Integer.parseInt(args[0])));
                                    if (args[0].equals("1")) {
                                        Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage("§f[§bDreamBind§f] §a你收到了一些绑定石");
                                    } else if (args[0].equals("2")) {
                                        Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage("§f[§bDreamBind§f] §a你收到了一些解绑石");
                                    } else {
                                        Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage("§f[§bDreamBind§f] §a你收到了一些死亡不掉落石");
                                    }
                                }
                            } else {
                                sender.sendMessage("§f[§bDreamBind§f] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                            }
                        } else {
                            sender.sendMessage("§f[§bDreamBind§f] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                        }
                    } else {
                        sender.sendMessage("§f[§bDreamBind§f] §c指令§d/bindstone <1/2/3> <玩家名> <数量>");
                    }
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能使用该命令!");
                }
                break;
            default:
                if (sender instanceof Player) {
                    switch (label.toLowerCase()) {
                        case "bind":
                            if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand.bind", "DreamBind.bind")))) {
                                if (setBind((Player) sender)) {
                                    sender.sendMessage("§f[§bDreamBind§f] §a手上物品成功被绑定!");
                                } else {
                                    sender.sendMessage("§f[§bDreamBind§f] §a手上物品无法被绑定!");
                                }
                            } else {
                                sender.sendMessage("§f[§bDreamBind§f] §c你没有权限使用该命令!");
                            }
                            break;
                        case "bindbag":
                            if (bag.getConfigurationSection(sender.getName()) != null) {
                                int i = Objects.requireNonNull(bag.getConfigurationSection(sender.getName())).getKeys(true).size();
                                if (i > 0) {
                                    sender.sendMessage("§f[§bDreamBind§f] §a已经为你自动打开绑定箱,请拿走绑定箱内物品!");
                                    openBag((Player) sender);
                                } else {
                                    sender.sendMessage("§f[§bDreamBind§f] §a你并未存在任何物品在绑定箱内!");
                                }
                            } else {
                                sender.sendMessage("§f[§bDreamBind§f] §a你并未存在任何物品在绑定箱内!");
                            }
                            break;
                        case "unbind":
                            if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand.unbind", "DreamBind.unbind")))) {
                                boolean b = true;
                                if (!sender.isOp()){
                                    if (!isOwner(((Player) sender).getItemInHand(), (Player) sender)){
                                        b = false;
                                    }
                                }
                                if (b){
                                    if (unBind((Player) sender)) {
                                        sender.sendMessage("§f[§bDreamBind§f] §a手上物品成功解除绑定!");
                                    } else {
                                        sender.sendMessage("§f[§bDreamBind§f] §a手上物品无法被解除绑定!");
                                    }
                                }else {
                                    sender.sendMessage("§f[§bDreamBind§f] §a你无法解除不是你的绑定物品！");
                                }
                            } else {
                                sender.sendMessage("§f[§bDreamBind§f] §c你没有权限使用该命令!");
                            }
                            break;
                    }
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能执行该命令！");
                }
                break;
        }
        return false;
    }
}
