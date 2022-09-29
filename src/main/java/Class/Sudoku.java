package Class;
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
    private Integer[][] rawSudoku;
    private Integer[][] solvedSudoku;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer[][] getRawSudoku() {
        return this.rawSudoku;
    }

    public Integer[][] getSolvedSudoku() {
        return this.solvedSudoku;
    }

    public Sudoku(String name) {
        this.name = name;
        rawSudoku = new Integer[9][9];
        solvedSudoku = new Integer[9][9];
    }

    public void generateSudoku() throws Exception{
        Random rand = new Random();
        int n = rand.nextInt(1); //aggiornare a 27

        readFromJSON(n, "SolvedSudoku", solvedSudoku);
        readFromJSON(n, "RawSudoku", rawSudoku);
    }

    public void readFromJSON(int sudokuNumber, String sudokuType, Integer[][] sudoku) throws FileNotFoundException, IOException, ParseException{
        int i = 0, riga = 0, colonna = 0;

        Object obj = new JSONParser().parse(new FileReader("/sudoku.json"));
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

    public boolean insert(int number, int row, int column) {
        if(rawSudoku[row][column]==0){
            rawSudoku[row][column] = number;
            return true;
        }
        return false;
    }
}