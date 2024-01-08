package esm.queries;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PostgreSqlQueries {

    //CERTIFICATES
    public static final String GET_GIFT_CERTIFICATE_BY_NAME = "SELECT * FROM certificates WHERE certificate_name = ?";
    public static final String GET_GIFT_CERTIFICATE_BY_SEARCH_WORD = "SELECT * FROM certificates WHERE certificate_name LIKE ? OR description LIKE ?";

    public static final String GET_GIFT_CERTIFICATE_BY_ID = "SELECT * FROM certificates WHERE certificate_id = ?";

    public static final String DELETE_GIFT_CERTIFICATE_BY_ID = "DELETE FROM certificates WHERE certificate_id = ?";

    //TAGS
    public static final String SAVE_TAG = "INSERT INTO tag (tag_name) VALUES (?)";

    public static final String GET_TAG_BY_ID = "SELECT * FROM tag WHERE tag_id = ?";
    public static final String GET_TAG_BY_NAME = "SELECT * FROM tag WHERE tag_name = ?";

    public static final String DELETE_TAG_BY_ID = "DELETE FROM tag WHERE tag_id = ?";

    //CERTIFICATE TAGS
    public static final String SAVE_TAGS_TO_GIFT_CERTIFICATES = "INSERT INTO gift_certificate_tag (certificate_id, tag_id) VALUES(?, ?)";
    public static final String GET_TAGS_BY_CERTIFICATE_ID = "SELECT tag_id FROM gift_certificate_tag WHERE certificate_id = ?";
    public static final String GET_CERTIFICATES_BY_TAG_ID = "SELECT certificate_id FROM gift_certificate_tag WHERE tag_id = ?";

    public static final String DELETE_CERTIFICATE_FROM_JOINT_TABLE = "DELETE FROM gift_certificate_tag WHERE certificate_id = ?";
    public static final String DELETE_TAG_FROM_JOINT_TABLE = "DELETE FROM gift_certificate_tag WHERE tag_id = ?";

    public static final String UPDATE_GIFT_CERTIFICATE = "UPDATE certificates SET certificate_name = COALESCE(?, certificate_name), description = COALESCE(?, description), price = COALESCE(?, price), duration = COALESCE(?, duration), last_update_date = TO_CHAR(CURRENT_TIMESTAMP, '%s') WHERE certificate_id = ?";
    public static final String SAVE_GIFT_CERTIFICATE = "INSERT INTO certificates (certificate_name, description, price, duration, create_date, last_update_date) VALUES (?, ?, ?, ?, TO_CHAR(CURRENT_TIMESTAMP, '%s'), TO_CHAR(CURRENT_TIMESTAMP, '%s'))";
    private String timestampFormat;

    @Value("${sql.timestamp-format}")
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public String getUpdateGiftCertificate() {
        return String.format(UPDATE_GIFT_CERTIFICATE, timestampFormat);
    }

    public String getSaveGiftCertificate() {
        return String.format(SAVE_GIFT_CERTIFICATE, timestampFormat, timestampFormat);
    }

}
