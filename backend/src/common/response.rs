use axum::Json;
use serde::Serialize;

#[derive(Serialize)]
pub struct ApiResponse<T> {
    pub code: u16,
    pub message: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<T>,
}

impl<T: Serialize> ApiResponse<T> {
    pub fn success(data: T) -> Json<Self> {
        Json(Self {
            code: 200,
            message: "ok".into(),
            data: Some(data),
        })
    }
}

impl ApiResponse<()> {
    pub fn message(msg: &str) -> Json<Self> {
        Json(Self {
            code: 200,
            message: msg.into(),
            data: None,
        })
    }
}

#[derive(Serialize)]
pub struct PaginatedResponse<T> {
    pub code: u16,
    pub message: String,
    pub data: Vec<T>,
    pub total: u64,
    pub page: u64,
    pub page_size: u64,
}

impl<T: Serialize> PaginatedResponse<T> {
    pub fn new(data: Vec<T>, total: u64, page: u64, page_size: u64) -> Json<Self> {
        Json(Self {
            code: 200,
            message: "ok".into(),
            data,
            total,
            page,
            page_size,
        })
    }
}
