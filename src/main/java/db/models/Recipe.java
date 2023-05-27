package db.models;

import db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

    /**
     * save new recipe in database
     */
    @Override
    public void save() {
        try {
            Statement statement = DBConnection.getStatment();
            String createNewRecipeSQL = String.format("insert into " + table + " (name,instructions) values(%s,%s)", getName(), getInstructions());
            statement.execute(createNewRecipeSQL); // create new recipe
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
            Statement statement = DBConnection.getStatment();
            String updateSQL = "update " + table + "set name='%s', instructions='%s' where id=%s";
            statement.executeUpdate(String.format(updateSQL, getName(), getInstructions(), getId())); // update recipe row in recipes table
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
            Statement statement = DBConnection.getStatment();
            String deleteSQL = "delete from " + table + " where id=" + getId();
            statement.execute(deleteSQL); //delete recipe row from recipes table
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

        ArrayList<Category> addedCategories = categories().stream()
                .filter((category -> categories.contains(category)))
                .collect(Collectors.toCollection(ArrayList::new)); // get categories that added to recipe and most link

        unlinkCategories(removedCategories); // unlink removed categories
        linkCategories(addedCategories); // link newly added categories
    }

    /**
     * link categories for recipe if there is one category that not exists in database create one
     */
    private void linkCategories() {
        try {
            Statement statement = DBConnection.getStatment();
            for (Category category : categories) {

                if (!Category.exists(category.getId()))
                    category.save();

                String linkRecipeCategorySQL = "insert into recipes_categories (recipe_id,category_id) values(%s,%s);";
                statement.execute(String.format(linkRecipeCategorySQL, getId(), category.getId()));
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
            Statement statement = DBConnection.getStatment();
            for (Category category : categories) {

                if (!Category.exists(category.getId()))
                    category.save();

                String linkRecipeCategorySQL = "insert into recipes_categories (recipe_id,category_id) values(%s,%s);";
                statement.execute(String.format(linkRecipeCategorySQL, getId(), category.getId()));
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
            Statement statement = DBConnection.getStatment();
            for (Category category : categories) {
                String unlinkCategorySQL = "delete from recipe_categories where recipe_id=%s and category_id=%s";
                statement.execute(String.format(unlinkCategorySQL, getId(), category.getId()));
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
            Statement statement = DBConnection.getStatment();
            for (Category category : categories) {
                String unlinkCategorySQL = "delete from recipe_categories where recipe_id=%s and category_id=%s";
                statement.execute(String.format(unlinkCategorySQL, getId(), category.getId()));
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

        ArrayList<Ingredient> addedIngredients = ingredients().stream()
                .filter((ingredient -> ingredients.contains(ingredient)))
                .collect(Collectors.toCollection(ArrayList::new));

        unlinkIngredients(removedIngredients); // unlink removed ingredients
        linkIngredients(addedIngredients); // link added ingredients
    }

    /**
     * link ingredients for recipe if there is one ingredient that not exists in database create one
     */
    private void linkIngredients() {
        try {
            Statement statement = DBConnection.getStatment();
            for (Ingredient ingredient : ingredients) {

                if (!Ingredient.exists(ingredient.getId()))
                    ingredient.save();

                String linkRecipeIngredientSQL = "insert into recipes_ingredients (recipe_id,ingredient_id) values(%s,%s);";
                statement.execute(String.format(linkRecipeIngredientSQL, getId(), ingredient.getId()));
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
            Statement statement = DBConnection.getStatment();
            for (Ingredient ingredient : ingredients) {

                if (!Ingredient.exists(ingredient.getId()))
                    ingredient.save();

                String linkRecipeIngredientSQL = "insert into recipes_ingredients (recipe_id,ingredient_id) values(%s,%s);";
                statement.execute(String.format(linkRecipeIngredientSQL, getId(), ingredient.getId()));
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
            Statement statement = DBConnection.getStatment();
            for (Ingredient ingredient : ingredients) {
                String unlinkCategorySQL = "delete from recipe_ingredients where recipe_id=%s and ingredient_id=%s";
                statement.execute(String.format(unlinkCategorySQL, getId(), ingredient.getId()));
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
            Statement statement = DBConnection.getStatment();
            for (Ingredient ingredient : ingredients) {
                String unlinkCategorySQL = "delete from recipe_ingredients where recipe_id=%s and ingredient_id=%s";
                statement.execute(String.format(unlinkCategorySQL, getId(), ingredient.getId()));
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
            Statement statement = DBConnection.getStatment();
            String getRecipeSQL = "select * from " + table + " where id=" + id;
            ResultSet recipeResult = statement.executeQuery(getRecipeSQL);
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
     * check if an recipe is exists
     *
     * @param recipeId recipe id
     * @return true if recipe is exists or false if its not
     */
    public static boolean exists(int recipeId) {
        Recipe recipe = find(recipeId);
        return recipe != null;
    }

    /**
     * get all recipes
     *
     * @return ArrayList of all recipes
     */
    public static ArrayList<Recipe> all() {
        ArrayList<Recipe> allData = new ArrayList<>();
        try {
            Statement statement = DBConnection.getStatment();
            String SQL = "select * from " + table;
            ResultSet result = statement.executeQuery(SQL);
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
    private static ArrayList<Category> getRecipeCategories(int recipeId) {
        ArrayList<Category> categories = new ArrayList<>();
        try {
            Statement statement = DBConnection.getStatment();
            String getCategoriesIdsSQL = "select category_id from recipes_categories where recipe_id=" + recipeId;
            ResultSet categoriesIds = statement.executeQuery(getCategoriesIdsSQL); // get ids of all categories that recipe have
            while (categoriesIds.next()) { // loop in selected category ids
                int categoryId = categoriesIds.getInt("category_id");
                String getCategoriesNameSQL = "select name from categories where id=" + categoryId;
                ResultSet categoriesNames = statement.executeQuery(getCategoriesNameSQL); // get names of all categories that recipe have
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
    private static ArrayList<Ingredient> getRecipeIngredients(int recipeId) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        try {
            Statement statement = DBConnection.getStatment();
            String getIngredientsIdsSQL = "select ingredient_id from recipes_ingredients where recipe_id=" + recipeId;
            ResultSet ingredientsIds = statement.executeQuery(getIngredientsIdsSQL); // get ids of all ingredients that recipe have
            while (ingredientsIds.next()) { // loop in selected ingredients ids
                int ingredientId = ingredientsIds.getInt("ingredient_id");
                String getIngredientsNameSQL = "select name from ingredients where id=" + ingredientId;
                ResultSet ingredientsNames = statement.executeQuery(getIngredientsNameSQL); // get names of all ingredients that recipe have
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
}
