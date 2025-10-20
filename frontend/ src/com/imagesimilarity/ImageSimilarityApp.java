package com.imagesimilarity;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.geometry.*;
import javafx.concurrent.Task;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class ImageSimilarityApp extends Application {
    private ImageView queryView = new ImageView();
    private FlowPane resultsPane = new FlowPane(10, 10);
    private File queryImage;

    @Override
    public void start(Stage stage) {
        Button chooseBtn = new Button("Choose Image");
        Button searchBtn = new Button("Search Similar");
        Button resetBtn = new Button("Reset Index");
        searchBtn.setDisable(true);

        chooseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png"));
            queryImage = fc.showOpenDialog(stage);
            if (queryImage != null) {
                queryView.setImage(new Image(queryImage.toURI().toString(), 200, 200, true, true));
                searchBtn.setDisable(false);
            }
        });

        searchBtn.setOnAction(e -> {
            resultsPane.getChildren().clear();
            findSimilarImages();
        });

        resetBtn.setOnAction(e -> resetIndex());

        VBox layout = new VBox(15, chooseBtn, queryView, searchBtn, resetBtn, resultsPane);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setTitle("CLIP + FAISS Image Similarity");
        stage.setScene(new Scene(layout, 900, 600));
        stage.show();
    }

    private void findSimilarImages() {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return sendImageToServer(queryImage);
            }
        };

        task.setOnSucceeded(e -> {
            for (String path : task.getValue()) {
                ImageView iv = new ImageView(new Image("file:" + path, 150, 150, true, true));
                resultsPane.getChildren().add(iv);
            }
        });

        new Thread(task).start();
    }

    private List<String> sendImageToServer(File image) throws Exception {
        String url = "http://127.0.0.1:5000/search_image";
        String boundary = "Boundary";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(("--" + boundary + "\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"image\"; filename=\"" + image.getName() + "\"\r\n").getBytes());
            os.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
            Files.copy(image.toPath(), os);
            os.write(("\r\n--" + boundary + "--\r\n").getBytes());
            os.flush();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JSONArray arr = new JSONObject(sb.toString()).getJSONArray("results");
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) paths.add(arr.getString(i));
        return paths;
    }

    private void resetIndex() {
        new Thread(() -> {
            try {
                URL url = new URL("http://127.0.0.1:5000/reset_index");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.getResponseCode();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}
