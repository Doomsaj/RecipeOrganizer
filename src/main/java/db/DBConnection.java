package db;
import db.models.Category;
import db.models.Ingredient;
import db.models.Recipe;

import java.sql.*;
import java.util.ArrayList;

public abstract class DBConnection {
    private static Connection connection;
    private static final String dbPath = "jdbc:sqlite:src/main/resources/database.db";
    public static void connect() {
        try {
            connection = DriverManager.getConnection(dbPath);
            createTablesIfNotExist();
            System.out.println("suc");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * get and return all categories from categories table in form of ArrayList
     * @return ArrayList of category objects
     */
    public static ArrayList<Category> getAllCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM categories";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                categories.add(new Category(id, name));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return categories;
    }

    /**
     * get all recipes from database
     * @return ArrayList of Recipes objects
     */
    public static ArrayList<Recipe> getAllRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String selectRecipesSQL = "select * form recipes";
            ResultSet recipesSelectResult = statement.executeQuery(selectRecipesSQL);
            while (recipesSelectResult.next()) {
                int id = recipesSelectResult.getInt("id");
                String name = recipesSelectResult.getString("name");
                String instructions = recipesSelectResult.getString("instructions");
                ArrayList<Category> categories = getRecipeCategories(id); //get recipe categories from recipes_categories table
                ArrayList<Ingredient> ingredients = getRecipeIngredients(id); //get recipe ingredients from recipes_ingredients table
                recipes.add(new Recipe(id, name, instructions, categories, ingredients));
            }
            recipesSelectResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return recipes;
    }

    /**
     * get all recipe ingredients
     * @param recipeId recipe id
     * @return ArrayList of Ingredient objectss
     */
    private static ArrayList<Ingredient> getRecipeIngredients(int recipeId) {
        ArrayList<Ingredient> recipeIngredients = new ArrayList<>();
        String selectRecipeIngredientsSQL = "select ingredient_id from recipes_ingredients where recipe_id=" + recipeId;
        try {
            Statement statement = connection.createStatement();
            ResultSet ingredientsIdsResult = statement.executeQuery(selectRecipeIngredientsSQL);
            while (ingredientsIdsResult.next()) {
                int ingredientId = ingredientsIdsResult.getInt("ingredient_id");
                recipeIngredients.add(getIngredientById(ingredientId));
            }
            ingredientsIdsResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return recipeIngredients;
    }

    /**
     * get all categories for a singel recipe
     * @param recipeId recipe id
     * @return ArrayList of Category object
     */
    private static ArrayList<Category> getRecipeCategories(int recipeId) {
        ArrayList<Category> recipeCategories = new ArrayList<>();
        String selectRecipeCategoriesSQL = "select category_id from recipes_categories where recipe_id=" + recipeId;
        try {
            Statement statement = connection.createStatement();
            ResultSet categoriesIdsResult = statement.executeQuery(selectRecipeCategoriesSQL);
            while (categoriesIdsResult.next()) {
                int categoryId = categoriesIdsResult.getInt("category_id");
                recipeCategories.add(getCategoryById(categoryId));
            }
            categoriesIdsResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return recipeCategories;
    }

    /**
     * get ingredient by id
     * @param ingredientId ingredient id
     * @return Ingredient object
     */
    public static Ingredient getIngredientById(int ingredientId) {
        Ingredient ingredient = null;
        String selectIngredientSQL = "select * from ingredients where id=" + ingredientId;
        try {
            Statement statement = connection.createStatement();
            ResultSet ingredientResult = statement.executeQuery(selectIngredientSQL);
            while (ingredientResult.next()) {
                int id = ingredientResult.getInt("id");
                String name = ingredientResult.getString("name");
                ingredient = new Ingredient(id, name);
            }
            ingredientResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return ingredient;
    }

    /**
     * get category by id from database
     * @param categoryId category id
     * @return Category object
     */
    public static Category getCategoryById(int categoryId) {
        Category category = null;
        String selectCategorySQL = "select * from categories where id=" + categoryId;
        try {
            Statement statement = connection.createStatement();
            ResultSet categoryResult = statement.executeQuery(selectCategorySQL);
            while (categoryResult.next()) {
                int id = categoryResult.getInt("id");
                String name = categoryResult.getString("name");
                category = new Category(id, name);
            }
            categoryResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return category;
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
}
