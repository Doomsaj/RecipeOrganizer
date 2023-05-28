package db.models;

import db.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class Recipe implements Model {
    private int id;
    private String name, instructions;
    private ArrayList<Category> categories;
    private ArrayList<Ingredient> ingredients;

    private static final String table = "recipes";

    public Recipe(int id, String name, String instructions, ArrayList<Category> categories, ArrayList<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.categories = categories;
        this.ingredients = ingredients;
    }

    public Recipe(String name, String instructions, ArrayList<Category> categories, ArrayList<Ingredient> ingredients) {
        this.name = name;
        this.instructions = instructions;
        this.categories = categories;
        this.ingredients = ingredients;
    }

    /**
     * getter for id
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * getter for name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for instructions
     *
     * @return instructions
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * getter for categories
     *
     * @return categories
     */
    public ArrayList<Category> getCategories() {
        return categories;
    }

    /**
     * getter for ingredients
     *
     * @return ingredients
     */
    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * setter for name
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * setter for instructions
     *
     * @param instructions new instructions
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * setter for categories
     *
     * @param categories new categories
     */
    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    /**
     * set ingredients
     *
     * @param ingredients new ingredients
     */
    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * add new category to categories
     *
     * @param category new category
     */
    public void addCategory(Category category) {
        categories.add(category);
    }

    /**
     * remove category from categories
     *
     * @param category category
     */
    public void removeCategory(Category category) {
        categories.remove(category);
    }

    /**
     * add new ingredient to ingredients
     *
     * @param ingredient new ingredient
     */
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    /**
     * remove ingredient from ingredients
     *
     * @param ingredient ingredient
     */
    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

    private void setId(int id) {
        this.id = id;
    }

    /**
     * save new recipe in database
     */
    @Override
    public void save() {
        try {
            String createNewRecipeSQL = "insert into " + table + " (name,instructions) values(?,?)";
            PreparedStatement statement = DBConnection.prepareStatment(createNewRecipeSQL);
            statement.setString(1, getName());
            statement.setString(2, getInstructions());
            int affectedRows = statement.executeUpdate(); // create new recipe
            if (affectedRows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int lastInsertedId = generatedKeys.getInt(1);
                    setId(lastInsertedId);
                }
            }
            statement.close();
            linkCategories(); // link categories for recipe
            linkIngredients(); // link ingredients for recipe
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * update the recipe in database
     */
    @Override
    public void update() {
        try {
            String updateSQL = "UPDATE " + table + " SET name = ?, instructions = ? where id = ?";
            PreparedStatement statement = DBConnection.prepareStatment(updateSQL);
            statement.setString(1, getName());
            statement.setString(2, getInstructions());
            statement.setInt(3, getId());
            statement.executeUpdate(); // update recipe row in recipes table
            statement.close();

            updateCategories(); // update categories
            updateIngredients(); // update ingredients
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * delete recipe
     */
    @Override
    public void delete() {
        try {
            String deleteSQL = "delete from " + table + " where id=?";
            PreparedStatement statement = DBConnection.prepareStatment(deleteSQL);
            statement.setInt(1, getId());
            statement.executeUpdate(); //delete recipe row from recipes table
            statement.close();

            unlinkCategories(); // unlink all categories
            unlinkIngredients(); // unlink all ingredients
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink removed and link added categories from recipe
     */
    private void updateCategories() {
        ArrayList<Category> removedCategories = categories().stream()
                .filter((category) -> !categories.contains(category))
                .collect(Collectors.toCollection(ArrayList::new)); // get categories that removed from recipe and most unlink

        ArrayList<Category> addedCategories = categories.stream()
                .filter((category -> !categories().contains(category)))
                .collect(Collectors.toCollection(ArrayList::new)); // get categories that added to recipe and most link

        unlinkCategories(removedCategories); // unlink removed categories
        linkCategories(addedCategories); // link newly added categories
    }

    /**
     * link categories for recipe if there is one category that not exists in database create one
     */
    private void linkCategories() {
        try {
            String linkRecipeCategorySQL = "insert into recipes_categories (recipe_id,category_id) values(?,?);";
            PreparedStatement statement = DBConnection.prepareStatment(linkRecipeCategorySQL);
            statement.setInt(1, getId());
            for (Category category : categories) {

                if (!Category.exists(category.getName()))
                    category.save();
                else
                    category = Category.find(category.getName());

                statement.setInt(2, category.getId());
                statement.executeUpdate();
            }
            statement.close(); //close statment
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * linked given categories to recipe
     *
     * @param categories categories ArrayList
     */
    private void linkCategories(ArrayList<Category> categories) {
        try {
            String linkRecipeCategorySQL = "insert into recipes_categories (recipe_id,category_id) values(?,?);";
            PreparedStatement statement = DBConnection.prepareStatment(linkRecipeCategorySQL);
            statement.setInt(1, getId());
            for (Category category : categories) {

                if (!Category.exists(category.getName()))
                    category.save();
                else
                    category = Category.find(category.getName());

                statement.setInt(2, category.getId());
                statement.executeUpdate();
            }
            statement.close(); //close statment
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink categories from recipes
     *
     * @param categories categories to unlink
     */
    private void unlinkCategories(ArrayList<Category> categories) {
        try {
            String unlinkCategorySQL = "delete from recipes_categories where recipe_id=? and category_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(unlinkCategorySQL);
            statement.setInt(1, getId());
            for (Category category : categories) {
                statement.setInt(2, category.getId());
                statement.executeUpdate();
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink all categories for this recipe
     */
    private void unlinkCategories() {
        try {
            String unlinkCategorySQL = "delete from recipe_categories where recipe_id=? and category_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(unlinkCategorySQL);
            statement.setInt(1, getId());
            for (Category category : categories) {
                statement.setInt(2, category.getId());
                statement.executeUpdate();
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink removed and link added ingredients from recipe
     */
    private void updateIngredients() {
        ArrayList<Ingredient> removedIngredients = ingredients().stream()
                .filter((ingredient -> !ingredients.contains(ingredient)))
                .collect(Collectors.toCollection(ArrayList::new)); // get ingredients that removed from recipe and most unlink

        ArrayList<Ingredient> addedIngredients = ingredients.stream()
                .filter((ingredient -> !ingredients().contains(ingredient)))
                .collect(Collectors.toCollection(ArrayList::new));

        unlinkIngredients(removedIngredients); // unlink removed ingredients
        linkIngredients(addedIngredients); // link added ingredients
    }

    /**
     * link ingredients for recipe if there is one ingredient that not exists in database create one
     */
    private void linkIngredients() {
        try {
            String linkRecipeIngredientSQL = "insert into recipes_ingredients (recipe_id,ingredient_id) values(?,?);";
            PreparedStatement statement = DBConnection.prepareStatment(linkRecipeIngredientSQL);
            statement.setInt(1, getId());
            for (Ingredient ingredient : ingredients) {

                if (!Ingredient.exists(ingredient.getName()))
                    ingredient.save();
                else
                    ingredient = Ingredient.find(ingredient.getName());

                statement.setInt(2, ingredient.getId());
                statement.executeUpdate();
            }
            statement.close(); //close statment
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * link given Ingredients to recipe
     *
     * @param ingredients ArrayList of Ingredients
     */
    private void linkIngredients(ArrayList<Ingredient> ingredients) {
        try {
            String linkRecipeIngredientSQL = "insert into recipes_ingredients (recipe_id,ingredient_id) values(?,?);";
            PreparedStatement statement = DBConnection.prepareStatment(linkRecipeIngredientSQL);
            statement.setInt(1, getId());
            for (Ingredient ingredient : ingredients) {

                if (!Ingredient.exists(ingredient.getName()))
                    ingredient.save();
                else
                    ingredient = Ingredient.find(ingredient.getName());

                statement.setInt(2, ingredient.getId());
                statement.executeUpdate();
            }
            statement.close(); //close statment
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink ingredients from recipe
     *
     * @param ingredients ingredients to unlink
     */
    private void unlinkIngredients(ArrayList<Ingredient> ingredients) {
        try {
            String unlinkCategorySQL = "delete from recipes_ingredients where recipe_id=? and ingredient_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(unlinkCategorySQL);
            statement.setInt(1, getId());
            for (Ingredient ingredient : ingredients) {
                statement.setInt(2, ingredient.getId());
                statement.executeUpdate();
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * unlink all ingredients for this recipe
     */
    private void unlinkIngredients() {
        try {
            String unlinkCategorySQL = "delete from recipes_ingredients where recipe_id=? and ingredient_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(unlinkCategorySQL);
            statement.setInt(1, getId());
            for (Ingredient ingredient : ingredients) {
                statement.setInt(2, ingredient.getId());
                statement.executeUpdate();
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * find an recipe in database based on given recipe id
     *
     * @param id of recipe
     * @return return null if no recipe found with given id or Recipe object of founded recipe
     */
    public static Recipe find(int id) {
        Recipe recipe = null;
        try {
            String getRecipeSQL = "select * from " + table + " where id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getRecipeSQL);
            statement.setInt(1, id);
            ResultSet recipeResult = statement.executeQuery();
            while (recipeResult.next()) {
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
     * find an recipe in database based on given name
     * @param name name of recipe
     * @return Recipe
     */
    public static Recipe find(String name) {
        Recipe recipe = null;
        try {
            String getRecipeSQL = "select * from " + table + " where name=?";
            PreparedStatement statement = DBConnection.prepareStatment(getRecipeSQL);
            statement.setString(1, name);
            ResultSet recipeResult = statement.executeQuery();
            while (recipeResult.next()) {
                int id = recipeResult.getInt("id");
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
     * check if an recipe is exists
     *
     * @param recipeId recipe id
     * @return true if recipe is exists or false if its not
     */
    public static boolean exists(int recipeId) {
        return find(recipeId) != null;
    }

    public static boolean exists(String name) {
        return find(name) != null;
    }

    /**
     * get all recipes
     *
     * @return ArrayList of all recipes
     */
    public static ArrayList<Recipe> all() {
        ArrayList<Recipe> allData = new ArrayList<>();
        try {
            String SQL = "select * from " + table;
            PreparedStatement statement = DBConnection.prepareStatment(SQL);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String instructions = result.getString("instructions");
                ArrayList<Category> categories = getRecipeCategories(id); // get all categories of recipe from database
                ArrayList<Ingredient> ingredients = getRecipeIngredients(id); // get all ingredients of recipe from database
                allData.add(new Recipe(id, name, instructions, categories, ingredients));
            }
            result.close(); // close recipes result set
            statement.close(); // close statments
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return allData;
    }

    /**
     * categories of recipe
     *
     * @return ArrayList of all Category objects of recipe
     */
    public ArrayList<Category> categories() {
        return getRecipeCategories(id);
    }

    /**
     * get all categories that an recipe have based on given recipe id
     *
     * @param recipeId id of recipe
     * @return ArrayList of Category objects
     */
    public static ArrayList<Category> getRecipeCategories(int recipeId) {
        ArrayList<Category> categories = new ArrayList<>();
        try {
            String getCategoriesIdsSQL = "select category_id from recipes_categories where recipe_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getCategoriesIdsSQL);
            statement.setInt(1, recipeId);
            ResultSet categoriesIds = statement.executeQuery(); // get ids of all categories that recipe have
            while (categoriesIds.next()) { // loop in selected category ids
                int categoryId = categoriesIds.getInt("category_id");
                String getCategoriesNameSQL = "select name from categories where id=" + categoryId;
                Statement categoryNameStatement = DBConnection.getStatment();
                ResultSet categoriesNames = categoryNameStatement.executeQuery(getCategoriesNameSQL); // get names of all categories that recipe have
                while (categoriesNames.next()) { // loop in selected category name
                    String categoryName = categoriesNames.getString("name");
                    categories.add(new Category(categoryId, categoryName)); //create and add new category object to categories ArrayList
                }
                categoriesNames.close(); // close name result set
            }
            categoriesIds.close(); // close ids result set
            statement.close(); // close statment
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return categories;
    }

    /**
     * ingredients of recipe
     *
     * @return ArrayList of all Ingredients objects of recipe
     */
    public ArrayList<Ingredient> ingredients() {
        return getRecipeIngredients(id);
    }

    /**
     * get all ingredients that an recipe have based on given recipe id
     *
     * @param recipeId id of recipe
     * @return ArrayList of Ingredient objects
     */
    public static ArrayList<Ingredient> getRecipeIngredients(int recipeId) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        try {
            String getIngredientsIdsSQL = "select ingredient_id from recipes_ingredients where recipe_id=?";
            PreparedStatement statement = DBConnection.prepareStatment(getIngredientsIdsSQL);
            statement.setInt(1, recipeId);
            ResultSet ingredientsIds = statement.executeQuery(); // get ids of all ingredients that recipe have
            while (ingredientsIds.next()) { // loop in selected ingredients ids
                int ingredientId = ingredientsIds.getInt("ingredient_id");
                String getIngredientsNameSQL = "select name from ingredients where id=" + ingredientId;
                Statement ingredientNameStatment = DBConnection.getStatment();
                ResultSet ingredientsNames = ingredientNameStatment.executeQuery(getIngredientsNameSQL); // get names of all ingredients that recipe have
                while (ingredientsNames.next()) { // loop in selected ingredient name
                    String ingredientName = ingredientsNames.getString("name");
                    ingredients.add(new Ingredient(ingredientId, ingredientName)); //create and add new ingredients object to categories ArrayList
                }
                ingredientsNames.close(); // close names result set
            }
            ingredientsIds.close(); // close ids result set
            statement.close(); // close statment
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return ingredients;
    }

    /**
     * get count of recipes in database
     * @return count of recipes
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
        Recipe recipe = (Recipe) o;
        return id == recipe.id || Objects.equals(name, recipe.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
