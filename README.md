# ![logo](https://github.com/intellectmind/Simpleback/blob/main/icon_40.png) Simpleback

**Read this in other languages: [English](README.md)，[中文](README_zh.md)。**

----------------------------------------------------------------------------------------------------------

#### Supports Folia's back plugin and also supports Paper, Bukkit, Purpur, Spigot.

#### After using commands such as/tp, enter/back to return to the previous coordinate.

#### You can modify the default configuration in the Simpleback folder under the plugins folder.

----------------------------------------------------------------------------------------------------------

#### Command：

| Command               | Description                                     | Permission                         |
|--------------------------|--------------------------------------------|----------------------------------|
| ```/back```       | Return to the previous coordinate             | simpleback.back (defaults to all)       |

----------------------------------------------------------------------------------------------------------

#### config.yml

```
# zh/en
language: en

# Maximum number of records
max-records: 1

# Transmission commands that need to be monitored.
tracked-commands:
  - /tp
  - /cmi:home # In the game, ensure to use /cmi:home rather than /home. If /home is required, you must add /cmi directly in the tracked-commands settings. This is due to the fact that when you type /home, the server interprets it as /cmi home.
  - /cmi:warp
```

----------------------------------------------------------------------------------------------------------

### bStats
![bStats](https://bstats.org/signatures/bukkit/Simpleback.svg)

### Star History
[![Star History Chart](https://api.star-history.com/svg?repos=Simpleback/Simpleback&type=Date)](https://star-history.com/#Simpleback/Simpleback&Date)
