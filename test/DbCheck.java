import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/wms_db";
        String user = "root";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("--- Connection Status ---");
            System.out.println("Connected to: " + url);

            DatabaseMetaData metaData = conn.getMetaData();

            System.out.println("\n--- Table Verification ---");
            checkTable(metaData, "pick_wave_gdn");
            checkTable(metaData, "pick_task");
            checkTable(metaData, "category");
            checkTable(metaData, "product_variant");

            System.out.println("\n--- Column Verification ---");
            checkColumn(metaData, "pick_task", "gdn_id");
            checkColumn(metaData, "category", "code");
            checkColumn(metaData, "category", "size_type");
            checkColumn(metaData, "product_variant", "color_hex");

        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    private static void checkTable(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
            if (rs.next()) {
                System.out.println("[OK] Table '" + tableName + "' exists.");
            } else {
                System.out.println("[FAIL] Table '" + tableName + "' DOES NOT exist.");
            }
        }
    }

    private static void checkColumn(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                System.out.println("[OK] Column '" + columnName + "' in table '" + tableName + "' exists.");
            } else {
                System.out.println("[FAIL] Column '" + columnName + "' in table '" + tableName + "' DOES NOT exist.");
            }
        }
    }
}
