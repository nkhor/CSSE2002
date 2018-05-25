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
    Canvas gameMap;
    GraphicsContext gc;
    final int SIDE = 38;

    public Cartographer(double width, double height) {
        super(width, height);
        this.width = width;
        this.height = height;

    }

    public void drawMap(GraphicsContext gc, Map<Room, Pair> coords) {
       // gc.setStroke(Color.BLUE);
       // gc.setLineWidth(5);
      //  gc.strokeLine(0, 10, 60, 40);
        for (Map.Entry<Room, Pair> entry: coords.entrySet()) {
            Pair pair = entry.getValue();
            Room drawRoom = entry.getKey();
       //     gc.strokeOval(pair.x * SIDE + CENTRE, pair.y * SIDE + CENTRE,
            // 5, 5);
            System.out.println("Pair: " + pair.x + " , " + pair.y);
            System.out.println("Room: " + drawRoom);
            drawNorth(gc, drawRoom, pair.x, pair.y);
            drawSouth(gc, drawRoom, pair.x, pair.y);
            drawEast(gc, drawRoom, pair.x, pair.y);
            drawWest(gc, drawRoom, pair.x, pair.y);

            fillRoom(gc, drawRoom, pair.x, pair.y);
        }

    }

    private void fillRoom(GraphicsContext gc, Room drawRoom, int x, int y) {
        List<Thing> contentsToDraw = drawRoom.getContents();
        System.out.println(contentsToDraw);
        double leftX = x * SIDE + SIDE * 0.05 + width/2;
        double topY = y * SIDE + SIDE * 0.3 + height/2;
        double rightX = x * SIDE + SIDE * 0.55 + width/2;
        double bottomY = y * SIDE + SIDE * 0.7 + height/2;

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
        double startX = x * SIDE + width/2;
        double startY = y * SIDE + height/2;
        //Drawing North Walls
        if (drawRoom.getExits().containsKey("North")) {
            // draw horizontal (left to right) half way
            gc.strokeLine(startX,startY,startX + SIDE/2, startY);
            //draw perpendicular dash, an eighth length of side length
            gc.strokeLine(startX + SIDE/2,startY + SIDE/16,
                    startX + SIDE/2, startY -SIDE/16);
            //draw remaining horizontal (left to right)
            gc.strokeLine(startX + SIDE/2,startY,startX + SIDE, startY);
        } else {
            //draw entire line
            gc.strokeLine(startX,startY,startX + SIDE, startY);
        }
    }

    private void drawSouth(GraphicsContext gc, Room drawRoom, double x, double
            y) {
        double startX = x * SIDE + width/2;
        double startY = y * SIDE + SIDE + height/2; //y increases down

        //Drawing South Walls
        if (drawRoom.getExits().containsKey("South")) {
            // draw horizontal (left to right) half way
            gc.strokeLine(startX, startY,startX + SIDE/2, startY);

            //draw perpendicular dash, an eighth length of side length
            gc.strokeLine(startX + SIDE/2,startY + SIDE/16,
                    startX + SIDE/2, startY - SIDE/16);

            //draw remaining horizontal (left to right)
            gc.strokeLine(startX + SIDE/2, startY,startX + SIDE, startY);
        } else {
            //draw entire line
            gc.strokeLine(startX, startY, startX + SIDE, startY );
        }
    }

    private void drawWest(GraphicsContext gc, Room drawRoom, double x, double
            y) {
        double startX = x * SIDE + width/2;
        double startY = y * SIDE + height/2;

        if (drawRoom.getExits(). containsKey("West")) {
            gc.strokeLine(startX, startY, startX, startY + SIDE/2);
            gc.strokeLine(startX - SIDE/16, startY + SIDE/2, startX +
                    SIDE/16, startY + SIDE/2);
            gc.strokeLine(startX, startY + SIDE/2, startX, startY + SIDE);
        } else {
            gc.strokeLine(startX, startY, startX, startY + SIDE);
        }
    }

    private void drawEast(GraphicsContext gc, Room drawRoom, double x, double
            y) {
      //  gc.setStroke(Color.BLUE);

        double startX = x * SIDE + width/2 + SIDE; //one side unit away from top
        // left corner
        double startY = y * SIDE + height/2;

        if (drawRoom.getExits().containsKey("East")) {
            gc.strokeLine(startX, startY, startX, startY + SIDE/2);
            gc.strokeLine(startX - SIDE/16, startY + SIDE/2, startX +
                    SIDE/16, startY + SIDE/2);
            gc.strokeLine(startX, startY + SIDE/2, startX, startY + SIDE);
        }
        gc.strokeLine(startX, startY, startX, startY + SIDE);


    }

        public void resetter(GraphicsContext gc) {
            gc.clearRect(0, 0, 300, 200);
    }
}
