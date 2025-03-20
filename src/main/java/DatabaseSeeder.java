import com.github.javafaker.Faker;

import java.sql.*;
import java.util.*;
import java.util.random.RandomGenerator;

public class DatabaseSeeder {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "mypassword";
    private static final int NUM_ORGS = 1000;
    private static final int NUM_TEMPLATES_PER_ORG = 30;
    private static final int NUM_STORES_PER_ORG = 2;
    private static final int NUM_USERS_PER_ORG = 1;
    private static final int NUM_TEXTS_PER_STORE = 2000;  // Large dataset
    private static final double CLICK_THROUGH_RATE = 0.15; // 15% of texts get clicked

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            Faker faker = new Faker();

            // Insert Organizations
            List<Integer> orgIds = insertOrganizations(conn, faker);

            // Insert Users
            insertUsers(conn, faker, orgIds);
            conn.commit(); // ✅ Ensure Users are committed

            // Insert Stores
            List<Integer> storeIds = insertStores(conn, faker, orgIds);
            conn.commit(); // ✅ Ensure Users are committed

            // Insert Clients
            int totalClients = storeIds.size() * 50;
            insertClients(conn, faker, totalClients);
            conn.commit(); // ✅ Ensure Users are committed

            // Insert Templates and store the store-template mapping
            Map<Integer, List<Integer>> storeTemplateMap = insertTemplates(conn, faker, storeIds);

            // Insert Messages using valid store-template mapping
            insertTextMessagesAndClicks(conn, faker, storeIds, storeTemplateMap);
            conn.commit(); // ✅ Ensure Stores are committed

            System.out.println("✅ Database seeding completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> insertOrganizations(Connection conn, Faker faker) throws SQLException {
        List<Integer> orgIds = new ArrayList<>();
        String sql = "INSERT INTO organization (name, created_at) VALUES (?, NOW()) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < NUM_ORGS; i++) {
                stmt.setString(1, faker.company().name());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) orgIds.add(rs.getInt(1));
            }
        }
        System.out.println("✔ Inserted " + orgIds.size() + " organizations.");
        return orgIds;
    }

    private static void insertUsers(Connection conn, Faker faker, List<Integer> orgIds) throws SQLException {
        String sql = "INSERT INTO \"user\" (first_name, last_name, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer orgId : orgIds) {
                stmt.setString(1, faker.name().firstName());
                stmt.setString(2, faker.name().lastName());
                stmt.addBatch();
            }
            stmt.executeBatch();  // Execute all inserts in a batch
        }
        System.out.println("✔ Inserted " + orgIds.size() + " users.");
    }

    private static List<Integer> insertStores(Connection conn, Faker faker, List<Integer> orgIds) throws SQLException {
        List<Integer> storeIds = new ArrayList<>();
        String sql = "INSERT INTO store (name, lat, long, org_id, created_at) VALUES (?, ?, ?, ?, NOW()) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer orgId : orgIds) {
                for (int j = 0; j < NUM_STORES_PER_ORG; j++) {
                    stmt.setString(1, faker.company().name() + " Store");
                    stmt.setDouble(2, Double.parseDouble(faker.address().latitude()));
                    stmt.setDouble(3, Double.parseDouble(faker.address().longitude()));
                    stmt.setInt(4, orgId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) storeIds.add(rs.getInt(1));
                }
            }
        }
        System.out.println("✔ Inserted " + storeIds.size() + " stores.");
        return storeIds;
    }

    private static Map<Integer, List<Integer>> insertTemplates(Connection conn, Faker faker, List<Integer> storeIds) throws SQLException {
        Map<Integer, List<Integer>> storeTemplateMap = new HashMap<>();
        String sql = "INSERT INTO text_message_template (store_id, template_name, message_text, created_at) VALUES (?, ?, ?, NOW()) RETURNING id";

        Set<String> uniqueTemplateNames = new HashSet<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer storeId : storeIds) {  // ✅ Only pick stores that actually exist
                List<Integer> templateIds = new ArrayList<>();

                for (int j = 0; j < NUM_TEMPLATES_PER_ORG; j++) {
                    String templateName;
                    do {
                        templateName = "Template " + faker.lorem().word() + RandomGenerator.getDefault().nextInt();
                    } while (!uniqueTemplateNames.add(templateName));

                    stmt.setInt(1, storeId);
                    stmt.setString(2, templateName);
                    stmt.setString(3, faker.lorem().sentence());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        templateIds.add(rs.getInt(1)); // ✅ Store the template ID under this store
                    }
                }
                storeTemplateMap.put(storeId, templateIds);
            }
        }
        System.out.println("✔ Inserted " + (storeIds.size() * NUM_TEMPLATES_PER_ORG) + " templates.");
        return storeTemplateMap;
    }


    private static void insertClients(Connection conn, Faker faker, int numClients) throws SQLException {
        String sql = "INSERT INTO client (first_name, last_name, phone_number, created_at) VALUES (?, ?, ?, NOW())";

        Set<String> usedPhoneNumbers = new HashSet<>(); // To store unique phone numbers

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int insertedCount = 0;
            while (insertedCount < numClients) {
                String phoneNumber;

                // Generate unique phone numbers
                do {
                    phoneNumber = faker.phoneNumber().cellPhone().replaceAll("[^0-9]", ""); // Keep only digits
                } while (!usedPhoneNumbers.add(phoneNumber)); // Retry if duplicate

                stmt.setString(1, faker.name().firstName());
                stmt.setString(2, faker.name().lastName());
                stmt.setString(3, phoneNumber);
                stmt.addBatch();

                insertedCount++;
            }
            stmt.executeBatch();
        }
        System.out.println("✔ Inserted " + numClients + " unique clients.");
    }

    private static List<Integer> getClientIds(Connection conn) throws SQLException {
        List<Integer> clientIds = new ArrayList<>();
        String sql = "SELECT id FROM client";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                clientIds.add(rs.getInt("id"));
            }
        }
        return clientIds;
    }

    private static void insertTextMessagesAndClicks(Connection conn, Faker faker, List<Integer> storeIds, Map<Integer, List<Integer>> storeTemplateMap) throws SQLException {
        String sql = "INSERT INTO sent_text_message (store_id, client_id, template_id, message_content, sent_at, tracking_link) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String clickSql = "INSERT INTO link_click (sent_text_message_id, clicked_at) VALUES (?, ?)";

        List<Integer> clientIds = getClientIds(conn);
        if (clientIds.isEmpty()) {
            System.err.println("❌ No clients found. Cannot send messages.");
            return;
        }

        Random random = new Random();
        try (PreparedStatement textStmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement clickStmt = conn.prepareStatement(clickSql)) {

            for (Integer storeId : storeIds) {
                // Ensure the store has valid templates
                List<Integer> templateIds = storeTemplateMap.get(storeId);
                if (templateIds == null || templateIds.isEmpty()) {
                    System.err.println("❌ No templates found for store_id=" + storeId);
                    continue;
                }

                for (int i = 0; i < NUM_TEXTS_PER_STORE; i++) {
                    int clientId = clientIds.get(random.nextInt(clientIds.size())); // Pick a valid client
                    int templateId = templateIds.get(random.nextInt(templateIds.size())); // Pick a valid template for this store
                    String trackingLink = faker.internet().url();

                    long twoYearsMillis = 2L * 365 * 24 * 60 * 60 * 1000;
                    Timestamp sentAt = new Timestamp(System.currentTimeMillis() - random.nextLong(twoYearsMillis));

                    textStmt.setInt(1, storeId);
                    textStmt.setInt(2, clientId);
                    textStmt.setInt(3, templateId);
                    textStmt.setString(4, "Exclusive Offer! " + trackingLink);
                    textStmt.setTimestamp(5, sentAt);
                    textStmt.setString(6, trackingLink);
                    textStmt.executeUpdate();

                    ResultSet generatedKeys = textStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int sentTextMessageId = generatedKeys.getInt(1);

                        if (random.nextDouble() < 0.15) { // 15% click rate
                            long clickDelayMillis = Math.abs(random.nextLong(30L * 24 * 60 * 60 * 1000));
                            Timestamp clickedAt = new Timestamp(sentAt.getTime() + clickDelayMillis);

                            clickStmt.setInt(1, sentTextMessageId);
                            clickStmt.setTimestamp(2, clickedAt);
                            clickStmt.addBatch();
                        }
                    }
                }
            }
            clickStmt.executeBatch();
        }
        System.out.println("✔ Inserted " + (storeIds.size() * NUM_TEXTS_PER_STORE) + " text messages.");
    }


    private static int getOrgIdForStore(Connection conn, int storeId) throws SQLException {
        String sql = "SELECT org_id FROM store WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("org_id");
            }
        }
        throw new SQLException("❌ Store ID not found: " + storeId);
    }

}
