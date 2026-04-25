use sea_orm::{Database, DatabaseConnection};
use crate::common::error::AppError;

pub async fn init_database(url: &str) -> Result<DatabaseConnection, AppError> {
    let conn = Database::connect(url).await?;
    tracing::info!("Database connection established");
    Ok(conn)
}
