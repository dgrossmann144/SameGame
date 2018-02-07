import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.sound.midi.Synthesizer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
    
    public static final int BLOCK_COUNT_HORIZONTAL = 15;
    public static final int BLOCK_COUNT_VERTICAL = 10;
    public static final int BLOCK_SIZE = 30;
    public static final int UI_OFFSET_TOP = 90;
    public static final int UI_OFFSET_BOTTOM = 35;
    public static final int WIDTH = BLOCK_COUNT_HORIZONTAL * BLOCK_SIZE;
    public static final int HEIGHT = BLOCK_COUNT_VERTICAL * BLOCK_SIZE + UI_OFFSET_TOP + UI_OFFSET_BOTTOM;
    public static final int COLOR_COUNT = 4;
    public static final Color[] COLOR_VALUES = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
    public static final int GROUP_SIZE = 2;
    public static int[][] board;
    public static ArrayList<int[][]> boardHistory;
    
    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Same Game");
        board = makeBoard(BLOCK_COUNT_HORIZONTAL, BLOCK_COUNT_VERTICAL, COLOR_COUNT);
        boardHistory = new ArrayList<int[][]>();
        boardHistory.add(copy2D(board));
        printBoard();
        
        Group root = new Group();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        draw(gc);
        
        scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseClick) {
                int xTile = (int) (mouseClick.getX() / BLOCK_SIZE);
                int yTile = (int) Math.floor(((mouseClick.getY() - UI_OFFSET_TOP) / BLOCK_SIZE));
                if (xTile >= 0 && xTile < BLOCK_COUNT_HORIZONTAL && yTile >= 0 && yTile < BLOCK_COUNT_VERTICAL) {
                    removeTiles(yTile, xTile);
                    draw(gc);
                    printBoard();
                } else {
                    System.out.println("Invalid click location");
                }
            }
        });
        
        Button undo = new Button("Undo");
        undo.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (boardHistory.size() > 1) {
                    board = copy2D(boardHistory.get(boardHistory.size() - 2));
                    boardHistory.remove(boardHistory.size() - 1);
                    draw(gc);
                    printBoard();
                }
            }
        });
        undo.setPrefWidth(60);
        undo.setPrefHeight(30);
        undo.relocate(WIDTH-undo.getPrefWidth()*1.5, undo.getPrefHeight());
        root.getChildren().add(undo);
        
        primaryStage.show();
    }
    
    public static void draw(GraphicsContext gc) {
        drawBoard(gc);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, HEIGHT - UI_OFFSET_BOTTOM, WIDTH, UI_OFFSET_BOTTOM);
        int[] colorAmounts = countColors();
        for (int x = 0; x < colorAmounts.length; x++) {
            gc.setFill(COLOR_VALUES[x]);
            gc.setFont(new Font("Calibri", 40));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(colorAmounts[x] + "", x * 110 + 60, HEIGHT - 5);
        }
    }
    
    public static void drawBoard(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, UI_OFFSET_TOP, BLOCK_COUNT_HORIZONTAL * BLOCK_SIZE, BLOCK_COUNT_VERTICAL * BLOCK_SIZE);
        for(int x = 0; x < board.length; x++) {
            for(int y = 0; y < board[x].length; y++) {
                if (board[x][y] != 0) {
                    gc.setFill(COLOR_VALUES[board[x][y]-1]);
                    gc.fillRect(y * BLOCK_SIZE, x * BLOCK_SIZE + UI_OFFSET_TOP, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
    }
    
    public static void removeTiles(int x, int y) {
        if (x >= 0 && x < board.length && y >= 0 && y < board[0].length) {
            HashSet<Point> removeTiles = getConnected(x, y, new HashSet<Point>()); 
            if(removeTiles.size() >= GROUP_SIZE) {
                for (Point tile : removeTiles) {
                    board[tile.x][tile.y] = 0;
                }
            }
            adjustBoard();
            int endResult = checkEnd();
            if (endResult != 2) {
                System.out.println("End of game: " + endResult);
            }
        } else {
            System.out.println("Number out of bounds");
        }
    }
    
    public static void adjustBoard() {
        boolean colHasZero = false;
        int[] column = new int[board.length];
        
        for (int x = 0; x < board[0].length; x++) {
            colHasZero = false;
            for (int y = board.length - 1; y >= 0; y--) {
                column[column.length - y - 1] = board[y][x];
                if (board[y][x] == 0) {
                    colHasZero = true;
                }
            }
            
            if (colHasZero) {
                for (int j = 0; j < column.length; j++) {
                    if (column[j] == 0) {
                        for (int k = j + 1; k < column.length; k++) {
                            if (column[k] != 0) {
                                column[j] = column[k];
                                column[k] = 0;
                                break;
                            }
                        }
                    }
                }
                
                for (int y = board.length - 1; y >= 0; y--) {
                    board[y][x] = column[column.length - y - 1];
                }
            }
        }
        
        for (int y = board[board.length-1].length-1; y >= 0; y--) {
            if (board[board.length-1][y] == 0) {
                for (int x = y; x < board[board.length-1].length-1; x++) {
                    for (int j = 0; j < board.length; j++) {
                        int temp = board[j][x];
                        board[j][x] = board[j][x+1];
                        board[j][x+1] = temp;
                    }
                }
            }
        }
        boardHistory.add(copy2D(board));
    }
    
    public static int checkEnd() {
        if (isEmpty()) {
            return 0; //Board is empty
        } else if (!hasMoves()) {
            return 1; //Board has no more moves
        } else {
            return 2; //Player can still make a move
        }
    }
    
    public static HashSet<Point> getConnected(int x, int y, HashSet<Point> result) {
        int tileColor = board[x][y];
        if (x > 0 && board[x-1][y] == tileColor) {
            if (result.add(new Point(x-1, y))) {
                result.addAll(getConnected(x-1, y, result));
            }
        }
        if(x < board.length - 1 && board[x+1][y] == tileColor) {
            if (result.add(new Point(x+1, y))) {
                result.addAll(getConnected(x+1, y, result));
            }
        }
        if(y > 0 && board[x][y-1] == tileColor) {
            if (result.add(new Point(x, y-1))) {
                result.addAll(getConnected(x, y-1, result));
            }
        }
        if(y < board[x].length - 1 && board[x][y+1] == tileColor) {
            if (result.add(new Point(x, y+1))) {
                result.addAll(getConnected(x, y+1, result));
            }
        }
        return result;
    }
    
    public static boolean hasMoves() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y] != 0 && getConnected(x, y, new HashSet<Point>()).size() >= GROUP_SIZE) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isEmpty() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static int[] countColors() {
        int[] result = new int[COLOR_COUNT];
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y]-1 >= 0 && board[x][y]-1 < COLOR_COUNT) {
                    result[board[x][y]-1]++;
                }
            }
        }
        return result;
    }
    
    public static int[][] makeBoard(int width, int height, int colors) {
        int[][] board = new int[height][width];
        
        ArrayList<Point> tiles = new ArrayList<Point>();
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                tiles.add(new Point(x, y));
            }
        }
        Collections.shuffle(tiles);
        
        int currentColor = 1;
        int currentColorCount = 0;
        int minimumBlockCount = width * height / (colors + 1);
        while (!tiles.isEmpty()) {
            if (currentColor > colors) {
                board[tiles.get(0).x][tiles.get(0).y] = (int) (Math.random() * colors + 1);
            } else {
                board[tiles.get(0).x][tiles.get(0).y] = currentColor;
                currentColorCount++;
            }
            tiles.remove(0);
            if (currentColorCount >= minimumBlockCount) {
                currentColor++;
                currentColorCount = 0;
            }
        }
        return board;
    }
    
    public static void printBoard() {
        System.out.print("  ");
        for (int x = 0; x < board[0].length; x++) {
            System.out.printf("%3d", x);
        }
        System.out.println();
        System.out.print("  ");
        for(int x = 0; x < board[0].length; x++) {
            System.out.print("---");
        }
        System.out.println();
        for (int x = 0; x < board.length; x++) {
            System.out.print(x + "|");
            for (int y = 0; y < board[x].length; y++) {
                System.out.printf("%3d", board[x][y]);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static int[][] copy2D(int[][] copyOf) {
        int[][] result = new int[copyOf.length][copyOf[0].length];
        for (int x = 0; x < copyOf.length; x++) {
            for (int y = 0; y < copyOf[x].length; y++) {
                result[x][y] = copyOf[x][y];
            }
        }
        return result;
    }
}
