import java.io.*;  // Import I/O classes for file reading/writing
import java.util.*;  // Import utility classes (Scanner, ArrayList, etc.)
import java.text.SimpleDateFormat;  // Import for formatting dates

// Custom exception raised when board dimensions are invalid
class InvalidBoardDimensionException extends Exception {
    public InvalidBoardDimensionException(String message) {
        super(message);  // Pass error message to parent Exception
    }
}

public class projectFinal {
    // Global variables for board dimensions and storage
    private static int ROWS;  // Number of rows in the game board
    private static int COLS;  // Number of columns in the game board
    private static char[][] board;  // 2D array representing the board grid

    // File name where game history is stored
    private static final String GAME_HISTORY_FILE = "game_history.txt";

    // Scanner for reading user input from console
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ensureHistoryFileExists();  // Make sure history file is created
        mainMenu();  // Enter the main menu loop
    }

    // Create history file if it does not exist
    private static void ensureHistoryFileExists() {
        File file = new File(GAME_HISTORY_FILE);  // Reference to file
        try {
            if (!file.exists()) {  // If file does not exist on disk
                file.createNewFile();  // Create empty history file
            }
        } catch (IOException e) {
            // Notify user if file creation fails
            System.out.println("Could not initialize history file: " + e.getMessage());
        }
    }

    // Display main menu options in a loop
    private static void mainMenu() {
        while (true) {
            System.out.println("\n==== Connect Four Game ====");
            System.out.println("1. Start New Game");  // Option to start a fresh game
            System.out.println("2. View Game History");  // Option to display saved results
            System.out.println("3. Delete Game History");  // Option to clear history file
            System.out.println("4. Exit Game");  // Option to quit application
            System.out.print("Please select an option (1-4): ");

            try {
                int choice = scanner.nextInt();  // Read numeric choice
                scanner.nextLine();  // Consume newline left by nextInt()
                switch (choice) {
                    case 1:
                        startNewGame();  // Begin a new game session
                        break;
                    case 2:
                        viewGameHistory();  // Show previous game results
                        break;
                    case 3:
                        deleteGameHistory();  // Delete the history file contents
                        break;
                    case 4:
                        System.out.println("\nThank you for playing!");
                        System.exit(0);  // Terminate the program
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1-4.");
                }
            } catch (InputMismatchException e) {
                // Handle non-numeric input gracefully
                System.out.println("Invalid input. Please enter a number between 1-4.");
                scanner.nextLine();  // Clear invalid token
            }
        }
    }

    // Start a new game: setup dimensions, initialize, and play
    private static void startNewGame() {
        try {
            setupGameDimensions();  // Ask user for rows and columns
            initializeBoard();  // Create empty board array
            playGame();  // Play until win or draw
        } catch (InvalidBoardDimensionException e) {
            // If dimensions invalid, show error and retry
            System.out.println("\nError: " + e.getMessage());
            System.out.println("Please try again with valid dimensions.");
            startNewGame();  // Recursively restart setup
        }
    }

    // Prompt user for board dimensions and validate
    private static void setupGameDimensions() throws InvalidBoardDimensionException {
        System.out.println("\n=== Game Setup ===");
        ROWS = getDimensionInput("rows");  // Get row count
        COLS = getDimensionInput("columns");  // Get column count
        if (ROWS < 4 || COLS < 4) {
            // Enforce minimum size rule
            throw new InvalidBoardDimensionException(
                "Both rows and columns must be at least 4. You entered: "
                + ROWS + " rows and " + COLS + " columns.");
        }
        // Confirm valid dimensions
        System.out.printf("\nGame board set to %d rows and %d columns.%n", ROWS, COLS);
    }

    // Read and validate a dimension (rows or columns)
    private static int getDimensionInput(String dimensionType) {
        while (true) {
            System.out.printf("Enter number of %s (minimum 4): ", dimensionType);
            String input = scanner.nextLine().trim();  // Read line and trim spaces
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;  // Loop until non-empty
            }
            try {
                return Integer.parseInt(input);  // Parse integer
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    // Allocate and clear the board array
    private static void initializeBoard() {
        board = new char[ROWS][COLS];  // Create 2D char array
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ' ';  // Fill each cell with blank space
            }
        }
    }

    // Main game loop: alternate turns until end condition
    private static void playGame() {
        char currentPlayer = 'X';  // Start with player X
        // Record timestamp for history
        String gameStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("\nGame started at: " + gameStartTime);
        while (true) {
            printBoard();  // Display current board state
            System.out.println("Player " + currentPlayer + "'s turn");
            int col = getValidColumn();  // Ask user for column choice
            if (placePiece(col, currentPlayer)) {
                // Check for win condition
                if (checkWin(currentPlayer)) {
                    printBoard();
                    System.out.println("\nPlayer " + currentPlayer + " wins!");
                    saveGameResult(currentPlayer, gameStartTime);  // Save outcome
                    break;  // Exit loop
                } else if (isBoardFull()) {
                    printBoard();
                    System.out.println("\nThe game is a draw!");
                    saveGameResult('D', gameStartTime);  // D = Draw
                    break;
                }
                // Switch player for next turn
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            } else {
                System.out.println("Column is full. Please choose another column.");
            }
        }
        askToPlayAgain();  // Offer replay
    }

    // Print the board with borders and indices
    private static void printBoard() {
        System.out.print("\n  ");
        for (int i = 1; i <= COLS; i++) {
            System.out.print(" " + i + "  ");  // Column labels
        }
        System.out.println();
        // Top border
        System.out.print(" ┌");
        for (int i = 0; i < COLS; i++) {
            System.out.print("───");
            if (i < COLS - 1) System.out.print("┬");
        }
        System.out.println("┐");
        // Board rows
        for (int i = 0; i < ROWS; i++) {
            System.out.print(" │");
            for (int j = 0; j < COLS; j++) {
                System.out.print(" " + board[i][j] + " │");  // Cell contents
            }
            System.out.println();
            if (i < ROWS - 1) {
                // Middle border between rows
                System.out.print(" ├");
                for (int j = 0; j < COLS; j++) {
                    System.out.print("───");
                    if (j < COLS - 1) System.out.print("┼");
                }
                System.out.println("┤");
            }
        }
        // Bottom border
        System.out.print(" └");
        for (int i = 0; i < COLS; i++) {
            System.out.print("───");
            if (i < COLS - 1) System.out.print("┴");
        }
        System.out.println("┘");
    }

    // Get a valid column index from user input
    private static int getValidColumn() {
        while (true) {
            System.out.printf("Select column (1-%d): ", COLS);
            try {
                int col = scanner.nextInt() - 1;  // Adjust to 0-based index
                scanner.nextLine();  // Consume newline
                if (col >= 0 && col < COLS) {
                    return col;  // Valid column
                } else {
                    System.out.printf("Invalid column. Enter 1 to %d.%n", COLS);
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();  // Clear invalid token
            }
        }
    }

    // Place a piece in the chosen column, bottommost available
    private static boolean placePiece(int col, char player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == ' ') {
                board[row][col] = player;  // Fill first empty cell from bottom
                return true;  // Placement successful
            }
        }
        return false;  // Column was full
    }

    // Check for any 4-in-a-row win horizontally, vertically, or diagonally
    private static boolean checkWin(char player) {
        // Horizontal check
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (board[r][c] == player && board[r][c+1] == player
                    && board[r][c+2] == player && board[r][c+3] == player) {
                    return true;
                }
            }
        }
        // Vertical check
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == player && board[r+1][c] == player
                    && board[r+2][c] == player && board[r+3][c] == player) {
                    return true;
                }
            }
        }
        // Diagonal down-right check
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (board[r][c] == player && board[r+1][c+1] == player
                    && board[r+2][c+2] == player && board[r+3][c+3] == player) {
                    return true;
                }
            }
        }
        // Diagonal down-left check
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 3; c < COLS; c++) {
                if (board[r][c] == player && board[r+1][c-1] == player
                    && board[r+2][c-2] == player && board[r+3][c-3] == player) {
                    return true;
                }
            }
        }
        return false;  // No win found
    }

    // Determine if the board is completely filled
    private static boolean isBoardFull() {
        for (int c = 0; c < COLS; c++) {
            if (board[0][c] == ' ') {
                return false;  // At least one open cell
            }
        }
        return true;  // Top row full means entire board is full
    }

    // Prompt the user to play another round or return to menu
    private static void askToPlayAgain() {
        System.out.print("\nWould you like to play again? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            startNewGame();  // Restart play
        } else {
            System.out.println("Returning to main menu...");
        }
    }

    // Append the game result to the history file
    private static void saveGameResult(char winner, String startTime) {
        String result = String.format(
            "Game at %s | Board: %dx%d | Result: %s",
            startTime, ROWS, COLS,
            (winner == 'D') ? "Draw" : "Player " + winner + " won"
        );
        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new FileWriter(GAME_HISTORY_FILE, true)))) {
            writer.println(result);  // Append line
            System.out.println("Game result saved to history.");
        } catch (IOException e) {
            System.out.println("Error saving game result: " + e.getMessage());
        }
    }

    // Read and display all lines from history file
    private static void viewGameHistory() {
        File file = new File(GAME_HISTORY_FILE);
        if (!file.exists() || file.length() == 0) {
            System.out.println("\nNo game history found.");
            return;  // Exit if no data
        }
        System.out.println("\n==== Game History ====");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // Print each entry
            }
        } catch (IOException e) {
            System.out.println("Error reading game history: " + e.getMessage());
        }
        System.out.println("======================");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();  // Wait for user input
    }

    // Delete the history file and recreate it empty
    private static void deleteGameHistory() {
        File file = new File(GAME_HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("\nNo game history to delete.");
            return;
        }
        System.out.print("\nAre you sure you want to delete ALL game history? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            if (file.delete()) {
                System.out.println("Game history deleted successfully.");
                ensureHistoryFileExists();  // Recreate empty file
            } else {
                System.out.println("Failed to delete game history.");
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }
}
