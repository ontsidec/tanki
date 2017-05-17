import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Objects;

class detectedRobot {
    // TODO poprawić porównywanie stringów
    // TODO poprawić rysowanie
    // TODO mapa + sterowanie po mapie
    private final double _positionX;
    private final double _positionY;
    private final String _robotID;

    public detectedRobot(double positionX, double positionY, String robotID) {
        this._positionX = positionX;
        this._positionY = positionY;
        this._robotID = robotID;
        System.out.println("Dodałem robota!");
    }

    public double getPositionX() {
        return _positionX;
    }

    public double getPositionY() {
        return _positionY;
    }

    public String getRobotID() {
        return _robotID;
    }
}

public class moloch extends AdvancedRobot {
    private Point2D robotDestination;
    private Point2D robotPosition;
    private Point2D startPosition;
    private boolean manual_movement = false;
    private static final double MAX_VELOCITY = 8;
    private static final double WALL_MARGIN = 25;

    private int scannedX = Integer.MIN_VALUE;
    private int scannedY = Integer.MIN_VALUE;
    private ArrayList<detectedRobot> robotList;
    private boolean[][] drawMap;

    public moloch() {
        this.robotList = new ArrayList<>();

    }

    /*********************************** MAIN PROGRAM ******************************************/
    public void run() {
        this.drawMap = new boolean[(int) (getBattleFieldWidth() / getWidth()) + 2][(int) (getBattleFieldHeight() / getHeight()) + 2];
        startPosition = new Point2D.Double(getX(), getY());

        while (true) {
            robotPosition = new Point2D.Double(getX(), getY()); // wolniejsze odświeżanie pozycji
            turnRadarRight(360);
            if (manual_movement) {
                manualMovement();
            }
        }
    }

    /*********************************** MOVEMENT ******************************************/
    private void manualMovement() {
        // Ruch manualny po przez wskazanie punktu docelowego
        checkDestination();
        goTo(robotDestination);
        if (robotPosition.equals(robotDestination)) {
            manual_movement = false;
            setMaxVelocity(0);
        }
    }

    private void goTo(Point2D destination) {
        // Jazda do punktu docelowego
        double angle = Utils.normalRelativeAngle(absoluteBearing(robotPosition, destination) - getHeadingRadians());
        double turnAngle = Math.atan(Math.tan(angle));
        setTurnRightRadians(turnAngle);
        setAhead(robotPosition.distance(destination) * (angle == turnAngle ? 1 : -1));
        setMaxVelocity(Math.abs(getTurnRemaining()) > 33 ? 0 : MAX_VELOCITY);
    }

    private static double absoluteBearing(Point2D source, Point2D target) {
        // Zwraca kąt obrotu
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    private void checkDestination() {
        // Sprawdzamy czy punkt docelowy nie znajduje się po za dostępnym polem bitwy
        if (!fieldRectangle(WALL_MARGIN).contains(robotDestination)) {
            double x = robotDestination.getX();
            double y = robotDestination.getY();
            System.out.println("Sprawdzam");
            if (x < WALL_MARGIN) {
                x = WALL_MARGIN;
                System.out.println("1 warunek");
            }
            if (x > getBattleFieldWidth() - WALL_MARGIN) {
                x = getBattleFieldWidth() - WALL_MARGIN;
                System.out.println("2 warunek");
            }
            if (y < WALL_MARGIN) {
                y = WALL_MARGIN;
                System.out.println("3 warunek");
            }
            if (y > getBattleFieldHeight() - WALL_MARGIN) {
                y = getBattleFieldHeight() - WALL_MARGIN;
                System.out.println("4 warunek");
            }
            robotDestination = new Point2D.Double(x, y);
        }
    }

    private RoundRectangle2D fieldRectangle(double margin) {
        // Obszar bitwy pomniejszony o marginesy
        return new RoundRectangle2D.Double(margin, margin,
                getBattleFieldWidth() - margin * 2, getBattleFieldHeight() - margin * 2, 75, 75);
    }

    /*********************************** EVENTS ******************************************/
    public void onScannedRobot(ScannedRobotEvent e) {
        // TODO Skanowanie przeszkód
        // Oblicz kąt wykrytego robota
        double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);

        // Oblicz pozycję wykrytego robota
        scannedX = (int) (getX() + Math.sin(angle) * e.getDistance());
        scannedY = (int) (getY() + Math.cos(angle) * e.getDistance());

        boolean isAdded = false;
        for (detectedRobot i : robotList) {
            if (Objects.equals(i.getRobotID(), e.getName())) {
                isAdded = true;
            }
        }

        if (!isAdded) {
            robotList.add(new detectedRobot(scannedX, scannedY, e.getName()));

            int i = scannedX / (int) getWidth();
            int j = scannedY / (int) getHeight();
            drawMap[i][j] = true;
        }
    }

    public void onMouseClicked(MouseEvent e) {
        // Kiedy mysz kliknięta wskaż punkt docelowy
        robotDestination = e.getPoint();
        manual_movement = true;
    }

    /*********************************** PAINTING ******************************************/
    public void onPaint(Graphics2D g) {
        // Odświeżenie pozycji robota
        // W tym miejscu, bo metoda onPaint kiedy jest włączona jest odświeżana najczęściej
        // Co daje najbardziej aktualną pozycję
        robotPosition = new Point2D.Double(getX(), getY());

        // Wyświetlanie różnych zmiennych
        g.setColor(Color.WHITE);
        g.drawString("Robot X: " + getX(), 10, 20);
        g.drawString("Robot Y: " + getY(), 10, 10);

        // Siatka
        int temp_x = (int) (startPosition.getX() - (getWidth() / 2));
        int temp_y = (int) (startPosition.getY() - (getHeight() / 2));
        while (temp_x > 0) {
            temp_x -= (int) getWidth();
        }
        while (temp_y > 0) {
            temp_y -= (int) getHeight();
        }

        for (int x = temp_x; x < (int) getBattleFieldWidth(); x += (int) getWidth()) {
            for (int y = temp_y; y < (int) getBattleFieldHeight(); y += (int) getHeight()) {
                g.drawLine(x, y, x , y + (int) getHeight());
                g.drawLine(x, y, x + (int) getWidth(), y);
            }
        }

        // Oznaczenie ostatnio wykrytego przeciwnika
        g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
        g.drawLine(scannedX, scannedY, (int) getX(), (int) getY());

        for (int i = 0; i < robotList.size(); i++) {
            string =
        }
        for (int i = 0; i < drawMap.length; i++) {
            for (int j = 0; j < drawMap[i].length; j++) {
                if (drawMap[i][j])
                    g.fillRect((int) getHeight() * i, (int) getWidth() * j, 40, 40);
            }
        }
    }
}