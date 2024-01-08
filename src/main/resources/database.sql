--DROP TABLE IF EXISTS gift_certificate_tag CASCADE;
--DROP TABLE IF EXISTS certificates CASCADE;
--DROP TABLE IF EXISTS tag CASCADE;

CREATE TABLE IF NOT EXISTS certificates(
    certificate_id SERIAL PRIMARY KEY NOT NULL,
    certificate_name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT 'empty', --the text can have up to 255 characters, to avoid unlimited lengths of TEXT type
    price DECIMAL(8,2) NOT NULL DEFAULT 0.0, --the number can have up to eight digits including two decimals
    duration INTEGER NOT NULL DEFAULT 1,
    create_date VARCHAR(255) NOT NULL,
    last_update_date VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tag(
    tag_id SERIAL PRIMARY KEY NOT NULL,
    tag_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS gift_certificate_tag (
    certificate_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (certificate_id, tag_id),
    FOREIGN KEY (certificate_id) REFERENCES certificates(certificate_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON UPDATE CASCADE ON DELETE CASCADE
);
