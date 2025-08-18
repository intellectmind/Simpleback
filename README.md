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

# Set to true to enable debugging mode, the console will output the commands it listens to
debug-mode: false

#Is the back and forth mode enabled? Once enabled, players can teleport back and forth between two positions
toggle-back-mode: true

# Transmission commands that need to be monitored.
tracked-commands:
  - "cmi home"
  - "cmi warp"
  - "res tp" # Match all commands starting with /res tp
  - "teleport"
  - "tp"
```

----------------------------------------------------------------------------------------------------------

### bStats
![bStats](https://bstats.org/signatures/bukkit/Simpleback.svg)

### Star History
[![Star History Chart](https://api.star-history.com/svg?repos=Simpleback/Simpleback&type=Date)](https://star-history.com/#Simpleback/Simpleback&Date)
