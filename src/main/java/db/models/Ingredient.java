package db.models;

import db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public void save() {
        try {
            Statement statement = DBConnection.getStatment();
            String createSQL = "insert into " + table + " (name) values(%s);";
            statement.execute(String.format(createSQL, getName()));
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
            Statement statement = DBConnection.getStatment();
            String getIngredientSQL = "select * from " + table + " where id=" + id;
            ResultSet ingredientResult = statement.executeQuery(getIngredientSQL);
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

    /**
     * check if Ingredient is exists in database
     * @param id ingredient id
     * @return true if exist or false if not
     */
    public static boolean exists(int id) {
        return find(id) != null;
    }

    /**
     * update the ingredient in database
     */
    @Override
    public void update() {
        try {
            Statement statement = DBConnection.getStatment();
            String updateSQL = "update " + table + " set name='%s' where id=" + id;
            statement.execute(String.format(updateSQL, getName()));
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
            Statement statement = DBConnection.getStatment();
            String getSQL = "select * from " + table;
            ResultSet resultSet = statement.executeQuery(getSQL);
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
            Statement statement = DBConnection.getStatment();
            for (int i : ingredientId) {
                String getRecipesIdsSQL = "select recipe_id from recipes_ingredients where ingredient_id=" + i;
                ResultSet recipesIdsResult = statement.executeQuery(getRecipesIdsSQL);
                while (recipesIdsResult.next()) {
                    int recipeId = recipesIdsResult.getInt("recipe_id");
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
            Statement statement = DBConnection.getStatment();
            String countSQL = "select count(id) from " + table;
            ResultSet resultSet = statement.executeQuery(countSQL);
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
