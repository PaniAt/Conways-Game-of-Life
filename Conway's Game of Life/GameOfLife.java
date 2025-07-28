/**
 * Plays Conway's Game of Life!
 *
 * @author Atreya Pandit
 * @version 20/05/2025
 *      - Made a simple version of the screen renderer.
 *      - Made a simple version of the tile map.
 * @version 26/05/2025
 *      - Added tile interactions
 *      - Added the ability to step single frames.
 *      - Added quit functionality.
 *      - Added accelerators for stepping frames.
 * @version 27/05/2025
 *      - Added the ability to step custom frames.
 *      - Added the ability to click and place tiles.
 *      - Added contextual popup messages.
 * @version 04/06/2025
 *      - Refactored the script for clicking tiles.
 * @version 09/06/2025
 *      - Added colour schemes and Interface menu.
 *      - Added the ability to toggle the grid.
 *      - (somewhat) removed the annoying screen flash effect.
 * @version 10/06/2025
 *      - Refactored the tile size, rows and cols calculations.
 * @version 16/06/2025
 *      - Added the autoplay feature.
 *      - Allowed users to drag the mouse cursor to change tiles.
 *      - Moved around a lot of code.
 * @version 17/06/2025
 *      - Added saving/loading features.
 *      - Birthday.
 * @version 18/06/2025
 *      - Added error handling for loading games.
 * @version 16/07/2025
 *      - Removed the print game feature.
 *      - Added the save game feature.
 *      - Mostly finished saving/loading.
 * @version 17/07/2025
 *      - Removed the unused "proxList" array.
 *      - Added a barely-working undo system.
 * @version 18/07/2025
 *      - Improved the undo system.
 *      - Minor changes to mouse logic.
 *      - Allowed "undoing" your undo.
 * @version 21/07/2025
 *      - Changed the colour schemes slightly.
 *      - Added some preset game boards for the user.
 * @version 22/07/2025
 *      - Added preset a few more preset game boards.
 *      - Allowed mouse actions during autoplay.
 * @version 23/07/2025
 *      - Made the screen now scale depending on display size.
 *      - Fixed TILE_COLS and TILE_ROWS being switched.
 *      - Moved the actionPerformed method to make code cleaner.
 *      - Added a message when illegal characters are entered in save file name.
 *      - Fixed inaccurate debug messages when loading boards.

 */
// Imports for User Interface and GUI.
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage; // For removing screen flash.
import java.awt.PointerInfo;
import java.io.FileWriter;

// File handling
import java.io.File;
import java.io.IOException;

public class GameOfLife extends JFrame implements ActionListener, MouseListener
{
    // Global popup message variable to prevent alerts from stacking.
    Popup popupMessage;
    // The offset that JFrame renders with.
    final int OFFSETX = 8;
    final int OFFSETY = 54;
    // Tile list dimensions
    final int TILE_ROWS = 80;
    final int TILE_COLS = 80;
    // Screen Dimensions
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // The game board should be a perfect square.
    int squareSize = (int) Math.min(screenSize.getWidth(), screenSize.getHeight())-OFFSETY*2;
    // Dividing then multiplying rounds to a factor of the tile array length.
    final int SCREEN_WIDTH = squareSize/TILE_COLS*TILE_COLS;
    final int SCREEN_HEIGHT = squareSize/TILE_ROWS*TILE_ROWS;
    // Tile List
    final int TILE_WIDTH = SCREEN_WIDTH/TILE_COLS;
    final int TILE_HEIGHT = SCREEN_HEIGHT/TILE_ROWS;
    int[][] tileList = new int[TILE_ROWS][TILE_COLS]; // List of tiles dead/alive
    int[][] prevTileList = new int[TILE_ROWS][TILE_COLS]; // List of the previous tile list, for the undo action.

    // Mouse Variables
    int[] mousePosition = new int[2]; // 0 - mouse x | 1 - mouse y
    boolean mouseDown = false;
    int[][] mouseChangedTiles = new int[TILE_ROWS][TILE_COLS]; // An array of positions the mouse interacted with during 1 click.
    boolean didMouseChangeTile = false; // Checks if a tile was changed in the mouseChangedTiles array.

    // Interface Variables
    int showGrid = 0;
    int colourMode = 1; // 1 - Normal colours | 0 - Inverted colours
    Color[] colourScheme = new Color[2];
    boolean debugMode = true;

    // Timing Variables
    int gameTimer = 0;
    boolean autoplay = false;
    /**
     * Constructor for objects of class GameOfLife
     */
    public GameOfLife()
    {
        setTitle("Conway's Game of Life");
        this.getContentPane().setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.getContentPane().setLayout(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        createMenuBars();

        addMouseListener(this); // For mouse events.

        colourScheme[0] = Color.WHITE;
        colourScheme[1] = Color.BLACK;

        this.pack();
        this.toFront();
        this.setVisible(true);

        while (true){
            gameTimer++;
            if (autoplay){
                if (gameTimer % 100 == 0){
                    gameStep();
                    repaint();
                }
            }
            mouseDownActions();

            try {
                Thread.sleep(1);
            } catch (InterruptedException interrupt){
                System.out.println("Thread Interupt Exception!");
                System.out.println(interrupt.getStackTrace());
            }
        }
    }

    private BufferedImage offScreenImage;
    /**
     * Draws out the screen.
     * 
     * @param g - The Graphics rendering thingy (given automatically)
     */
    public void paint(Graphics g){
        super.paint(g);
        if (offScreenImage == null){
            offScreenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D ctx = (Graphics2D) offScreenImage.getGraphics();
        ctx.setColor(new Color(200, 200, 200));
        ctx.fillRect(0+OFFSETX, 0+OFFSETY, SCREEN_WIDTH, SCREEN_HEIGHT);

        for (int y = 0; y < TILE_ROWS; y++){
            for (int x = 0; x < TILE_COLS; x++){
                if (tileList[y][x] == colourMode){
                    ctx.setColor(colourScheme[0]);
                } else {
                    ctx.setColor(colourScheme[1]);
                }
                ctx.fillRect(x*TILE_WIDTH+OFFSETX, y*TILE_HEIGHT+OFFSETY, TILE_WIDTH-showGrid, TILE_HEIGHT-showGrid);
                if (mouseChangedTiles[y][x] == 1){
                    // Grey square on-top to signify change.
                    ctx.setColor(new Color(200, 200, 200, 110));
                    ctx.fillRect(x*TILE_WIDTH+OFFSETX, y*TILE_HEIGHT+OFFSETY, TILE_WIDTH-showGrid, TILE_HEIGHT-showGrid);
                }
            }
        }

        g.drawImage(offScreenImage, 0, 0, null);
    }

    /**
     * Manages the actions which occur when the mouse is down.
     */
    public void mouseDownActions()
    {
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        // Get mouse position relative to the window.
        int mouseX = mouseLocation.x-getX();
        int mouseY = mouseLocation.y-getY();
        // Get the tile the mouse is touching
        int tileX = (mouseX-OFFSETX)/TILE_WIDTH;
        int tileY = (mouseY-OFFSETY)/TILE_HEIGHT;
        // Fast and easy way to limit the X and Y to be within the tile list.
        tileX = Math.max(Math.min(tileX, TILE_COLS-1), 0);
        tileY = Math.max(Math.min(tileY, TILE_ROWS-1), 0);
        // Switch the state of the tile and redraw the screen.
        if (mouseDown){
            if (mouseChangedTiles[tileY][tileX] != 1){
                mouseChangedTiles[tileY][tileX] = 1;
                didMouseChangeTile = true;
            }
            if (gameTimer % 50 == 0 && didMouseChangeTile){
                repaint();
                didMouseChangeTile = false;
            }
        }
    }

    /**
     * Steps the game by one frame. Does not automatically refresh the screen.
     */
    public void gameStep()
    {
        int[][] targetTileList = new int[TILE_ROWS][TILE_COLS];
        for (int y = 0; y < TILE_ROWS; y++){
            for (int x = 0; x < TILE_COLS; x++){
                targetTileList[y][x] = tileList[y][x];
                int nearbyTiles = 0;
                for (int checky = y-1; checky <= y+1; checky++){
                    if (checky >= 0 && checky < TILE_ROWS){
                        for (int checkx = x-1; checkx <= x+1; checkx++){
                            if (checkx >= 0 && checkx < TILE_COLS){
                                if (tileList[checky][checkx] == 1){
                                    if (checky != y || checkx != x){
                                        nearbyTiles++;
                                    }
                                }
                            }
                        }
                    }
                }
                // Live cells with fewer than two neighbours DIES!
                if (tileList[y][x] == 1 && nearbyTiles < 2) {
                    targetTileList[y][x] = 0;
                }
                // Live cells with two or three live neighbours DO NOTHING!
                if (tileList[y][x] == 1 && (nearbyTiles == 2 || nearbyTiles == 3)){
                    targetTileList[y][x] = 1;
                }
                // Live cells with more than three neighbours DIES!
                if (tileList[y][x] == 1 && nearbyTiles > 3){
                    targetTileList[y][x] = 0;
                }
                // Dead cells with exactly three neighbours REVIVES!
                if (tileList[y][x] == 0 && nearbyTiles == 3){
                    targetTileList[y][x] = 1;
                }
            }
        }
        tileList = targetTileList;
    }

    /**
     * Undoes the user's last action that could've changed the board.
     */
    public void revertGameBoard()
    {
        int[][] tempTileList = tileList.clone();
        tileList = prevTileList.clone();
        prevTileList = tempTileList.clone();
    }

    /**
     * Loads the game board from a file
     *
     * @param filePath - The path at which the target is located
     * @param replaceAll - Whether the entire game board should be loaded, or just active tiles.
     */
    public void loadBoard(String filePath, boolean replaceAll)
    {
        String toDecode = FileScanner.readFile(filePath);
        if (!toDecode.equals("NoFileError")){
            int[][] decodedBoard = decodeGameBoard(toDecode);
            switch (decodedBoard[0][0]){
                case -1: // Row amount error
                    createPopup("There are an incorrect amount of rows in that file!");
                    break;
                case -2: // Column amount error
                    createPopup("There are an incorrect amount of columns in that file!");
                    break;
                case -3: // Invalid character error
                    createPopup("That file contains an invalid character!");
                    break;
                default: // No error code!
                    prevTileList = new int[TILE_ROWS][TILE_COLS];
                    for (int y = 0; y < TILE_ROWS; y++){
                        for (int x = 0; x < TILE_COLS; x++){
                            prevTileList[y][x] = tileList[y][x];
                            tileList[y][x] = decodedBoard[y][x] == 1 || replaceAll? decodedBoard[y][x] : tileList[y][x];
                        }
                    }
                    repaint();
                    break;
            }
        } else {
            createPopup("That file path was invalid! Open your SavedGames folder to view all files!");
        }
    }

    /**
     * Creates a popup alert with the given text.
     *
     * @param text - What the popup should say.
     */
    public void createPopup(String text)
    {
        if (popupMessage != null){
            popupMessage.dispose();
        }
        popupMessage = new Popup(text);
        popupMessage.setLocationRelativeTo(this);
    }

    public void mouseExited(MouseEvent evt){}

    public void mouseEntered(MouseEvent evt){}

    public void mouseReleased(MouseEvent evt){
        if (mouseDown){
            mouseDown = false;
            prevTileList = new int[TILE_ROWS][TILE_COLS];
            for(int y = 0; y < TILE_ROWS; y++){
                for (int x = 0; x < TILE_COLS; x++){
                    prevTileList[y][x] = tileList[y][x];
                    if (mouseChangedTiles[y][x] == 1){
                        tileList[y][x] = 1 - tileList[y][x];
                        mouseChangedTiles[y][x] = 0;
                    }
                }
            }
            repaint();
        }
    }

    public void mousePressed(MouseEvent evt){
        int type = evt.getButton();
        if (type == 1){
            mouseDown = true;
            prevTileList = tileList.clone();
            mouseDownActions();
        } else if (type == 3 && mouseDown) {
            mouseDown = false;
            mouseChangedTiles = new int[TILE_ROWS][TILE_COLS];
            repaint();
        }
    }

    public void mouseClicked(MouseEvent evt){}

    /**
     * Encodes the entire game board.
     * 
     * @return text - The entire game board.
     */
    public String getGameBoard()
    {
        String text = "";
        for (int y = 0; y < TILE_ROWS; y++){
            for (int x = 0; x < TILE_COLS; x++){
                text = text+tileList[y][x]+(x==TILE_COLS-1? "" : ",");
            }
            text = text+(y!=TILE_ROWS-1?"-":"");
        }
        return text;
    }

    /**
     * Decodes an encoded game board, returning the result in a 2 dimensional array.
     *
     * @param encodedBoard - The encoded game board, comma separated along the x axis and pipe separated along the y axis.
     * @return The decoded version of the game board as an int[][] array.
     */
    public int[][] decodeGameBoard(String encodedBoard)
    {
        String[] stringBoardRows = encodedBoard.split("-");
        int[][] errorBoard = new int[1][1]; // To output when an error occurs.
        if (stringBoardRows.length != TILE_ROWS){
            if (debugMode){
                System.out.println("Error! Too "+(stringBoardRows.length<TILE_ROWS?"few":"many")+" rows!");
                System.out.println("Expected: "+TILE_ROWS+"! Got: "+stringBoardRows.length+"!");
            }
            errorBoard[0][0] = -1;
            return errorBoard; // Rows wrong
        }
        String[][] stringBoardFull = new String[TILE_ROWS][TILE_COLS];
        for (int i = 0; i < stringBoardRows.length; i++){
            if (stringBoardRows[i].split(",").length != TILE_COLS){
                if (debugMode){
                    System.out.println("Error! Too "+(stringBoardRows[i].split(",").length<TILE_COLS?"few":"many")+" columns at index: "+i+"!");
                    System.out.println("Expected: "+TILE_COLS+"! Got: "+stringBoardRows[i].split(",").length+"!");
                }
                errorBoard[0][0] = -2;
                return errorBoard; // Cols wrong
            }
            stringBoardFull[i] = stringBoardRows[i].split(",");
        }
        int[][] parsedBoard = new int[TILE_ROWS][TILE_COLS];
        for (int y = 0; y < TILE_ROWS; y++){
            for (int x = 0; x < TILE_COLS; x++){
                if (!stringBoardFull[y][x].equals("1") && !stringBoardFull[y][x].equals("0")){
                    if (debugMode){
                        System.out.println("Error! Did not recieve either 0 or 1 when reading file!");
                        System.out.println("At: (x:"+x+",y:"+y+"), Got: "+stringBoardFull[y][x]+"!"); 
                    }
                    errorBoard[0][0] = -3;
                    return errorBoard; // Invalid tile state
                }
                parsedBoard[y][x] = Integer.parseInt(stringBoardFull[y][x]);
            }
        }
        return parsedBoard;
    }

    /**
     * Creates all the menu bars.
     * I put it down here to make my code cleaner.
     */
    public void createMenuBars()
    {
        // Menu Bar Variables
        JMenuBar menuBar;
        JMenu menu;
        JMenu subMenu;
        JMenuItem menuItem;
        // Define the menu bar.
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        // The Game menu.
        menu = new JMenu("Game");
        menuBar.add(menu);

        // Game : Quit
        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Reset
        menuItem = new JMenuItem("Reset");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Step 1 Frame
        menuItem = new JMenuItem("Step 1 Frame");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('f')); // Only F key.
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Step Custom Frames
        menuItem = new JMenuItem("Step Custom Frames");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('F')); // Shift and F keys.
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Toggle Autoplay
        menuItem = new JMenuItem("Toggle Autoplay");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('p'));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Save
        menuItem = new JMenuItem("Save");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Preset Boards
        subMenu = new JMenu("Preset Boards");
        menu.add(subMenu);
        // Game : Preset Boards : Glider Gun
        menuItem = new JMenuItem("Glider Gun");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // Game : Preset Boards : Pascal's Triangle
        menuItem = new JMenuItem("Pascal's Triangle");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // Game : Preset Boards : Spaceship
        menuItem = new JMenuItem("Big Glider");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // Game : Preset Boards : Space Battle
        menuItem = new JMenuItem("Space Battle");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // Game : Load
        menuItem = new JMenuItem("Load");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Game : Undo
        menuItem = new JMenuItem("Undo");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('z'));
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The UI menu.
        menu = new JMenu("Interface");
        menuBar.add(menu);
        // UI : Toggle Grid
        menuItem = new JMenuItem("Toggle Grid");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('g'));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // UI : Colour Scheme
        subMenu = new JMenu("Colour Scheme");
        menu.add(subMenu);
        // UI : Colour Scheme : Default
        menuItem = new JMenuItem("Default");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Red
        menuItem = new JMenuItem("Red");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Orange
        menuItem = new JMenuItem("Orange");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Yellow
        menuItem = new JMenuItem("Yellow");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Green
        menuItem = new JMenuItem("Green");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Light Blue
        menuItem = new JMenuItem("Light Blue");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Dark Blue
        menuItem = new JMenuItem("Dark Blue");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Purple
        menuItem = new JMenuItem("Purple");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Pink
        menuItem = new JMenuItem("Pink");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Grey
        menuItem = new JMenuItem("Grey");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
        // UI : Colour Scheme : Toggle Inverted
        menuItem = new JMenuItem("Invert Colours");
        menuItem.addActionListener(this);
        subMenu.add(menuItem);
    }

    /**
     * Used for interactive menu bars.
     *
     * @param evt - The event action (given automatically)
     */
    public void actionPerformed(ActionEvent evt)
    {
        String cmd = evt.getActionCommand();
        switch (cmd){
            case "Quit":
                System.exit(0);
                break;
            case "Reset":
                prevTileList = tileList.clone();
                tileList = new int[TILE_ROWS][TILE_COLS];
                repaint();
                break;
            case "Step 1 Frame":
                prevTileList = tileList.clone();
                gameStep();
                repaint();
                break;
            case "Step Custom Frames":
                repaint();
                InputDialog userInput = new InputDialog("Enter how many frames you want to step (1-1000):");
                userInput.setLocationRelativeTo(this);
                userInput.setVisible(true);
                String userResponse = userInput.getText();
                // I love ripping code out of my other projects!
                try{ // The parseInt function will throw a NumberFormatException error if a non-integer is entered.
                    int stepAmount = Integer.parseInt(userResponse);
                    if (stepAmount < 1){ // There is no undoing your mistakes!
                        createPopup("Please enter a number greater than 0!");
                    } else if (stepAmount > 1000){ // Too many will make a huge delay.
                        createPopup("Please enter a number fewer than 1,000!");
                    } else {
                        prevTileList = tileList.clone();
                        for (int i = 0; i < stepAmount; i++){
                            gameStep();
                        }
                        repaint();
                    }
                } catch(NumberFormatException e){ // The NumberFormatException is the only 'expected' error.
                    createPopup("Please enter a number from 1 to 1,000!");
                }
                break;
            case "Toggle Autoplay":
                if (autoplay){
                    autoplay = false;
                } else {
                    prevTileList = tileList.clone();
                    autoplay = true;
                }
                repaint();
                break;
            case "Save":
                repaint();
                String board = getGameBoard();
                InputDialog getSaveFile = new InputDialog("What do you want to name this save?");
                getSaveFile.setLocationRelativeTo(this);
                getSaveFile.setVisible(true);
                String saveFileName = "./SavedGames/"+getSaveFile.getText()+".txt";
                // Make sure there are no illegal characters.
                String legalCharacters = "abcdefghijklmnopqrstuvwxyz1234567890 _-";
                boolean validName = true;
                for (int i = 0; i < getSaveFile.getText().split("").length && validName; i++){ // Evil.
                    String c = getSaveFile.getText().split("")[i];
                    if (legalCharacters.indexOf(c.toLowerCase()) < 0){
                        createPopup("Please only use alphanumeric characters!");
                        validName = false;
                    }
                }
                if (validName){
                    // Create the file, overrides any pre-existing file with the same name.
                    try{
                        File saveFile = new File(saveFileName);
                        if (!saveFile.createNewFile()){
                            saveFile.delete();
                            saveFile.createNewFile();
                        }
                        FileWriter fileWriter = new FileWriter(saveFileName);
                        fileWriter.write(board);
                        fileWriter.close(); // BlueJ said I'd have a memory leak, which sounds bad.
                    } catch (IOException e){
                        if (debugMode){
                            e.printStackTrace();
                        }
                        createPopup("There was an error creating the file!"); // "Help users recognise, diagnose and recover from errors"
                    }
                }
                repaint();
                break;
            case "Load":
                repaint();
                InputDialog getFile = new InputDialog("Enter the name of your file:");
                getFile.setLocationRelativeTo(this);
                getFile.setVisible(true);
                String userFile = getFile.getText();
                loadBoard("./SavedGames/"+userFile+".txt", true);
                break;
            case "Undo":
                boolean canUndo = false;
                for (int y = 0; y < TILE_ROWS && !canUndo; y++){
                    for (int x = 0; x < TILE_COLS && !canUndo; x++){
                        if (prevTileList[y][x] != tileList[y][x]){
                            canUndo = true;
                        }
                    }
                }
                if (!canUndo){
                    createPopup("There is nothing to undo!");
                } else if (autoplay) {
                    createPopup("You cannot undo while autoplay is active!");
                } else{
                    revertGameBoard();
                }
                repaint();
                break;

            case "Toggle Grid":
                showGrid = 1 - showGrid;
                repaint();
                break;
            case "Invert Colours":
                colourMode = 1 - colourMode;
                repaint();
                break;
            case "Red":
                colourScheme[0] = new Color(240, 85, 110);
                colourScheme[1] = new Color(220, 20, 50);
                repaint();
                break;
            case "Orange":
                colourScheme[0] = new Color(230, 95, 15);
                colourScheme[1] = new Color(220, 50, 15);
                repaint();
                break;
            case "Yellow":
                colourScheme[0] = new Color(255, 190, 30);
                colourScheme[1] = new Color(250, 169, 15);
                repaint();
                break;
            case "Green":
                colourScheme[0] = new Color(205, 255, 50);
                colourScheme[1] = new Color(110, 220, 20);
                repaint();
                break;
            case "Light Blue":
                colourScheme[0] = new Color(145, 225, 240);
                colourScheme[1] = new Color(0, 180, 220);
                repaint();
                break;
            case "Dark Blue":
                colourScheme[0] = new Color(0, 120, 180);
                colourScheme[1] = new Color(10, 10, 95);
                repaint();
                break;
            case "Purple":
                colourScheme[0] = new Color(190, 100, 230);
                colourScheme[1] = new Color(65, 10, 95);
                repaint();
                break;
            case "Pink":
                colourScheme[0] = new Color(255, 195, 230);
                colourScheme[1] = new Color(255, 145, 180);
                repaint();
                break;
            case "Grey":
                colourScheme[0] = Color.GRAY;
                colourScheme[1] = Color.DARK_GRAY;
                repaint();
                break;
            case "Default":
                colourScheme[0] = Color.WHITE;
                colourScheme[1] = Color.BLACK;
                repaint();
                break;

            case "Glider Gun":
                loadBoard("./PresetGameBoards/GliderGun.txt", false);
                repaint();
                break;
            case "Pascal's Triangle":
                loadBoard("./PresetGameBoards/PascalTriangle.txt", true);
                repaint();
                break;
            case "Big Glider":
                loadBoard("./PresetGameBoards/BigGlider.txt", false);
                repaint();
                break;
            case "Space Battle":
                loadBoard("./PresetGameBoards/Battle.txt", true);
                repaint();
                break;
        }
    }
}