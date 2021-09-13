package me.mcdcs.dreambind.Cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.bag;
import static me.mcdcs.dreambind.DreamBind.openBag;

public class bindBag implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (bag.getConfigurationSection(sender.getName()) != null) {
                int i = Objects.requireNonNull(bag.getConfigurationSection(sender.getName())).getKeys(true).size();
                if (i > 0) {
                    String owner = sender.getName();
                    if (sender.isOp() & args.length >= 1){
                        owner = args[0];
                    }
                    sender.sendMessage("§f[§bDreamBind§f] §a已经为你自动打开绑定箱,请拿走绑定箱内物品!");
                    openBag((Player) sender,owner);
                } else {
                    sender.sendMessage("§f[§bDreamBind§f] §a你并未存在任何物品在绑定箱内!");
                }
            } else {
                sender.sendMessage("§f[§bDreamBind§f] §a你并未存在任何物品在绑定箱内!");
            }
        } else {
            sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能执行该命令！");
        }

        return false;
    }
}
