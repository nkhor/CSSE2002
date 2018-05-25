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


public class CrawlGui extends Application {
    //OVERWRITE WITH CANVAS IMPLEMENTATION

    private static BoundsMapper bm;
    private static Room startRoom;
    private Pair mapSize;
    final int SIDE = 38;
    private GraphicsContext gc;

    /**
     * Expects filename to load map, launches program
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Object[] startingObjects;
        if (args.length<1) {
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

        startingObjects = MapIO.loadMap(args[0]);

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
        Room room2 = new Room("my first room");
        Room room3 = new Room("my second room");
        Room room4 = new Room("my third room");
        Critter frog = new Critter("frog", "green frog", 5, 10);
        Critter bug = new Critter("bug", "big bug", 5, 10);
        Critter kitten = new Critter("kitty", "white kitty", 5, 10);

        Treasure coins = new Treasure("money", 10);
        Treasure notes = new Treasure("notes", 20);
        Treasure cash = new Treasure("cash", 20);


        room2.enter(frog); room3.enter(bug); room4.enter(kitten);
        room3.enter(coins); room4.enter(notes); room4.enter(cash);

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
     **
     * Map origin will always load in centre of canvas, origin may not
     * necessarily be the middle room, thus canvas size must be double
     * furthest room to origin to ensure all rooms are loaded in all directions
     *
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

       if (Math.abs(bm.xMin) > (Math.abs(bm.yMax)))  {
           xMap = Math.abs(bm.xMin);
       }
       if (Math.abs(bm.yMin) > (Math.abs(bm.yMax))) {
           yMap = Math.abs(bm.yMin);
       }

        System.out.print(bm.xMax); System.out.println(bm.xMin);
        System.out.print(bm.yMax); System.out.println(bm.yMin);
        System.out.print(xMap); System.out.println(yMap);
        return new Pair(2 * (xMap + 1), 2 * (yMap + 1));
    }

    private Canvas startCanvas() {
        //Change the width and height according to how big the loaded map is
        //base this on absolute value of coords
        Pair mapSize = mapSize(bm.coords);
        double x = mapSize.x * SIDE + SIDE;
        double y = mapSize.y * SIDE + SIDE;
        Cartographer carto = new Cartographer(x, y);
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
        TextArea history = new TextArea();
        history.setPrefRowCount(8);
        history.setEditable(false);
        history.appendText("You find yourself in " +
                findPlayer().getDescription() + "\n");

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
        Button lookButt = new Button("Look");
        Button examineButt = new Button("Examine");
        Button takeButt = new Button("Drop");
        Button dropButt = new Button("Take");
        Button fightButt = new Button("Fight");
        Button saveButt = new Button("Save");

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

        Button northButt = new Button("North");
        Button westButt = new Button("West");
        Button southButt = new Button("South");
        Button eastButt = new Button("East");

        dirnGrid.add(northButt, 1, 0);
        dirnGrid.add(westButt, 0, 1);
        dirnGrid.add(eastButt, 2, 1);
        dirnGrid.add(southButt, 1, 2);
        return dirnGrid;
    }

    /**
     * Finds first instance of player in mapped rooms
     * @return Room player is in
     */
    private Room findPlayer() {
        for (Room room : bm.coords.keySet()) {
            for (Thing thing : room.getContents()) {
                if (thing instanceof Player) {
                    return room;
                }
            }
        }
        return null;
    }
}
