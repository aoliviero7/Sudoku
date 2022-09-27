import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

public class Main {
    public static void main(String[] args) throws Exception {
        Sudoku sudoku = new Sudoku("name");
        sudoku.generateSudoku();
    }
}
