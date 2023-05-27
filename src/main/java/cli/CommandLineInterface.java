package cli;

import db.DBConnection;
import db.models.Category;
import db.models.Ingredient;
import db.models.Recipe;

import java.util.ArrayList;
import java.util.Scanner;

public abstract class CommandLineInterface {
    private static Scanner scanner = new Scanner(System.in);
    public static void start() {
        DBConnection.connect();
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int mainMenuChoice = scanner.nextInt();
            switch (mainMenuChoice) {
                case 1 -> { //manage recipes
                    manageRecipes();
                }
                case 2 -> { //manage categories
                    manageCategories();
                }
                case 3 -> { //purge all data
                    purgeData();
                }
                case 4 -> { //exit
                    System.out.println("Goodby!");
                    DBConnection.close();
                    exit = true;
                }
                default -> {
                    System.err.println("please enter an valid value");
                }
            }
        }
    }

    /**
     * print main menu
     */
    private static void printMainMenu() {
        Menu mainMenu = new Menu("<--- Welcome to Recipe Organizer Application --->");
        mainMenu.addOption("manage recipes");
        mainMenu.addOption("manage categories");
        mainMenu.addOption("purge data");
        mainMenu.addOption("exit");

        mainMenu.display();
    }

    private static void manageRecipes() {
        boolean exit = false;
        while (!exit) {
            printManageRecipesMenu();
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> { //add recipe
                    addRecipe();
                }
                case 2 -> { //show all recipes
                    showAllRecipes();
                }
                case 3 -> { //search in recipes
                    // search in recipes
                }
                case 4 -> { //back
                    exit = true;
                }
                default -> {
                    System.err.println("please enter an valid value");
                }
            }
        }
    }

    private static void printManageRecipesMenu() {
        Menu manageRecipes = new Menu("<--- Manage Recipes Menu --->");
        manageRecipes.addOption("add recipe");
        manageRecipes.addOption("show all recipes");
        manageRecipes.addOption("search in recipes");
        manageRecipes.addOption("back to main menu");
        manageRecipes.display();
    }


    private static void addRecipe() {
        System.out.println("<--- recipe insertion --->");
        System.out.println("enter recipe name");
        String name = scanner.next();

        scanner.nextLine();

        System.out.println("enter recipe instructions");
        String instructions = scanner.nextLine();

        System.out.println("please enter categories");
        System.out.println("(use '-' as delimiter)");
        String[] categoriesNames = scanner.nextLine().split("-");
        ArrayList<Category> categories = new ArrayList<>();
        for (String categoryName : categoriesNames) {
            categories.add(new Category(categoryName));
        }

        System.out.println("please enter ingredients");
        System.out.println("(use '-' as delimiter)");
        String[] ingredientsNames = scanner.nextLine().split("-");
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        for (String ingredientName : ingredientsNames) {
            ingredients.add(new Ingredient(ingredientName));
        }

        Recipe newRecipe = new Recipe(name, instructions, categories, ingredients);
        newRecipe.save();
    }

    private static void showAllRecipes() {
        boolean exit = false;
        while (!exit) {
            printShowAllRecipesMenu();
            int choice = scanner.nextInt();

            if (choice == -1)
                exit = true;
            else
                singelRecipe(choice);
        }
    }

    private static void printShowAllRecipesMenu() {
        System.out.println("<--- all recipes menu --->");
        System.out.println("(select from recipes IDs to enter recipe menu or -1 for exit)");
        System.out.println();
        CommandLineTable table = new CommandLineTable();
        table.setShowVerticalLines(true);
        table.setHeaders("id", "name", "instructions", "categories", "ingredients");
        ArrayList<Recipe> recipes = Recipe.all();
        recipes.forEach(
                (recipe) -> {
                    String id = String.valueOf(recipe.getId());
                    String name = recipe.getName();
                    String instructions = recipe.getInstructions();
                    String categories = getRecipeCategoriesText(recipe.getId());
                    String ingredients = getRecipeIngredientsText(recipe.getId());
                    table.addRow(id,name, instructions, categories, ingredients);
                }
        );
        table.print();
    }

    private static void singelRecipe(int recipeId) {
        if (!Recipe.exists(recipeId)) {// check if recipe exists
            System.err.println("recipe not found :(");
            return;
        }
        Recipe recipe = Recipe.find(recipeId);
        boolean exit = false;
        while (!exit) {
            printSingleRecipeMenu(recipe);
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> { //edit name
                    // edit name
                }
                case 2 -> { //edit instructions
                    // edit instructions
                }
                case 3 -> { //edit categories
                    //edit categories
                }
                case 4 -> { //edit ingredients
                    //edit ingredients
                }
                case 5 -> {
                    exit = true;
                }
            }
        }
    }

    private static void  printSingleRecipeMenu(Recipe recipe) {
        System.out.println("<--- singel recipe menu --->");
        System.out.println("(select desired action)");
        System.out.println();
        CommandLineTable table = new CommandLineTable();
        table.setShowVerticalLines(true);
        table.setHeaders("id", "name", "instructions", "categories", "ingredients");
        String recipeId = String.valueOf(recipe.getId());
        String recipeName = recipe.getName();
        String recipeInstructions = recipe.getInstructions();
        String recipeCategories = getRecipeCategoriesText(recipe.getId());
        String recipeIngredients = getRecipeIngredientsText(recipe.getId());
        table.addRow(recipeId, recipeName, recipeInstructions, recipeCategories, recipeIngredients);

        table.print(); //display current recipe in table

        Menu recipeMenu = new Menu("");
        recipeMenu.addOption("edit name");
        recipeMenu.addOption("edit instructions");
        recipeMenu.addOption("edit categories");
        recipeMenu.addOption("edit ingredients");
        recipeMenu.addOption("back to all recipes menu");
        recipeMenu.display(); //display options
    }

    private static void manageCategories() {
        boolean exit = false;
        while (!exit) {
            printManageCategoriesMenu();
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> { // add category
                    // add category
                }
                case 2 -> { // show all categories
                    //show all categories
                }
                case 3 -> { // back
                    exit = true;
                }
            }
        }
    }

    private static void printManageCategoriesMenu() {
        Menu manageCategories = new Menu("<--- choose desired action --->");
        manageCategories.addOption("add category");
        manageCategories.addOption("show all categories");
        manageCategories.addOption("back");
        manageCategories.display();
    }

    private static void purgeData() {
        printPurgeDataMenu();
        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> {
                DBConnection.purgeAllData();
            }
            case 2 -> {
                return;
            }
        }
    }

    private static void printPurgeDataMenu() {
        Menu purgeData = new Menu("this delete all data stored in database are you sure ?");
        purgeData.addOption("yes");
        purgeData.addOption("no");
        purgeData.display();
    }

    /**
     * get recipe all categories in string format
     * @param recipeId recipe id
     * @return string of all categories of recipe
     */
    private static String getRecipeCategoriesText(int recipeId) {
        StringBuilder builder = new StringBuilder();

        Recipe recipe = Recipe.find(recipeId);
        ArrayList<Category> recipeCategories = recipe.categories();

        recipeCategories.forEach(
                (category) -> {
                    builder.append(category.getName());
                    builder.append("-");
                }
        );

        return builder.toString();
    }
    private static String getRecipeIngredientsText(int recipeId) {
        StringBuilder builder = new StringBuilder();

        Recipe recipe = Recipe.find(recipeId);
        ArrayList<Ingredient> recipeCategories = recipe.ingredients();

        recipeCategories.forEach(
                (ingredient) -> {
                    builder.append(ingredient.getName());
                    builder.append("-");
                }
        );

        return builder.toString();
    }
}
