import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import java.util.Map;
import java.lang.Math;
import java.util.Optional;

/**
 *  Launches GUI, expects a filename upon launch
 *
 *  Loads three main areas of GUI
 */
public class CrawlGui extends Application {

    private static BoundsMapper bm;

    private GraphicsContext gc;
    private Cartographer carto;
    private TextArea history;
    private BorderPane borderPane;
    private int side = 38;


    /**
     * Expects filename to load map as first command line argument, loads
     * file and sets up map, and launches application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Object[] startingObjects;
       /** if (args.length != 1) {
            System.err.println("Usage: java CrawlGui mapname\n");
            System.exit(1);
        } else {
            try {
                startingObjects = MapIO.loadMap(args[0]);
            } catch (Exception e) {
                //Does not call?
                System.err.println("Unable to load file " + args[0] + "\n");
                System.exit(2);
            }
        }

        startingObjects = MapIO.loadMap(args[0]);*/

        startingObjects = MapIO.loadMap("demo.map");

        if (startingObjects != null) {
            Room startRoom = (Room) startingObjects[1];
            Player player = (Player) startingObjects[0];
            addMoreToRooms(startingObjects, startRoom);

            bm = new BoundsMapper(startRoom);
            bm.walk();
            startRoom.enter(player);
        } else {
            System.out.println("Starting objects null, failed to load");
        }
        launch(args);
    }

    /**
     * Fills the map with more rooms and things for testing
     *
     * @param startingObjects Object array of Player and starting Room
     * @return true if successful
     */
    private static void addMoreToRooms(Object[] startingObjects, Room
            startRoom) {
        Room room2 = new Room("garden");
        Room room3 = new Room("bedroom");
        Room room4 = new Room("shed");
        Critter frog = new Critter("frog", "green frog", 5, 10);
        Critter bug = new Critter("bug", "big bug", 5, 10);
        Critter kitten = new Critter("kitty", "white kitty", 5, 10);
        Critter kitten2 = new Critter("kitty", "white kitty", 5, 10);
        Critter kitten3 = new Critter("kitty", "white kitty", 5, 10);

        Critter dead = new Critter("dead bug", "small dead bug", 5, 0);

        Treasure coins = new Treasure("money", 10);
        Treasure notes = new Treasure("notes", 20);
        Treasure cash = new Treasure("cash", 20);
        Treasure cash2 = new Treasure("cash", 20);
        Treasure cash3 = new Treasure("cash", 20);

        room2.enter(dead);
        room3.enter(bug);
        room4.enter(kitten);
        room4.enter(kitten2);
        room4.enter(kitten3);

        room3.enter(coins);
        room4.enter(notes);
        room4.enter(cash);
        room4.enter(cash2);
        room4.enter(cash3);


        if (startingObjects != null) {
            try {
                Room.makeExitPair(room2, startRoom, "North", "South");
                Room.makeExitPair(room2, room3, "East", "West");
                Room.makeExitPair(room4, startRoom, "East", "West");
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns the (x,y) dimensions the map should be based upon the furthest
     * room's coordinate
     *
     * Map origin will always load in centre of canvas, but the origin point
     * may not necessarily be the middle room, thus canvas size must be
     * double the distance of the furthest wall (*) to the origin to ensure all
     * rooms are loaded in all directions
     *
     * (*) Room's coord will be top left corner of room, so if furthest room is
     * East or South of origin, right or bottom side of room will not be within
     * canvas. Thus, buffer of +1 * side length is added to both sides
     *
     * @param coords Map of rooms to room coordinates
     * @return mapSize as Pair(x, y) or (0,0) if no max or mins
     */
    private Pair mapSize(Map<Room, Pair> coords) {
        int xMap = bm.xMax; //initialise to max values
        int yMap = bm.yMax;
        xMap = Math.abs(bm.xMax - bm.xMin);
        yMap = Math.abs(bm.yMax - bm.yMin);

        if (Math.abs(bm.xMin) > (Math.abs(bm.yMax))) {
            xMap = Math.abs(bm.xMin);
        }
        if (Math.abs(bm.yMin) > (Math.abs(bm.yMax))) {
            yMap = Math.abs(bm.yMin);
        }

        return new Pair(2 * (xMap + 1), 2 * (yMap + 1));
    }

    private Canvas startCanvas() {
        //Change the width and height according to how big the loaded map is
        //base this on absolute value of coords
        Pair mapSize = mapSize(bm.coords);
        double x = mapSize.x * side + side;
        double y = mapSize.y * side + side;
        carto = new Cartographer(x, y, side);
        Canvas gameMap = new Canvas(x, y);
        gc = gameMap.getGraphicsContext2D();
        carto.drawMap(gc, bm.coords);
        gc.strokeRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight
                ());
        return gameMap;
    }

    /**
     * Starts the application window using a BorderPane to hold three main
     * GUI elements (map, buttons and text box)
     *
     * @param stage Stage to display GUI on
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Crawl - Explore");

        BorderPane bp = makeGUIElements();
        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Top level method to make three main GUI elements by creating a
     * TextArea and calling individual methods for remaining elements (canvas
     * and buttons)
     *
     * Buttons are assembled using two GridPanes (one for directions, other
     * for functionality), and then put into a Vbox which is added to the
     * BorderPane
     *
     * @return BorderPane that elements are in
     */
    private BorderPane makeGUIElements() {
        //starting Canvas
        Canvas canvas = startCanvas();

        //TextArea
        history = new TextArea();
        history.setPrefRowCount(8);
        history.setEditable(false);
        history.appendText("You find yourself in " +
                roomPlayerIsIn().getDescription() + "\n");

        //Overall layout
        //BorderPane must be initialised before buttons and be passed into
        // button maker methods because their event handler will need to
        // disable
        // these buttons if the player dies

        //Direction Buttons
        GridPane dirnGrid = makeDirectionGrid();

        //Function Buttons
        GridPane funcGrid = makeFunctionGrid();

        //Stack them on top of each other in a vbox
        VBox allButtons = new VBox();
        allButtons.getChildren().addAll(dirnGrid);
        allButtons.getChildren().addAll(funcGrid);

        //Overall Layout
        borderPane = new BorderPane();
        borderPane.setCenter(canvas);
        borderPane.setBottom(history);
        borderPane.setRight(allButtons);
        return borderPane;
    }

    /**
     * Creates function buttons, sets value of action property when clicked
     * and adds them to a GridPane
     *
     * Function buttons are: Look, Examine, Drop, Take, Fight and Save
     *
     * @return GridPane buttons have been added to
     */
    private GridPane makeFunctionGrid() {
        GridPane funcGrid = new GridPane();
        //Make new event handler for all button presses
        EventHandler<ActionEvent> e = new ButtonDoer();


        Button lookButt = new Button("Look");
        lookButt.setOnAction(e);

        Button examineButt = new Button("Examine");
        examineButt.setOnAction(e);

        Button takeButt = new Button("Drop");
        takeButt.setOnAction(e);


        Button dropButt = new Button("Take");
        dropButt.setOnAction(e);

        Button fightButt = new Button("Fight");
        fightButt.setOnAction(e);

        Button saveButt = new Button("Save");
        saveButt.setOnAction(e);


        funcGrid.add(lookButt, 0, 0);
        funcGrid.add(examineButt, 1, 0);
        funcGrid.add(dropButt, 0, 1);
        funcGrid.add(takeButt, 1, 1);
        funcGrid.add(fightButt, 0, 2);
        funcGrid.add(saveButt, 0, 3);
        return funcGrid;
    }

    /**
     * Creates direction buttons, sets value of action property when clicked
     * and adds them to a GridPane
     *
     * Direction buttons are: North, South, East and West
     *
     * @return GridPane buttons have been added to
     */
    private GridPane makeDirectionGrid() {
        GridPane dirnGrid = new GridPane();
        EventHandler<ActionEvent> e = new ButtonDoer();

        Button northButt = new Button("North");
        northButt.setOnAction(e);

        Button westButt = new Button("West");
        westButt.setOnAction(e);

        Button southButt = new Button("South");
        southButt.setOnAction(e);

        Button eastButt = new Button("East");
        eastButt.setOnAction(e);


        dirnGrid.add(northButt, 1, 0);
        dirnGrid.add(westButt, 0, 1);
        dirnGrid.add(eastButt, 2, 1);
        dirnGrid.add(southButt, 1, 2);
        return dirnGrid;
    }

    /**
     * Finds first instance of player in mapped rooms
     *
     * @return Room player is in
     */
    private Room roomPlayerIsIn() {
        for (Room room : bm.coords.keySet()) {
            for (Thing thing : room.getContents()) {
                if (thing instanceof Player) {
                    return room;
                }
            }
        }
        return null;
    }

    /**
     * Finds player object from given room
     *
     * @return Player object or null if not found or null room given
     */
    private Player playerInRoom(Room currentRoom) {
        if (currentRoom == null) {
            return null;
        }
        for (Thing item : currentRoom.getContents()) {
            if (item instanceof Player) {
                return (Player)item;
            }
        }
        return null;
    }

    /**
     * Helper class to manage button functionality
     */
    private class ButtonDoer implements EventHandler<ActionEvent> {

        /**
         * Helper method to handle all button presses, depending upon the
         * source of the button, will call the intended method and redraw the
         * canvas if button affects graphics
         *
         * @param e Event object
         */
        public void handle(ActionEvent e) {
            Button pressedButton = (Button) e.getSource();
            String toDraw = pressedButton.getText();
            Room currentRoom = roomPlayerIsIn();
            Player player = playerInRoom(currentRoom);

            //check if player is null
            if (player != null) {
                switch (toDraw) {
                    case "North":
                    case "South":
                    case "West":
                    case "East":
                        //attempt to move player, same method for all dirctions
                        movePlayer(currentRoom, player, toDraw);
                        break;
                    case "Look":
                        lookAt(currentRoom, player);
                        break;
                    case "Examine":
                        examine(currentRoom, player);
                        break;
                    case "Fight":
                        fightThings(currentRoom, player);
                        carto.updateMap(gc, bm.coords);
                        break;
                    case "Save":
                    case "Take":
                        takeItem(currentRoom, player);
                        carto.updateMap(gc, bm.coords);
                        break;
                    case "Drop":
                        dropItem(currentRoom, player);
                        carto.updateMap(gc, bm.coords);
                        break;
                }
            }
        }
    }

    /**
     * Looks through player's inventory to find short description of item to
     * remove and add to current room
     *
     * Does not update map
     *
     * @param currentRoom Room to add item into
     * @param player Player whose inventory item is removed from
     */
    private void dropItem(Room currentRoom, Player player) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Item to drop?");
        Optional<String> result = dialog.showAndWait();

        String entered;
        //if user has entered something
        if (result.isPresent() && !result.get().isEmpty()) {
            //get result
            entered = result.get();
            /* player.drop will return object dropped or null if item could not
             be found */
            Thing tryToDropItem = player.drop(entered);
            if (tryToDropItem != null) {
                currentRoom.enter(tryToDropItem);
            } else {
                history.appendText("Nothing found with that name\n");
            }
        }
    }

    /**
     * Attempts to remove an item from a room and add it into the player's
     * inventory
     *
     * Will fail silently if attempting to pick up a live Mob and if .leave()
     * returns false - if something in room wishes to fight item
     * @param currentRoom
     * @param player
     */
    private void takeItem(Room currentRoom, Player player) {
        TextInputDialog dialog = new TextInputDialog();
        Optional<String> result = dialog.showAndWait();

        String entered;

        dialog.setTitle("Take what?");

        if (result.isPresent() && !result.get().isEmpty()) {
            entered = result.get();
            for (Thing item : currentRoom.getContents()) {
                //if item meets the description, is not a player and can
                // leave the room i.e. nothing wants to fight it
                if (item.getShort().equals(entered) && !(item instanceof
                        Player)) {
                    //if instance of mob
                    if (item instanceof Mob) {
                        Mob mobItem = (Mob) item;
                        if (!mobItem.isAlive()) {
                            //will actually remove item from room
                            //If possible to remove from room, add to invent
                            if (currentRoom.leave(item)) {
                                player.add(item);
                                //stop loop once found
                                break;
                            }
                        }
                    //if not instance of mob
                    } else {
                        if (currentRoom.leave(item)){
                            player.add(item);
                            //stop loop once found
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the long description of the first occurring item whose short
     * description is entered by user in dialog box
     *
     * Checks player's inventory first, then the contents of the given room
     *
     * Displays "Nothing found with that name" if no item in inventory or
     * room is found to match given short description
     * @param currentRoom Room to find item in
     * @param player Player whose inventory is to be searched
     */
    private void examine(Room currentRoom, Player player) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Examine what?");
        Optional<String> result = dialog.showAndWait();

        String entered;
        boolean found = false;

        //if user has entered something
        if (result.isPresent() && !result.get().isEmpty()) {
            entered = result.get();
            for (Thing item : player.getContents()) {
                if (item.getShort().equals(entered)) {
                    history.appendText(item.getLong() + "\n");
                    found = true;
                    break;
                }
            }
            if (found == false) {
                for (Thing item : currentRoom.getContents()) {
                    if (item.getShort().equals(entered)) {
                        history.appendText(item.getLong() + "\n");
                        found = true;
                        break;
                    }
                }
            }
            if (found == false) {
                history.appendText("Nothing found with that name\n");
            }
        }
    }

    /**
     * Called when Look button is pressed and will display the following
     * information on the game GUI:
     *  Short description of room
     *  Items player is carrying
     *  Total worth of items carried
     *
     * @param currentRoom Room whose contents are to be looked at
     * @param player Player whose inventory is to be looked at
     */
    private void lookAt(Room currentRoom, Player player) {
        double totalWorth = 0;

        history.appendText(currentRoom.getDescription() + " - you see:\n");
        for (Thing item : currentRoom.getContents()) {
            history.appendText(" " + item.getShort() +"\n");
        }
        history.appendText("You are carrying:");
        for (Thing item : player.getContents()) {
            history.appendText("\n " + item.getShortDescription());
            if (item instanceof Treasure) {
                Treasure treas = (Treasure) item;
                totalWorth = totalWorth + treas.getValue();
            } else if (item instanceof Critter) {
                Critter crit = (Critter) item;
                totalWorth = totalWorth + crit.getValue();
            }
        }

        history.appendText("\nworth " + totalWorth + " in total\n");
    }

    /**
     *
     *
     * @param currentRoom
     * @param player
     */
    private void fightThings(Room currentRoom, Player player) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Fight what?");
        Optional<String> result = dialog.showAndWait();
        String entered;
        //if user has entered something
        if (result.isPresent() && !result.get().isEmpty()) {
            //get result
            entered = result.get();
            for (Thing fighter : currentRoom.getContents()) {
                if (fighter.getShort().equals(entered)){
                    if (fighter instanceof Critter) {
                        Critter crit = (Critter) fighter;
                        if (crit.isAlive()) {
                            player.fight(crit);
                            //if player dies
                            if (!player.isAlive()) {
                                endGame();
                                break;// to only fight one match of descript
                            } else {
                                history.appendText("You won\n");
                                break; //to only fight one critter
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Disables buttons after game is over
     */
    private void endGame() {
        history.appendText("Game over\n");
        borderPane.getRight().setDisable(true);
    }


    /**
     * Attempts to move the player across the map while checking if there is
     * an exit existing or if the player is prevented from leaving
     *
     * Called when any directional button is pressed - North, South East and
     * West
     *
     * @param currentRoom Current room player is in
     * @param player Player object to move
     * @param exitDirection Direction player wishes to move
     */
    private void movePlayer(Room currentRoom, Player player,
                          String exitDirection) {
        //check if exit exists

        if (currentRoom.getExits().containsKey(exitDirection)) {
            //find room to try to move into
            Room nextRoom = currentRoom.getExits().get(exitDirection);
            //try to leave
            if (currentRoom.leave(player)) {
                currentRoom.leave(player);
                nextRoom.enter(player);
                history.appendText("You enter " + nextRoom.getDescription() + "\n");
            } else {
                history.appendText("Something prevents you from leaving\n");
            }

            carto.updateMap(gc, bm.coords);
        } else {
            history.appendText("No door that way\n");
        }
    }
}
