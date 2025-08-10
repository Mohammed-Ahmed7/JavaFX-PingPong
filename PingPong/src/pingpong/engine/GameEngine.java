package pingpong.engine;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import pingpong.model.Ball;
import pingpong.model.Paddle;

import java.net.URL;

public class GameEngine {
    // Unser Spielfeld-Container, in dem alles stattfindet
    private final AnchorPane pane;
    // Die Spieler-Paddles links und rechts
    private final Paddle leftPaddle, rightPaddle;
    // Das Ball-Objekt und seine grafische Darstellung
    private final Ball ball;
    private final Circle ballView;
    // Labels für die Spielstände
    private final Label scoreLeftLabel, scoreRightLabel;

    // AnimationTimer sorgt für die Spiel-Loop
    private AnimationTimer timer;
    private int scoreLeft, scoreRight;

    // AudioClip-Objekte für Sounds (Start, Ende, Paddle, Wand, Punkt)
    private final AudioClip soundStart;
    private final AudioClip soundEnd;
    private final AudioClip soundPaddle;
    private final AudioClip soundWall;
    private final AudioClip soundScore;

    // Konstruktor ruft Paddle-, Ball- und Sound-Initialisierung auf
    public GameEngine(AnchorPane pane,
                      Rectangle leftRect,
                      Rectangle rightRect,
                      Circle ballView,
                      Label leftScore,
                      Label rightScore) {
        // Speichern der Referenzen auf UI-Elemente
        this.pane            = pane;
        this.leftPaddle      = new Paddle(leftRect);
        this.rightPaddle     = new Paddle(rightRect);
        this.ball            = new Ball(ballView);
        this.ballView        = ballView;
        this.scoreLeftLabel  = leftScore;
        this.scoreRightLabel = rightScore;

        // Sounds laden, geben null zurück, falls Datei fehlt
        soundStart  = loadClip("/sounds/game-start-317318.mp3");
        soundEnd    = loadClip("/sounds/game-over-arcade-6435.mp3");
        soundPaddle = loadClip("/sounds/mouth-sound-ping-pong.wav");
        soundWall   = soundPaddle;  // Für Wand-Kollisionen denselben Sound verwenden
        soundScore  = loadClip("/sounds/video-game-bonus-323603.mp3");

        // Timer einrichten, der regelmäßig update() aufruft
        setupTimer();
    }

    /**
     * Lädt eine Audiodatei vom Klassenpfad und gibt sie als AudioClip zurück.
     */
    private AudioClip loadClip(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            return null; // Datei nicht gefunden
        }
        return new AudioClip(url.toExternalForm());
    }

    // Erstellt den AnimationTimer für die Haupt-Spielschleife

    private void setupTimer() {
        timer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    // Beim ersten Aufruf nur Zeitstempel setzen
                    lastTime = now;
                    return;
                }
                // Zeitdifferenz in Sekunden berechnen
                double deltaSeconds = (now - lastTime) / 1e9;
                lastTime = now;
                // Spielzustand updaten
                update(deltaSeconds);
            }
        };
    }

    // Startet oder setzt das Spiel zurück (Score, Position, Timer)
    public void start() {
        // Sound abspielen, falls geladen
        if (soundStart != null) soundStart.play();
        // Punkte zurücksetzen
        scoreLeft = scoreRight = 0;
        updateScores();
        // Ball und Paddles in die Mitte setzen
        ball.reset(pane.getWidth() / 2, pane.getHeight() / 2);
        leftPaddle.reset(0, pane.getHeight() / 2);
        rightPaddle.reset(0, pane.getHeight() / 2);
        // Timer starten → Animation läuft
        timer.start();
    }

    //Stoppt das Spiel und spielt Game-Over-Sound

    public void stop() {
        timer.stop();
        if (soundEnd != null) soundEnd.play();
    }

    /**
     * Wird jeden Frame aufgerufen: bewegt Ball und Paddles und prüft Kollision.
     * @param dt Zeitunterschied in Sekunden seit letztem Frame
     */
    private void update(double dt) {
        ball.update(dt);            // Ball bewegen
        leftPaddle.update(dt);      // linkes Paddle bewegen
        rightPaddle.update(dt);     // rechtes Paddle bewegen
        checkCollisions();          // Kollisionen prüfen
    }

    //Prüft alle Kollisionen: Paddle, Ränder und Punkte
    private void checkCollisions() {
        Bounds playBounds = pane.getLayoutBounds();

        // Paddle-Kollision: exakte Kreis/Rechteck-Prüfung
        Bounds leftBounds  = leftPaddle.getBounds();
        Bounds rightBounds = rightPaddle.getBounds();
        if (isCircleRectCollision(ballView, leftBounds)) {
            handlePaddleCollision(leftBounds);
        } else if (isCircleRectCollision(ballView, rightBounds)) {
            handlePaddleCollision(rightBounds);
        }

        // Ball links raus → Punkt für rechts
        if (ballView.getCenterX() - ballView.getRadius() <= playBounds.getMinX()) {
            scoreRight++;
            updateScores();
            if (soundScore != null) soundScore.play();
            ball.reset(playBounds.getWidth() / 2, playBounds.getHeight() / 2);
        }
        // Ball rechts raus → Punkt für links
        else if (ballView.getCenterX() + ballView.getRadius() >= playBounds.getMaxX()) {
            scoreLeft++;
            updateScores();
            if (soundScore != null) soundScore.play();
            ball.reset(playBounds.getWidth() / 2, playBounds.getHeight() / 2);
        }

        // Ball oben/unten abprallen lassen
        if (ballView.getCenterY() - ballView.getRadius() <= playBounds.getMinY()
         || ballView.getCenterY() + ballView.getRadius() >= playBounds.getMaxY()) {
            ball.reverseY();                          // Y-Richtung umkehren
            if (soundWall != null) soundWall.play();  // Wand-Sound abspielen
        }
    }

    /**
     * Exakte Kreis-zu-Rechteck-Kollisionserkennung
     * param c Ball als Circle-View
     * param r Paddle-/Spielfeld-Bounds
     * return true, falls Kollision stattfindet
     */
    private boolean isCircleRectCollision(Circle c, Bounds r) {
        // Ball-Mittelpunkt und Radius
        double cx = c.getCenterX();
        double cy = c.getCenterY();
        double radius = c.getRadius();

        // Rechteck-Koordinaten
        double rx = r.getMinX();
        double ry = r.getMinY();
        double rw = r.getWidth();
        double rh = r.getHeight();

        // Punkt auf Rechteck, der dem Ballzentrum am nächsten ist
        double closestX = clamp(cx, rx, rx + rw);
        double closestY = clamp(cy, ry, ry + rh);

        // Abstand zum Kreiszentrum
        double dx = cx - closestX;
        double dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }

    // Verschiebt den Ball aus dem Paddle heraus und kehrt X-Richtung um
    
    private void handlePaddleCollision(Bounds r) {
        // Ball direkt neben das Paddle setzen, je nachdem ob links oder rechts getroffen
        if (r.getMinX() < pane.getWidth() / 2) {
            ballView.setCenterX(r.getMaxX() + ballView.getRadius());
        } else {
            ballView.setCenterX(r.getMinX() - ballView.getRadius());
        }
        ball.reverseX();                     // X-Richtung wechseln
        if (soundPaddle != null) soundPaddle.play(); // Paddle-Sound abspielen
    }

    //Hilfsmethode: beschränkt einen Wert auf ein Intervall [min, max].
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    //Aktualisiert die angezeigten Punkte im UI
     
    private void updateScores() {
        scoreLeftLabel .setText(String.valueOf(scoreLeft));
        scoreRightLabel.setText(String.valueOf(scoreRight));
    }

    // Methoden zum Setzen von Ball- und Paddle-Geschwindigkeit von außen
    public void setBallSpeed(double speed) {
        ball.setSpeed(speed);
    }

    public void setPaddleSpeed(double speed) {
        leftPaddle.setSpeed(speed);
        rightPaddle.setSpeed(speed);
    }

    // Methoden, um die Richtung der Paddles zu steuern (z.B. Tastatur)
    public void setLeftPaddleDirection(pingpong.model.Direction dir) {
        leftPaddle.setDirection(dir);
    }

    public void setRightPaddleDirection(pingpong.model.Direction dir) {
        rightPaddle.setDirection(dir);
    }
}