package esm.mapper;

import static com.epam.esm.enums.Columns.*;

import com.epam.esm.model.GiftCertificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
@Component
public class GiftCertificateRowMapper implements RowMapper<GiftCertificate> {

    @Override
    public GiftCertificate mapRow(ResultSet rs, int rowNum) throws SQLException {


        return GiftCertificate.builder()
                .id(rs.getLong(GIFT_CERTIFICATE_ID.getColumn()))
                .name(rs.getString(GIFT_CERTIFICATE_NAME.getColumn()))
                .description(rs.getString(GIFT_CERTIFICATE_DESCRIPTION.getColumn()))
                .price(rs.getDouble(GIFT_CERTIFICATE_PRICE.getColumn()))
                .duration(rs.getLong(GIFT_CERTIFICATE_DURATION.getColumn()))
                .createDate(rs.getString(GIFT_CERTIFICATE_CREATE_DATE.getColumn()))
                .lastUpdateDate(rs.getString(GIFT_CERTIFICATE_LAST_UPDATE_DATE.getColumn()))
                .build();
    }
}


