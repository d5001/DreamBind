package me.mcdcs.dreambind.Cmd;

import me.mcdcs.dreambind.DItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class bindOnGet implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.isOp()) {
                DItem dItem = new DItem(((Player) sender).getItemInHand());
                if (dItem.isItemStack()) {
                    if (dItem.isBind("onBindGet")) {
                        sender.sendMessage("§f[§bDreamBind§f] §c已经有了该类型的绑定！");
                    } else {
                        if (dItem.isBind()) {
                            sender.sendMessage("§f[§bDreamBind§f] §c已经绑定过的物品无法进行该操作！");
                        } else {
                            ((Player) sender).setItemInHand(dItem.setBindAction("onBindGet"));
                            sender.sendMessage("§f[§bDreamBind§f] §a成功为手上物品附加装备绑定！");
                        }
                    }
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §c必须持有物品才能进行该命令！");
                }
            } else {
                sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能执行该命令！");
            }
        } else {
            sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能执行该命令！");
        }
        return false;
    }
}
