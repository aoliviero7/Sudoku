import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Sudoku implements Serializable{
    private String name;
    private Integer[][] raw_sudoku;
    private Integer[][] solved_sudoku;

    public Sudoku(String name) {
        this.name = name;
        raw_sudoku = new Integer[9][9];
        solved_sudoku = new Integer[9][9];
    }

    public void generateSudoku() throws Exception{
        Random rand = new Random();
        int n = rand.nextInt(1);

        readFromJSON(n, "SolvedSudoku", solved_sudoku);
        readFromJSON(n, "RawSudoku", raw_sudoku);
    }

    public void readFromJSON(int sudokuNumber, String sudokuType, Integer[][] sudoku) throws FileNotFoundException, IOException, ParseException{
        int i = 0, riga = 0, colonna = 0;

        Object obj = new JSONParser().parse(new FileReader("sudoku.json"));
        JSONObject jo = (JSONObject) obj;
        JSONArray ja = (JSONArray) jo.get(sudokuType);
       
        for(Object s : ja){
            JSONArray js = (JSONArray) s;
            if (i==sudokuNumber){
                for(Object r : js){
                    JSONArray jr = (JSONArray) r;
                    for(colonna=0; colonna<9; colonna++)
                        sudoku[riga][colonna] = Integer.parseInt(String.valueOf(jr.get(colonna)));
                    riga++;
                }
            }
            i++;
        }
    }
}
