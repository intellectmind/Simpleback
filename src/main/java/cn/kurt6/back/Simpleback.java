package cn.kurt6.back;

import cn.kurt6.back.bStats.Metrics;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Simpleback extends JavaPlugin implements Listener {

    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private List<String> trackedCommands = new ArrayList<>();
    private File dataFile;

    private String language; // 当前语言

    // 多语言消息映射
    private static final Map<String, Map<String, String>> messages = new HashMap<>();

    static {
        // 中文消息
        Map<String, String> zhMessages = new HashMap<>();
        zhMessages.put("command.player_only", "§c仅玩家可使用此命令");
        zhMessages.put("command.no_permission", "§c没有权限");
        zhMessages.put("command.no_location", "§c没有可用的传送记录");
        zhMessages.put("command.teleport_success", "§a已传送至最后记录位置");
        zhMessages.put("command.teleport_fail", "§c传送失败：目标位置不安全");
        zhMessages.put("logger.locations_loaded", "已加载 {0} 个位置记录");
        zhMessages.put("logger.locations_saved", "成功保存 {0} 个位置记录");

        // 英文消息
        Map<String, String> enMessages = new HashMap<>();
        enMessages.put("command.player_only", "§cOnly players can use this command");
        enMessages.put("command.no_permission", "§cNo permission");
        enMessages.put("command.no_location", "§cNo available location recorded");
        enMessages.put("command.teleport_success", "§aTeleported to last location");
        enMessages.put("command.teleport_fail", "§cTeleport failed: destination is unsafe");
        enMessages.put("logger.locations_loaded", "Loaded {0} location records");
        enMessages.put("logger.locations_saved", "Successfully saved {0} location records");

        messages.put("zh", zhMessages);
        messages.put("en", enMessages);
    }

    // 获取格式化消息
    private String getMessage(String key, Object... args) {
        Map<String, String> langMessages = messages.getOrDefault(language, messages.get("zh"));
        String message = langMessages.getOrDefault(key, key);
        return MessageFormat.format(message, args);
    }

    @Override
    public void onEnable() {
        // bStats
        int pluginId = 24847;
        Metrics metrics = new Metrics(this, pluginId);

        // 创建插件目录
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("无法创建插件目录");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化配置文件
        initConfig();

        // 加载存储数据
        loadLocations();

        // 注册事件
        getServer().getPluginManager().registerEvents(this, this);

        // 注册命令
        Objects.requireNonNull(getCommand("back")).setExecutor(this);
    }

    private void initConfig() {
        // 创建/加载配置文件
        saveDefaultConfig();
        getConfig().options().copyDefaults(true); // 确保默认值被写入
        trackedCommands = getConfig().getStringList("tracked-commands");
        language = getConfig().getString("language", "zh").toLowerCase();
        getLogger().info("当前语言: " + language);
        getLogger().info("已加载监听命令: " + trackedCommands);

        // 初始化数据文件
        dataFile = new File(getDataFolder(), "locations.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建数据文件: " + e.getMessage());
            }
        }
    }

    private void loadLocations() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : yaml.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                World world = Bukkit.getWorld(yaml.getString(key + ".world"));
                if (world == null) continue;

                Location loc = new Location(
                        world,
                        yaml.getDouble(key + ".x"),
                        yaml.getDouble(key + ".y"),
                        yaml.getDouble(key + ".z")
                );
                lastLocations.put(uuid, loc);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的UUID格式: " + key);
            }
        }
        getLogger().info(getMessage("logger.locations_loaded", lastLocations.size()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] parts = event.getMessage().toLowerCase().split(" ");
        if (parts.length == 0) return;

        String baseCommand = parts[0];
        if (trackedCommands.contains(baseCommand)) {
            Player player = event.getPlayer();
            Location loc = player.getLocation();

            lastLocations.put(player.getUniqueId(), loc);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("command.player_only"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("simpleback.back")) {
            player.sendMessage(getMessage("command.no_permission"));
            return true;
        }

        Location loc = lastLocations.get(player.getUniqueId());
        if (loc == null || loc.getWorld() == null) {
            player.sendMessage(getMessage("command.no_location"));
            return true;
        }

        player.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                player.sendMessage(getMessage("command.teleport_success"));
                getLogger().info(getMessage("logger.location_recorded", player.getName(), formatLocation(loc)));
            } else {
                player.sendMessage(getMessage("command.teleport_fail"));
            }
        });
        return true;
    }

    @Override
    public void onDisable() {
        saveLocations();
    }

    private void saveLocations() {
        YamlConfiguration yaml = new YamlConfiguration();
        lastLocations.forEach((uuid, loc) -> {
            String path = uuid.toString();
            yaml.set(path + ".world", loc.getWorld().getName());
            yaml.set(path + ".x", loc.getX());
            yaml.set(path + ".y", loc.getY());
            yaml.set(path + ".z", loc.getZ());
        });

        try {
            yaml.save(dataFile);
            getLogger().info(getMessage("logger.locations_saved", lastLocations.size()));
        } catch (IOException e) {
            getLogger().severe("保存位置数据失败: " + e.getMessage());
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%s [%d, %d, %d]",
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }
}
