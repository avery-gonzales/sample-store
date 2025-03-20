import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.database.jvm.JdbcConnection;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseMigration {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "mypassword";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Database database = new PostgresDatabase();
            database.setConnection(new JdbcConnection(conn));

            Liquibase liquibase = new Liquibase("liquibase/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(), database);

            liquibase.update(""); // Apply all changesets
            System.out.println("Database migration completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

