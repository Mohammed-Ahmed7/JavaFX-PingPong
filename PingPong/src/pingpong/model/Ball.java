package pingpong.model;

import javafx.geometry.Bounds;
import javafx.scene.shape.Circle;
import pingpong.config.GameConfig;

/**
 * Die Ball-Klasse repräsentiert den runden Ball im Spiel.
 * Sie verwaltet Position, Geschwindigkeit und Reset-Verhalten.
 */
public class Ball implements Moveable {
    // Grafische Darstellung des Balls (JavaFX Circle)
    private final Circle view;
    // Geschwindigkeit in X- und Y-Richtung
    private double vx, vy;
    // Basis-Geschwindigkeit, einstellbar über GameConfig
    private double speed = GameConfig.BALL_SPEED;

    /**
     * Konstruktor speichert die Referenz auf den Circle, der den Ball darstellt.
     * @param view Ein Circle-Objekt aus der UI, das den Ball zeigt
     */
    public Ball(Circle view) {
        this.view = view;
    }

    /**
     * Setzt die Ballgeschwindigkeit (wird z.B. beim Spielstart angepasst).
     * @param speed Neue Geschwindigkeit in Einheiten pro Sekunde
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * update wird jeden Frame aufgerufen und verschiebt den Ball basierend
     * auf seiner Geschwindigkeit und der verstrichenen Zeit.
     * @param deltaTime Zeit in Sekunden seit dem letzten Frame
     */
    @Override
    public void update(double deltaTime) {
        // X-Position um vx * Zeit verschieben
        view.setCenterX(view.getCenterX() + vx * deltaTime);
        // Y-Position um vy * Zeit verschieben
        view.setCenterY(view.getCenterY() + vy * deltaTime);
    }

    /**
     * reset positioniert den Ball neu und startet ihn in eine zufällige Richtung.
     * Wird z.B. nach einem Punkt oder Spielstart aufgerufen.
     * @param centerX X-Koordinate der Mitte
     * @param centerY Y-Koordinate der Mitte
     */
    @Override
    public void reset(double centerX, double centerY) {
        // Ball in die Spielfeldmitte setzen
        view.setCenterX(centerX);
        view.setCenterY(centerY);
        // Zufälliger Startwinkel zwischen 0 und 360 Grad (0 bis 2π)
        double angle = Math.random() * 2 * Math.PI;
        // vx und vy so berechnen, dass die Gesamtgeschwindigkeit = speed ist
        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;
    }

    /**
     * getBounds liefert die aktuellen Begrenzungen des Circles zurück,
     * damit z.B. die GameEngine Kollisionen erkennen kann.
     */
    @Override
    public Bounds getBounds() {
        return view.getBoundsInParent();
    }

    /**
     * reverseX kehrt die X-Richtung um (Ball prallt an vertikaler Wand/Paddle ab).
     */
    @Override
    public void reverseX() {
        vx = -vx;
    }

    /**
     * reverseY kehrt die Y-Richtung um (Ball prallt an horizontaler Wand ab).
     */
    @Override
    public void reverseY() {
        vy = -vy;
    }
}

