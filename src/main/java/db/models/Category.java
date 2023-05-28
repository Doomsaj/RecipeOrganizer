package db.models;

import db.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Category implements Model {
    private int id;
    private String name;
    private static final String table = "categories";

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    /**
     * save category to database
     */
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
     * update category
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

    /**
     * delete this category from database
     */
    @Override
    public void delete() {
        try {
            String deleteSQL = "delete from " + table + " where id=?";
            PreparedStatement statement = DBConnection.prepareStatment(deleteSQL);
            statement.setInt(1, getId());
            statement.executeUpdate();
            statement.close();

            unlinkRecipes();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink recipes of category
     */
    private void unlinkRecipes() {
        try {
            String deleteSQL = "delete from recipes_categories where category_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(deleteSQL);
            statement.setInt(1, getId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * get all categories in database
     * @return ArrayList of Category objects
     */
    public static ArrayList<Category> all() {
        ArrayList<Category> categories = new ArrayList<>();
        try {
            String getSQL = "select * from " + table;
            PreparedStatement statement = DBConnection.prepareStatment(getSQL);
            ResultSet resultSet = statement.executeQuery();
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
     * find category by id
     * @param id category id
     * @return Category or null
     */
    public static Category find(int id) {
        Category category = null;
        try {
            String getCategorySQL = "select * from " + table + " where id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getCategorySQL);
            statement.setInt(1, id);
            ResultSet categoryResult = statement.executeQuery();
            while (categoryResult.next()) {
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
     * find category by given name
     * @param name category name
     * @return Category or null
     */
    public static Category find(String name) {
        Category category = null;
        try {
            String getCategorySQL = "select * from " + table + " where name=?";
            PreparedStatement statement = DBConnection.prepareStatment(getCategorySQL);
            statement.setString(1, name);
            ResultSet categoryResult = statement.executeQuery();
            while (categoryResult.next()) {
                int id = categoryResult.getInt("id");
                category = new Category(id, name);
            }
            categoryResult.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return category;
    }

    public static boolean exists(int id) {
        return find(id) != null;
    }

    public static boolean exists(String name) {
        return find(name) != null;
    }

    /**
     * get all recipes of this category
     * @return ArrayList of Recipe objects
     */
    public ArrayList<Recipe> recipes() {
        return getCategoryRecipes(id);
    }

    /**
     * get all recipes that has given category id
     * @param categoryId category id
     * @return ArrayList of Recipe objects
     */
    public static ArrayList<Recipe> getCategoryRecipes(int ...categoryId) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            String getRecipesIdsSQL = "select recipe_id from recipes_categories where category_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getRecipesIdsSQL);
            for (int i : categoryId) {
                statement.setInt(1, i);
                ResultSet recipesIdsResult = statement.executeQuery();
                while (recipesIdsResult.next()) {
                    int recipeId = recipesIdsResult.getInt("recipe_id");
                    Recipe recipe = Recipe.find(recipeId);
                    if (!recipes.contains(recipe))
                        recipes.add(recipe);
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
     * count all categories in database
     * @return count of categories
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

    /**
     * count of recipes that have this category
     * @return count of recipes
     */
    public int recipesCount() {
        return countCategoryRecipes(id);
    }

    /**
     * count of recipes that have given category
     * @param categoryId category id
     * @return count of recipes
     */
    public static int countCategoryRecipes(int categoryId) {
        int count = 0;
        try {
            String countSQL = "select count(recipe_id) from recipes_categories where category_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(countSQL);
            statement.setInt(1, categoryId);
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
        Category category = (Category) o;
        return id == category.id || Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
