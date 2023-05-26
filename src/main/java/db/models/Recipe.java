package db.models;

import java.util.ArrayList;

public class Recipe {
    private int id;
    private String name, instructions;
    private ArrayList<Category> categories;
    private ArrayList<Ingredient> ingredients;

    public Recipe(int id, String name, String instructions, ArrayList<Category> categories, ArrayList<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.categories = categories;
        this.ingredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInstructions() {
        return instructions;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }
}
