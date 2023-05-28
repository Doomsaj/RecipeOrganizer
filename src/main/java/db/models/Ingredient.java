package db.models;

import db.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Ingredient implements Model {
    private int id;
    private String name;
    private static final String table = "ingredients";

    public Ingredient(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Ingredient(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void save() {
        try {
            String createSQL = "insert into " + table + " (name) values(?);";
            PreparedStatement statement = DBConnection.prepareStatment(createSQL);
            statement.setString(1, getName());
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int lastInsertedId = generatedKeys.getInt(1);
                    setId(lastInsertedId);
                }
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * find ingredient based on id in database
     * @param id ingredient id
     * @return Ingredient
     */
    public static Ingredient find(int id) {
        Ingredient ingredient = null;
        try {
            String getIngredientSQL = "select * from " + table + " where id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getIngredientSQL);
            statement.setInt(1, id);
            ResultSet ingredientResult = statement.executeQuery();
            while (ingredientResult.next()) {
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
    public static Ingredient find(String name) {
        Ingredient ingredient = null;
        try {
            String getIngredientSQL = "select * from " + table + " where name=?";
            PreparedStatement statement = DBConnection.prepareStatment(getIngredientSQL);
            statement.setString(1, name);
            ResultSet ingredientResult = statement.executeQuery();
            while (ingredientResult.next()) {
                int id = ingredientResult.getInt("id");
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
     * check if Ingredient is exists in database
     * @param id ingredient id
     * @return true if exist or false if not
     */
    public static boolean exists(int id) {
        return find(id) != null;
    }

    public static boolean exists(String name) {
        return find(name) != null;
    }

    /**
     * update the ingredient in database
     */
    @Override
    public void update() {
        try {
            String updateSQL = "update " + table + " set name=? where id=?";
            PreparedStatement statement = DBConnection.prepareStatment(updateSQL);
            statement.setString(1, getName());
            statement.setInt(2, getId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void delete() {
        System.err.println("you cannot delete an ingredient");
    }

    /**
     * return all recipes that has this ingredient
     * @return ArrayList of Recipe objects
     */
    public ArrayList<Recipe> recipes() {
        return getIngredientRecipes(id);
    }

    /**
     * get all ingredients in database
     * @return ArrayList of ingredient objects
     */
    public static ArrayList<Ingredient> all() {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        try {
            String getSQL = "select * from " + table;
            PreparedStatement statement = DBConnection.prepareStatment(getSQL);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                ingredients.add(new Ingredient(id, name));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return ingredients;
    }

    /**
     * return all recipes that has this ingredient based on ingredient id
     * @param ingredientId ingredient id
     * @return ArrayList of Recipes
     */
    public static ArrayList<Recipe> getIngredientRecipes(int ...ingredientId) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            String getRecipesIdsSQL = "select recipe_id from recipes_ingredients where ingredient_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getRecipesIdsSQL);
            for (int i : ingredientId) {
                statement.setInt(1, i);
                ResultSet recipesIdsResult = statement.executeQuery();
                while (recipesIdsResult.next()) {
                    int recipeId = recipesIdsResult.getInt("recipe_id");
                    Recipe recipe = Recipe.find(recipeId);
                    if (!recipes.contains(recipe))
                        recipes.add(Recipe.find(recipeId));
                }
                recipesIdsResult.close();
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return recipes;
    }

    /**
     * count of ingredients in database
     * @return count of ingredients
     */
    public static int count() {
        int count = 0;
        try {
            String countSQL = "select count(id) from " + table;
            PreparedStatement statement = DBConnection.prepareStatment(countSQL);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id || Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
