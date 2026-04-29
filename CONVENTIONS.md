# 编码规范

> 湘西果王油商城系统 — 第一期开发编码约定
>
> 本文档定义项目编码、命名、提交等方面的统一规范，所有代码贡献必须遵守。

---

## 1. 命名规范

### 1.1 Java / Kotlin 命名

| 元素 | 规范 | 示例 |
|:---|:---|:---|
| 类名（Entity / DTO / Service / Controller） | PascalCase | `User`, `OrderItem`, `AddressService` |
| 方法名 | camelCase | `getUserById()`, `createOrder()` |
| 变量名 | camelCase | `orderNo`, `payAmount`, `isDeleted` |
| 常量 | UPPER_SNAKE_CASE | `MAX_ADDRESS_COUNT`, `ORDER_TIMEOUT_MINUTES` |
| 枚举值 | UPPER_SNAKE_CASE | `PENDING_PAYMENT`, `PAID`, `SHIPPED` |

### 1.2 数据库表名与字段

| 元素 | 规范 | 示例 |
|:---|:---|:---|
| 表名 | snake_case 复数 | `users`, `orders`, `order_items`, `product_skus` |
| 字段名 | snake_case 单数 | `user_id`, `created_at`, `is_deleted` |
| 主键 | `id UUID` | 统一使用 UUID |
| 外键 | `{关联表单数}_id` | `user_id` → `users.id`, `order_id` → `orders.id` |

数据库表汇总：

| 表名 | 实体 |
|:---|:---|
| `users` | 用户 |
| `addresses` | 收货地址 |
| `categories` | 商品分类 |
| `products` | 商品 |
| `product_skus` | 商品规格 (SKU) |
| `orders` | 订单主表 |
| `order_items` | 订单商品明细 |
| `return_orders` | 退款/售后单 |
| `delivery_orders` | 配送单 |

### 1.3 API 路径

| 规范 | 示例 |
|:---|:---|
| 统一前缀 `/api/` | `/api/user/me` |
| 资源名使用 kebab-case 复数 | `/api/order-items` |
| RESTful 风格 | `GET /api/products`, `POST /api/products`, `PUT /api/products/{id}` |
| 子资源嵌套不超过一层 | `/api/orders/{id}/confirm-receipt` |

### 1.4 包结构

```
com.xxgwy
├── common/          # 公共组件
│   ├── entity/      #   BaseEntity
│   ├── exception/   #   全局异常处理
│   ├── config/      #   公共配置
│   └── util/        #   工具类
├── user/            # 用户中心模块
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── product/         # 商品与分类模块
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── order/           # 订单系统模块
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── logistics/       # 物流查询模块
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
└── payment/         # 支付模块
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/
    └── dto/
```

分层规则：
- **controller** — 接收请求、参数校验（`@Valid`）、调用 Service、返回响应
- **service** — 接口定义（`interface`）+ 实现（`impl/` 子包或同包 `*ServiceImpl`）
- **repository** — 继承 `JpaRepository<Entity, UUID>`
- **entity** — JPA 实体类
- **dto** — Request / Response DTO，用 Kotlin `data class`

---

## 2. Git 提交规范

### 2.1 提交格式

使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
type: description

type: scoped description
```

### 2.2 type 类型

| type | 用途 | 示例 |
|:---|:---|:---|
| `feat` | 新功能 | `feat: add user wx-login API` |
| `fix` | 修复 bug | `fix: resolve inventory race condition on order create` |
| `docs` | 文档变更 | `docs: add context quick reference` |
| `refactor` | 重构 | `refactor: extract BaseEntity from entity classes` |
| `chore` | 杂项（构建、依赖等） | `chore: upgrade Spring Boot to 3.2.x` |
| `test` | 测试 | `test: add order service unit tests` |

### 2.3 分支策略

| 分支 | 用途 |
|:---|:---|
| `main` | 生产就绪代码 |
| `dev` | 开发集成分支 |
| `feat/{name}` | 功能分支，从 `dev` 拉出，合并回 `dev` |

---

## 3. JPA Entity 约定

### 3.1 BaseEntity

所有 Entity 必须继承 `BaseEntity`：

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ColumnDefault("false")
    var isDeleted: Boolean = false

    @CreatedDate
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}
```

### 3.2 软删除

- 所有实体必须在类级别添加：`@SQLRestriction("is_deleted = false")`
- 所有实体必须包含字段：`@ColumnDefault("false") var isDeleted: Boolean = false`
- **禁止**物理删除（`DELETE`），删除操作仅将 `isDeleted` 设为 `true`
- 若需恢复数据，将 `isDeleted` 设回 `false`

### 3.3 枚举映射

所有枚举字段必须使用：

```kotlin
@Enumerated(EnumType.STRING)
var status: OrderStatus = OrderStatus.PENDING_PAYMENT
```

### 3.4 主键策略

- 所有 Entity 主键类型：`UUID`
- 主键生成策略：`GenerationType.UUID`（由 JPA/Hibernate 自动生成）

### 3.5 时间戳

- `createdAt` + `@CreatedDate`：自动填充创建时间
- `updatedAt` + `@LastModifiedDate`：自动填充更新时间
- 需配合 `@EntityListeners(AuditingEntityListener::class)` 及启用 `@EnableJpaAuditing`

---

## 4. API 响应格式

### 4.1 统一响应体

```json
// 成功
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 业务错误
{
  "code": 400,
  "message": "具体错误信息",
  "data": null
}
```

### 4.2 HTTP 状态码约定

| 状态码 | 场景 |
|:---|:---|
| `200` | 请求成功 |
| `400` | 参数校验失败、业务规则不满足 |
| `401` | 未登录或 token 过期 |
| `403` | 无权限访问 |
| `404` | 资源不存在 |
| `409` | 资源冲突（如库存不足、重复提交） |

### 4.3 统一异常处理

使用 `@RestControllerAdvice` 实现全局异常处理：

- 参数校验异常（`MethodArgumentNotValidException`）→ 400
- 业务异常（自定义 `BusinessException`）→ 400 / 409
- 认证异常（`AuthenticationException`）→ 401
- 鉴权异常（`AccessDeniedException`）→ 403
- 其他未捕获异常 → 500（生产环境不暴露堆栈）

### 4.4 参数校验

- Controller 请求体参数使用 `@Valid @RequestBody`
- DTO 字段使用 `jakarta.validation` 注解：`@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Max` 等
- 校验失败返回 400，`message` 中包含具体字段错误

---

## 5. Kotlin 编码风格

### 5.1 DTO

- 使用 Kotlin `data class` 定义 Request / Response DTO
- 响应 DTO 可嵌套其他 DTO
- 避免在 DTO 中引入业务逻辑

```kotlin
data class CreateOrderRequest(
    @field:NotNull val skuId: UUID,
    @field:Min(1) val quantity: Int,
    @field:NotNull val addressId: UUID
)
```

### 5.2 Controller

- 使用 `@RestController` + `@RequestMapping`
- 方法参数使用 `@Valid @RequestBody` / `@PathVariable` / `@RequestParam`
- 返回类型统一为 `ApiResponse<T>`（自定义响应包装类）
- 条件查询使用 `@RequestParam` 传参，路径变量用 `@PathVariable`

### 5.3 Service

- 每个模块定义 `interface`（如 `UserService`）和对应的 `class UserServiceImpl`
- 事务注解 `@Transactional` 放在 Service 层方法上
- 读操作使用 `@Transactional(readOnly = true)`

### 5.4 Repository

- 继承 `JpaRepository<Entity, UUID>`
- 使用方法命名约定定义查询：`findByUserIdAndIsDeletedFalse(userId: UUID)`
- 复杂查询使用 `@Query` 注解（JPQL），避免使用原生 SQL

---

## 6. 其他约定

### 6.1 日志

- 使用 SLF4J + Logback
- 关键操作记录 INFO 日志（登录、下单、支付回调、发货、退款）
- 异常记录 ERROR 日志（含请求上下文）
- 禁止打印敏感信息（openid、手机号明文）

### 6.2 配置文件

- 敏感配置（密钥、数据库密码）通过环境变量注入，不入库
- 开发/生产环境配置使用 Spring Profile 分离（`application-dev.yml` / `application-prod.yml`）
- Redis 配置、微信支付配置、物流 API 配置均放在对应 Profile 配置文件中

### 6.3 测试

- 单元测试：JUnit 5 + MockK（Kotlin）
- Service 层核心逻辑必须覆盖单元测试
- 库存扣减、支付回调、优惠计算为必测场景

---

> 本规范在第一期开发启动前定稿，后续如有修订，需同步更新本文档并提交 `docs` 类型的 commit。
