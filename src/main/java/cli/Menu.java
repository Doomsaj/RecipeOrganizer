package cli;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String title;
    private List<String> options;
    private int indentationLevel;

    public Menu(String title) {
        this.title = title;
        this.options = new ArrayList<>();
        this.indentationLevel = 0;
    }

    public void addOption(String option) {
        options.add(option);
    }

    public void setIndentationLevel(int indentationLevel) {
        this.indentationLevel = indentationLevel;
    }

    public void display() {
        System.out.println(title);

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            String formattedOption = getFormattedOption(i + 1, option);
            System.out.println(formattedOption);
        }
    }

    private String getFormattedOption(int number, String option) {
        String indentation = getIndentation();
        return String.format("%s%d. %s", indentation, number, option);
    }

    private String getIndentation() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentationLevel; i++) {
            sb.append("    "); // Four spaces for each level of indentation
        }
        return sb.toString();
    }
}
