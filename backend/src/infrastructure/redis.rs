use redis::aio::MultiplexedConnection;
use crate::common::error::AppError;

pub async fn init_redis(url: &str) -> Result<MultiplexedConnection, AppError> {
    let client = redis::Client::open(url)
        .map_err(|e| AppError::Internal(format!("Redis connection failed: {e}")))?;

    let conn = client
        .get_multiplexed_tokio_connection()
        .await
        .map_err(|e| AppError::Internal(format!("Redis connect error: {e}")))?;

    tracing::info!("Redis connection established");
    Ok(conn)
}
