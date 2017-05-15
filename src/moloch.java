import robocode.*;
import java.awt.*;

public class moloch extends AdvancedRobot {
    private int wall_offset = 60;
    private int wall_distance = 0;
    private int kierunek = 1;

    public void run() {
        addCustomEvent(new Condition("wall") {
            public boolean test() {
                return getX() <= wall_offset || getY() <= wall_offset || getX() >= getBattleFieldWidth() - wall_offset || getY() >= getBattleFieldHeight() - wall_offset;
            }
        });
        
        // movement robota
        while(true) {
            movement();
            System.out.print(wall_distance);
        }
    }
    private void movement() {

        if (wall_distance > 0) {
            wall_distance--;
        }
        // normal movement: switch directions if we've stopped
        if (getVelocity() == 0) {
            setTurnRight(10);
            setAhead(9 * kierunek);
        }

        ahead(100);
    }

    public void onScannedRobot(ScannedRobotEvent event){
        //wyniki z radaru
    }

    public void onCustomEvent(CustomEvent e) {
        if (e.getCondition().getName().equals("wall"))
        {
            if (wall_distance <= 0) {
                wall_distance += wall_offset;
                setMaxVelocity(0);
            }
        }
    }

    public void onPaint(Graphics2D g) {
        String x = "X: " + Double.toString(getX());
        String y = "Y: " + Double.toString(getY());
        String d = Integer.toString(wall_distance);
        g.setColor(Color.WHITE);
        g.drawString(x, 10 ,20);
        g.drawString(y, 10, 10);
        g.drawString(d, 10,30);

    }
}
