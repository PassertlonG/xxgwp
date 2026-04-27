# Project Phases

## Current Phase: Phase 3

Phase 3: Product & Dealer Management — 商品与经销商管理

## Phase Checklist

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | Completed | Project skeleton and infrastructure setup (Spring Boot + Gradle + JDK 21 + PostgreSQL + Redis + Docker) |
| Phase 2 | Completed | User authentication module (Kotlin, register/login/me/refresh + admin CRUD, 20 tests) |
| **Phase 3** | **In Progress** | Product & Dealer Management |
| Phase 4 | Pending | Order & Transaction Module |
| Phase 5 | Pending | Frontend pages |
| Phase 6 | Pending | Notifications & Optimization |

## Deviation Rule

When the user requests a feature/change that belongs to a Phase OTHER than the current Phase:

1. **First, save current work**: Run `git add . && git commit -m "chore: save before switching to [requested-phase]"`
2. **Do NOT push** to remote
3. **Notify the user**: Inform them that the work has been committed locally and the request falls under a different Phase
4. Only proceed with the requested work after the user explicitly confirms
