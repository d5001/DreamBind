package me.mcdcs.dreambind.Cmd;

import me.mcdcs.dreambind.DItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.*;

public class unBind implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.isOp() | sender.hasPermission(Objects.requireNonNull(config.getString("onBindCommand.unbind", "DreamBind.unbind")))) {
                boolean b = true;
                DItem dItem = new DItem(((Player) sender).getItemInHand());
                if (!sender.isOp()){
                    if (!dItem.isOwner(sender.getName())){
                        b = false;
                    }
                }
                if (b){
                    if (dItem.isBind()) {
                        ((Player) sender).setItemInHand(dItem.unBind());
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
        } else {
            sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能执行该命令！");
        }

        return false;
    }
}
