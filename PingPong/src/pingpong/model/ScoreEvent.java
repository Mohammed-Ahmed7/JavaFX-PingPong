package pingpong.model;

/**
 * ScoreEvent speichert den Punktestand beider Spieler, wenn ein Punkt erzielt wird
 * Dies ist nützlich, um z.B in einer Event-Liste die Entwicklung des Spiels zu protokollieren
 */
public class ScoreEvent {
    // Punktzahl des linken Spielers
    private final int leftScore;
    // Punktzahl des rechten Spielers
    private final int rightScore;

    /**
     * Konstruktor legt die Punktzahlen fest, wenn ein Punkt erzielt wird
     * param l Punkte des linken Spielers
     * param r Punkte des rechten Spielers
     */
    public ScoreEvent(int l, int r) {
        this.leftScore  = l;
        this.rightScore = r;
    }

    /**
     * Gibt die aktuelle Punktzahl des linken Spielers zurück
     * return Punkte links
     */
    public int getLeftScore() {
        return leftScore;
    }

    /**
     * Gibt die aktuelle Punktzahl des rechten Spielers zurück
     * return Punkte rechts
     */
    public int getRightScore() {
        return rightScore;
    }
}