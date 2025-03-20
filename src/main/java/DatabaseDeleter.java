import java.sql.*;

public class DatabaseDeleter {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "mypassword";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            clearDatabaseInBatches(conn); // Clear database safely
            System.out.println("✔ Old data deleted.");

            // Continue seeding new data...
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void clearDatabaseInBatches(Connection conn) throws SQLException {
        conn.setAutoCommit(false); // Improve performance

        // Disable foreign key constraints
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET session_replication_role = 'replica';"); // PostgreSQL-specific
        }

        int batchSize = 50000; // Adjust batch size based on memory availability

        deleteInBatches(conn, "link_click", "id", batchSize);
        deleteInBatches(conn, "sent_text_message", "id", batchSize);
        deleteInBatches(conn, "text_message_template", "id", batchSize);
        deleteInBatches(conn, "store", "id", batchSize);
        deleteInBatches(conn, "client", "id", batchSize);
        deleteInBatches(conn, "user_role", "user_id", batchSize);
        deleteInBatches(conn, "user_organization", "user_id", batchSize);
        deleteInBatches(conn, "role", "id", batchSize);
        deleteInBatches(conn, "\"user\"", "id", batchSize); // Escape reserved "user" table
        deleteInBatches(conn, "organization", "id", batchSize);

        // Re-enable foreign key constraints
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET session_replication_role = 'origin';");
        }

        conn.commit();
        System.out.println("✅ Database cleared in batches.");
    }

    private static void deleteInBatches(Connection conn, String tableName, String idColumn, int batchSize) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " IN (SELECT " + idColumn + " FROM " + tableName + " LIMIT ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            while (true) {
                stmt.setInt(1, batchSize);
                int rowsDeleted = stmt.executeUpdate();
                System.out.println("✔ Deleted " + rowsDeleted + " rows from " + tableName);

                if (rowsDeleted < batchSize) {
                    break; // Stop when no more rows left
                }
            }
        }
    }
}
