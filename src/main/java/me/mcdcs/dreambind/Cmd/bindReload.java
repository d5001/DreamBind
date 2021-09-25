package me.mcdcs.dreambind.Cmd;

import me.mcdcs.dreambind.DreamBind;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import static me.mcdcs.dreambind.DreamBind.*;
import static me.mcdcs.dreambind.DreamBind.bindfile;

public class bindReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            color = config.getString("bindColor");
            if (color != null){
                color = color.replace("&","§");
                if (!tihuan(color).equals("")){
                    color = "";
                }
            }
            sender.sendMessage("§f[§bDreamBind§f] §a插件已经成功重载完毕!");
        } else {
            sender.sendMessage("§f[§bDreamBind§f] §c只有OP才能使用该命令!");
        }
        return false;
    }
}
