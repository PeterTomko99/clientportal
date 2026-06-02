CREATE TABLE invoices (
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT               NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    amount     NUMERIC(10, 2)       NOT NULL,
    due_date   DATE                 NOT NULL,
    status     VARCHAR(20)          NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
