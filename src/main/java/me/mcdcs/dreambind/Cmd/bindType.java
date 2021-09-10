package me.mcdcs.dreambind.Cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class bindType implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.sendMessage(p.getItemInHand().getType().toString());
        } else {
            sender.sendMessage("§f[§bDreamBind§f] §c只有玩家才能进行该命令！");
        }
        return false;
    }
}
