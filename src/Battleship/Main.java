package Battleship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {

    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {

        try {
            GameField gameField = new GameField("Player 1");
            createPlayer(gameField);
            clrscr();
            System.out.println("\nPress enter to pass the move to another player");
            reader.readLine();
            GameField gameField2 = new GameField("Player 2");
            createPlayer(gameField2);
            clrscr();
            System.out.println();

            makeAmove(gameField, gameField2); // recursive method
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void clrscr() {
        //Clears Screen in java
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                System.out.println();
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    static void createPlayer(GameField gameField) {
        gameField.printGrid(gameField.getGrid());
        for (int i = 0; i < gameField.battleships.length; ) {
            System.out.printf("%nEnter the coordinates for the %s (%d cells):%n%n",
                    gameField.battleships[i].getName(), gameField.battleships[i].getCells());
            try {
                String[] input = reader.readLine().trim().split(" ");
                if (input.length < 2) throw new Exception("Invalid coordinates");
                gameField.placeUnit(gameField.battleships[i], input[0], input[1]);
                System.out.println();
                gameField.printGrid(gameField.getGrid());
                i++;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    static void makeAmove(GameField player, GameField enemy) {
        try {
            System.out.println("\nPress enter to pass the move to another player");

            reader.readLine();
            clrscr();
            System.out.println();
            player.printGrid(enemy.getGridFog());
            System.out.println("----------------------");
            player.printGrid(player.getGrid());
            System.out.println("\n" + player.getPlayerName() + ", it's your turn.\n");
            player.takeAshot(reader.readLine(), enemy);
//            clrscr();
            if (enemy.getRemainingHits() == 0) return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        makeAmove(enemy, player);
    }
}

enum UnitType {
    AIRCRAFT_CARRIER(5, "Aircraft Carrier"), BATTLESHIP(4, "Battleship"),
    SUBMARINE(3, "Submarine"), CRUSIER(3, "Cruiser"), DESTROYER(2, "Destroyer");
    private int cells;
    private String name;

    UnitType(int cells, String name) {
        this.cells = cells;
        this.name = name;
    }

    public int getCells() {
        return this.cells;
    }

    public String getName() {
        return name;
    }
}

class GameField {
    String playerName;
    BattleShip battleShip = new BattleShip(UnitType.BATTLESHIP);
    BattleShip aircraftCarrier = new BattleShip(UnitType.AIRCRAFT_CARRIER);
    BattleShip cruiser = new BattleShip(UnitType.CRUSIER);
    BattleShip destroyer = new BattleShip(UnitType.DESTROYER);
    BattleShip submarine = new BattleShip(UnitType.SUBMARINE);
    BattleShip[] battleships = {aircraftCarrier, battleShip, submarine, cruiser, destroyer};
    private int rows = 10;
    private int columns = 10;
    private String[][] grid = new String[rows + 1][columns + 1];
    private String[][] gridFog = new String[rows + 1][columns + 1];
    private int remainingHits;

    //create game field and initialize cells to "~" and end column and row to numbers and letters for coordinates
    GameField(String playerName) {
        this.playerName = playerName;
        System.out.printf("%n%s, place your ships to the game field%n%n", playerName);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                this.grid[i][j] = i == 0 && j != 0 ? String.valueOf(j) :
                        j == 0 && i != 0 ? String.valueOf((char) ('@' + i)) : "~";
                this.gridFog[i][j] = i == 0 && j != 0 ? String.valueOf(j) :
                        j == 0 && i != 0 ? String.valueOf((char) ('@' + i)) : "~";
            }
        }
        for (BattleShip b : this.battleships) {
            this.remainingHits += b.getCells();
        }
        grid[0][0] = " ";
        gridFog[0][0] = " ";
    }

    void placeUnit(BattleShip battleShip, String coordFirst, String coordSecond) throws Exception {

        Coordinates firstCoord = new Coordinates(coordFirst);
        Coordinates secondCoord = new Coordinates(coordSecond);
        Coordinates startCoord = firstCoord.getX() < secondCoord.getX() || firstCoord.getY() < secondCoord.getY() ?
                firstCoord : secondCoord;
        Coordinates endCoord = startCoord.equals(firstCoord) ? secondCoord : firstCoord;
        if (firstCoord.getX() != secondCoord.getX() && firstCoord.getY() != secondCoord.getY()) {
            throw new Exception("\nError! Wrong ship location! Try again:");
        }
        if (startCoord.getX() == endCoord.getX() && endCoord.getY() - startCoord.getY() != battleShip.getCells() - 1 ||
                startCoord.getY() == endCoord.getY() && endCoord.getX() - startCoord.getX() != battleShip.getCells() - 1) {
            throw new Exception(String.format("%nError! Wrong length of the %s! Try again: ", battleShip.getName()));
        }
        for (int i = 0; i < battleShip.getCells(); i++) {

            if (startCoord.getX() == endCoord.getX()) {
                if (!this.isFree(startCoord.getY() + i, endCoord.getX())) {
                    throw new Exception("Occupied cell");
                }
                if (startCoord.getX() > 1 && !this.isFree(startCoord.getY() + i, startCoord.getX() - 1) ||
                        startCoord.getX() < 10 && !this.isFree(startCoord.getY() + i, startCoord.getX() + 1)) {
                    throw new Exception("Error! You placed it too close to another one. Try again:");
                }
            }
            if (startCoord.getY() == endCoord.getY()) {
                if (!this.isFree(startCoord.getY(), startCoord.getX() + i)) {
                    throw new Exception("Occupied cell");
                }
                if (startCoord.getY() > 1 && !this.isFree(startCoord.getY() - 1, startCoord.getX() + i) ||
                        startCoord.getY() < 10 && !this.isFree(startCoord.getY() + 1, startCoord.getX() + i)) {
                    throw new Exception("Error! You placed it too close to another one. Try again:");
                }
            }
        }


        if (startCoord.getY() == endCoord.getY()) {
            if (startCoord.getX() > 1 && !isFree(startCoord.getY(), startCoord.getX() - 1) ||
                    endCoord.getX() < 10 && !isFree(startCoord.getY(), startCoord.getX() + 1)) {
                throw new Exception("Error! You placed it too close to another one. Try again:");
            }
        }
        if (startCoord.getX() == endCoord.getX()) {
            if (startCoord.getY() > 1 && !isFree(startCoord.getY() - 1, startCoord.getX()) ||
                    endCoord.getY() < 10 && !isFree(endCoord.getY() + 1, startCoord.getX())) {
                throw new Exception("Error! You placed it too close to another one. Try again:");
            }
        }

        for (int i = 0; i < battleShip.getCells(); i++) {
            if (startCoord.getX() == endCoord.getX()) {
                this.grid[startCoord.getY() + i][startCoord.getX()] = "O";
                battleShip.addcoordsToList(new Coordinates(startCoord.getY() + i, startCoord.getX()));
            } else {
                this.grid[startCoord.getY()][startCoord.getX() + i] = "O";
                battleShip.addcoordsToList(new Coordinates(startCoord.getY(), startCoord.getX() + i));
            }
        }
    }

    public String[][] getGrid() {
        return grid;
    }

    public String[][] getGridFog() {
        return gridFog;
    }

    public void printGrid(String[][] grid1) {
        for (int i = 0; i < grid1.length; i++) {
            for (String s : grid1[i]) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }

    boolean isFree(int row, int col) {
        return this.grid[row][col].equals("~");
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getRemainingHits() {
        return remainingHits;
    }

    void takeAshot(String inputShotCoords, GameField enemyField) throws Exception {
        String message;
        Coordinates shotCoords = new Coordinates(inputShotCoords);
        if (!shotCoords.isValidCoord(10, 1)) {
            throw new Exception("Invalid shot coordinate");
        }
        if (enemyField.getGrid()[shotCoords.getY()][shotCoords.getX()].equals("O")) {
            enemyField.getGridFog()[shotCoords.getY()][shotCoords.getX()] = "X";
            enemyField.getGrid()[shotCoords.getY()][shotCoords.getX()] = "X";
            enemyField.remainingHits--;


            message = "\nYou hit a ship!\n";
            for (BattleShip unit : enemyField.battleships) {
                unit.updateCoordsList(shotCoords);
                if (unit.getCells() == 0) {
                    message = "\nYou sank a ship! Specify a new target:\n";
                    unit.setCells(-1);
                    break;
                }
            }
            if (enemyField.getRemainingHits() == 0) {
                message = "\nYou sank the last ship. You won. Congratulations!\n";
            }
        } else {
            message = "\nYou missed. Try again:\n";
            if (enemyField.getGrid()[shotCoords.getY()][shotCoords.getX()].equals("~")) {
                enemyField.getGridFog()[shotCoords.getY()][shotCoords.getX()] = "M";
                enemyField.getGrid()[shotCoords.getY()][shotCoords.getX()] = "M";
            }
        }
        System.out.println();
        Main.clrscr();
        System.out.print(message);
    }

    class Coordinates {
        int x;
        int y;

        Coordinates(String coordinates) throws Exception {
            String[] coordArr = coordinates.split("");
            if (coordArr.length < 2 || coordArr.length > 3 ||
                    !Character.isDigit(coordArr[1].charAt(0)) ||
                    (coordArr.length == 3 && !Character.isDigit(coordArr[2].charAt(0))))
                throw new Exception("Invalid from coordinate");
            int y = Integer.valueOf(coordArr[0].charAt(0)) - 64; //convert char(y) coordinate to int for ease
            int x = coordArr.length == 3 ? Integer.valueOf(coordArr[1] + coordArr[2]) : Integer.valueOf(coordArr[1]);
            this.x = x;
            this.y = y;
        }

        Coordinates(int y, int x) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isValidCoord(int coordsMaxLimit, int coordsMinLimit) {
            return this.x >= coordsMinLimit && this.y >= coordsMinLimit &&
                    this.x <= coordsMaxLimit && this.y <= coordsMaxLimit;
        }

        @Override
        public String toString() {
            return String.valueOf(y) + x;
        }

        @Override
        public boolean equals(Object object) {
            return this.y == ((Coordinates) object).y && this.x == ((Coordinates) object).x;
        }
    }
}

class BattleShip {

    UnitType unitType;
    String name;
    private int cells;
    private boolean destroyed = false;
    public ArrayList<GameField.Coordinates> shipCoordsList = new ArrayList<>();

    public BattleShip(UnitType unitType) {
        this.unitType = unitType;
        this.cells = unitType.getCells();
        this.name = unitType.getName();
    }

    public void updateCoordsList(GameField.Coordinates coordinates) {
        if (this.shipCoordsList.contains(coordinates)) {
            this.shipCoordsList.remove(coordinates);
            this.cells--;
        }
    }

    public void addcoordsToList(GameField.Coordinates coordinates) {
        this.shipCoordsList.add(coordinates);
    }

    public String getName() {
        return name;
    }

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }
}



