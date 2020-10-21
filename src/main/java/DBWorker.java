import java.sql.*;

public class DBWorker {
    private static final String URL = "jdbc:postgresql://kandula.db.elephantsql.com:5432/okyahpzd";
    private static final String USER = "okyahpzd";
    private static final String PASS = "qAa4s3buu4SGmdB-E-Qx_BBUG9H6KRpX";
    private static Connection connection;

    public DBWorker() {
        try {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USER, PASS);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
