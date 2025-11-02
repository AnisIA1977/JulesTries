CREATE TABLE naval_units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    level INT NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE logistic_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id_str VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    unit_id BIGINT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    due_date DATE,
    FOREIGN KEY (unit_id) REFERENCES naval_units(id)
);

CREATE TABLE kpis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    change_description VARCHAR(255),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
