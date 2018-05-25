import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import java.util.Map;
import java.lang.Math;

/**
 *  Launches GUI, expects a filename upon launch
 *
 *  Loads three main areas of GUI
 */

public class CrawlGui extends Application {
    private int side = 38;

    private static BoundsMapper bm;
    private static Room startRoom;
    private Pair mapSize;
    private GraphicsContext gc;
    private Cartographer carto;
    private TextArea history;

    /**
     * Expects filename to load map, launches program
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Object[] startingObjects;
       /* if (args.length < 1) {
            System.err.println("Usage: java CrawlGui mapname\n");
            System.exit(1);
        } else {
            try {
                startingObjects = MapIO.loadMap(args[0]);
            } catch (Exception e) {
                System.err.println("Unable to load file " + args[0] + "\n");
                System.exit(2);
            }
        }

        startingObjects = MapIO.loadMap(args[0]);*/

        startingObjects = MapIO.loadMap("demo.map");

        addMoreToRooms(startingObjects);
        if (startingObjects != null) {
            startRoom = (Room) startingObjects[1];
            Player player = (Player) startingObjects[0];

            bm = new BoundsMapper(startRoom);
            bm.walk();
            startRoom.enter(player);
        } else {
            System.out.println("Starting objects null, failed to load");
        }
        launch(args);
    }

    /**
     * Fills the map with more rooms and things
     *
     * @param startingObjects Object array of Player and starting Room
     * @return true if successful
     */
    private static void addMoreToRooms(Object[] startingObjects) {
        //ADDING MORE TESTS TO TEST MAPPING
        Room room2 = new Room("garden");
        Room room3 = new Room("bedroom");
        Room room4 = new Room("shed");
        Critter frog = new Critter("frog", "green frog", 5, 10);
        Critter bug = new Critter("bug", "big bug", 5, 10);
        Critter kitten = new Critter("kitty", "white kitty", 5, 10);

        Treasure coins = new Treasure("money", 10);
        Treasure notes = new Treasure("notes", 20);
        Treasure cash = new Treasure("cash", 20);


        room2.enter(frog);
        room3.enter(bug);
        room4.enter(kitten);
        room3.enter(coins);
        room4.enter(notes);
        room4.enter(cash);

        if (startingObjects != null) {
            startRoom = (Room) startingObjects[1];
            Player player = (Player) startingObjects[0];

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
     * *
     * Map origin will always load in centre of canvas, origin may not
     * necessarily be the middle room, thus canvas size must be double
     * furthest room to origin to ensure all rooms are loaded in all directions
     * <p>
     * Room coord will be left corner of room, so if furthest room is East
     * of origin, right side of room will not be within canvas. Thus, buffer
     * of +1 is added to both sides
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

     /*   System.out.print(bm.xMax);
        System.out.println(bm.xMin);
        System.out.print(bm.yMax);
        System.out.println(bm.yMin);
        System.out.print(xMap);
        System.out.println(yMap);*/
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

    // Called to start doing application stuff
    @Override
    public void start(Stage stage) {
        stage.setTitle("Crawl - Explore");

        BorderPane bp = makeGUIElements();
        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.show();

    }

    private BorderPane makeGUIElements() {
        //starting Canvas
        Canvas canvas = startCanvas();

        //TextArea
        history = new TextArea();
        history.setPrefRowCount(8);
        history.setEditable(false);
        history.appendText("You find yourself in " +
                roomPlayerIsIn().getDescription() + "\n");

        //Direction Buttons
        GridPane dirnGrid = makeDirectionGrid();

        //Function Buttons
        GridPane funcGrid = makeFunctionGrid();

        //Stack them on top of each other in a vbox
        VBox allButtons = new VBox();
        allButtons.getChildren().addAll(dirnGrid);
        allButtons.getChildren().addAll(funcGrid);

        //Overall Layout
        BorderPane bp = new BorderPane();
        bp.setCenter(canvas);
        bp.setBottom(history);
        bp.setRight(allButtons);
        return bp;
    }

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

    private class ButtonDoer implements EventHandler<ActionEvent> {

        public void handle(ActionEvent e) {
            Button pressedButton = (Button) e.getSource();
            String toDraw = pressedButton.getText();
            Room currentRoom = roomPlayerIsIn();
            Player player = playerInRoom(currentRoom);

            //check if player is null
            if (player != null) {
                //attempt to move player
                movePlayer(currentRoom, player, toDraw);
            }
        }
    }

    /**
     * Atte,pts to move the player across the map while checking if there is
     * an exit existing or if the player is prevented from leaving
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
            System.out.println("        into " + nextRoom.getDescription());
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
