import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import javafx.scene.paint.Color;

public class Board {
    public int[][] board;
    private int width;
    private int height;
    private int minGroupSize;
    public ArrayList<int[][]> boardHistory;
    public Color[] colors;

    public Board(int width, int height, int minGroupSize, Color[] colors) {
        this.width = width;
        this.height = height;
        this.minGroupSize = minGroupSize;
        this.colors = colors;
        boardHistory = new ArrayList<int[][]>();
        generateBoard();
    }
    
    public void removeGroup(int x, int y) {
        if (x >= 0 && x < board.length && y >= 0 && y < board[x].length) {
            HashSet<Point> group = getGroup(x, y, new HashSet<Point>());
            if (group.size() >= minGroupSize) {
                for (Point tile : group) {
                    board[tile.x][tile.y] = 0;
                }
            }
            adjustBoard();
        } else {
            System.out.println("Number out of bounds");
        }
    }
    
    public void adjustBoard() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 1; y < board[x].length; y++) {
                if (board[x][y] == 0) {
                    for (int z = y - 1; z >= 0; z--) {
                        board[x][z + 1] = board[x][z];
                        board[x][z] = 0;
                    }
                }
            }
        }
        
        for (int x = board.length - 2; x >= 0; x--) {
            if(board[x][board[x].length - 1] == 0) {
                for (int y = x + 1; y < board.length; y++) {
                    for (int z = 0; z < board[y].length; z++) {
                        board[y - 1][z] = board[y][z];
                        board[y][z] = 0;
                    }
                }
                
            }
        }
        boardHistory.add(copyBoard());
    }
    
    public HashSet<Point> getAllGroups() {
        HashSet<Point> result = new HashSet<Point>();
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                HashSet<Point> group = getGroup(x, y, new HashSet<Point>());
                boolean inResult = false;
                if (result.isEmpty() && !group.isEmpty()) {
                    result.add((Point) group.toArray()[0]);
                } else if (!group.isEmpty()) {
                    for (Point point : result) {
                        if (group.contains(point)) {
                            inResult = true;
                            break;
                        }
                    }
                    if (!inResult) {
                        result.add((Point) group.toArray()[0]);
                    }
                }
            }
        }
        
        return result;
    }
    
    public HashSet<Point> getGroup(int x, int y, HashSet<Point> result) {
        int tileColor = board[x][y];
        if (tileColor == 0) {
            result.clear();
            return result;
        }
        if (x > 0 && board[x - 1][y] == tileColor)  {
            if (result.add(new Point(x - 1, y))) {
                result.addAll(getGroup(x - 1, y, result));
            }
        }
        if (x < board.length - 1 && board[x + 1][y] == tileColor)  {
            if (result.add(new Point(x + 1, y))) {
                result.addAll(getGroup(x + 1, y, result));
            }
        }
        if (y > 0 && board[x][y - 1] == tileColor)  {
            if (result.add(new Point(x, y - 1))) {
                result.addAll(getGroup(x, y - 1, result));
            }
        }
        if (y < board[x].length - 1 && board[x][y + 1] == tileColor)  {
            if (result.add(new Point(x, y + 1))) {
                result.addAll(getGroup(x, y + 1, result));
            }
        }
        if (result.size() >= minGroupSize) {
            return result;
        }
        result.clear();
        return result;
    }
    
    public int checkEnd() {
        if (isCleared()) {
            return 0; // Board is cleared
        } else if (!hasMoves()) {
            return 1; // Board has no more moves left
        } else {
            return 2; // Player still has a move
        }
    }
    
    public boolean hasMoves() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y] != 0 && getGroup(x, y, new HashSet<Point>()).size() >= minGroupSize) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public int[] countColors() {
        int[] colorCounts = new int[colors.length];
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y] != 0) {
                    colorCounts[board[x][y] - 1]++;
                }
            }
        }
        return colorCounts;
    }
    
    // untested
    public boolean isCleared() {
        return board[0][board[0].length - 1] == 0;
    }

    public void generateBoard() {
        board = new int[width][height];

        // Completely random
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                board[x][y] = (int) (Math.random() * colors.length + 1);
            }
        }
        
       // Generates board where each color is at least 1/(n-1) of the board
//        ArrayList<Point> tiles = new ArrayList<Point>();
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                tiles.add(new Point(x, y));
//            }
//        }
//        Collections.shuffle(tiles);
//        
//        int currentColor = 1;
//        int currentColorCount = 0;
//        int minimumBlockCount = width * height / (numColors + 1);
//        while (!tiles.isEmpty()) {
//            if (currentColor > numColors) {
//                board[tiles.get(0).x][tiles.get(0).y] = (int) (Math.random() * numColors + 1);
//            } else {
//                board[tiles.get(0).x][tiles.get(0).y] = currentColor;
//                currentColorCount++;
//            }
//            tiles.remove(0);
//            if (currentColorCount >= minimumBlockCount) {
//                currentColor++;
//                currentColorCount = 0;
//            }
//        }
        
        boardHistory.clear();
        boardHistory.add(copyBoard());
    }
    
    public void setBoard(int[][] newBoard) {
        for (int x = 0; x < newBoard.length; x++) {
            for (int y = 0; y < newBoard[x].length; y++) {
                board[x][y] = newBoard[x][y];
            }
        }
    }
    
    public int[][] copyBoard() {
        int[][] result = new int[board.length][board[0].length];
        
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                result[x][y] = board[x][y];
            }
        }
        
        return result;
    }
    
    //clones everything except for boardHistory
    public Board cloneBoard() {
        Color[] colorClone = new Color[colors.length];
        for (int x = 0; x < colors.length; x++) {
            colorClone[x] = colors[x];
        }
        Board result = new Board(width, height, minGroupSize, colorClone);
        result.board = copyBoard();
        
        return result;
    }

    public void printBoard() {
        System.out.print("   ");
        for (int x = 0; x < board.length; x++) {
            System.out.printf("%3d", x);
        }
        System.out.println();
        System.out.print("   ");
        for (int x = 0; x < board.length; x++) {
            System.out.printf("%3s", "---");
        }
        System.out.println();
        for (int y = 0; y < board[0].length; y++) {
            System.out.printf("%3s", y + "|");
            for (int x = 0; x < board.length; x++) {
                System.out.printf("%3d", board[x][y]);
            }
            System.out.println();
        }
        System.out.println();
    }
}
