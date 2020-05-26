package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class Controller {
    //co-ordinates for mouse movement
    double startX, startY, prevX, prevY, currentX, currentY;
    boolean dragging, newRectBeingDrawn, newElliBeingDrawn, newLineBeingDrawn;

    FileChooser fileChooser = new FileChooser();
//initialise shape objects
    Rectangle newRectangle = new Rectangle();
    Ellipse newEllipse = new Ellipse();
    Line newLine = new Line();
    Text newText = new Text();

    ObservableList<String> fonts = FXCollections.observableArrayList(Font.getFamilies());
//initialise undo/redo
    Stack<Shape> undoHist = new Stack();
    Stack<Shape> redoHist = new Stack();

    @FXML private ScrollPane scrollPane;
    @FXML private Pane mainPane;
    @FXML private Canvas canvas;
    @FXML private Pane pane;

    @FXML private Button clearCanvas;
    @FXML private Button resizeCanvas;
    @FXML private Button setBackgroundCol;
    @FXML private Button undo;
    @FXML private Button redo;

    @FXML private ColorPicker brushStroke = new ColorPicker();
    @FXML private ColorPicker shapeFill = new ColorPicker();
    @FXML private ColorPicker shapeStroke = new ColorPicker();
    @FXML private ColorPicker backgroundColour = new ColorPicker();
    @FXML private ColorPicker textStroke = new ColorPicker();
    @FXML private ColorPicker textFill = new ColorPicker();
    @FXML private Slider brushOpacity = new Slider(0, 1, 1);
    @FXML private Slider shapeOpacity = new Slider(0, 1, 1);
    @FXML private Slider textOpacity = new Slider(0, 1, 1);
    @FXML private Slider brushSize = new Slider(0, 200, 20);
    @FXML private Slider shapeStrokeSize = new Slider(0, 200, 20);
    @FXML private Slider textStrokeSize = new Slider(0, 200, 20);
    @FXML private Slider textSize = new Slider(0, 200, 20);

    @FXML private TextField tfCanvasW;
    @FXML private TextField tfCanvasH;
    @FXML private TextField writeText;
    @FXML private ChoiceBox<String> selectFont;

    @FXML private ToggleButton eraser;
    @FXML private ToggleButton brush;
    @FXML private ToggleButton rectangle;
    @FXML private ToggleButton ellipse;
    @FXML private ToggleButton line;
    @FXML private ToggleButton text;
    @FXML private ToggleGroup group = new ToggleGroup();

    @FXML private CheckBox filled;

    public void initialize() {
        mainPane.setPrefSize(800, 600);
        scrollPane.setPrefSize(800, 600);
        scrollPane.setContent(mainPane);
        pane.prefHeightProperty().bind(canvas.heightProperty());
        pane.prefWidthProperty().bind(canvas.widthProperty());
        GraphicsContext g = canvas.getGraphicsContext2D();
        selectFont.setItems(fonts);
        clipChildren(pane);
        group.getToggles().addAll(eraser, brush, rectangle, ellipse, line, text);

        fileChooser.setTitle("Open image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(".png",
                        "*.png"),
                new FileChooser.ExtensionFilter(".jpg", "*.jpg"));

        canvas.setOnMousePressed(e -> {
            startX = prevX = currentX = e.getX();
            startY = prevY = currentY = e.getY();
            dragging = true;

            if(rectangle.isSelected()) {
                if (!newRectBeingDrawn) {
                    newRectangle = new Rectangle();
                    newRectangle.setFill(Color.GRAY);
                    newRectangle.setStroke(shapeStroke.getValue());
                    newRectangle.setStrokeWidth(shapeStrokeSize.getValue());
                    pane.getChildren().add(newRectangle);
                    newRectBeingDrawn = true;
                }
            }
            if(ellipse.isSelected()) {
                if (!newElliBeingDrawn) {
                    newEllipse = new Ellipse();
                    newEllipse.setFill(Color.GRAY);
                    newEllipse.setStroke(shapeStroke.getValue());
                    newEllipse.setStrokeWidth(shapeStrokeSize.getValue());
                    newEllipse.toFront();
                    pane.getChildren().add(newEllipse);
                    newElliBeingDrawn = true;
                }
            }
            if(line.isSelected()) {
                if(!newLineBeingDrawn) {
                    newLine = new Line();
                    newLine.setStroke(Color.GRAY);
                    newLine.setStrokeWidth(shapeStrokeSize.getValue());
                    newLine.toFront();
                    pane.getChildren().add(newLine);
                    newLineBeingDrawn = true;
                }
            }
            if(text.isSelected()) {
                newText = new Text();
                newText.setText(writeText.getText());
                newText.setStroke(textStroke.getValue());
                newText.setFill(textFill.getValue());
                newText.setStrokeWidth(textStrokeSize.getValue());
                newText.setOpacity(textOpacity.getValue());
                newText.setFont(Font.font(selectFont.getValue(), textSize.getValue()));
                pane.getChildren().add(newText);
                adjustText(currentX, currentY, newText);
                newText.toFront();
            }
        });

        canvas.setOnMouseDragged(e -> {
            if(!dragging)
                return;
            currentX = e.getX();
            currentY = e.getY();
            if(brush.isSelected()) {
                g.setFill(brushStroke.getValue());
                g.setGlobalAlpha(brushOpacity.getValue());
                g.fillOval(currentX-(brushSize.getValue() / 2), currentY-(brushSize.getValue() / 2), brushSize.getValue(), brushSize.getValue());
            } else if (eraser.isSelected()) {
                g.clearRect(currentX-(brushSize.getValue() / 2), currentY-(brushSize.getValue() / 2), brushSize.getValue(), brushSize.getValue());
            } else if (rectangle.isSelected()) {
                if (newRectBeingDrawn) {
                    adjustRect(startX, startY, currentX, currentY, newRectangle);
                }
            } else if (ellipse.isSelected()) {
                if(newElliBeingDrawn) {
                    adjustElli(startX, startY, currentX, currentY, newEllipse);
                }
            } else if (line.isSelected()) {
                if(newLineBeingDrawn) {
                    adjustLine(startX, startY, currentX, currentY, newLine);
                }
            }
            prevX = currentX;
            prevY = currentY;
        });

        canvas.setOnMouseReleased(e -> {
            dragging = false;

            if(newRectBeingDrawn) {
                newRectangle.setStrokeWidth(shapeStrokeSize.getValue());
                newRectangle.setStroke(shapeStroke.getValue());
                newRectangle.setOpacity(shapeOpacity.getValue());
                if(filled.isSelected()) {
                    newRectangle.setFill(shapeFill.getValue());
                } else {
                    newRectangle.setFill(Color.TRANSPARENT);
                }
                undoHist.push(new Rectangle(newRectangle.getX(), newRectangle.getY(), newRectangle.getWidth(), newRectangle.getHeight()));
                newRectBeingDrawn = false;
            }
            if(newElliBeingDrawn) {
                newEllipse.setStrokeWidth(shapeStrokeSize.getValue());
                newEllipse.setStroke(shapeStroke.getValue());
                newEllipse.setOpacity(shapeOpacity.getValue());
                if(filled.isSelected()) {
                    newEllipse.setFill(shapeFill.getValue());
                } else {
                    newEllipse.setFill(Color.TRANSPARENT);
                }
                undoHist.push(new Ellipse(newEllipse.getCenterX(), newEllipse.getCenterY(), newEllipse.getRadiusX(), newEllipse.getRadiusY()));
                newElliBeingDrawn = false;
            }
            if(newLineBeingDrawn) {
                newLine.setStrokeWidth(shapeStrokeSize.getValue());
                newLine.setStroke(shapeStroke.getValue());
                newLine.setOpacity(shapeOpacity.getValue());
                undoHist.push(new Line(newLine.getStartX(), newLine.getStartY(), newLine.getEndX(), newLine.getEndY()));
                newLineBeingDrawn = false;
            }
            if(text.isSelected()) {
                undoHist.push(new Text(newText.getX(), newText.getY(), newText.getText()));
            }
        });

        resizeCanvas.setOnAction(e -> resetCanvasSize());

        undo.setOnAction(e -> {
            if(!undoHist.isEmpty()) {
                Shape removedShape = undoHist.lastElement();
                if (removedShape.getClass() == Rectangle.class) {
                    Rectangle tempRect = (Rectangle) removedShape;
                    tempRect.setFill(newRectangle.getFill());
                    tempRect.setStroke(newRectangle.getStroke());
                    tempRect.setStrokeWidth(newRectangle.getStrokeWidth());
                    redoHist.push(new Rectangle(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight()));
                    pane.getChildren().remove(newRectangle);
                }
                if (removedShape.getClass() == Ellipse.class) {
                    Ellipse tempElli = (Ellipse) removedShape;
                    tempElli.setFill(newEllipse.getFill());
                    tempElli.setStroke(newEllipse.getStroke());
                    tempElli.setStrokeWidth(newEllipse.getStrokeWidth());
                    redoHist.push(new Ellipse(tempElli.getCenterX(), tempElli.getCenterY(), tempElli.getRadiusX(), tempElli.getRadiusY()));
                    pane.getChildren().remove(newEllipse);
                }
                if (removedShape.getClass() == Line.class) {
                    Line tempLine = (Line) removedShape;
                    tempLine.setStroke(newLine.getStroke());
                    tempLine.setStrokeWidth(newLine.getStrokeWidth());
                    redoHist.push(new Line(tempLine.getStartX(), tempLine.getStartY(), tempLine.getEndX(), tempLine.getEndY()));
                    pane.getChildren().remove(newLine);
                }
                if(removedShape.getClass() == Text.class) {
                    Text tempText = (Text) removedShape;
                    tempText.setFill(newText.getFill());
                    tempText.setStroke(newText.getStroke());
                    tempText.setStrokeWidth(newText.getStrokeWidth());
                    tempText.setText(newText.getText());
                    redoHist.push(new Text(tempText.getX(), tempText.getY(), tempText.getText()));
                    pane.getChildren().remove(newText);
                }
                if (redoHist.lastElement() != null) {
                    Shape lastRedo = redoHist.lastElement();
                    lastRedo.setFill(removedShape.getFill());
                    lastRedo.setStroke(removedShape.getStroke());
                    lastRedo.setStrokeWidth(removedShape.getStrokeWidth());
                    undoHist.pop();
                }
            } else {
                System.out.println("no shape to undo");
            }
        });

        redo.setOnAction(e -> {
            if(!redoHist.isEmpty()) {
                Shape shape = redoHist.lastElement();
                g.setLineWidth(shape.getStrokeWidth());
                g.setStroke(shape.getStroke());
                g.setFill(shape.getFill());

                redoHist.pop();
                if(shape.getClass() == Rectangle.class) {
                    newRectangle = (Rectangle) shape;
                    pane.getChildren().add(newRectangle);
                    undoHist.push(new Rectangle(newRectangle.getX(), newRectangle.getY(), newRectangle.getWidth(), newRectangle.getHeight()));
                }
                else if(shape.getClass() == Ellipse.class) {
                    newEllipse = (Ellipse) shape;
                    pane.getChildren().add(newEllipse);
                    undoHist.push(new Ellipse(newEllipse.getCenterX(), newEllipse.getCenterY(), newEllipse.getRadiusX(), newEllipse.getRadiusY()));
                }
                else if(shape.getClass() == Line.class) {
                    newLine = (Line) shape;
                    pane.getChildren().add(newLine);
                    undoHist.push(new Line(newLine.getStartX(), newLine.getStartY(), newLine.getEndX(), newLine.getEndY()));
                }
                else if(shape.getClass() == Text.class) {
                    newText = (Text) shape;
                    pane.getChildren().add(newText);
                    undoHist.push(new Text(newText.getX(), newText.getY(), newText.getText()));
                }
                Shape lastUndo = undoHist.lastElement();
                lastUndo.setFill(shapeFill.getValue());
                lastUndo.setStroke(shapeStroke.getValue());
                lastUndo.setStrokeWidth(shapeStrokeSize.getValue());
            }
        });

        clearCanvas.setOnAction(e -> {
            pane.getChildren().clear();
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        setBackgroundCol.setOnAction(e -> pane.setBackground(new Background(new BackgroundFill(backgroundColour.getValue(), CornerRadii.EMPTY, Insets.EMPTY))));
    }

    void adjustRect (double x1, double y1, double x2, double y2, Rectangle rect) {
        rect.setX(x1);
        rect.setY(y1);
        rect.setWidth(x2 - x1);
        rect.setHeight(y2 - y1);
        if(rect.getWidth() < 0) {
            rect.setWidth( - rect.getWidth());
            rect.setX(rect.getX() - rect.getWidth());
        }
        if(rect.getHeight() < 0) {
            rect.setHeight( - rect.getHeight());
            rect.setY(rect.getY() - rect.getHeight());
        }
    }

    void adjustElli (double x1, double y1, double x2, double y2, Ellipse elli) {
        elli.setCenterX(x1);
        elli.setCenterY(y1);
        elli.setRadiusX(x2 - x1);
        elli.setRadiusY(y2 - y1);
        if(elli.getRadiusX() < 0) {
            elli.setRadiusX( - elli.getRadiusX());
            elli.setCenterX(elli.getCenterX() - elli.getRadiusX());
        }
        if(elli.getRadiusY() < 0) {
            elli.setRadiusY( - elli.getRadiusY());
            elli.setCenterY(elli.getCenterY() - elli.getRadiusY());
        }
    }

    void adjustLine (double x1, double y1, double x2, double y2, Line line) {
        line.setStartX(x1);
        line.setStartY(y1);
        line.setEndX(x2);
        line.setEndY(y2);
    }

    void adjustText (double x1, double y1, Text text) {
        text.setX(x1);
        text.setY(y1);
    }

    public void clipChildren(Region region) {
        Rectangle clip = new Rectangle(700, 500);
        pane.setClip(clip);
        region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            clip.setWidth(Double.parseDouble(tfCanvasW.getText()));
            clip.setHeight(Double.parseDouble(tfCanvasH.getText()));
        });
    }

    public void resetCanvasSize() {
        double w, h;
        w = Double.parseDouble(tfCanvasW.getText());
        h = Double.parseDouble(tfCanvasH.getText());
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    public void onSave() {
        Image image = mainPane.snapshot(null, null);
        fileChooser.setTitle("Save Image");
        Window stage = mainPane.getScene().getWindow();

        File file = fileChooser.showSaveDialog(stage);
        if(file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image,
                        null), "png", file);
            } catch (IOException ignored) {}
        }
    }

    public void onExit() {
        Platform.exit();
    }
}
