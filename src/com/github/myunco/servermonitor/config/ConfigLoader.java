package com.github.myunco.servermonitor.config;

import com.github.myunco.servermonitor.ServerMonitor;
import com.github.myunco.servermonitor.util.Log;
import com.github.myunco.servermonitor.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class ConfigLoader {
    static ServerMonitor pl = ServerMonitor.plugin;
    static boolean flag;

    public static boolean load() {
        flag = false;
        pl.saveDefaultConfig();
        File file = new File(pl.getDataFolder(), "/config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        //FileConfiguration config = pl.getConfig();
        Config.language = config.getString("language");
        if (Config.language == null) {
            loadError("配置文件错误! 请检查 language 是否存在.");
            return false;
        }
        loadLanguage(Config.language);
        Config.dateFormat = config.getString("dateFormat");
        if (Config.dateFormat == null) {
            //loadError("配置文件错误! 请检查 dateFormat 是否存在.");
            loadError(Language.messageConfigError.replace("{path}", "dateFormat"));
            return false;
        }
        Util.setSdf(Config.dateFormat);
        Config.lineSeparator = config.getString("lineSeparator");
        if (Config.lineSeparator == null) {
            //loadError("配置文件错误! 请检查 lineSeparator 是否存在.");
            loadError(Language.messageConfigError.replace("{path}", "lineSeparator"));
            return false;
        }
        if (Config.lineSeparator.toLowerCase().equals("auto"))
            Config.lineSeparator = System.lineSeparator();
        Config.realTimeSave = config.getBoolean("realTimeSave");
        //1.1.0新增
        checkContain(config, "zipOldLog", true);
        Config.zipOldLog = config.getBoolean("zipOldLog");
        checkContain(config, "delOldLog", 0);
        Config.delOldLog = config.getInt("delOldLog");
        checkContain(config, "checkUpdate", false);
        Config.checkUpdate = config.getBoolean("checkUpdate");

        Config.playerChat.put("enable", config.getBoolean("playerChat.enable"));
        Config.playerChat.put("perPlayer", config.getBoolean("playerChat.perPlayer"));
        Config.playerCommand.put("enable", config.getBoolean("playerCommand.enable"));
        Config.playerCommand.put("perPlayer", config.getBoolean("playerCommand.perPlayer"));
        Config.playerCommand.put("consoleCommand", config.getBoolean("playerCommand.consoleCommand"));
        //1.0.3新增
        /*if (!config.contains("playerCommand.commandBlockCommand")) {
            config.set("playerCommand.commandBlockCommand", true);
            flag = true;
        }*/
        checkContain(config, "playerCommand.commandBlockCommand", true);
        Config.playerCommand.put("commandBlockCommand", config.getBoolean("playerCommand.commandBlockCommand"));

        Config.playerGameModeChange.put("enable", config.getBoolean("playerGameModeChange.enable"));
        Config.playerGameModeChange.put("perPlayer", config.getBoolean("playerGameModeChange.perPlayer"));
        Config.opChange = config.getBoolean("playerCommand.opChange");
        Config.joinAndLeave = config.getBoolean("joinAndLeave");
        Config.commandAlert = config.getBoolean("commandAlert.enable");
        if (Config.commandAlert) {
            Config.whitelist = config.getStringList("commandAlert.whitelist");
            Config.cancel = config.getBoolean("commandAlert.cancel");
            Config.commandWhiteList = config.getStringList("commandAlert.commandWhiteList");
            ConfigurationSection cs = config.getConfigurationSection("commandAlert.handleMethod");
            if (cs == null) {
                //loadError("配置文件错误! 请检查 commandAlert下的handleMethod 是否存在.");
                loadError(Language.messageConfigError.replace("{path}", "commandAlert.handleMethod"));
                return false;
            }
            Set<String> s = cs.getKeys(false);
            if (s.size() < 8) {
                //loadError("配置文件错误! 请检查 commandAlert下的handleMethod下的所有项 是否存在.");
                loadError(Language.messageConfigError.replace("{path}", "commandAlert.handleMethod.?"));
                return false;
            }
            for (String value : s) {
                if (value.equals("method")) {
                    Config.handleMethod = config.getInt("commandAlert.handleMethod.method");
                    continue;
                }
                Config.handleMethodConfig.put(value, config.getStringList("commandAlert.handleMethod." + value));
            }
        }
        /*if (Config.playerChat.get("enable")) {
            try {
                Log.createChatLog();
            } catch (IOException e) {
                //Log.sendException("§4[错误] §5在打开ChatLog时发生IO异常!", e.getMessage());
                //Log.sendException(Language.messageOpenException.replace("{file}", "Chat.log"), e.getMessage());
                Log.sendException(Language.messageOpenException.replace("{file}", "Chat -> " + Util.logName + ".log"), e.getMessage());
                return false;
            }
        }
        if (Config.playerCommand.get("enable")) {
            try {
                Log.createCommandLog();
            } catch (IOException e) {
                //Log.sendException("§4[错误] §5在打开CommandLog时发生IO异常!", e.getMessage());
                //Log.sendException(Language.messageOpenException.replace("{file}", "Command.log"), e.getMessage());
                Log.sendException(Language.messageOpenException.replace("{file}", "Command -> " + Util.logName + ".log"), e.getMessage());
                return false;
            }
        }
        if (Config.playerGameModeChange.get("enable")) {
            try {
                Log.createGameModeLog();
            } catch (IOException e) {
                //Log.sendException("§4[错误] §5在打开GameModeLog时发生IO异常!", e.getMessage());
                //Log.sendException(Language.messageOpenException.replace("{file}", "GameMode.log"), e.getMessage());
                Log.sendException(Language.messageOpenException.replace("{file}", "GameMode -> " + Util.logName + ".log"), e.getMessage());
                return false;
            }
        }
        if (Config.opChange) {
            try {
                Log.createOpChangeLog();
            } catch (IOException e) {
                //Log.sendException("§4[错误] §5在打开OpChangeLog时发生IO异常!", e.getMessage());
                Log.sendException(Language.messageOpenException.replace("{file}", "OpChange.log"), e.getMessage());
                return false;
            }
        }
        if (Config.joinAndLeave) {
            try {
                Log.createJoinLeaveLog();
            } catch (IOException e) {
                //Log.sendException("§4[错误] §5在打开JoinLeaveLog时发生IO异常!", e.getMessage());
                //Log.sendException(Language.messageOpenException.replace("{file}", "JoinAndLeave.log"), e.getMessage());
                Log.sendException(Language.messageOpenException.replace("{file}", "JoinLeave -> " + Util.logName + ".log"), e.getMessage());
                return false;
            }
        }
        pl.getServer().getOnlinePlayers().forEach(player -> {
            String playerName = player.getName();
            if (Config.playerChat.get("perPlayer"))
                Log.addPlayerChatLog(playerName);
            if (Config.playerCommand.get("perPlayer"))
                Log.addPlayerCommandLog(playerName);
            if (Config.playerGameModeChange.get("perPlayer"))
                Log.addPlayerGameModeLog(playerName);
        });*/
        if (!Log.createAllLog(true))
            return false;
        if (flag) {
            try {
                config.save(file);
            } catch (IOException e) {
                Log.sendException(Language.messageSaveException.replace("{file}", "config.yml"), e.getMessage());
            }
        }
        pl.enable();
        return true;
    }

    public static void reload() {
        //Log.closeAllLog();
        //pl.reloadConfig();
        pl.disable();
        load();
    }

    public static void loadLanguage(String language) {
        //File file = new File(pl.getDataFolder(), "/languages/zh_cn.yml");
        String langPath = "languages/" + language + ".yml";
        File file = new File(pl.getDataFolder(), langPath);
        if (!file.exists()) {
            if (pl.getResource(langPath) == null) {
                InputStream in = pl.getResource("languages/zh_cn.yml");
                if (in != null) {
                    try {
                        OutputStream out = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        Log.sendException("§4[错误] §5在保存 " + language + ".yml 时发生IO异常!", e.getMessage());
                    }
                    System.out.println("[ServerMonitor] 语言文件: " + file.getPath() + " 不存在,已自动创建.");
                } else {
                    System.out.println("[ServerMonitor] 错误! 语言文件: " + language + ".yml 不存在,并且在插件本体内找不到默认语言文件: zh_cn.yml");
                }
            } else {
                pl.saveResource(langPath, false);
            }
        }
        /*
        if (!"zh_cn".equals(language)) {
            file = new File(pl.getDataFolder(), "languages/" + language + ".yml");
            if (!file.exists()) {
                InputStream in = pl.getResource("languages/zh_cn.yml");
                if (in != null) {
                    try {
                        OutputStream out = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                        System.out.println("[ServerMonitor] 语言文件: " + file.getPath() + " 不存在,已自动创建.");
                    } catch (IOException e) {
                        Log.sendException("§4[错误] §5在保存 " + language + ".yml 时发生IO异常!", e.getMessage());
                    }
                }
            }
        }
         */
        YamlConfiguration lang = YamlConfiguration.loadConfiguration(file);
        Language.version = lang.getInt("version");
        Language.messageSaveException = lang.getString("message.saveException");
        Language.messageLangUpdate = lang.getString("message.langUpdate");
        Language.messageLangUpdated = lang.getString("message.langUpdated");
        if (languageUpdate(lang)) {
            try {
                lang.save(file);
                ServerMonitor.consoleSender.sendMessage(Language.MSG_PREFIX + Language.messageLangUpdated);
            } catch (IOException e) {
                //Log.sendException("§4[错误] §5在保存 " + language + ".yml 时发生IO异常!", e.getMessage());
                Log.sendException(Language.messageSaveException.replace("{file}", language + ".yml"), e.getMessage());
            }
        }
        //不判断空指针了，乱搞就要有报错的准备。
        Language.enabled = lang.getString("enabled");
        Language.disabled = lang.getString("disabled");
        StringBuilder helpMsg = new StringBuilder();
        lang.getStringList("helpMsg").forEach(value -> helpMsg.append(value).append("\n"));
        Language.helpMsg = helpMsg.toString();
        Language.reloaded = lang.getString("reloaded");
        Language.commandError = lang.getString("commandError");
        Language.logPlayerChat = lang.getString("log.playerChat");
        Language.logIsOp = lang.getString("log.isOP");
        Language.logNonOp = lang.getString("log.nonOP");
        Language.logPlayerCommand = lang.getString("log.playerCommand");
        Language.logConsoleCommand = lang.getString("log.consoleCommand");
        Language.logOpped = lang.getString("log.opped");
        Language.logDeOpped = lang.getString("log.deOpped");
        Language.logConsoleOpped = lang.getString("log.consoleOpped");
        Language.logConsoleDeOpped = lang.getString("log.consoleDeOpped");
        Language.logPlayerGameModeChange = lang.getString("log.playerGameModeChange");
        Language.logPlayerJoin = lang.getString("log.playerJoin");
        Language.logPlayerQuit = lang.getString("log.playerQuit");
        Language.logPlayerKick = lang.getString("log.playerKick");
        Language.messageOpenException = lang.getString("message.openException");
        Language.messageWriteException = lang.getString("message.writeException");
        Language.messageCloseException = lang.getString("message.closeException");
        Language.messageConfigError = lang.getString("message.configError");
        Language.messageZipException = lang.getString("message.zipException");
        Language.messageDeleteError = lang.getString("message.deleteError");
        Language.messageCheckUpdateException = lang.getString("message.checkUpdateException");
        Language.messageCheckUpdateError = lang.getString("message.checkUpdateError");
        Language.messageMajorUpdate = lang.getString("message.majorUpdate");
        Language.messageFoundNewVersion = lang.getString("message.foundNewVersion");
    }

    public static boolean languageUpdate(YamlConfiguration lang) {
        int currentVersion = 2;
        if (Language.version < currentVersion) {
            //pl.getServer().getConsoleSender().sendMessage(Language.MSG_PREFIX + "§c语言文件版本：§a" + Language.version + " §c最新版本：§b1 §6需要更新.");
            ServerMonitor.consoleSender.sendMessage(Language.MSG_PREFIX + Language.messageLangUpdate
                    .replace("{version}", String.valueOf(Language.version))
                    .replace("{$version}", String.valueOf(currentVersion)));
            switch (Language.version) {
                case 1: //如果版本为1 则添加相对于版本2缺少的内容
                    lang.set("message.zipException", "§4[错误] §5在压缩 {file} 时发生IO异常!");
                    lang.set("message.deleteError", "§4[错误] §5删除 {file} 失败!");
                    lang.set("message.checkUpdateException", "§4[错误] §c在检查更新时发生IO异常!");
                    lang.set("message.checkUpdateError", "§4[错误] §c检查更新失败, 状态码: §b{code}");
                    lang.set("message.majorUpdate", "§4重大更新");
                    lang.set("message.foundNewVersion", "§c发现新版本：{$version} §b当前版本: {version} 前往查看: {url}");
                    break; //最后一个case才break
                default:
                    loadError("Language version error.");
            }
            Language.version = currentVersion;
            lang.set("version", Language.version);
            return true;
        }
        return false;
    }

    public static void loadError(String msg) {
        pl.getLogger().warning(msg);
    }

    public static void checkContain(YamlConfiguration config, String path, Object defaults) {
        if (!config.contains(path)) {
            config.set(path, defaults);
            flag = true;
        }
    }
}
