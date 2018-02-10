import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Main extends Application {
    /* Gameplay:
     * Can only click on groups of 3 or more blocks of same color
     * Points for click = (#blocks removed - 2)^2 + 1000 for clearing board
     * When a column is empty, shift everything to the left
     * 
     * Options: (default)
     * Minimum block group that is clearable (2)
     * Number of unique block colors (4)
     * Size of board (10*15)
     * 
     * Features:
     * Undo button
     * Display number of blocks of color left
     */
    
    public static final int BLOCK_COUNT_HORIZONTAL = 5;
    public static final int BLOCK_COUNT_VERTICAL = 5;
    public static final int BLOCK_SIZE = 30;
    public static final int UI_OFFSET_TOP = 90;
    public static final int UI_OFFSET_BOTTOM = 35;
    public static final int WIDTH = BLOCK_COUNT_HORIZONTAL * BLOCK_SIZE;
    public static final int HEIGHT = BLOCK_COUNT_VERTICAL * BLOCK_SIZE + UI_OFFSET_TOP + UI_OFFSET_BOTTOM;
    
    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Same Game");
        
        Board board = new Board(5, 5, 2, new Color[] {Color.RED, Color.BLUE, Color.FORESTGREEN, Color.YELLOW});
        
        Group root = new Group();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        draw(gc, board);
        
        scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseClick) {
                int xTile = (int) (mouseClick.getX() / BLOCK_SIZE);
                int yTile = (int) Math.floor(((mouseClick.getY() - UI_OFFSET_TOP) / BLOCK_SIZE));
                if (xTile >= 0 && xTile < BLOCK_COUNT_HORIZONTAL && yTile >= 0 && yTile < BLOCK_COUNT_VERTICAL) {
                    board.removeGroup(xTile, yTile);
                    draw(gc, board);
//                    board.printBoard();
                    if (board.checkEnd() != 2) {
                        System.out.println("End of game: " + board.checkEnd());
                    }
                } else {
                    System.out.println("Invalid click location");
                }
            }
        });
        
        Button undo = new Button("Undo");
        undo.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (board.boardHistory.size() > 1) {
                    board.setBoard(board.boardHistory.get(board.boardHistory.size() - 2));
                    board.boardHistory.remove(board.boardHistory.size() - 1);
                    draw(gc, board);
//                    board.printBoard();
                }
            }
        });
        undo.setPrefWidth(60);
        undo.setPrefHeight(30);
        undo.relocate(WIDTH-undo.getPrefWidth()*1.5, undo.getPrefHeight());
        root.getChildren().add(undo);
        
        Button solve = new Button("Solve");
        solve.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                ArrayList<Point> solution = bruteForce(board);
                if (solution != null) {
                    for (int x = solution.size() - 1; x >= 0; x--) {
                        System.out.println(solution.get(x));
                    }
                } else {
                    System.out.println("No solution");
                }
            }
        });
        root.getChildren().add(solve);
        
        primaryStage.show();
    }
    
    public static ArrayList<Point> bruteForce(Board board) {
        if (board.checkEnd() == 0) {
            return new ArrayList<Point>();
        } else {
            Object[] movesArray = board.getAllGroups().toArray();
            ArrayList<Point> moves = new ArrayList<Point>();
            for (int x = 0; x < movesArray.length; x++) {
                moves.add((Point) movesArray[x]);
            }
            if (moves.size() != 0) {
                for (int x = 0; x < moves.size(); x++) {
                    Board newBoard = board.cloneBoard();
                    Point p = moves.remove(x);
                    newBoard.removeGroup((int) p.getX(), (int) p.getY());
                    ArrayList<Point> result = bruteForce(newBoard);
                    if (result != null) {
                        result.add(p);
                        return result;
                    }
                }
            }
            return null;
        }
    }

    public static void draw(GraphicsContext gc, Board board) {
        drawBoard(gc, board);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, HEIGHT - UI_OFFSET_BOTTOM, WIDTH, UI_OFFSET_BOTTOM);
        int[] colorAmounts = board.countColors();
        for (int x = 0; x < colorAmounts.length; x++) {
            gc.setFill(board.colors[x]);
            gc.setFont(new Font("Calibri", 40));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(colorAmounts[x] + "", x * WIDTH / board.colors.length + WIDTH / board.colors.length / 2, HEIGHT - 5);
        }
    }
    
    public static void drawBoard(GraphicsContext gc, Board board) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, UI_OFFSET_TOP, BLOCK_COUNT_HORIZONTAL * BLOCK_SIZE, BLOCK_COUNT_VERTICAL * BLOCK_SIZE);
        for(int y = 0; y < board.board[0].length; y++) {
            for(int x = 0; x < board.board.length; x++) {
                if (board.board[x][y] != 0) {
                    gc.setFill(board.colors[board.board[x][y] - 1]);
                    gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE + UI_OFFSET_TOP, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
    }
}
