package cli;

import db.DBConnection;
import db.models.Category;
import db.models.Ingredient;
import db.models.Recipe;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public abstract class CommandLineInterface {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * main menu
     */
    public static void start() {
        clearScreen();
        DBConnection.connect();
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int mainMenuChoice = scanner.nextInt();
                scanner.nextLine();
                switch (mainMenuChoice) {
                    case 1 -> { //manage recipes
                        clearScreen();
                        manageRecipes();
                    }
                    case 2 -> { //manage categories
                        clearScreen();
                        manageCategories();
                    }
                    case 3 -> { //purge all data
                        clearScreen();
                        purgeData();
                    }
                    case 4 -> { //exit
                        clearScreen();
                        System.out.println("Goodby!");
                        DBConnection.close();
                        exit = true;
                    }
                    default -> {
                        clearScreen();
                        System.err.println("please enter an valid value");
                    }
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

    /**
     * manage recipes menu
     */
    private static void manageRecipes() {
        boolean exit = false;
        while (!exit) {
            printManageRecipesMenu();
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> { //add recipe
                        clearScreen();
                        addRecipe();
                    }
                    case 2 -> { //show all recipes
                        clearScreen();
                        showAllRecipes();
                    }
                    case 3 -> { //search in recipes
                        clearScreen();
                        searchRecipes();
                    }
                    case 4 -> { //back
                        clearScreen();
                        exit = true;
                    }
                    default -> {
                        clearScreen();
                        System.err.println("please enter an valid value");
                    }
                }
            }
        }
    }

    /**
     * print manage recipes menu
     */
    private static void printManageRecipesMenu() {
        Menu manageRecipes = new Menu("<--- Manage Recipes Menu --->");
        manageRecipes.addOption("add recipe");
        manageRecipes.addOption("show all recipes");
        manageRecipes.addOption("search in recipes");
        manageRecipes.addOption("back to main menu");
        manageRecipes.display();
    }

    /**
     * add recipe menu
     */
    private static void addRecipe() {
        System.out.println("<--- recipe insertion --->");
        System.out.println("enter recipe name :");
        String name = scanner.nextLine();

        System.out.println("enter recipe instructions :");
        String instructions = scanner.nextLine();

        System.out.println("please enter categories :");
        System.out.println("(use '-' as seperator)");
        String[] categoriesNames = scanner.nextLine().split("-");
        ArrayList<Category> categories = new ArrayList<>();
        for (String categoryName : categoriesNames) {
            Category newCategory = new Category(categoryName);
            categories.add(newCategory);
        }

        System.out.println("please enter ingredients :");
        System.out.println("(use '-' as seperator)");
        String[] ingredientsNames = scanner.nextLine().split("-");
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        for (String ingredientName : ingredientsNames) {
            Ingredient newIngredient = new Ingredient(ingredientName);
            ingredients.add(newIngredient);
        }

        Recipe newRecipe = new Recipe(name, instructions, categories, ingredients);
        newRecipe.save();
        clearScreen();
    }

    /**
     * show all recipes menu
     */
    private static void showAllRecipes() {
        boolean exit = false;
        while (!exit) {
            printShowAllRecipesMenu();
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == -1)
                    exit = true;
                else {
                    clearScreen();
                    singelRecipe(choice);
                }
            }
        }
        clearScreen();
    }

    /**
     * print show all recipes menu
     */
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
                    table.addRow(id, name, instructions, categories, ingredients);
                }
        );
        table.print();
    }

    /**
     * single recipe menu
     *
     * @param recipeId recipe id
     */
    private static void singelRecipe(int recipeId) {
        if (!Recipe.exists(recipeId)) {// check if recipe exists
            System.err.println("recipe not found :(");
            return;
        }
        Recipe recipe = Recipe.find(recipeId);
        boolean exit = false;
        while (!exit) {
            printSingleRecipeMenu(recipe);
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> { //edit name
                        clearScreen();
                        System.out.println("<--- edit " + recipe.getName() + " recipe name --->");
                        System.out.println("enter new name : ");
                        String newName = scanner.nextLine();
                        recipe.setName(newName);
                        recipe.update();
                        clearScreen();
                    }
                    case 2 -> { //edit instructions
                        clearScreen();
                        System.out.println("<--- edit " + recipe.getName() + " recipe instructions --->");
                        System.out.println("enter new instructions : ");
                        String newInstructions = scanner.nextLine();
                        recipe.setInstructions(newInstructions);
                        recipe.update();
                        clearScreen();
                    }
                    case 3 -> { //edit categories
                        clearScreen();
                        boolean exitCategories = false;
                        while (!exitCategories) {
                            printEditSingleRecipeCategoriesMenu(recipe);
                            if (!scanner.hasNextInt()) {
                                clearScreen();
                                String next = scanner.nextLine();
                                System.err.println("<--- please enter an valid value --->");
                            } else {
                                int editCategoriesChoice = scanner.nextInt();
                                scanner.nextLine();
                                switch (editCategoriesChoice) {
                                    case 1 -> { //add categories
                                        clearScreen();
                                        printEditSingleRecipeCategoriesTable(recipe);
                                        System.out.println("<--- Add categories to recipe " + recipe.getName() + " --->");
                                        System.out.println("enter new categories names :");
                                        System.out.println("(use '-' as seperator )");
                                        String[] categoriesNames = scanner.nextLine().split("-");
                                        ArrayList<Category> newCategories = recipe.categories(); // initial value of recipe categories

                                        for (String categoryName : categoriesNames) { //add only unique categories to ArrayList
                                            Category newCategory = new Category(categoryName);
                                            newCategories.add(newCategory);
                                        }

                                        recipe.setCategories(newCategories);
                                        recipe.update();
                                        clearScreen();
                                    }
                                    case 2 -> { //remove categories
                                        clearScreen();
                                        printEditSingleRecipeCategoriesTable(recipe);
                                        System.out.println("<--- Remove categories from recipe " + recipe.getName() + " --->");
                                        System.out.println("enter categories you want to remove :");
                                        System.out.println("(use '-' as seperator )");
                                        String[] categoriesNames = scanner.nextLine().split("-"); //get categories that user wants to remove

                                        ArrayList<Category> newCategories = recipe.getCategories().stream() //filter categories based on user input
                                                .filter(
                                                        (category) -> {
                                                            boolean found = false;
                                                            for (String categoryName : categoriesNames) {
                                                                if (category.getName().equals(categoryName)) {
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                            return !found;
                                                        }
                                                )
                                                .collect(Collectors.toCollection(ArrayList::new));

                                        recipe.setCategories(newCategories); //set new categories
                                        recipe.update(); //update recipe in database
                                        clearScreen();
                                    }
                                    case 3 -> { //back to recipe menu
                                        clearScreen();
                                        exitCategories = true;
                                    }
                                    default -> {
                                        clearScreen();
                                        System.err.println("please enter an valid value");
                                    }
                                }
                            }
                        }
                        clearScreen();
                    }
                    case 4 -> { //edit ingredients
                        clearScreen();
                        boolean exitIngredients = false;
                        while (!exitIngredients) {
                            printEditSingleRecipeIngredientsMenu(recipe);
                            if (!scanner.hasNextInt()) {
                                clearScreen();
                                String next = scanner.nextLine();
                                System.err.println("<--- please enter an valid value --->");
                            } else {
                                int ingredientChoice = scanner.nextInt();
                                scanner.nextLine();
                                switch (ingredientChoice) {
                                    case 1 -> { //add ingredients
                                        clearScreen();
                                        printEditSingleRecipeIngredientsTable(recipe);
                                        System.out.println("<--- Add ingredients to recipe " + recipe.getName() + " --->");
                                        System.out.println("enter new ingredients names :");
                                        System.out.println("(use '-' as seperator )");
                                        String[] ingredientsNames = scanner.nextLine().split("-");
                                        ArrayList<Ingredient> newIngredients = recipe.ingredients(); // initial value of recipe categories

                                        for (String ingredientName : ingredientsNames) { //add only unique categories to ArrayList
                                            Ingredient newIngredient = new Ingredient(ingredientName);
                                            newIngredients.add(newIngredient);
                                        }

                                        recipe.setIngredients(newIngredients);
                                        recipe.update();
                                        clearScreen();
                                    }
                                    case 2 -> { //remove ingredinets
                                        clearScreen();
                                        printEditSingleRecipeIngredientsTable(recipe);
                                        System.out.println("<--- Remove ingredients from recipe " + recipe.getName() + " --->");
                                        System.out.println("enter ingredients you want to remove :");
                                        System.out.println("(use '-' as seperator )");
                                        String[] ingredientsNames = scanner.nextLine().split("-"); //get categories that user wants to remove

                                        ArrayList<Ingredient> newIngredients = recipe.getIngredients().stream() //filter categories based on user input
                                                .filter(
                                                        (ingredient) -> {
                                                            boolean found = false;
                                                            for (String ingredientName : ingredientsNames) {
                                                                if (ingredient.getName().equals(ingredientName)) {
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                            return !found;
                                                        }
                                                )
                                                .collect(Collectors.toCollection(ArrayList::new));

                                        recipe.setIngredients(newIngredients); //set new categories
                                        recipe.update(); //update recipe in database
                                        clearScreen();
                                    }
                                    case 3 -> { //back to recipe menu
                                        clearScreen();
                                        exitIngredients = true;
                                    }
                                    default -> {
                                        clearScreen();
                                        System.err.println("please enter an valid value");
                                    }
                                }
                            }
                        }
                        clearScreen();
                    }
                    case 5 -> { // go back to all recipes menu
                        clearScreen();
                        exit = true;
                    }
                    default -> {
                        clearScreen();
                        System.err.println("please enter an valid value");
                    }
                }
            }
        }
        clearScreen();
    }

    /**
     * print menu of edit single recipe
     *
     * @param recipe recipe object
     */
    private static void printSingleRecipeMenu(Recipe recipe) {
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

    /**
     * print menu of edit single recipe categories
     *
     * @param recipe recipe object
     */
    private static void printEditSingleRecipeCategoriesMenu(Recipe recipe) {
        printEditSingleRecipeCategoriesTable(recipe);

        Menu editCategoryMenu = new Menu("<--- edit " + recipe.getName() + " recipe categories --->");
        editCategoryMenu.addOption("add new categories");
        editCategoryMenu.addOption("remove categories");
        editCategoryMenu.addOption("back to recipe menu");
        editCategoryMenu.display();
    }

    /**
     * print table of all single recipe categories
     *
     * @param recipe recipe object
     */
    private static void printEditSingleRecipeCategoriesTable(Recipe recipe) {
        ArrayList<Category> categories = recipe.categories(); //get recipe all categories

        CommandLineTable table = new CommandLineTable();
        table.setHeaders("id", "name");
        table.setShowVerticalLines(true);

        categories.forEach( // add all recipe categories to table
                (category) -> {
                    String id = String.valueOf(category.getId());
                    table.addRow(id, category.getName());
                }
        );

        System.out.println("<<<------ Current Categories of " + recipe.getName() + " Recipe ------>>>");
        table.print();
    }

    /**
     * print menu of edit single recipe ingreidnets
     *
     * @param recipe recipe object
     */
    private static void printEditSingleRecipeIngredientsMenu(Recipe recipe) {
        printEditSingleRecipeIngredientsTable(recipe);

        Menu editCategoryMenu = new Menu("<--- edit " + recipe.getName() + " recipe ingredients --->");
        editCategoryMenu.addOption("add new ingredients");
        editCategoryMenu.addOption("remove ingredients");
        editCategoryMenu.addOption("back to recipe menu");
        editCategoryMenu.display();
    }

    /**
     * print table of all single recipe ingredients
     *
     * @param recipe recipe object
     */
    private static void printEditSingleRecipeIngredientsTable(Recipe recipe) {
        ArrayList<Ingredient> ingredients = recipe.ingredients(); //get recipe all ingredients

        CommandLineTable table = new CommandLineTable();
        table.setHeaders("id", "name");
        table.setShowVerticalLines(true);

        ingredients.forEach( // add all recipe ingredients to table
                (ingredient) -> {
                    String id = String.valueOf(ingredient.getId());
                    table.addRow(id, ingredient.getName());
                }
        );

        System.out.println("<<<------ Current Ingredients of " + recipe.getName() + " Recipe ------>>>");
        table.print();
    }

    /**
     * search in recipes menu
     */
    private static void searchRecipes() {
        boolean exit = false;
        while (!exit) {
            printSearchRecipesMenu();
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> { //search with categories
                        clearScreen();
                        System.out.println("<--- search recipes with category --->");
                        System.out.println("enter categories names :");
                        System.out.println("( use '-' as seperator )");
                        String[] categoriesNames = scanner.nextLine().split("-"); //get categories names from user
                        int[] categoriesIds = new int[categoriesNames.length];

                        for (int i = 0; i < categoriesIds.length; i++) { // add id of entered category names
                            if (Category.exists(categoriesNames[i]))
                                categoriesIds[i] = Category.find(categoriesNames[i]).getId();
                        }
                        clearScreen();
                        ArrayList<Recipe> foundedRecipes = Category.getCategoryRecipes(categoriesIds); // search in recipes with category ids

                        boolean exitCategory = false;
                        while (!exitCategory) {
                            System.out.println("<--- founded recipes --->");
                            System.out.println("(select recipe or enter -1 to exit)");
                            printSearchRecipesTable(foundedRecipes);
                            if (!scanner.hasNextInt()) {
                                clearScreen();
                                String next = scanner.nextLine();
                                System.err.println("<--- please enter an valid value --->");
                            } else {
                                int categoryChoice = scanner.nextInt();
                                scanner.nextLine();
                                if (categoryChoice == -1)
                                    exitCategory = true;
                                else {
                                    clearScreen();
                                    singelRecipe(categoryChoice);
                                }
                            }
                        }
                        clearScreen();
                    }
                    case 2 -> { //search with ingredients
                        clearScreen();
                        System.out.println("<--- search recipes with ingredient --->");
                        System.out.println("enter ingredients names :");
                        System.out.println("( use '-' as seperator )");
                        String[] ingredientsNames = scanner.nextLine().split("-"); //get categories names from user
                        int[] ingredientsId = new int[ingredientsNames.length];

                        for (int i = 0; i < ingredientsId.length; i++) { // add id of entered category names
                            if (Ingredient.exists(ingredientsNames[i]))
                                ingredientsId[i] = Ingredient.find(ingredientsNames[i]).getId();
                        }
                        clearScreen();
                        ArrayList<Recipe> foundedRecipes = Ingredient.getIngredientRecipes(ingredientsId); // search in recipes with category ids

                        boolean exitIngredient = false;
                        while (!exitIngredient) {
                            System.out.println("<--- founded recipes --->");
                            System.out.println("(select recipe or enter -1 to exit)");
                            printSearchRecipesTable(foundedRecipes);
                            if (!scanner.hasNextInt()) {
                                clearScreen();
                                String next = scanner.nextLine();
                                System.err.println("<--- please enter an valid value --->");
                            } else {
                                int categoryChoice = scanner.nextInt();
                                scanner.nextLine();
                                if (categoryChoice == -1)
                                    exitIngredient = true;
                                else {
                                    clearScreen();
                                    singelRecipe(categoryChoice);
                                }
                            }
                        }
                        clearScreen();
                    }
                    case 3 -> {
                        clearScreen();
                        exit = true;
                    }
                    default -> {
                        clearScreen();
                        System.err.println("please enter an valid value");
                    }
                }
            }
        }
        clearScreen();
    }

    /**
     * print search recipes menu
     */
    private static void printSearchRecipesMenu() {
        Menu menu = new Menu("<--- search in recipes --->");
        menu.addOption("search with categories");
        menu.addOption("search with ingredients");
        menu.addOption("back to manage recipes");
        menu.display();
    }

    private static void printSearchRecipesTable(ArrayList<Recipe> recipes) {
        CommandLineTable table = new CommandLineTable();
        table.setHeaders("id", "name", "instructions", "categories", "ingredients");
        table.setShowVerticalLines(true);

        recipes.forEach(
                (recipe) -> {
                    String id = String.valueOf(recipe.getId());
                    String name = recipe.getName();
                    String instructions = recipe.getInstructions();
                    String categories = getRecipeCategoriesText(recipe.getId());
                    String ingredients = getRecipeIngredientsText(recipe.getId());
                    table.addRow(id, name, instructions, categories, ingredients);
                }
        );

        table.print();
    }

    /**
     * manage category menu
     */
    private static void manageCategories() {
        boolean exit = false;
        while (!exit) {
            printManageCategoriesMenu();
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int choice = scanner.nextInt();
                scanner.nextLine();
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
                    default -> {
                        clearScreen();
                        System.err.println("please enter an valid value");
                    }
                }
            }
        }
    }

    /**
     * print manage category menu
     */
    private static void printManageCategoriesMenu() {
        Menu manageCategories = new Menu("<--- choose desired action --->");
        manageCategories.addOption("add category");
        manageCategories.addOption("show all categories");
        manageCategories.addOption("back");
        manageCategories.display();
    }

    /**
     * purge data menu
     */
    private static void purgeData() {
        boolean exit = false;
        while (!exit) {
            printPurgeDataMenu();
            if (!scanner.hasNextInt()) {
                clearScreen();
                String next = scanner.nextLine();
                System.err.println("<--- please enter an valid value --->");
            } else {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> {
                        DBConnection.purgeAllData();
                    }
                    case 2 -> {
                        exit = true;
                    }
                    default -> {
                        clearScreen();
                        System.err.println("please enter an valid value");
                    }
                }
                clearScreen();
            }
        }
    }

    /**
     * print purge data menu
     */
    private static void printPurgeDataMenu() {
        Menu purgeData = new Menu("this delete all data stored in database are you sure ?");
        purgeData.addOption("yes");
        purgeData.addOption("no");
        purgeData.display();
    }

    /**
     * get recipe all categories in string format
     *
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

    /**
     * get recipe all ingredients in string format
     *
     * @param recipeId recipe id
     * @return string of all ingredients of recipe
     */
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

    /**
     * clear command line screen
     */
    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
