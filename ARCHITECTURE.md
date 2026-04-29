# 项目架构文档

> 湘西果王油商城系统 — 第一期技术架构
>
> 本文档描述系统的分层架构、模块划分、数据流方向、关键设计决策及安全架构。

---

## 1. 分层架构

```
┌─────────────────────────────────────────────┐
│                 Controller 层                │
│   接收请求 → @Valid 参数校验 → 调用 Service   │
│   返回统一 ApiResponse<T>                    │
├─────────────────────────────────────────────┤
│                 Service 层                   │
│   业务逻辑编排 → @Transactional 事务边界      │
│   库存扣减 → 优惠计算 → 支付发起 → 回调处理   │
├─────────────────────────────────────────────┤
│                Repository 层                 │
│   JpaRepository<Entity, UUID>               │
│   @Query 自定义查询 → 软删除自动过滤          │
├─────────────────────────────────────────────┤
│              Entity / DTO                    │
│   Entity: JPA 实体映射（BaseEntity 子类）     │
│   DTO: Request/Response 数据传输对象          │
└─────────────────────────────────────────────┘
```

### 1.1 各层职责

| 层 | 职责 | 禁止事项 |
|:---|:---|:---|
| **Controller** | 接收 HTTP 请求、参数校验、调用 Service、封装响应 | 不写业务逻辑、不直接操作 Repository |
| **Service** | 业务逻辑实现、事务管理、跨模块调用 | 不做 HTTP 参数解析、不直接返回 HTTP 状态码 |
| **Repository** | 数据访问、JPA 查询方法定义 | 不写业务逻辑、不进行数据转换 |
| **Entity** | 数据库表映射、字段定义 | 不写业务方法、不依赖 Service/Controller |
| **DTO** | 请求/响应数据载体 | 不写业务方法、不含数据库注解 |

### 1.2 横向职责

```
@RestControllerAdvice      → 统一异常处理（全局）
Spring Security Filter     → 认证鉴权（JWT 校验）
RedisTemplate              → 缓存 / 分布式锁 / 库存扣减
@Scheduled                 → 定时任务（超时订单取消）
```

---

## 2. 模块划分

```
com.xxgwy
├── common/           公共组件
│   ├── entity/       BaseEntity、BaseTimeEntity
│   ├── exception/    BusinessException、GlobalExceptionHandler、ErrorCode 枚举
│   ├── config/       JacksonConfig、RedisConfig、SecurityConfig、CorsConfig
│   ├── util/         JwtUtil、AesUtil、SnowflakeIdUtil
│   └── dto/          ApiResponse<T>、PageResult<T>
│
├── user/             用户中心模块
│   ├── entity/       User、Address
│   ├── dto/          WxLoginRequest、WxLoginResponse、UserProfileResponse、...
│   ├── repository/   UserRepository、AddressRepository
│   ├── service/      UserService、UserServiceImpl
│   └── controller/   AuthController、UserController、AddressController
│
├── product/          商品与分类模块
│   ├── entity/       Category、Product、ProductSku
│   ├── dto/          ProductListRequest、ProductDetailResponse、...
│   ├── repository/   CategoryRepository、ProductRepository、ProductSkuRepository
│   ├── service/      CategoryService、ProductService、SkuService
│   └── controller/   CategoryController、ProductController、AdminStockController
│
├── order/            订单系统模块
│   ├── entity/       Order、OrderItem、ReturnOrder、DeliveryOrder
│   ├── dto/          CreateOrderRequest、OrderConfirmResponse、OrderDetailResponse、...
│   ├── repository/   OrderRepository、OrderItemRepository、ReturnOrderRepository、...
│   ├── service/      OrderService、OrderServiceImpl、ReturnOrderService、...
│   └── controller/   OrderController、AdminOrderController、ReturnController
│
├── logistics/        物流查询模块
│   ├── dto/          ExpressTrackResponse、...
│   ├── service/      ExpressService（对接快递100/快递鸟）
│   └── controller/   DeliveryController
│
└── payment/          支付模块
    ├── entity/       微信支付单记录（可选，防重表）
    ├── dto/          WxPayNotifyRequest、WxPayResponse、...
    ├── service/      WxPayService、PaymentService
    └── controller/   WxPayController（回调入口）
```

### 2.1 模块依赖关系

```
user ──────┐
           │
product ───┼──→ common
           │
order ─────┤
           │
logistics ─┤
           │
payment ───┘
```

- `common` 被所有模块依赖，不依赖任何模块。
- 业务模块之间通过 Service 接口调用，避免直接访问 Repository。
- `order` 可依赖 `user`（获取用户信息）、`product`（获取 SKU 信息快照）、`payment`（发起支付）。
- `payment` 可依赖 `order`（更新支付状态）。

---

## 3. 数据流方向

### 3.1 用户下单支付流

```
用户请求                         系统处理                          外部交互
────────                        ────────                          ──────

POST /api/orders/confirm    →  OrderService.confirm()
                                ├─ 查询 SKU 库存
                                ├─ 计算优惠（第二件 N 折）
                                └─ 返回确认页数据（不创建订单）

POST /api/orders             →  OrderService.createOrder()
  (用户点击"去支付")             ├─ Redis DECR 扣减库存              → Redis
                                ├─ 库存不足 → INCR 回滚 → HTTP 409
                                ├─ 写入 orders + order_items       → PostgreSQL
                                ├─ 调用微信统一下单 API              → 微信支付
                                └─ 返回支付参数给前端

POST /api/orders/wx-notify   →  WxPayService.handleNotify()        ← 微信回调
                                ├─ 幂等校验（支付单号+订单号）
                                ├─ 更新订单状态 → 已支付
                                └─ 返回成功应答给微信
```

### 3.2 发货与物流流

```
管理员后台                     系统处理                          用户端
────────                      ────────                          ──────

POST /api/admin/orders/{id}
  /deliveries               →  OrderService.ship()
                                ├─ 创建 delivery_orders 记录
                                ├─ 更新订单状态 → 已发货
                                └─ （物流轨迹由快递平台查询）

用户查看订单详情              →  DeliveryController               → 快递100/快递鸟
  GET /api/deliveries/{id}
    /express                   ├─ 调用第三方 API 查询物流轨迹
                                └─ 返回轨迹时间线
```

### 3.3 退款审核流

```
用户申请退款                 系统处理                          管理员操作
────────                    ────────                          ────────

POST /api/orders/{id}
  /refund                  →  ReturnOrderService.apply()
                                ├─ 校验订单状态（已支付/已发货）
                                ├─ 创建 return_orders 记录
                                └─ 订单状态 → 退款中

管理员审核                   →  ReturnOrderService.review()      ← POST /api/admin/returns/{id}/review
                                ├─ 通过：调用微信退款 API
                                │   └─ 订单状态 → 已退款
                                └─ 拒绝：恢复订单原状态
```

### 3.4 定时任务流

```
@Scheduled(fixedDelay = 60000)      每分钟执行一次
  OrderTimeoutService.cancelTimeoutOrders()
    ├─ 查询 status = 待支付 AND created_at < now() - 30min
    ├─ 订单状态 → 已取消
    └─ Redis INCR 回滚 SKU 库存
```

---

## 4. 关键设计决策

### 4.1 库存扣减时机

- **决策**：订单创建时（用户点击"去支付"）即扣减库存，不是支付成功时。
- **理由**：防止超卖。库存被锁定后，其他用户无法购买相同 SKU。
- **回滚**：支付失败 / 订单超时取消 → 定时任务或失败回调中回滚库存。

### 4.2 支付幂等性

- **决策**：微信支付单号（`transaction_id`）+ 订单号（`order_no`）联合唯一索引防重。
- **机制**：
  1. 回调通知到达时，先查询 `transaction_id` 是否已处理
  2. 若已处理，直接返回成功应答（不重复更新订单状态）
  3. 若未处理，更新订单状态并在支付防重表中记录

### 4.3 超时订单取消

- **决策**：待支付订单 30 分钟未支付 → 系统自动取消并释放库存。
- **实现**：Spring `@Scheduled` 定时任务，每分钟扫描一次。
- **库存回滚**：使用 Redis `INCR` 原子操作将库存加回。

### 4.4 软删除

- **决策**：所有表不执行物理 `DELETE`，通过 `is_deleted` 标记。
- **实现**：Entity 类加 `@SQLRestriction("is_deleted = false")`，JPA 自动过滤已删除记录。
- **例外**：删除操作仅将 `is_deleted` 设为 `true`，非真正删除。后期数据库维护时统一批量清理。

### 4.5 商品数据快照

- **决策**：订单商品的价格（`price`）、商品名（`product_name`）、规格名（`sku_name`）必须在 `order_items` 中快照。
- **理由**：即使商品后续调价或下架，订单信息不受影响，保证交易记录的可追溯性。

### 4.6 无购物车

- **决策**：第一期不实现购物车功能。
- **流程**：用户直接从商品详情页 → 选择规格/数量 → 点击"立即购买" → 进入订单确认页 → 点击支付。

### 4.7 登录方式

- **决策**：仅支持微信静默授权 + 手机号授权，不支持账号密码注册/登录。
- **认证流程**：微信 OAuth → 获取 `openid` → 服务端生成 JWT → 返回前端 → 后续请求携带 JWT。

### 4.8 支付渠道

- **决策**：用户端仅支持微信支付。
- **例外**：厂家后台可手动创建订单并记录线下支付方式（私对公、转账等），仅做记录用途。

---

## 5. 安全架构

### 5.1 认证流程

```
用户打开小程序
  → 微信静默授权（获取 code）
  → POST /api/auth/wx-login { code }
  → 服务端调用微信 code2Session 获取 openid + session_key
  → 服务端查找或创建用户记录
  → 生成 JWT（含 userId + role），返回给前端
  → 前端存储 token，后续请求在 Header 中携带：
      Authorization: Bearer <jwt_token>
```

### 5.2 鉴权机制

- 框架：Spring Security + `@PreAuthorize` 注解
- JWT 解析 Filter：从 `Authorization` Header 提取 token → 校验签名 → 注入 `SecurityContext`
- 角色校验：
  - `hasRole('CUSTOMER')` — 普通消费者
  - `hasRole('ADMIN')` — 厂家/管理员

### 5.3 敏感数据保护

| 字段 | 保护方式 |
|:---|:---|
| `openid` | AES-256 加密存储，不解密到 DTO 响应中 |
| `phone` | AES-256 加密存储，返回时脱敏（`138****1234`） |
| `session_key` | 仅用于微信接口调用，不持久化 |
| JWT secret | 通过环境变量注入，不入库 |

### 5.4 API 限流

- 使用 Redis + 令牌桶算法
- 针对高敏感接口（登录、下单、支付回调）限流
- 超出限制返回 HTTP 429

---

## 6. 技术栈总览

| 层级 | 技术 | 版本 |
|:---|:---|:---|
| 语言 | Kotlin | 1.9+ |
| 框架 | Spring Boot | 3.x |
| ORM | Spring Data JPA + Hibernate | 6.x |
| 数据库 | PostgreSQL | 16+ |
| 缓存 | Redis | 7+ |
| 前端-用户端 | Taro 4.x (React) → 微信小程序 | 4.x |
| 前端-后台 | H5 网页 (React / Taro H5) | — |
| 支付 | 微信支付 API V3 | — |
| 物流 | 快递100 / 快递鸟 API | — |

---

> 本文档在第一期开发启动前定稿，架构决策如有变更，需同步更新本文档并记录变更原因。
