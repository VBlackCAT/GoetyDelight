# GoetyDelight 1.5.3fix1 更新日志

> Minecraft 1.20.1 / Forge

本次更新主要集中在神金锅工作站配方、JEI 分类、灵魂食物、盛宴方块掉落与多人游戏修复，同时包含一批性能优化和兼容性修正。

---

## 新增

### 神金锅工作站配方系统

- 新增 `goetydelight:workstation_cooking` 配方类型与序列化器。
- 服务端启动时扫描**所有命名空间**的 `goety:brazier` 配方，不再局限于 `data/goety/recipes`。
- `goety:brazier` 配方在原料数量不超过 6 个时，自动转换为天启锅（神金锅）配方。
- 生成配方 ID 为 `goetydelight:apocalyptium_brazier/<来源命名空间>_<来源路径>`，避免不同模组之间冲突。
- 天启锅兼容的 brazier 配方：
  - 烹饪时间默认 = `soulCost / 5`
  - 经验默认 = `soulCost / 2`
  - 未填写 `soulCost` 时按 1000 计算，即 200 tick、500 经验
- 原有特殊烹饪配方自动转换：
  - 含 `goety:unholy_blood` → 诅咒金属锅配方
  - 含 `goety_revelation:apocalyptium_ingot` 或 `goety_revelation:doom_medal` → 天启锅配方
- 天启锅是诅咒金属锅的升级版本，因此也能烹饪诅咒金属锅的专属配方。
- 转换后的特殊配方会从普通农夫乐事锅中移除，避免普通锅绕过工作方块限制。
- 普通农夫乐事锅配方仍然保留，两个锅都可以正常烹饪普通配方。
- 增加配方查找兜底：即使自定义 RecipeType 的索引在某些加载环境下异常，也会直接遍历配方管理器查找匹配配方。

### JEI 工作站分类

新增三个 JEI 分区：

- **诅咒烹饪**
  - 原版农夫乐事锅配方
  - 诅咒金属锅专属配方
- **神灵烹饪**
  - 原版农夫乐事锅配方
  - 诅咒金属锅专属配方
  - 天启锅专属配方
- **神灵烹饪：亡灵火炉**
  - 由 `goety:brazier` 转换而来的天启锅配方

细节：

- 分类图标分别使用诅咒金属锅、天启锅、`goety:necro_brazier` 的物品贴图。
- 原“不洁烹饪”已改名为“诅咒烹饪”。
- 仅保留“神灵烹饪：亡灵火炉”这个子分类。
- JEI 中可直接查询到新生成的工作站配方。

---

## 调整

### 烹饪锅

- **诅咒金属锅**基础烹饪速度改为农夫乐事锅的 **2 倍**。
- 诅咒金属锅的灵魂供能不再额外叠加烹饪速度，避免变成 4 倍。
- 黑铁锅保留原有机制：
  - 烹饪时间减半
  - 灵魂供能额外 2 倍速度
  - 产物保留灵魂强度
- **诅咒金属锅和天启锅不再给产物写入 `SoulInfused` / `SoulInfusedLevel`**。
- 灵魂值改为食用时通过 `FoodSoulEnergyConfig` 发放，避免产物 NBT 与食用奖励重复计算。

### 食物灵魂值

为需要诅咒金属锅/天启锅的食物新增食用灵魂值：

| 食物 | 灵魂值 |
|---|---:|
| `goetydelight:tainted_drink` | 1000 |
| `goetydelight:pure_drink` | 3000 |
| `goetydelight:unholy_communion` | 6000 |
| `goetydelight:doom_cookie` | 8000 |
| `goetydelight:ascension_cod_stew` | 20000 |

### 盛宴与多方块

- **烤全监（BoatStuffedRoastedWarden）**
  - 破坏时通过方块物品 NBT 保存剩余份数，重新放置后恢复。
  - 修复空盘/被破坏时不掉落容器的问题。
  - 兼容 BOMD 乐事（Bosses Delight）的花蜜果冻冰：一次回满整个多方块结构，而不是只替换单格。
  - 修复多方块结构被单格替换后整盘被拆掉的问题。
- **烤老王（RoastLaowang）**
  - 修正多方块朝向、HEAD/FOOT 位置与结构拆除逻辑。
  - 战利品表只由 HEAD 触发，避免 HEAD/FOOT 双重掉落。
  - 修正取餐、破坏和份数同步。
- 统一盛宴方块的农夫乐事式掉落：
  - 破坏时按剩余份数掉落对应方块物品。
  - 空盘拆除时正常走掉落表，容器不再凭空消失。
  - 涉及烤全监、烤老王、巫妖乱炖、骨王灰饭、夜晚心豆汤、腐尸蛆宴、蛇形不洁牛肚、高颅骨饭、虚空果冻等。
- 4 档份数显示与取餐逻辑统一到 `FeastServingHelper`。

### 其他行为

- 晋升蛋糕（Apollyon Cake）：
  - 放置时同步 owner 数据到客户端。
  - 客户端不再预测取餐，避免非 owner 在多人游戏中出现“本地取餐但服务端拒绝”的幽灵物品。
  - 服务端增加最终 owner 校验。
- 死亡之箭（DeathArrow）命中方块后立即清理，不再永久插在地里。
- Π 派箭雨增加冷却与空任务跳过，避免无意义服务端 tick。
- 天命棒棒糖支持自动喂食，并增加 5 秒玩家级冷却。
- 玩偶 `doll_p0` / `doll_p7` 增加 63 条随机右键文本提示，连续两次不会重复。

---

## 修复

- 修复诅咒金属锅界面 shift-click 灵魂源槽时物品数量翻倍的问题。
- 修复烤老王 HEAD/FOOT 同时触发战利品表导致的双倍掉落。
- 修复晋升蛋糕非 owner 也能取餐、客户端幽灵物品的问题。
- 修复烤全监剩余份数在破坏/放置后丢失的问题。
- 修复烤全监被花蜜果冻冰回满时结构损坏的问题。
- 修复 DeathArrow 落地后不消失的问题。
- 修复部分盛宴方块空盘后容器不返还的问题。
- 修复农夫乐事刀具/托盘标签在正式环境反射失效的问题。
- 修复实体玩偶不能装备到头饰栏的问题。
- 修复皇家蛋糕不朝向玩家的问题。
- 修复巫妖乱炖与玩家碰撞时的卡顿问题。
- 修复永久强效加成在玩家死亡时丢失的问题。
- 提高农作物相关堆肥概率。

---

## 兼容性

- 完善 Farmer's Delight 旧版本兼容层：
  - 刀具标签
  - 物品标签
  - 托盘标签
  - 音效与文本工具
- 新增 Bosses Delight 花蜜果冻冰兼容。
- 新增 FTB Ultimine（连锁破坏）兼容：
  - 天启镰刀右键收割时，连锁收割沿用镰刀自身的双倍掉落和“只降一级”逻辑；
  - 为下界疙瘩和 SnapWarts 注册 FTB Ultimine 作物处理器；
  - 通过 Mixin 接管 FTB Ultimine 自带的 CropBlock 处理器。
- 新增 Right Click, Get Crops 兼容：
  - 手持天启镰刀时跳过 RCGC 的 HIGHEST 优先级收菜处理器，避免其覆盖镰刀逻辑。
- 新增 Sophisticated Backpacks / Travelers Backpack 自动喂食升级兼容。
- 精妙背包/旅行者背包自动喂食天命棒棒糖时遵循冷却。
- 转化仆从黑名单补充 Goety 至高 Boss 级仆从。
- 服务端/客户端注册逻辑调整，避免 `DistExecutor.safeRunWhenOn` 在正式环境触发不安全引用。

---

## 性能优化

- `HealthDataInterceptor`：
  - 仅创造模式玩家生效。
  - 自定义生命数据只安装一次。
  - 背包扫描限制为每 20 tick 一次。
  - 移除高频生命同步 INFO 日志。
- `LichsChaosStewBlock` / `RoyalCakeBlock`：
  - 预计算朝向/份数对应的 VoxelShape，避免每次 `getShape` 旋转。
- `TimedEntityManager`：
  - 增加空 owner 快速判断。
  - 提供 UUID 列表查询，减少实体快照创建。
- `FalseProverbsItem`：
  - 背包扫描增加 20 tick 缓存。
  - 有效性判断改为 O(1)。
  - 背面模型同步降为 5 tick 一次。
  - 移除不必要的服务端每 tick 清理。
- `SoulMendingEnchantment`：
  - 保留 4 tick 触发间隔。
  - 改为直接遍历主手/副手/头/胸/腿/脚，避免 `getAllSlots()` 全背包扫描。
- `PiPieItem`：
  - 没有待处理箭雨/追踪箭时直接跳过服务端 tick。
  - 箭雨冷却改为 5 tick。
- 神金锅/黑铁锅/天启锅配方缓存与工作站匹配逻辑优化，避免每 tick 重复查询配方。

---

## 构建

- 版本：`1.5.3fix1`
- 产物：`build/libs/goetydelight-1.5.3fix1.jar`
