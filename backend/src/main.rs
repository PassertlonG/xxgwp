mod common;
mod infrastructure;
mod modules;

use axum::Router;
use common::config::AppConfig;
use std::net::SocketAddr;
use tower_http::cors::CorsLayer;
use tower_http::trace::TraceLayer;
use tracing_subscriber::EnvFilter;

#[derive(Clone)]
pub struct AppState {
    pub config: AppConfig,
    pub db: sea_orm::DatabaseConnection,
}

async fn health_check() -> &'static str {
    "ok"
}

fn configure_routes() -> Router {
    Router::new()
        .route("/api/health", axum::routing::get(health_check))
        .layer(TraceLayer::new_for_http())
        .layer(CorsLayer::permissive())
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt()
        .with_env_filter(EnvFilter::try_from_default_env().unwrap_or_else(|_| "info".into()))
        .init();

    dotenvy::dotenv().ok();

    let config = AppConfig::from_env().unwrap_or_default();
    tracing::info!("Starting server on {}:{}", config.server.host, config.server.port);

    let app = configure_routes();

    let addr = SocketAddr::new(
        config.server.host.parse().unwrap_or_else(|_| "0.0.0.0".parse().unwrap()),
        config.server.port,
    );

    tracing::info!("Listening on {addr}");

    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;

    Ok(())
}
