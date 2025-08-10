package pingpong.model;

import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;
import pingpong.config.GameConfig;

/**
 * Paddle repräsentiert ein sichtbares Paddle im Spiel, das sich hoch und runter bewegt.
 */
public class Paddle implements Moveable {
    // Rechteck aus der UI, das unser Paddle darstellt
    private final Rectangle view;
    // Geschwindigkeit in Y-Richtung (positiv = nach unten)
    private double vy;
    // Basisgeschwindigkeit, anpassbar über GameConfig
    private double speed = GameConfig.PADDLE_SPEED;

    /**
     * Konstruktor speichert die Ansicht (Rectangle) für das Paddle
     * param view JavaFX-Rectangle, das das Paddle im UI ist
     */
    public Paddle(Rectangle view) {
        this.view = view;
    }

    /**
     * Ändert die Geschwindigkeit des Paddles.
     * @param speed Neue Geschwindigkeit in Einheiten pro Sekunde
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Setzt die Bewegungsrichtung des Paddles basierend auf einer Direction.
     * UP    → Paddle bewegt sich nach oben (negatives vy)
     * DOWN  → Paddle bewegt sich nach unten (positives vy)
     * NONE  → Paddle stoppt (vy = 0)
     * param dir Die gewünschte Richtung aus der Direction-Enum
     */
    public void setDirection(Direction dir) {
        switch (dir) {
            case UP:    vy = -speed; break;
            case DOWN:  vy =  speed; break;
            default:    vy = 0;        
        }
    }

    /**
     * update wird jeden Frame aufgerufen und verschiebt das Paddle.
     * Wir ändern dabei das LayoutY und stellen sicher, dass es im Spielbereich bleibt.
     * param deltaTime Zeit in Sekunden seit dem letzten Frame
     */
    @Override
    public void update(double deltaTime) {
        // Neue Y-Position berechnen
        double newY = view.getLayoutY() + vy * deltaTime;
        // Spielfeldgrenzen herausfinden
        Bounds b = view.getParent().getLayoutBounds();
        // Sicherstellen, dass Paddle nicht über den Rand hinausgeht
        newY = Math.max(0, Math.min(newY, b.getHeight() - view.getHeight()));
        // Position im UI setzen
        view.setLayoutY(newY);
    }

    /**
     * reset setzt das Paddle mittig auf der Y-Achse zurück.
     * param centerX Nicht verwendet (Paddles bewegen sich nur vertikal)
     * param centerY Y-Koordinate der Spielfeldmitte
     */
    @Override
    public void reset(double centerX, double centerY) {
        // Paddle so setzen, dass es mittig an centerY ausgerichtet ist
        view.setLayoutY(centerY - view.getHeight() / 2.0);
    }

    /**
     * Liefert die aktuellen Bounds des Paddles für Kollisionschecks.
     */
    @Override
    public Bounds getBounds() {
        return view.getBoundsInParent();
    }

    /**
     * Paddle prallt nicht horizontal ab, daher keine Aktion.
     */
    @Override
    public void reverseX() { /* nicht benötigt */ }

    /**
     * Paddle prallt nicht vertikal ab, daher keine Aktion.
     */
    @Override
    public void reverseY() { /* nicht benötigt */ }
}