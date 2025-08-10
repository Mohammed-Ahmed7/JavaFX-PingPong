// Datei: src/pingpong/app/FXMLDocumentController.java
package pingpong.app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Optional;
import pingpong.config.GameConfig;
import pingpong.engine.GameEngine;
import pingpong.model.Direction;

public class FXMLDocumentController {

    // Hier werden alle UI-Elemente aus der FXML-Datei verbunden
    @FXML private AnchorPane  rootPane;       // Container für alle Spielobjekte
    @FXML private Circle      ballView;       // Kreis, der den Ball darstellt
    @FXML private Rectangle   leftPaddle, rightPaddle;  // Zwei Rechtecke als Paddles
    @FXML private Line        midLine;        // Mittellinie im Spielfeld
    @FXML private Label       scoreLeftLabel, scoreRightLabel; // Punktestände
    @FXML private Label       timeLabel;      // Countdown-Anzeige
    @FXML private Button      startButton;    // Start/Stopp-Knopf

    // Engine steuert Spiel-Loop, Bewegungen und Kollisionen
    private GameEngine engine;

    // Basisgrößen aus der Konfiguration für Skalierung
    private static final double BASE_W = GameConfig.BASE_WIDTH;
    private static final double BASE_H = GameConfig.BASE_HEIGHT;
    private double baseBallSpeed = GameConfig.BALL_SPEED;  // Startgeschwindigkeit des Balls

    // Für den 3-Minuten-Countdown
    private Timeline countdown;
    private int remainingSeconds = 180;  // Gesamtzeit in Sekunden
    private boolean firstStart = true;   // um einmalig die Geschwindigkeit abzufragen

    /**
     * Diese Methode wird automatisch nach dem Laden der FXML aufgerufen
     * Hier initialisieren wir Bindings, Listener und Event-Handler
     */
    @FXML
    public void initialize() {
        // Größe der Formen an die Fenstergröße binden
        // Der Ball-Radius wächst bzw. schrumpft mit der Höhe des Fensters
        ballView.radiusProperty().bind(
            rootPane.heightProperty().multiply(GameConfig.BALL_RADIUS / BASE_H)
        );
        // Die Breite und Höhe der Paddles passen sich an die Fenstergröße an
        leftPaddle.widthProperty().bind(
            rootPane.widthProperty().multiply(GameConfig.PADDLE_WIDTH / BASE_W)
        );
        leftPaddle.heightProperty().bind(
            rootPane.heightProperty().multiply(GameConfig.PADDLE_HEIGHT / BASE_H)
        );
        // Rechtes Paddle spiegelt einfach linkes Paddle in Größe
        rightPaddle.widthProperty().bind(leftPaddle.widthProperty());
        rightPaddle.heightProperty().bind(leftPaddle.heightProperty());

        // GameEngine erstellen und mit Referenzen versorgen
        engine = new GameEngine(
            rootPane,
            leftPaddle, rightPaddle,
            ballView,
            scoreLeftLabel, scoreRightLabel
        );

        // Listener für Fenstergrößenänderung: aktualisiert Geschwindigkeiten und Positionen
        ChangeListener<Number> resizeListener = (obs, oldV, newV) -> {
            updateSpeeds();  // Ball- und Paddle-Geschwindigkeit anpassen
            positionAll();   // Alle Elemente neu platzieren
        };
        rootPane.widthProperty().addListener(resizeListener);
        rootPane.heightProperty().addListener(resizeListener);

        // Nach dem ersten Layout ausgeführt, damit Breiten/Höhen bekannt sind
        Platform.runLater(() -> {
            updateSpeeds();
            positionAll();
        });

        // Countdown einstellen: verringert jede Sekunde remainingSeconds
        timeLabel.setText(formatTime(remainingSeconds));  // Startanzeige
        countdown = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                remainingSeconds--;               // Sekunde runter
                timeLabel.setText(formatTime(remainingSeconds)); // Anzeige aktualisieren
                if (remainingSeconds <= 0) {      // Zeit um?
                    countdown.stop();             // Stopp die Uhr
                    engine.stop();               // Spiel beenden
                    startButton.setText("Start");
                }
            })
        );
        countdown.setCycleCount(Timeline.INDEFINITE); // unendlich oft

        // Start-Stop-Button: togglet zwischen Spielen und Pausieren
        startButton.setOnAction(e -> {
            if (startButton.getText().equals("Start")) {
                // Beim ersten Start fragt Dialog die Ballgeschwindigkeit ab
                if (firstStart) {
                    TextInputDialog dlg = new TextInputDialog(
                        String.valueOf(GameConfig.BALL_SPEED)
                    );
                    dlg.setTitle("Ballgeschwindigkeit");
                    dlg.setHeaderText("Bitte die Ballgeschwindigkeit eingeben:");
                    Optional<String> res = dlg.showAndWait();
                    res.ifPresent(s -> {
                        try {
                            baseBallSpeed = Double.parseDouble(s);
                        } catch (NumberFormatException ex) {
                            // Fallback auf Default, falls ungültige Eingabe
                            baseBallSpeed = GameConfig.BALL_SPEED;
                        }
                    });
                    firstStart = false; // Dialog nur ein Mal zeigen
                }
                // Spiel (neu) starten: Zeit zurücksetzen, Engine starten, Button-Text wechseln
                remainingSeconds = 180;
                timeLabel.setText(formatTime(remainingSeconds));
                updateSpeeds();
                engine.start();
                countdown.playFromStart();
                startButton.setText("Stop");
                rootPane.requestFocus();  // Fokus für Key-Events
            } else {
                // Aktives Spiel stoppen
                engine.stop();
                countdown.stop();
                startButton.setText("Start");
            }
        });

        //  Tastatur-Controls: W/S für links, Pfeil hoch/runter für rechts
        rootPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W)    engine.setLeftPaddleDirection(Direction.UP);
            if (e.getCode() == KeyCode.S)    engine.setLeftPaddleDirection(Direction.DOWN);
            if (e.getCode() == KeyCode.UP)   engine.setRightPaddleDirection(Direction.UP);
            if (e.getCode() == KeyCode.DOWN) engine.setRightPaddleDirection(Direction.DOWN);
        });
        rootPane.setOnKeyReleased(e -> {
            // Bei Loslassen auf NONE setzen, damit Paddle stoppt
            if (e.getCode()==KeyCode.W || e.getCode()==KeyCode.S)
                engine.setLeftPaddleDirection(Direction.NONE);
            if (e.getCode()==KeyCode.UP || e.getCode()==KeyCode.DOWN)
                engine.setRightPaddleDirection(Direction.NONE);
        });
    }

    // Formatiert eine Zeitangabe in Sekunden zu "MM:SS"
    
    private String formatTime(int secs) {
        int m = secs / 60;
        int s = secs % 60;
        return String.format("%02d:%02d", m, s);
    }

    // Aktualisiert Ball- und Paddle-Geschwindigkeiten basierend auf Fenstergröße
    
    private void updateSpeeds() {
        engine.setBallSpeed(baseBallSpeed);  // Ball behält eingestellte Basisgeschwindigkeit
        // Paddles werden etwas langsamer bei kleinerem Fenster
        double scale = Math.min(
            rootPane.getWidth()  / BASE_W,
            rootPane.getHeight() / BASE_H
        );
        engine.setPaddleSpeed(GameConfig.PADDLE_SPEED * scale);
    }

    /**
     * Positioniert alle Spielfiguren, Score-Zahlen und UI-Elemente neu.
     * Wird bei Fenster-Resize und Spielstart aufgerufen
     */
    private void positionAll() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();
        double scale = Math.min(w/BASE_W, h/BASE_H);
        double px = GameConfig.PADDLE_OFFSET * scale;
        double ph = leftPaddle.getHeight();
        double margin = 10; // Abstand zu Rand

        //  Mittellinie zeichnen
        midLine.setStartX(w/2);
        midLine.setStartY(0);
        midLine.setEndX(w/2);
        midLine.setEndY(h);

        // Paddles mittig hoch/runter platzieren
        leftPaddle.setLayoutX(px);
        leftPaddle.setLayoutY((h - ph) / 2);
        rightPaddle.setLayoutX(w - px - rightPaddle.getWidth());
        rightPaddle.setLayoutY((h - ph) / 2);

        // Ball immer in die Mitte setzen
        ballView.setCenterX(w / 2);
        ballView.setCenterY(h / 2);

        // Score-Labels oben links und oben rechts
        scoreLeftLabel.setLayoutX(margin);
        scoreLeftLabel.setLayoutY(margin);
        scoreRightLabel.setLayoutX(w - margin - scoreRightLabel.getWidth());
        scoreRightLabel.setLayoutY(margin);

        // Timer-Anzeige oben mittig
        timeLabel.setLayoutX((w - timeLabel.getWidth()) / 2);
        timeLabel.setLayoutY(margin);

        // Start/Stop-Button unten mittig
        startButton.setLayoutX((w - startButton.getWidth()) / 2);
        startButton.setLayoutY(h - startButton.getHeight() - margin);
    }
}
