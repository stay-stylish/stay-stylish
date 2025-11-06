-- V2__add_performance_indexes.sql

-- TravelOutfit 도메인
CREATE INDEX IF NOT EXISTS idx_travel_outfit_user_created_desc
    ON travel_outfit_recommendation (user_id, created_at DESC);

-- User 도메인
CREATE INDEX IF NOT EXISTS idx_users_deleted_at
    ON users (deleted_at);

-- LocalWeather 도메인
CREATE INDEX IF NOT EXISTS idx_region_grid_lat_lon
    ON region_grid (latitude, longitude);

-- DailyOutfit 도메인
CREATE INDEX IF NOT EXISTS idx_user_feedback_user_id_created_at
    ON user_category_feedback (user_id, created_at DESC);