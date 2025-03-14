# ![logo](https://github.com/intellectmind/Simpleback/blob/main/icon_40.png) Simpleback

**其他语言版本: [English](README.md)，[中文](README_zh.md)。**

----------------------------------------------------------------------------------------------------------

#### 支持Folia的back插件，同时支持Paper, Bukkit, Purpur, Spigot

#### 使用/tp等指令后，输入/back可返回到上一个坐标

#### 可在plugins文件夹下的Simpleback文件夹内修改默认配置

----------------------------------------------------------------------------------------------------------

#### 命令：

| 命令                     | 描述                                         | 权限                             |
|--------------------------|--------------------------------------------|----------------------------------|
| ```/back```       | 返回上一个坐标                               | simpleback.back 默认全部       |

----------------------------------------------------------------------------------------------------------

#### 配置文件（config.yml）

```
# zh/en
language: zh

# 最大记录数量
max-records: 2

# 需要监听的传送类命令
tracked-commands:
  - /tp
  - /cmi:home # 注意游戏内也要使用‘/cmi:home’ 而不是‘/home’。如果需要/home，需要再tracked-commands直接添加‘/cmi’，因为你输入‘/home’但服务端收到的是‘/cmi home’
  - /cmi:warp
```

----------------------------------------------------------------------------------------------------------

### bStats
![bStats](https://bstats.org/signatures/bukkit/Simpleback.svg)

### Star History
[![Star History Chart](https://api.star-history.com/svg?repos=Simpleback/Simpleback&type=Date)](https://star-history.com/#Simpleback/Simpleback&Date)
