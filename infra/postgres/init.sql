CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    auth_token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_users_auth_token ON users(auth_token);

-- Seed sample users
INSERT INTO users (id, username, email, auth_token) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'alice', 'alice@example.com', 'token-alice'),
    ('a0000000-0000-0000-0000-000000000002', 'bob', 'bob@example.com', 'token-bob'),
    ('a0000000-0000-0000-0000-000000000003', 'charlie', 'charlie@example.com', 'token-charlie'),
    ('a0000000-0000-0000-0000-000000000004', 'diana', 'diana@example.com', 'token-diana'),
    ('a0000000-0000-0000-0000-000000000005', 'eve', 'eve@example.com', 'token-eve')
ON CONFLICT DO NOTHING;
