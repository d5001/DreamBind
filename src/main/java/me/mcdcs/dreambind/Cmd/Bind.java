package me.mcdcs.dreambind.Cmd;

import me.mcdcs.dreambind.Api.DItem;
import me.mcdcs.dreambind.Config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Bind implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.isOp() | sender.hasPermission(Config.getString("onBindCommand.bind", "DreamBind.bind"))) {
                DItem dItem = new DItem(((Player) sender).getItemInHand());
                if (!dItem.isBind()) {
                    ((Player) sender).setItemInHand(dItem.setBind((Player) sender));
                    sender.sendMessage("§f[§bDreamBind§f] §a手上物品成功被绑定!");
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §a手上物品无法被绑定!");
                }
            } else {
                sender.sendMessage("§f[§bDreamBind§f] §c你没有权限使用该命令!");
            }
        } else {
            sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能执行该命令！");
        }

        return false;
    }
}
