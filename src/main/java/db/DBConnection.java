package db;


import java.sql.*;

public abstract class DBConnection {
    private static Connection connection;
    private static final String dbPath = "jdbc:sqlite:src/main/resources/database.db"; //database path

    /**
     * initial connection
     */
    public static void connect() {
        try {
            connection = DriverManager.getConnection(dbPath);
            createTablesIfNotExist();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * close connection
     */
    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static PreparedStatement prepareStatment(String SQL) throws SQLException {
        return connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
    }

    public static Statement getStatment() throws SQLException {
        return connection.createStatement();
    }

    /**
     * Create tables for application if there is they are not exist
     */
    private static void createTablesIfNotExist() {
        String[] queries = new String[]{
                "create table if not exists categories(id integer not null constraint categories_pk primary key autoincrement,name TEXT not null unique);",
                "create table if not exists ingredients(id integer not null constraint ingredients_pk primary key autoincrement,name TEXT not null unique);",
                "create table if not exists recipes(id integer not null constraint recipes_pk primary key autoincrement,name TEXT not null,instructions TEXT not null);",
                "create table if not exists recipes_ingredients(recipe_id integer not null constraint recipes references recipes,ingredient_id integer not null constraint ingredients references ingredients);",
                "create table if not exists recipes_categories(recipe_id integer not null constraint recipes references recipes,category_id integer not null constraint categories references categories);"
        };
        try {
            Statement statement = connection.createStatement();
            for (String query : queries) {
                statement.execute(query);
            }
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * truncate all tables datas
     */
    private static void truncateTablesData() {
        String[] SQLs = {
                "drop table if exists categories;",
                "drop table if exists recipes;",
                "drop table if exists ingredients;",
                "drop table if exists recipes_ingredients;",
                "drop table if exists recipes_categories;"
        };
        try {
            Statement statement = connection.createStatement();
            for (String sql : SQLs) {
                statement.execute(sql);
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * purge all data stored in database when called
     */
    public static void purgeAllData() {
        truncateTablesData();
        createTablesIfNotExist();
    }
}
