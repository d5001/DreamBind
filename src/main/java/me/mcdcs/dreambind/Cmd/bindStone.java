package me.mcdcs.dreambind.Cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

import static me.mcdcs.dreambind.DreamBind.getStone;
import static me.mcdcs.dreambind.DreamBind.number;

public class bindStone implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        return false;
    }
}
