-- certificate table--
create table IF NOT EXISTS certificates(
    certificate_id SERIAL PRIMARY KEY NOT NULL,
    certificate_name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL, --the text can have up to 255 characters, to avoid unlimited lengths of TEXT type
    price DECIMAL(8,2) NOT NULL, --the number can have up to eight digits including two decimals
    duration INT NOT NULL,
    create_date VARCHAR(255) NOT NULL,
    last_update_date VARCHAR(255) NOT NULL
);

create table IF NOT EXISTS tag(
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

INSERT INTO certificates (certificate_name, description, price, duration, create_date, last_update_date) VALUES ('certificate'  , 'description 1'    , 310.00, 20, '2023-11-21T16:48:04:309Z', '2023-12-25T16:48:04:309Z');
INSERT INTO certificates (certificate_name, description, price, duration, create_date, last_update_date) VALUES ('certificate 2', 'description 2'    , 372.12, 11, '2023-11-25T16:28:04:309Z', '2023-12-20T16:48:04:309Z');
INSERT INTO certificates (certificate_name, description, price, duration, create_date, last_update_date) VALUES ('name 3'       , 'description three', 300.50, 23, '2023-11-24T16:18:04:309Z', '2023-12-10T16:48:04:309Z');

INSERT INTO tag (tag_name) VALUES ('tag 3');
INSERT INTO tag (tag_name) VALUES ('tag 1');
INSERT INTO tag (tag_name) VALUES ('tag 2');
INSERT INTO tag (tag_name) VALUES ('blue');
INSERT INTO tag (tag_name) VALUES ('colour');
INSERT INTO tag (tag_name) VALUES ('animal 1');

INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (1, 1);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (1, 5);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (2, 2);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (2, 6);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (3, 1);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (3, 3);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (3, 4);
INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES (3, 5);
