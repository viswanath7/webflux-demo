DROP TABLE IF EXISTS NEWS;
CREATE TABLE NEWS (
    id BIGINT PRIMARY KEY,
    title VARCHAR(1000),
    story_date DATE,
    news_type ARRAY,
    content VARCHAR(5000) DEFAULT '',
    hyperlink VARCHAR(1200) DEFAULT '',
    score INT,
    children ARRAY
);
