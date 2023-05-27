package db.models;

import db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * save category to database
     */
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
     * update category
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

    /**
     * delete this category from database
     */
    @Override
    public void delete() {
        try {
            Statement statement = DBConnection.getStatment();
            String deleteSQL = "delete from " + table + " where id=" + id;
            statement.execute(deleteSQL);
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
            Statement statement = DBConnection.getStatment();
            String deleteSQL = "delete from recipes_categories where category_id=" + id;
            statement.execute(deleteSQL);
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
            Statement statement = DBConnection.getStatment();
            String getSQL = "select * from " + table;
            ResultSet resultSet = statement.executeQuery(getSQL);
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
            Statement statement = DBConnection.getStatment();
            String getCategorySQL = "select * from " + table + " where id=" + id;
            ResultSet categoryResult = statement.executeQuery(getCategorySQL);
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
    public static Category findByName(String name) {
        Category category = null;
        try {
            Statement statement = DBConnection.getStatment();
            String getCategorySQL = "select * from " + table + " where name='%s'";
            ResultSet categoryResult = statement.executeQuery(String.format(getCategorySQL, name));
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
            Statement statement = DBConnection.getStatment();
            for (int i : categoryId) {
                String getRecipesIdsSQL = "select recipe_id from recipes_categories where category_id=" + i;
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
     * count all categories in database
     * @return
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
        Category category = (Category) o;
        return id == category.id || Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
