import org.kohsuke.args4j.Option;

public class Main {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id;
    
    public static void main(String[] args) throws Exception {
        Sudoku sudoku = new Sudoku("name");
        sudoku.generateSudoku();
    }
}
