import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.*;

public class moloch extends AdvancedRobot {
    private Point2D robotDestination;
    private Point2D robotPosition;
    private boolean manual_movement = false;
    private static final double MAX_VELOCITY = 8;
    private static final double WALL_MARGIN = 25;


    private int scannedX = Integer.MIN_VALUE;
    private int scannedY = Integer.MIN_VALUE;

    /*********************************** MAIN PROGRAM ******************************************/
    public void run() {
        addCustomEvent(new Condition("too_close_to_walls") {
            public boolean test() {
                return ((getX() < WALL_MARGIN || getX() > getBattleFieldWidth() - WALL_MARGIN
                        || getY() < WALL_MARGIN || getY() > getBattleFieldHeight() - WALL_MARGIN));
            }
        });

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
    }

    public void onMouseClicked(MouseEvent e) {
        // Kiedy mysz kliknięta wskaż punkt docelowy
        robotDestination = e.getPoint();
        manual_movement = true;
    }

    public void onCustomEvent(CustomEvent e) {
        if (e.getCondition().getName().equals("too_close_to_walls")) {
            // Zatrzymaj jeżeli za blisko ściany
            setMaxVelocity(0);
        }
    }

    /*********************************** PAINTING ******************************************/
    public void onPaint(Graphics2D g) {
        // Odświeżenie pozycji robota
        // W tym miejscu, bo metoda onPaint kiedy jest włączona jest odświeżana najczęściej
        // Co daje najbardziej aktualną pozycję
        robotPosition = new Point2D.Double(getX(), getY());

        // Wyświetlanie różnych zmiennych
        String x = "Robot X: " + Double.toString(getX());
        String y = "Robot Y: " + Double.toString(getY());

        g.setColor(Color.WHITE);
        g.drawString(x, 10, 20);
        g.drawString(y, 10, 10);

        // Oznaczenie ostatnio wykrytego przeciwnika
        g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
        g.drawLine(scannedX, scannedY, (int) getX(), (int) getY());
        g.fillRect(scannedX - 20, scannedY - 20, 40, 40);
    }
}