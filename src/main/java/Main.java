import db.DBConnection;
import db.models.Category;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DBConnection.connect();
        System.out.println("please enter name");
        String name = sc.next();
        System.out.println("please enter inst");
        String inst = sc.next();
        System.out.println("please enter category");
        String cat = sc.next();
        ArrayList<String> cats = new ArrayList<>();
        cats.add(cat);
        System.out.println("please enter ingr");
        String ing = sc.next();
        ArrayList<String> ings = new ArrayList<>();
        ings.add(ing);
        DBConnection.addRecipe(name, inst, cats, ings);
        DBConnection.close();
    }
}
