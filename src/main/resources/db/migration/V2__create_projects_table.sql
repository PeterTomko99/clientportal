CREATE TABLE projects (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT               NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(200)         NOT NULL,
    description TEXT,
    status      VARCHAR(20)          NOT NULL DEFAULT 'PENDING',
    deadline    DATE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
