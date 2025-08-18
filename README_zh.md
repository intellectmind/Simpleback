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

# 设置为true开启调试模式，控制台会输出监听到的命令
debug-mode: false

# 是否启用来回back模式，启用后玩家可以在两个位置之间来回传送
toggle-back-mode: true

# 需要监听的传送类命令
tracked-commands:
  - "cmi home"
  - "cmi warp"
  - "res tp" # 匹配/res tp开头的所有命令
  - "teleport"
  - "tp"
```

----------------------------------------------------------------------------------------------------------

### bStats
![bStats](https://bstats.org/signatures/bukkit/Simpleback.svg)

### Star History
[![Star History Chart](https://api.star-history.com/svg?repos=Simpleback/Simpleback&type=Date)](https://star-history.com/#Simpleback/Simpleback&Date)
