import com.sun.corba.se.impl.orbutil.graph.GraphImpl;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.Map;
import java.util.List;

public class Cartographer extends Canvas {
    private double width = 0;
    private double height = 0;
    int side;

    public Cartographer(double width, double height, int side) {
        super(width, height);
        this.width = width;
        this.height = height;
        this.side = side;

    }

    public void drawMap(GraphicsContext gc, Map<Room, Pair> coords) {
        for (Map.Entry<Room, Pair> entry: coords.entrySet()) {
            Pair pair = entry.getValue();
            Room drawRoom = entry.getKey();
            drawNorth(gc, drawRoom, pair.x, pair.y);
            drawSouth(gc, drawRoom, pair.x, pair.y);
            drawEast(gc, drawRoom, pair.x, pair.y);
            drawWest(gc, drawRoom, pair.x, pair.y);

            fillRoom(gc, drawRoom, pair.x, pair.y);
        }

    }

    private void fillRoom(GraphicsContext gc, Room drawRoom, int x, int y) {
        List<Thing> contentsToDraw = drawRoom.getContents();
        double leftX = x * side + side * 0.05 + width/2;
        double topY = y * side + side * 0.3 + height/2;
        double rightX = x * side + side * 0.55 + width/2;
        double bottomY = y * side + side * 0.7 + height/2;

        /* TEST ALL SPACES
        gc.fillText("A", leftX, topY);
        gc.fillText("B", leftX, bottomY);
        gc.fillText("C", rightX, topY);
        gc.fillText("D", rightX, bottomY);*/


        for (Thing thing: contentsToDraw) {
            if (thing instanceof Player) {
                //draw @
                gc.fillText("@", leftX, topY);
            } else if (thing instanceof Treasure) {
                //draw $
                gc.fillText("$", rightX, topY);
            } else if (thing instanceof Critter) {
                Critter crit = (Critter) thing; //might not need this
                if (crit.isAlive()) {
                    //draw M
                    gc.fillText("M", leftX, bottomY);
                } else {
                    //draw m
                    gc.fillText("m", rightX, bottomY);
                }
            }
        }
    }

    private void drawNorth(GraphicsContext gc, Room drawRoom,
                           double x, double y) {
        double startX = x * side + width/2;
        double startY = y * side + height/2;
        //Drawing North Walls
        if (drawRoom.getExits().containsKey("North")) {
            // draw horizontal (left to right) half way
            gc.strokeLine(startX,startY,startX + side/2, startY);
            //draw perpendicular dash, an eighth length of side length
            gc.strokeLine(startX + side/2,startY + side/16,
                    startX + side/2, startY -side/16);
            //draw remaining horizontal (left to right)
            gc.strokeLine(startX + side/2,startY,startX + side, startY);
        } else {
            //draw entire line
            gc.strokeLine(startX,startY,startX + side, startY);
        }
    }

    private void drawSouth(GraphicsContext gc, Room drawRoom, double x, double
            y) {
        double startX = x * side + width/2;
        double startY = y * side + side + height/2; //y increases down

        //Drawing South Walls
        if (drawRoom.getExits().containsKey("South")) {
            // draw horizontal (left to right) half way
            gc.strokeLine(startX, startY,startX + side/2, startY);

            //draw perpendicular dash, an eighth length of side length
            gc.strokeLine(startX + side/2,startY + side/16,
                    startX + side/2, startY - side/16);

            //draw remaining horizontal (left to right)
            gc.strokeLine(startX + side/2, startY,startX + side, startY);
        } else {
            //draw entire line
            gc.strokeLine(startX, startY, startX + side, startY );
        }
    }

    private void drawWest(GraphicsContext gc, Room drawRoom, double x, double
            y) {
        double startX = x * side + width/2;
        double startY = y * side + height/2;

        if (drawRoom.getExits(). containsKey("West")) {
            gc.strokeLine(startX, startY, startX, startY + side/2);
            gc.strokeLine(startX - side/16, startY + side/2, startX +
                    side/16, startY + side/2);
            gc.strokeLine(startX, startY + side/2, startX, startY + side);
        } else {
            gc.strokeLine(startX, startY, startX, startY + side);
        }
    }

    private void drawEast(GraphicsContext gc, Room drawRoom, double x, double
            y) {
        double startX = x * side + width/2 + side; //one side unit away from top
        // left corner
        double startY = y * side + height/2;

        if (drawRoom.getExits().containsKey("East")) {
            gc.strokeLine(startX, startY, startX, startY + side/2);
            gc.strokeLine(startX - side/16, startY + side/2, startX +
                    side/16, startY + side/2);
            gc.strokeLine(startX, startY + side/2, startX, startY + side);
        }
        gc.strokeLine(startX, startY, startX, startY + side);


    }

    public void updateMap(GraphicsContext gc, Map<Room, Pair> coords) {
        gc.clearRect(0, 0, width, height);
        drawMap(gc, coords);
    }
}
