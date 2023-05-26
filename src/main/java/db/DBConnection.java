package db;

import db.models.Category;
import db.models.Ingredient;
import db.models.Recipe;

import java.sql.*;
import java.util.ArrayList;

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
            System.out.println("suc");
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

    public static ArrayList<Recipe> getCategoryRecipes(String categoryName) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        Category category = getCategoryByName(categoryName);
        if (category == null) {
            return recipes;
        }
        try {
            String getCategoryRecipesIdsSQL = "select recipe_id from recipes_categories where category_id=" + category.getId();
            getLinkedRecipes(recipes, getCategoryRecipesIdsSQL);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return recipes;
    }

    public static ArrayList<Recipe> getIngredientsRecipe(ArrayList<String> ingredientsNames) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        for (String ingredientName : ingredientsNames) {
            Ingredient ingredient = getIngredientByName(ingredientName);

            if (ingredient == null) //continue if ingredient not found
                continue;

            try {
                String ingredientRecipesIdsSQL = "select recipe_id from recipes_ingredients where ingredient_id=" + ingredient.getId();
                getLinkedRecipes(recipes, ingredientRecipesIdsSQL);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        return recipes;
    }

    private static void getLinkedRecipes(ArrayList<Recipe> recipes, String getRecipesIdsSQL) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet recipesIdsResult = statement.executeQuery(getRecipesIdsSQL);
        while (recipesIdsResult.next()) {
            int recipeId = recipesIdsResult.getInt("recipe_id");
            recipes.add(getRecipeById(recipeId));
        }
        recipesIdsResult.close();
        statement.close();
    }

    /**
     * add new recipe to database
     * @param recipeName recipe name
     * @param recipeInstructions recipe instructions
     * @param categoriesNames recipe categories
     * @param ingredientsNames recipe ingredients
     * @return -1 if recipe added or existed recipe id
     */
    public static int addRecipe(String recipeName, String recipeInstructions, ArrayList<String> categoriesNames, ArrayList<String> ingredientsNames) {
        Recipe recipe = getRecipeByName(recipeName);
        if (recipe == null) {
            insertRecipe(recipeName, recipeInstructions);
            recipe = getRecipeByName(recipeName);
            for (String categoryName : categoriesNames) {
                linkRecipeCategories(recipe.getId(), categoryName);
            }
            for (String ingredientName : ingredientsNames) {
                linkRecipeIngredient(recipe.getId(), ingredientName);
            }
            return -1;
        } else {
            return recipe.getId();
        }
    }

    /**
     * link category with recipe
     * @param recipeId recipe id
     * @param categoryName category name
     */
    private static void linkRecipeCategories(int recipeId, String categoryName) {
        Category category = getCategoryByName(categoryName);
        if (category == null) {
            addCategory(categoryName);
            category = getCategoryByName(categoryName);
        }
        try {
            String linkRecipeCategorySQL = "insert into recipes_categories (recipe_id,category_id) values (" + recipeId + ", " + category.getId() + ");";
            Statement statement = connection.createStatement();
            statement.execute(linkRecipeCategorySQL);
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * link ingredient with recipe
     * @param recipeId recipe id
     * @param ingredientName ingredient name
     */
    private static void linkRecipeIngredient(int recipeId, String ingredientName) {
        Ingredient ingredient = getIngredientByName(ingredientName);
        if (ingredient == null) {
            addIngredient(ingredientName);
            ingredient = getIngredientByName(ingredientName);
        }
        try {
            String linkRecipeIngredient = "insert into recipes_ingredients (recipe_id,ingredient_id) values(" + recipeId + ", " + ingredient.getId() + ");";
            Statement statement = connection.createStatement();
            statement.execute(linkRecipeIngredient);
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * insert new recipe into database
     * @param name recipe name
     * @param instructions recipe instructions
     * */
    private static void insertRecipe(String name, String instructions) {
        try {
            String createNewRecipeSQL = "insert into recipes (name,instructions) values ('" + name + "','" + instructions + "');";
            Statement statement = connection.createStatement();
            statement.executeUpdate(createNewRecipeSQL);
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * add new category to database
     * @param categoryName category name
     * @return -1 if a new category is created or id of founded category
     */
    public static int addCategory(String categoryName) {
        Category category = getCategoryByName(categoryName);
        if (category == null) {
            try {
                String createNewCategorySQL = "insert into categories (name) values ('" + categoryName + "');";
                Statement statement = connection.createStatement();
                statement.execute(createNewCategorySQL);
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
            return -1;
        } else {
            System.err.println("category with name " + categoryName + " is already exist");
            return category.getId();
        }
    }

    /**
     * add new ingredient to database
     *
     * @param ingredientName ingredient name
     * @return -1 if a new ingredient is created or id of founded ingredient
     */
    private static int addIngredient(String ingredientName) {
        Ingredient ingredient = getIngredientByName(ingredientName);
        if (ingredient == null) {
            try {
                String createNewIngredientSQL = "insert into ingredients (name) values ('" + ingredientName + "');";
                Statement statement = connection.createStatement();
                statement.execute(createNewIngredientSQL);
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
            return -1;
        } else {
            return ingredient.getId();
        }
    }

    /**
     * get and return all categories from categories table in form of ArrayList
     *
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
     *
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
     *
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
     *
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
     * get recipe by id
     * @param recipeId recipe id
     * @return Recipe object
     */
    public static Recipe getRecipeById(int recipeId) {
        String selectRecipeSQL = "select * from recipes where id=" + recipeId;
        return getRecipe(selectRecipeSQL);
    }

    /**
     * get recipe by name
     * @param recipeName recipe name
     * @return Recipe object
     */
    public static Recipe getRecipeByName(String recipeName) {
        String selectRecipeSQL = "select * from recipes where name='" + recipeName + "'";
        return getRecipe(selectRecipeSQL);
    }

    /**
     * get recipe from database based on given SQL query
     * @param selectRecipeSQL SQL query
     * @return Recipe object
     */
    private static Recipe getRecipe(String selectRecipeSQL) {
        Recipe recipe = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet recipeResult = statement.executeQuery(selectRecipeSQL);
            while (recipeResult.next()) {
                int id = recipeResult.getInt("id");
                String name = recipeResult.getString("name");
                String instructions = recipeResult.getString("instructions");
                ArrayList<Category> categories = getRecipeCategories(id);
                ArrayList<Ingredient> ingredients = getRecipeIngredients(id);
                recipe = new Recipe(id, name, instructions, categories, ingredients);
            }
            recipeResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return recipe;
    }

    /**
     * get ingredient by id
     *
     * @param ingredientId ingredient id
     * @return Ingredient object
     */
    public static Ingredient getIngredientById(int ingredientId) {
        String selectIngredientSQL = "select * from ingredients where id=" + ingredientId;
        return getIngredient(selectIngredientSQL);
    }

    /**
     * get ingredient by name
     *
     * @param ingredientName ingredient name
     * @return Ingredient object
     */
    public static Ingredient getIngredientByName(String ingredientName) {
        String selectIngredientSQL = "select * from ingredients where name='" + ingredientName + "'";
        return getIngredient(selectIngredientSQL);
    }

    /**
     * get ingredient from database based on given SQL query
     *
     * @param selectIngredientSQL SQL query
     * @return Ingredient object
     */
    private static Ingredient getIngredient(String selectIngredientSQL) {
        Ingredient ingredient = null;
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
     *
     * @param categoryId category id
     * @return Category object
     */
    public static Category getCategoryById(int categoryId) {
        String selectCategorySQL = "select * from categories where id=" + categoryId;
        return getCategory(selectCategorySQL);
    }

    /**
     * get category by name from database
     *
     * @param categoryName category name
     * @return Category object
     */
    public static Category getCategoryByName(String categoryName) {
        String selectCategorySQL = "select * from categories where name='" + categoryName + "'";
        return getCategory(selectCategorySQL);
    }

    /**
     * get category from table based on given SQL query
     *
     * @param selectCategorySQL SQL query
     * @return Category object
     */
    private static Category getCategory(String selectCategorySQL) {
        Category category = null;
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
