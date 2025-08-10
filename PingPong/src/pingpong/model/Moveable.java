package pingpong.model;

import javafx.geometry.Bounds;

/**
 * Moveable ist ein Interface, das vorgibt, welche Methoden jede bewegliche
 * Spielfigur (z.B. Ball oder Paddle) implementieren muss.
 */
public interface Moveable {
    /**
     * update wird in jedem Frame aufgerufen.
     * Hier soll die Logik stehen, die das Objekt basierend auf
     * der vergangenen Zeit (deltaTime) bewegt.
     * @param deltaTime Zeit in Sekunden seit dem letzten Update
     */
    void update(double deltaTime);

    /**
     * reset setzt das Objekt auf eine Anfangsposition zurück,
     * z.B. Ball in die Mitte.
     * @param centerX X-Koordinate für die neue Mitte
     * @param centerY Y-Koordinate für die neue Mitte
     */
    void reset(double centerX, double centerY);

    /**
     * getBounds liefert die aktuellen Begrenzungen des Objekts,
     * damit z.B. Kollisionen erkannt werden können.
     * @return Ein Bounds-Objekt, das den Bereich umfasst
     */
    Bounds getBounds();

    /**
     * reverseX kehrt die horizontale Bewegungsrichtung um,
     * z.B. beim Abprall an einer vertikalen Fläche.
     */
    void reverseX();

    /**
     * reverseY kehrt die vertikale Bewegungsrichtung um,
     * z.B. beim Abprall an einer horizontalen Fläche.
     */
    void reverseY();
}

