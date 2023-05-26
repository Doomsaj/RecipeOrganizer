import db.DBConnection;
import db.models.Category;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        DBConnection.connect();
        DBConnection.close();
//        DBConnection.getAllCategories();
    }
}
