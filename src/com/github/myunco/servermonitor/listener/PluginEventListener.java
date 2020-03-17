package com.github.myunco.servermonitor.listener;

import com.github.myunco.servermonitor.config.Config;
import com.github.myunco.servermonitor.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.HashMap;
import java.util.List;

public class PluginEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAsyncChatEvent(AsyncPlayerChatEvent event) {
        String str = Util.getTime() + " 玩家[" + event.getPlayer().getName() + "]说 : " + event.getMessage();
        Bukkit.broadcastMessage(str);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage();
        String playerName = event.getPlayer().getName();
        boolean isOp = event.getPlayer().isOp();
        String str = Util.getTime() + " 玩家[" + playerName + "]" + (isOp ? "(OP)" : "(非OP)") + "执行指令 : " + cmd;
        Bukkit.broadcastMessage(str);
        //不知道怎么判断非op玩家是否有权限执行这条命令，干脆改成只检测op执行吧
        if (!isOp)
            return;
        if (cmd.toLowerCase().startsWith("/op ")) {
            str = Util.getTime() + " 玩家[" + playerName + "]Opped : " + Util.getTextRight(cmd, " ");
            Bukkit.broadcastMessage(str);
        } else if (cmd.toLowerCase().startsWith("/deop ")) {
            str = Util.getTime() + " 玩家[" + playerName + "]De-Opped : " + Util.getTextRight(cmd, " ");
            Bukkit.broadcastMessage(str);
        }
        if (Util.isWhiteList(playerName))
            return;
        if (Util.isCommandWhiteList(Util.getTextLeft(cmd, " ")))
            return;

        str = Util.getTime() + "玩家[" + playerName + "]是OP且不在白名单内并使用了警报命令：" + cmd;
        Bukkit.broadcastMessage(str);
        int method = Config.handleMethod;
        if (method == 0)
            return;
        HashMap<String, List<String>> handleMethodConfig = Config.handleMethodConfig;
        List<String> list;
        if ((method & 1) == 1) {
            list = handleMethodConfig.get("broadcast");
            //list.forEach(Bukkit::broadcastMessage);
            list.forEach(value -> Bukkit.broadcastMessage(value.replace("{player}", playerName).replace("{command}", cmd)));
            System.out.println("选项包含1");
        }
        if ((method & 2) == 2) {
            list = handleMethodConfig.get("consoleCmd");
            list.forEach(value -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value.replace("{player}", playerName).replace("{command}", cmd)));
            System.out.println("选项包含2");
        }
        if ((method & 4) == 4) {
            list = handleMethodConfig.get("playerCmd");
            //performCommand要去掉 '/' 所以这里直接用chat吧
            list.forEach(value -> event.getPlayer().chat(value.replace("{player}", playerName).replace("{command}", cmd)));
            System.out.println("选项包含4");
        }
        if ((method & 8) == 8) {
            list = handleMethodConfig.get("playerSendMsg");
            list.forEach(value -> event.getPlayer().chat(value.replace("{player}", playerName).replace("{command}", cmd)));
            System.out.println("选项包含8");
        }
        if ((method & 16) == 16) {
            list = handleMethodConfig.get("sendMsgToPlayer");
            list.forEach(value -> event.getPlayer().sendMessage(value.replace("{player}", playerName).replace("{command}", cmd)));
            System.out.println("选项包含16");
        }
        if ((method & 32) == 32) {
            list = handleMethodConfig.get("consoleWarning");
            list.forEach(value -> Bukkit.getConsoleSender().sendMessage(value.replace("{player}", playerName).replace("{command}", cmd)));
            System.out.println("选项包含32");
        }
        if ((method & 64) == 64) {
            list = handleMethodConfig.get("warningLog");
            System.out.println("选项包含64");
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void serverCommandEvent(ServerCommandEvent event) {
        String str = Util.getTime() + " 控制台[" + event.getSender().getName() + "]执行指令 : " + event.getCommand();
        Bukkit.broadcastMessage(str);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        String str = Util.getTime() + " 玩家[" + event.getPlayer().getName() + "]的游戏模式更改为 : " + event.getNewGameMode().toString();
        Bukkit.broadcastMessage(str);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoinEvent(PlayerJoinEvent event) {
        String str = Util.getTime() + " 玩家[" + event.getPlayer().getName() + "](" + event.getPlayer().getAddress().toString() + ") : 加入服务器";
        Bukkit.broadcastMessage(str);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuitEvent(PlayerQuitEvent event) {
        String str = Util.getTime() + " 玩家[" + event.getPlayer().getName() + "](" + event.getPlayer().getAddress().toString() + ") : 退出服务器";
        Bukkit.broadcastMessage(str);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerKickEvent(PlayerKickEvent event) {
        if (event.isCancelled())
            return;
        String str = Util.getTime() + " 玩家[" + event.getPlayer().getName() + "](" + event.getPlayer().getAddress().toString() + ") : 被踢出游戏";
        Bukkit.broadcastMessage(str);
    }

}
