# 项目速查卡片 (CONTEXT)

> 快速恢复上下文，每次中断后回归的第一份文档。比 SRS 更轻量，只记关键信息。

---

## 项目名称

**湘西果王油商城系统** — 面向中产家庭的健康食品自营电商小程序（含分销体系，一期仅做交易闭环）。

---

## 技术栈

`Kotlin + Spring Boot 3 + JPA + PostgreSQL + Redis` | `Taro 4.x (React) → 微信小程序` | `H5 后台管理`

---

## 当前阶段

**第一期：需求分析与设计**

- [x] 需求调研完成（15 轮 QA）
- [x] SRS 定稿（4 模块 × 9 张实体表）
- [x] PlantUML 用例图 / 状态图 / ER 图 / 活动图 / 支付序列图
- [x] 可行性自检通过
- [x] 编码规范 (`CONVENTIONS.md`) 定稿
- [x] 架构文档 (`ARCHITECTURE.md`) 定稿
- [ ] 本地开发环境搭建（Docker: PostgreSQL + Redis）
- [ ] 项目脚手架初始化（Spring Boot Kotlin 项目）
- [ ] 第一期模块编码启动

---

## 启动步骤

> 目前仅有文档阶段，以下为待补充项：

1. Docker 启动 PostgreSQL + Redis
2. 初始化 Spring Boot 项目，配置 `application-dev.yml`
3. 创建 `BaseEntity` 及各模块 Entity
4. 按模块顺序开发：`user` → `product` → `order` → `logistics` → `payment`

---

## 关键决策摘要

| # | 决策 | 说明 |
|:---|:---|:---|
| 1 | **软删除** | 所有表统一 `is_deleted BOOLEAN DEFAULT FALSE`，不物理删除 |
| 2 | **库存扣减时机** | 订单创建时（点击支付）即扣，非支付成功时；超时/失败回滚 |
| 3 | **登录方式** | 仅微信静默授权 + 手机号授权，无账号密码 |
| 4 | **无购物车** | 第一期直接从详情页"立即购买"进入确认页 |
| 5 | **支付仅微信** | 用户端仅微信支付；厂家后台可记录线下支付 |
| 6 | **第二件打折** | 第一期唯一优惠：同规格第二件 N 折，折扣存入 `orders.discount_amount` |
| 7 | **商品数据快照** | 下单时商品名、规格名、价格快照到 `order_items`，不受后续调价影响 |
| 8 | **商品挂叶子分类** | 分类树无限级，商品只能挂在最末级分类下 |
| 9 | **30 分钟超时取消** | 定时任务扫描待支付超时订单，自动取消 + 回滚库存 |
| 10 | **支付幂等** | 微信支付单号 + 订单号唯一索引防重回调 |
| 11 | **"无条件仅退款"政策（二期）** | 每人限一次，二期通过微信支付实名 + 公安实名认证实现自然人唯一性校验 |

---

## 文件/模块索引

### 需求与设计文档（`docs/`）

| 文件 | 说明 |
|:---|:---|
| [`软件需求规格说明书.md`](./软件需求规格说明书.md) | 一期 SRS：4 模块完整需求、实体表、API 接口、非功能需求 |
| [`需求调研问答记录.md`](./需求调研问答记录.md) | 15 轮 QA 原始对话，所有决策的来源 |
| [`湘西果王油软件开发可行性分析.md`](./湘西果王油软件开发可行性分析.md) | 可行性自检清单（技术/时间/资源/风险） |
| `用例图.puml` | PlantUML 用例图 |
| `状态图.puml` | PlantUML 订单状态机图 |
| `实体关系图.puml` | PlantUML ER 图 |
| `活动图.puml` | PlantUML 下单活动图 |
| `支付序列图.puml` | PlantUML 支付序列图 |

### 工程规范文档（根目录）

| 文件 | 说明 |
|:---|:---|
| [`CONVENTIONS.md`](../CONVENTIONS.md) | 编码规范：命名、提交、Entity、API 响应、Kotlin 风格 |
| [`ARCHITECTURE.md`](../ARCHITECTURE.md) | 架构文档：分层、模块、数据流、设计决策、安全 |

### 数据库实体速查

| 表名 | 模块 | 核心字段 |
|:---|:---|:---|
| `users` | 用户中心 | id, openid, phone, nickname, avatar, role |
| `addresses` | 用户中心 | id, user_id, name, phone, province, city, district, detail, is_default |
| `categories` | 商品分类 | id, parent_id, name, sort_order, enabled |
| `products` | 商品 | id, name, description, category_id, images, status |
| `product_skus` | 商品规格 | id, product_id, name, price, stock, image_url |
| `orders` | 订单 | id, order_no, user_id, status, total_amount, discount_amount, pay_amount, address_snapshot |
| `order_items` | 订单明细 | id, order_id, product_id, sku_id, product_name, sku_name, price, quantity, subtotal |
| `return_orders` | 退款 | id, return_no, order_id, refund_type, refund_reason, refund_amount, status |
| `delivery_orders` | 配送 | id, delivery_no, order_id, express_company, express_no, status |

### API 前缀

所有接口以 `/api/` 开头，各模块路径前缀：

| 模块 | 路径前缀 | 说明 |
|:---|:---|:---|
| 认证 | `/api/auth/` | 微信登录 |
| 用户 | `/api/user/` | 个人信息 |
| 地址 | `/api/addresses/` | 收货地址 CRUD |
| 分类 | `/api/categories/` | 分类树管理 |
| 商品 | `/api/products/` | 商品 CRUD + 上下架 |
| 库存 | `/api/admin/stock/` | 后台库存管理 |
| 订单(买家) | `/api/orders/` | 下单、列表、详情、收货、退款 |
| 订单(卖家) | `/api/admin/orders/` | 后台订单管理、发货 |
| 退款审核 | `/api/admin/returns/` | 后台退款审核 |
| 配送查询 | `/api/deliveries/` | 物流轨迹查询 |
| 支付回调 | `/api/orders/wx-notify` | 微信支付回调通知 |

---

## Git 分支说明

| 分支 | 说明 |
|:---|:---|
| `main` | 生产分支（尚未创建，待 MVP 完成） |
| `dev` | 开发主分支，当前所有文档和数据文件在此 |
| `feat/*` | 功能分支，开发新功能时从 `dev` 拉出 |

---

> 每次中断后回归开发时，先读此文档 5 分钟即可恢复上下文。如有重大决策变更，及时更新此文档。
