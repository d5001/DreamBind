package me.mcdcs.dreambind.Cmd;

import me.mcdcs.dreambind.Config;
import me.mcdcs.dreambind.DItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class unBindAll implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() | sender.hasPermission(Config.getString("onBindCommand.unbindall", "DreamBind.unbindall"))){
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
                    DItem dItem = new DItem(i);
                    if (dItem.isBind()){
                        dItem.unBind();
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
        return false;
    }
}
