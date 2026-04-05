package pawtner_core.pawtner_care_api.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DonationTransactionSchemaInitializer implements ApplicationRunner {

    private static final String TABLE_NAME = "donation_transactions";
    private static final String TRANSACTION_ID_COLUMN = "transaction_id";
    private static final String CREATED_DATE_COLUMN = "created_date";
    private static final String UPDATED_DATE_COLUMN = "updated_date";

    private final JdbcTemplate jdbcTemplate;

    public DonationTransactionSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureTransactionIdColumn();
        ensureTimestampColumn(CREATED_DATE_COLUMN);
        ensureTimestampColumn(UPDATED_DATE_COLUMN);
        backfillTransactionIds();
        backfillTimestampColumns();
        enforceTransactionIdNotNull();
        enforceNotNull(CREATED_DATE_COLUMN);
        enforceNotNull(UPDATED_DATE_COLUMN);
    }

    private void ensureTransactionIdColumn() {
        Integer columnCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = ?
              AND COLUMN_NAME = ?
            """,
            Integer.class,
            TABLE_NAME,
            TRANSACTION_ID_COLUMN
        );

        if (columnCount != null && columnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TRANSACTION_ID_COLUMN + " VARCHAR(32) NULL");
        }
    }

    private void ensureTimestampColumn(String columnName) {
        Integer columnCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = ?
              AND COLUMN_NAME = ?
            """,
            Integer.class,
            TABLE_NAME,
            columnName
        );

        if (columnCount != null && columnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " DATETIME NULL");
        }
    }

    private void backfillTimestampColumns() {
        jdbcTemplate.update(
            """
            UPDATE donation_transactions
            SET created_date = COALESCE(created_date, CURRENT_TIMESTAMP),
                updated_date = COALESCE(updated_date, CURRENT_TIMESTAMP)
            WHERE created_date IS NULL
               OR updated_date IS NULL
            """
        );
    }

    private void backfillTransactionIds() {
        jdbcTemplate.update(
            """
            UPDATE donation_transactions
            SET transaction_id = CONCAT('DON-', DATE_FORMAT(CURRENT_TIMESTAMP, '%Y%m%d%H%i%s'), '-', LPAD(FLOOR(RAND() * 9000) + 1000, 4, '0'))
            WHERE transaction_id IS NULL
               OR TRIM(transaction_id) = ''
            """
        );
    }

    private void enforceTransactionIdNotNull() {
        jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " MODIFY COLUMN " + TRANSACTION_ID_COLUMN + " VARCHAR(32) NOT NULL");
    }

    private void enforceNotNull(String columnName) {
        jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " MODIFY COLUMN " + columnName + " DATETIME NOT NULL");
    }
}
