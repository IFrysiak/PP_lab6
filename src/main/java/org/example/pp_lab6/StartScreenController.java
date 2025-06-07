package org.example.pp_lab6;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

public class StartScreenController {

    @FXML private ComboBox<String> operationComboBox;
    @FXML private Button executeButton;
    @FXML private ImageView imagePreview;
    @FXML private Button loadImageButton;
    @FXML private Button saveImageButton;
    @FXML private Button resizeImageButton;
    @FXML private Button rotateLeftButton;
    @FXML private Button rotateRightButton;

    private File loadedImageFile;
    private boolean operationExecuted = false;
    private Image originalImage;

    @FXML
    public void initialize() {
        operationComboBox.getItems().addAll("Negatyw", "Progowanie", "Konturowanie");
        executeButton.setDisable(true);
        operationComboBox.setDisable(true);
        saveImageButton.setDisable(true);
        resizeImageButton.setDisable(true);
        rotateLeftButton.setDisable(true);
        rotateRightButton.setDisable(true);
    }

    @FXML
    public void handleLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz obraz JPG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Obrazy JPG", "*.jpg"));

        File file = fileChooser.showOpenDialog(loadImageButton.getScene().getWindow());

        if (file != null) {
            try {
                if (!file.getName().toLowerCase().endsWith(".jpg")) {
                    Toast.show("Niedozwolony format pliku");
                    return;
                }

                loadedImageFile = null;
                imagePreview.setImage(null);

                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                loadedImageFile = file;
                originalImage = image;

                executeButton.setDisable(false);
                operationComboBox.setDisable(false);
                saveImageButton.setDisable(false);
                resizeImageButton.setDisable(false);
                rotateLeftButton.setDisable(false);
                rotateRightButton.setDisable(false);
                operationExecuted = false;

                Toast.show("Pomyślnie załadowano plik");
            } catch (Exception e) {
                Toast.show("Nie udało się załadować pliku");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleExecute() {
        String selected = operationComboBox.getValue();
        if (selected == null) {
            Toast.show("Nie wybrano operacji do wykonania");
        } else if (selected.equals("Negatyw")) {
            applyNegative();
        } else if (selected.equals("Progowanie")) {
            showThresholdDialog();
        } else if (selected.equals("Konturowanie")) {
            applyEdgeDetection();
        } else {
            System.out.println("Wykonuję: " + selected);
            Toast.show("Wykonano: " + selected);
            operationExecuted = true;
        }
    }

    @FXML
    public void handleSaveImage() {
        if (loadedImageFile == null || imagePreview.getImage() == null) {
            Toast.show("Brak załadowanego pliku");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zapisz obraz");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label nameLabel = new Label("Nazwa pliku:");
        TextField nameField = new TextField();
        nameField.setPromptText("(3-100 znaków)");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        Label warningLabel = new Label("Na pliku nie zostały wykonane żadne operacje!");
        warningLabel.setStyle("-fx-text-fill: orange;");
        warningLabel.setVisible(!operationExecuted);

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 3 && newVal.length() <= 100) {
                errorLabel.setVisible(false);
                saveButton.setDisable(false);
            } else {
                errorLabel.setText("Wpisz co najmniej 3 znaki");
                errorLabel.setVisible(true);
                saveButton.setDisable(true);
            }
        });

        VBox content = new VBox(10, nameLabel, nameField, warningLabel, errorLabel);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return nameField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(filename -> {
            String userHome = System.getProperty("user.home");
            File picturesDir = new File(userHome, "Pictures");
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();
            }

            File outputFile = new File(picturesDir, filename + ".jpg");

            if (outputFile.exists()) {
                Toast.show("Plik " + filename + ".jpg już istnieje w systemie. Podaj inną nazwę pliku!");
                return;
            }

            try {
                Image fxImage = imagePreview.getImage();
                BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);

                if (bImage.getType() != BufferedImage.TYPE_INT_RGB) {
                    BufferedImage newBufferedImage = new BufferedImage(
                            bImage.getWidth(),
                            bImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB
                    );
                    newBufferedImage.createGraphics().drawImage(bImage, 0, 0, null);
                    bImage = newBufferedImage;
                }

                ImageIO.write(bImage, "jpg", outputFile);
                Toast.show("Zapisano obraz w pliku " + filename + ".jpg");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.show("Nie udało się zapisać pliku " + filename + ".jpg");
            }
        });
    }

    @FXML
    public void handleResizeImage() {
        if (imagePreview.getImage() == null) {
            Toast.show("Brak obrazu do przeskalowania");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Skalowanie obrazu");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label widthLabel = new Label("Szerokość (px):");
        TextField widthField = new TextField();

        Label heightLabel = new Label("Wysokość (px):");
        TextField heightField = new TextField();

        Label widthError = new Label("Pole jest wymagane");
        widthError.setStyle("-fx-text-fill: red;");
        widthError.setVisible(false);

        Label heightError = new Label("Pole jest wymagane");
        heightError.setStyle("-fx-text-fill: red;");
        heightError.setVisible(false);

        ButtonType resizeButtonType = new ButtonType("Zmień rozmiar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType resetButtonType = new ButtonType("Przywróć oryginał", ButtonBar.ButtonData.OTHER);

        dialog.getDialogPane().getButtonTypes().addAll(resizeButtonType, cancelButtonType, resetButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(widthLabel, 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(widthError, 1, 1);
        grid.add(heightLabel, 0, 2);
        grid.add(heightField, 1, 2);
        grid.add(heightError, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == resizeButtonType) {
                boolean valid = true;
                widthError.setVisible(false);
                heightError.setVisible(false);

                String widthText = widthField.getText();
                String heightText = heightField.getText();

                if (widthText.isEmpty()) {
                    widthError.setVisible(true);
                    valid = false;
                }
                if (heightText.isEmpty()) {
                    heightError.setVisible(true);
                    valid = false;
                }

                if (!valid) return null;

                try {
                    int width = Integer.parseInt(widthText);
                    int height = Integer.parseInt(heightText);
                    if (width <= 0 || width > 3000 || height <= 0 || height > 3000) {
                        Toast.show("Podaj liczby z zakresu 1–3000");
                        return null;
                    }

                    Image fxImage = imagePreview.getImage();
                    BufferedImage input = SwingFXUtils.fromFXImage(fxImage, null);
                    BufferedImage output = new BufferedImage(width, height,
                            input != null && input.getType() != 0 ? input.getType() : BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = output.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(input, 0, 0, width, height, null);
                    g2d.dispose();

                    Image scaledImage = SwingFXUtils.toFXImage(output, null);
                    imagePreview.setImage(scaledImage);
                    operationExecuted = true;

                    Toast.show("Zmieniono rozmiar obrazu");
                } catch (NumberFormatException e) {
                    Toast.show("Wprowadź poprawne liczby");
                }
            } else if (button == resetButtonType) {
                imagePreview.setImage(originalImage);
                Toast.show("Przywrócono oryginalne wymiary");
            }

            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    public void handleRotateLeft() {
        rotateImage(-90);
    }

    @FXML
    public void handleRotateRight() {
        rotateImage(90);
    }

    private void rotateImage(double angle) {
        if (imagePreview.getImage() == null) return;

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imagePreview.getImage(), null);

            double radians = Math.toRadians(angle);
            double sin = Math.abs(Math.sin(radians));
            double cos = Math.abs(Math.cos(radians));
            int newWidth = (int) Math.round(bufferedImage.getWidth() * cos + bufferedImage.getHeight() * sin);
            int newHeight = (int) Math.round(bufferedImage.getWidth() * sin + bufferedImage.getHeight() * cos);

            BufferedImage rotated = new BufferedImage(newWidth, newHeight,
                    bufferedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType());

            Graphics2D g2d = rotated.createGraphics();
            AffineTransform at = new AffineTransform();
            at.translate((newWidth - bufferedImage.getWidth()) / 2, (newHeight - bufferedImage.getHeight()) / 2);
            at.rotate(Math.toRadians(angle), bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2);
            g2d.setTransform(at);
            g2d.drawImage(bufferedImage, 0, 0, null);
            g2d.dispose();

            Image rotatedImage = SwingFXUtils.toFXImage(rotated, null);
            imagePreview.setImage(rotatedImage);
            operationExecuted = true;

            Toast.show("Obrócono obraz o " + angle + " stopni");
        } catch (Exception e) {
            Toast.show("Błąd podczas obracania obrazu");
            e.printStackTrace();
        }
    }

    private void applyNegative() {
        if (imagePreview.getImage() == null) {
            Toast.show("Brak obrazu do przetworzenia");
            return;
        }

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imagePreview.getImage(), null);
            BufferedImage negativeImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    bufferedImage.getType()
            );

            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int a = (rgb >> 24) & 0xff;
                    int r = 255 - ((rgb >> 16) & 0xff);
                    int g = 255 - ((rgb >> 8) & 0xff);
                    int b = 255 - (rgb & 0xff);
                    negativeImage.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }

            Image negativeFxImage = SwingFXUtils.toFXImage(negativeImage, null);
            imagePreview.setImage(negativeFxImage);
            operationExecuted = true;
            Toast.show("Negatyw został wygenerowany pomyślnie!");
        } catch (Exception e) {
            Toast.show("Nie udało się wykonać negatywu.");
            e.printStackTrace();
        }
    }

    private void applyThreshold(int threshold) {
        if (imagePreview.getImage() == null) {
            Toast.show("Brak obrazu do przetworzenia");
            return;
        }

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imagePreview.getImage(), null);
            BufferedImage thresholdImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                    int newColor = (gray > threshold) ? 255 : 0;
                    int newRgb = (newColor << 16) | (newColor << 8) | newColor;
                    thresholdImage.setRGB(x, y, newRgb);
                }
            }

            Image thresholdFxImage = SwingFXUtils.toFXImage(thresholdImage, null);
            imagePreview.setImage(thresholdFxImage);
            operationExecuted = true;
            Toast.show("Progowanie zostało przeprowadzone pomyślnie!");
        } catch (Exception e) {
            Toast.show("Nie udało się wykonać progowania.");
            e.printStackTrace();
        }
    }

    private void showThresholdDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Progowanie obrazu");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label thresholdLabel = new Label("Wartość progu (0-255):");
        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Wprowadź wartość 0-255");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        ButtonType applyButtonType = new ButtonType("Wykonaj progowanie", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, cancelButtonType);

        Button applyButton = (Button) dialog.getDialogPane().lookupButton(applyButtonType);
        applyButton.setDisable(true);

        thresholdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                thresholdField.setText(newVal.replaceAll("[^\\d]", ""));
                return;
            }

            if (!newVal.isEmpty()) {
                try {
                    int value = Integer.parseInt(newVal);
                    if (value >= 0 && value <= 255) {
                        errorLabel.setVisible(false);
                        applyButton.setDisable(false);
                    } else {
                        errorLabel.setText("Wartość musi być w zakresie 0-255");
                        errorLabel.setVisible(true);
                        applyButton.setDisable(true);
                    }
                } catch (NumberFormatException e) {
                    errorLabel.setText("Nieprawidłowa wartość");
                    errorLabel.setVisible(true);
                    applyButton.setDisable(true);
                }
            } else {
                applyButton.setDisable(true);
                errorLabel.setVisible(false);
            }
        });

        VBox content = new VBox(10, thresholdLabel, thresholdField, errorLabel);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == applyButtonType) {
                try {
                    return Integer.parseInt(thresholdField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(this::applyThreshold);
    }

    private void applyEdgeDetection() {
        if (imagePreview.getImage() == null) {
            Toast.show("Brak obrazu do przetworzenia");
            return;
        }

        try {
            BufferedImage originalImage = SwingFXUtils.fromFXImage(imagePreview.getImage(), null);
            BufferedImage grayImage = convertToGrayscale(originalImage);
            BufferedImage edgeImage = detectEdges(grayImage);

            Image edgeFxImage = SwingFXUtils.toFXImage(edgeImage, null);
            imagePreview.setImage(edgeFxImage);
            operationExecuted = true;
            Toast.show("Konturowanie zostało przeprowadzone pomyślnie!");
        } catch (Exception e) {
            Toast.show("Nie udało się wykonać konturowania.");
            e.printStackTrace();
        }
    }

    private BufferedImage convertToGrayscale(BufferedImage original) {
        BufferedImage grayImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        Graphics g = grayImage.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return grayImage;
    }

    private BufferedImage detectEdges(BufferedImage grayImage) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = 0;
                int gy = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = grayImage.getRGB(x + j, y + i) & 0xff;
                        gx += pixel * sobelX[i + 1][j + 1];
                        gy += pixel * sobelY[i + 1][j + 1];
                    }
                }

                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                magnitude = Math.min(255, Math.max(0, magnitude));

                int edgePixel = 255 - magnitude;
                edgeImage.setRGB(x, y, (edgePixel << 16) | (edgePixel << 8) | edgePixel);
            }
        }
        return edgeImage;
    }
}
