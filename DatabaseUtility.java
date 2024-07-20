package com.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseUtility {

    private static final String PROPERTIES_FILE = "datasources.config";
    private final Map<String, Connection> connections = new HashMap<>();

    private final Map<String, Map<String,String>> databaseConfigMap = new HashMap<>() ;

    public DatabaseUtility() {
//        loadPropertiesAndInitializeConnections(PROPERTIES_FILE);
        init();
    }

    public void init()
    {
        try {
        loadProperties(PROPERTIES_FILE);
        setupConnections();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize datasources");
            System.exit(1);
        }
    }

    private void setupConnections() throws SQLException {
        if(databaseConfigMap.isEmpty())
            return  ;
        else
        {
            for(String mainKey : databaseConfigMap.keySet())
            {
                String url = databaseConfigMap.get(mainKey).get("url") ;
                String username = databaseConfigMap.get(mainKey).get("username") ;
                String password = databaseConfigMap.get(mainKey).get("password") ;
                if(url.contains("oracle"))
                    loadDrivers("oracle") ;
                if(url.contains("mysql"))
                    loadDrivers("mysql") ;
                connections.put(mainKey,DriverManager.getConnection(url,username,password));
            }
        }
    }

    private void loadProperties(String propertiesFilePath)
    {
        String key = null;
        String[] kvMap = new String[2];
        HashMap<String,String> hm =  new HashMap<>() ;;
        try {
            BufferedReader reader = new BufferedReader( new FileReader(propertiesFilePath));
            String line = reader.readLine();
            while(line != null )
            {
                if(line.startsWith("#"))
                    continue;
                else
                {
                    if(line.startsWith("[") && line.endsWith("]"))
                    {
                        if(!hm.isEmpty()) {
                            databaseConfigMap.put(key, hm);
                            hm = new HashMap<>() ;
                        }
                        key = line.substring(1,line.length()-1);
                    }
                    if(isKeyValueFormat(line))
                    {
                        kvMap = line.split("=") ;
                        hm.put(kvMap[0],kvMap[1]);
                    }
                }
                line = reader.readLine();
            }
            databaseConfigMap.put(key,hm);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isKeyValueFormat(String line) {
        // Regex pattern for key-value format (key=value)
        String patternString = "^[^=]+=[^=]+$";
        Pattern pattern = Pattern.compile(patternString);

        // Create a matcher for the line
        Matcher matcher = pattern.matcher(line);

        // Check if the line matches the key-value format
        return matcher.matches();
    }
/*
    private void loadPropertiesAndInitializeConnections(String propertiesFilePath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFilePath)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + propertiesFilePath);
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            Map<String, Properties> dataSourcePropertiesMap = new HashMap<>();

            for (String key : prop.stringPropertyNames()) {
                String[] parts = key.split("\\.");
                String dbName = parts[0];
                String propName = parts[1];

                dataSourcePropertiesMap
                        .computeIfAbsent(dbName, k -> new Properties())
                        .setProperty(propName, prop.getProperty(key));
            }

            initializeConnections(dataSourcePropertiesMap);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeConnections(Map<String, Properties> dataSourcePropertiesMap) {
        for (Map.Entry<String, Properties> entry : dataSourcePropertiesMap.entrySet()) {
            String dbName = entry.getKey();
            Properties props = entry.getValue();

            String url = props.getProperty("url");
            String username = props.getProperty("username");
            String password = props.getProperty("password");

            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                connections.put(dbName, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    */
    private boolean loadDrivers(String driverType) {
        try {
            if(driverType.equalsIgnoreCase("oracle"))
                loadOracleDriver();
            if(driverType.equalsIgnoreCase("mysql"))
                loadMysqlDriver();

            return true ;
        }catch (Exception e)
        {
            System.out.println("Unable to load driver for "+driverType);
            e.printStackTrace();
            return false;
        }

    }
    private void loadOracleDriver() throws ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver") ;
    }

    private void loadMysqlDriver() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver") ;
    }


    public Connection getConnection(String dbName) throws SQLException {
        Connection connection = connections.get(dbName);
        if (connection == null) {
            throw new SQLException("No connection found for " + dbName);
        }
        return connection;
    }

    public void executeQueryWithParameters(String dbName, String sql, Object[] parameters, ResultSetHandler handler) {
        try (Connection conn = getConnection(dbName);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                setParameter(stmt, i + 1, parameters[i]);
            }

            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                handler.handle(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Double) {
            stmt.setDouble(index, (Double) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof java.sql.Date) {
            stmt.setDate(index, (java.sql.Date) value);
        } else {
            throw new SQLException("Unsupported parameter type: " + value.getClass().getName());
        }
    }

    public void executeQuery(String dbName, String sql, ResultSetHandler handler) {
        try (PreparedStatement stmt = getConnection(dbName).prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            handler.handle(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public interface ResultSetHandler {
        void handle(ResultSet rs) throws SQLException;
    }

    public void closeConnections() {
        for (Connection connection : connections.values()) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        DatabaseUtility dbUtility = new DatabaseUtility();

        String sql = "SELECT * FROM your_table";
        dbUtility.executeQuery("db1", sql, rs -> {
            while (rs.next()) {
                System.out.println("Column1: " + rs.getString("column1"));
                System.out.println("Column2: " + rs.getString("column2"));
            }
        });

        dbUtility.executeQuery("db2", sql, rs -> {
            while (rs.next()) {
                System.out.println("Column1: " + rs.getString("column1"));
                System.out.println("Column2: " + rs.getString("column2"));
            }
        });

        // Close all connections at the end
        dbUtility.closeConnections();
    }

    
    /*
    		<dependency>
			<groupId>com.oracle.database.jdbc</groupId>
			<artifactId>ojdbc8</artifactId>
			<version>12.2.0.1</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/com.mysql/mysql-connector-j -->
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<version>8.0.32</version>
		</dependency>
		
     */


}
