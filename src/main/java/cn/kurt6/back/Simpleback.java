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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Simpleback extends JavaPlugin implements Listener {

    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private List<String> trackedCommands = new ArrayList<>();
    private File dataFile;

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
        trackedCommands = getConfig().getStringList("tracked-commands");
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
        getLogger().info("已加载 " + lastLocations.size() + " 个位置记录");
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
            getLogger().info("记录位置: " + player.getName() + " -> " + formatLocation(loc));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c仅玩家可使用此命令");
            return true;
        }

        Player player = (Player) sender;
        Location loc = lastLocations.get(player.getUniqueId());

        if (loc == null || loc.getWorld() == null) {
            player.sendMessage("§c没有可用的传送记录");
            return true;
        }

        player.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a已传送至最后记录位置");
                getLogger().info(player.getName() + " 传送至 " + formatLocation(loc));
            } else {
                player.sendMessage("§c传送失败：目标位置不安全");
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
            getLogger().info("成功保存 " + lastLocations.size() + " 个位置记录");
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
