import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.util.Map;
import java.util.List;

/**
 * Cartographer handles the GUI part of drawing the rooms
 */
public class Cartographer extends Canvas {
    private double width;
    private double height;

    //Length of one side unit
    int side;

    /**
     *
     * @param width Width of canvas
     * @param height Height of canvas
     * @param side Length of side of rooms
     */
    public Cartographer(double width, double height, int side) {
        super(width, height);
        this.width = width;
        this.height = height;
        this.side = side;
    }

    /**
     * Top level method called to draw map by using individual direction methods
     *
     * @param gContext Graphics context to draw map on
     * @param coords Map of all rooms to draw and their location on map
     */
    public void drawMap(GraphicsContext gContext, Map<Room, Pair> coords) {
        for (Map.Entry<Room, Pair> entry: coords.entrySet()) {
            Pair pair = entry.getValue();
            Room drawRoom = entry.getKey();
            drawNorth(gContext, drawRoom, pair.x, pair.y);
            drawSouth(gContext, drawRoom, pair.x, pair.y);
            drawEast(gContext, drawRoom, pair.x, pair.y);
            drawWest(gContext, drawRoom, pair.x, pair.y);

            fillRoom(gContext, drawRoom, pair.x, pair.y);
        }

    }

    /**
     * Fills the rooms of all types of GUI contents - players, treasures and
     * alive and dead critters
     *
     * @param gContext Graphics context to draw items on
     * @param drawRoom Room to draw symbols in
     * @param xCorner x value of top left corner of room on map
     * @param yCorner y value of top left corner of room on map
     */
    private void fillRoom(GraphicsContext gContext, Room drawRoom, int xCorner,
                          int yCorner) {
        List<Thing> contentsToDraw = drawRoom.getContents();

        //Segmenting each room into quadrants - finding best coordinate
        // within each segment to display relevant character
        double leftX = xCorner * side + side * 0.05 + width/2;
        double topY = yCorner * side + side * 0.3 + height/2;
        double rightX = xCorner * side + side * 0.5 + width/2;
        double bottomY = yCorner * side + side * 0.7 + height/2;

        for (Thing thing: contentsToDraw) {
            if (thing instanceof Player) {
                //draw @
                gContext.fillText("@", leftX, topY);
            } else if (thing instanceof Treasure) {
                //draw $
                gContext.fillText("$", rightX, topY);
            } else if (thing instanceof Critter) {
                Critter crit = (Critter) thing;
                if (crit.isAlive()) {
                    //draw M
                    gContext.fillText("M", leftX, bottomY);
                } else {
                    //draw m
                    gContext.fillText("m", rightX, bottomY);
                }
            }
        }
    }

    /**
     * Draws the north walls of every room on the map, finds whether the
     * rooms have a "North" exit, if so, draws a perpendicular dash
     *
     * @param gContext Graphics contexts to draw map on
     * @param drawRoom Room whose North wall is to be drawn
     * @param xCorner x value of top left corner of given room
     * @param yCorner y value of top left corner of given room
     */
    private void drawNorth(GraphicsContext gContext, Room drawRoom,
                           double xCorner, double yCorner) {
        //Drawing North walls from top left corner - the scaled coord of Room
        //Other directions will be based upon this
        double startX = xCorner * side + width/2;
        double startY = yCorner * side + height/2;

        if (drawRoom.getExits().containsKey("North")) {
            // draw horizontal (left to right) half way
            gContext.strokeLine(startX,startY,startX + side/2, startY);
            //draw perpendicular dash, an eighth length of side length
            gContext.strokeLine(startX + side/2,startY + side/16,
                    startX + side/2, startY -side/16);
            //draw remaining horizontal (left to right)
            gContext.strokeLine(startX + side/2,startY,startX + side, startY);
        } else {
            //draw entire line
            gContext.strokeLine(startX,startY,startX + side, startY);
        }
    }

    /**
     * Draws the south walls of every room on the map, finds whether the
     * rooms have a "South" exit, if so, draws a perpendicular dash
     *
     * @param gContext Graphics contexts to draw map on
     * @param drawRoom Room whose North wall is to be drawn
     * @param xCorner x value of top left corner of given room
     * @param yCorner y value of top left corner of given room
     */
    private void drawSouth(GraphicsContext gContext, Room drawRoom, double
            xCorner, double yCorner) {

        //Adjusting start values to the southern wall (i.e. one side unit down
        // from North)
        double startX = xCorner * side + width/2;
        double startY = yCorner * side + side + height/2; //y increases down

        //Drawing South Walls
        if (drawRoom.getExits().containsKey("South")) {
            // draw horizontal (left to right) half way
            gContext.strokeLine(startX, startY,startX + side/2, startY);

            //draw perpendicular dash, an eighth length of side length
            gContext.strokeLine(startX + side/2,startY + side/16,
                    startX + side/2, startY - side/16);

            //draw remaining horizontal (left to right)
            gContext.strokeLine(startX + side/2, startY,startX + side,
                    startY);
        } else {
            //draw entire line
            gContext.strokeLine(startX, startY, startX + side, startY );
        }
    }

    /**
     * Draws the West walls of every room on the map, finds whether the
     * rooms have a "West" exit, if so, draws a perpendicular dash
     *
     * @param gContext Graphics contexts to draw map on
     * @param drawRoom Room whose North wall is to be drawn
     * @param xCorner x value of top left corner of given room
     * @param yCorner y value of top left corner of given room
     */
    private void drawWest(GraphicsContext gContext, Room drawRoom,
                          double xCorner, double yCorner) {

        //Adjusting start values to Western wall (same top left corner as North)
        double startX = xCorner * side + width/2;
        double startY = yCorner * side + height/2;

        if (drawRoom.getExits(). containsKey("West")) {
            gContext.strokeLine(startX, startY, startX, startY + side/2);
            gContext.strokeLine(startX - side/16, startY + side/2, startX +
                    side/16, startY + side/2);
            gContext.strokeLine(startX, startY + side/2, startX, startY +
                    side);
        } else {
            gContext.strokeLine(startX, startY, startX, startY + side);
        }
    }

    /**
     * Draws the east walls of every room on the map, finds whether the
     * rooms have a "East" exit, if so, draws a perpendicular dash
     *
     * @param gContext Graphics contexts to draw map on
     * @param drawRoom Room whose North wall is to be drawn
     * @param xCorner x value of top left corner of given room
     * @param yCorner y value of top left corner of given room
     */
    private void drawEast(GraphicsContext gContext, Room drawRoom,
                          double xCorner, double yCorner) {
        //Adjusting starting coordinates to Eastern wall - one side unit
        // right of the starting point for North
        double startX = xCorner * side + width/2 + side;
        double startY = yCorner * side + height/2;

        if (drawRoom.getExits().containsKey("East")) {
            gContext.strokeLine(startX, startY, startX, startY + side/2);
            gContext.strokeLine(startX - side/16, startY + side/2, startX +
                    side/16, startY + side/2);
            gContext.strokeLine(startX, startY + side/2, startX, startY +
                    side);
        }
        gContext.strokeLine(startX, startY, startX, startY + side);


    }

    /**
     * Clears and redraws the map
     *
     * @param gContext Graphics context map is drawn on
     * @param coords Map of Rooms to draw and their location on map as a Pair
     */
    public void updateMap(GraphicsContext gContext, Map<Room, Pair> coords) {
        gContext.clearRect(0, 0, width, height);
        drawMap(gContext, coords);
    }
}
