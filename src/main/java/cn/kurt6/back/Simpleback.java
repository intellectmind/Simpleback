package cn.kurt6.back;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class Simpleback extends JavaPlugin implements Listener {

    private final ConcurrentHashMap<UUID, Deque<Location>> locationHistory = new ConcurrentHashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfigValues();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // 重载配置
    public void reloadConfigValues() {
        reloadConfig();
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!config.getBoolean("enabled", true)) return;

        Player player = event.getPlayer();
        Location from = event.getFrom();

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) return;

        // 获取或初始化位置队列
        Deque<Location> history = locationHistory.computeIfAbsent(
                player.getUniqueId(),
                k -> new LinkedBlockingDeque<>(config.getInt("max-back-entries", 3))
        );

        // 移除最旧的位置（当队列满时）
        if (history.size() >= config.getInt("max-back-entries", 3)) {
            history.removeFirst();
        }
        history.addLast(from.clone());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("back")) {
            handleBackCommand(sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("simpleback") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            handleReloadCommand(sender);
            return true;
        }
        return false;
    }

    private void handleBackCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用此命令！");
            return;
        }

        Player player = (Player) sender;

        // 检查功能是否启用
        if (!config.getBoolean("enabled", true)) {
            sendMessage(player, "messages.disabled");
            return;
        }

        // 检查权限
        if (!player.hasPermission("simpleback.use")) {
            sendMessage(player, "messages.no-permission");
            return;
        }

        player.getScheduler().run(this, task -> {
            Deque<Location> history = locationHistory.get(player.getUniqueId());
            if (history == null || history.isEmpty()) {
                sendMessage(player, "messages.no-location");
                return;
            }

            Location target = history.pollLast();
            Location current = player.getLocation();

            player.teleportAsync(target).thenAccept(success -> {
                if (success) {
                    // 将当前位置存入历史以便连续回退
                    history.addLast(current);
                    sendMessage(player, "messages.success");
                }
            });
        }, null);
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("simpleback.admin")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }

        reloadConfigValues();
        sendMessage(sender, "messages.reloaded");
    }

    private void sendMessage(CommandSender sender, String configPath) {
        String message = config.getString(configPath, "");
        if (!message.isEmpty()) {
            sender.sendMessage(message.replace('&', '§'));
        }
    }
}
