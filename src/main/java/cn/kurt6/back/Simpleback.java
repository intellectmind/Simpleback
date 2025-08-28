package cn.kurt6.back;

import cn.kurt6.back.bStats.Metrics;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Simpleback extends JavaPlugin implements Listener {

    private static final Logger log = LoggerFactory.getLogger(Simpleback.class);
    private final Map<UUID, Deque<Location>> lastLocations = new ConcurrentHashMap<>();
    private int maxRecords;
    private List<String> trackedCommands = new ArrayList<>();
    private File dataFile;
    private boolean debugMode;
    private boolean toggleBackMode;

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
        zhMessages.put("logger.location_teleported", "玩家 {0} 传送到位置 {1}");
        zhMessages.put("logger.debug.command_tracked", "[调试] 监听到玩家 {0} 输入的命令: {1}");
        zhMessages.put("config.toggle_back_mode", "来回back模式: {0}");

        // 英文消息
        Map<String, String> enMessages = new HashMap<>();
        enMessages.put("command.player_only", "§cOnly players can use this command");
        enMessages.put("command.no_permission", "§cNo permission");
        enMessages.put("command.no_location", "§cNo available location recorded");
        enMessages.put("command.teleport_success", "§aTeleported to last location");
        enMessages.put("command.teleport_fail", "§cTeleport failed: destination is unsafe");
        enMessages.put("logger.locations_loaded", "Loaded {0} location records");
        enMessages.put("logger.locations_saved", "Successfully saved {0} location records");
        enMessages.put("logger.location_teleported", "Player {0} teleported to {1}");
        enMessages.put("logger.debug.command_tracked", "[DEBUG] Tracked command from player {0}: {1}");
        enMessages.put("config.toggle_back_mode", "Toggle back mode: {0}");

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
        maxRecords = getConfig().getInt("max-records", 2);
        toggleBackMode = getConfig().getBoolean("toggle-back-mode", false);
        getLogger().info("language: " + language);
        getLogger().info("trackedCommands: " + trackedCommands);
        debugMode = getConfig().getBoolean("debug-mode", false);

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
                Deque<Location> queue = new ArrayDeque<>();
                // 检查新旧数据格式
                if (yaml.contains(key + ".0")) {
                    int index = 0;
                    while (yaml.contains(key + "." + index)) {
                        // 加载每个位置
                        World world = Bukkit.getWorld(yaml.getString(key + "." + index + ".world"));
                        if (world == null) {
                            index++;
                            continue;
                        }
                        Location loc = new Location(
                                world,
                                yaml.getDouble(key + "." + index + ".x"),
                                yaml.getDouble(key + "." + index + ".y"),
                                yaml.getDouble(key + "." + index + ".z")
                        );
                        queue.add(loc);
                        index++;
                    }
                } else {
                    // 旧格式处理
                    World world = Bukkit.getWorld(yaml.getString(key + ".world"));
                    if (world != null) {
                        Location loc = new Location(
                                world,
                                yaml.getDouble(key + ".x"),
                                yaml.getDouble(key + ".y"),
                                yaml.getDouble(key + ".z")
                        );
                        queue.add(loc);
                    }
                }
                // 截断到最大记录数
                while (queue.size() > maxRecords) {
                    queue.pollLast();
                }
                if (!queue.isEmpty()) {
                    lastLocations.put(uuid, queue);
                }
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的UUID格式: " + key);
            }
        }
        // 更新日志信息
        int total = lastLocations.values().stream().mapToInt(Deque::size).sum();
        getLogger().info(getMessage("logger.locations_loaded", total));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1).toLowerCase(); // 去掉"/"并转小写
        boolean isTracked = trackedCommands.stream()
                .anyMatch(cmd -> command.startsWith(cmd.toLowerCase()));

        if (isTracked) {
            Player player = event.getPlayer();
            Location loc = player.getLocation();

            if (debugMode) {
                getLogger().info(getMessage("logger.debug.command_tracked", player.getName(), event.getMessage()));
            }

            Deque<Location> queue = lastLocations.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
            queue.offerFirst(loc);
            while (queue.size() > maxRecords) {
                queue.pollLast();
            }
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

        Deque<Location> queue = lastLocations.get(player.getUniqueId());
        if (queue == null || queue.isEmpty()) {
            player.sendMessage(getMessage("command.no_location"));
            return true;
        }

        Location currentLoc = player.getLocation();
        Location targetLoc;

        if (toggleBackMode) {
            // 来回模式：取出最新的位置，并将当前位置添加到队列
            targetLoc = queue.pollFirst();
            if (targetLoc == null) {
                player.sendMessage(getMessage("command.no_location"));
                return true;
            }

            // 将当前位置添加到队列末尾
            queue.offerLast(currentLoc);
            while (queue.size() > maxRecords) {
                queue.pollFirst(); // 移除最旧的位置
            }
        } else {
            // 普通模式：直接取出最新的位置
            targetLoc = queue.pollFirst();
        }

        // 检查目标位置是否有效
        if (targetLoc.getWorld() == null || Bukkit.getWorld(targetLoc.getWorld().getName()) == null) {
            player.sendMessage(getMessage("command.teleport_fail", "目标世界未加载或不存在"));
            return true;
        }

        // 安全传送
        player.teleportAsync(targetLoc).thenAccept(success -> {
            if (success) {
                player.sendMessage(getMessage("command.teleport_success"));
                getLogger().info(getMessage("logger.location_teleported",
                        player.getName(),
                        formatLocation(targetLoc)));
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
        lastLocations.forEach((uuid, queue) -> {
            String uuidStr = uuid.toString();
            int index = 0;
            for (Location loc : queue) {
                String path = uuidStr + "." + index;
                yaml.set(path + ".world", loc.getWorld().getName());
                yaml.set(path + ".x", loc.getX());
                yaml.set(path + ".y", loc.getY());
                yaml.set(path + ".z", loc.getZ());
                index++;
            }
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