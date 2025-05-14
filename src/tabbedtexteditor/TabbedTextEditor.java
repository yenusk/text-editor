/*
 * Modern Tabbed Text Editor with Enhanced Features
 */
package tabbedtexteditor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TabbedTextEditor extends Application {

    private TabPane tabPane = new TabPane();
    private MenuBar menuBar = new MenuBar();
    private FileChooser fileChooser = new FileChooser();
    private int untitledCount = 1;
    private StackPane rootPane = new StackPane();
    private BorderPane editorPane = new BorderPane();
    private VBox welcomePane = new VBox(20);
    private VBox aboutPane = new VBox(20);
    private VBox settingsPane = new VBox(20);
    private ObservableList<DocumentInfo> recentDocuments = FXCollections.observableArrayList();
    private FilteredList<DocumentInfo> filteredDocuments = new FilteredList<>(recentDocuments);
    private boolean darkMode = false;
    private static final String RECENT_FILES_PATH = "recent_files.dat";
    private static final String SETTINGS_PATH = "app_settings.dat";

    private final List<String> suggestedExtensions = Arrays.asList(
            ".txt", ".java", ".html", ".css", ".js", ".json", ".xml", ".md", ".rtf"
    );

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadRecentFiles();
        loadSettings();
        setupFileChooser();
        createWelcomeScreen();
        createAboutPage();
        createSettingsPage();
        setupEditorUI();

        rootPane.getChildren().add(welcomePane);

        Scene scene = new Scene(rootPane, 1000, 700);
        applyCurrentTheme(scene);
        primaryStage.setTitle("Modern Text Editor");
        primaryStage.getIcons().add(new Image("file:text-editor-icon.png"));
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            saveRecentFiles();
            saveSettings();
        });
    }

    private void createSettingsPage() {
        settingsPane.setAlignment(Pos.CENTER);
        settingsPane.setPadding(new Insets(40));

        ImageView logo = new ImageView(new Image("file:text-editor-icon.png", 80, 80, true, true));
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.setSelected(darkMode);
        themeToggle.setOnAction(e -> {
            darkMode = themeToggle.isSelected();
            applyCurrentTheme(rootPane.getScene());
        });

        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> switchToWelcome());

        settingsPane.getChildren().addAll(
                logo, title,
                new Separator(),
                new Label("Appearance:"), themeToggle,
                new Separator(),
                backButton
        );
    }

    private void applyCurrentTheme(Scene scene) {
        if (darkMode) {
            scene.getRoot().setStyle("-fx-base: #3f3f3f; -fx-background: #2d2d2d;");
            editorPane.setStyle("-fx-base: #3f3f3f; -fx-background: #2d2d2d;");
            welcomePane.setStyle("-fx-base: #3f3f3f; -fx-background: #2d2d2d;");
            aboutPane.setStyle("-fx-base: #3f3f3f; -fx-background: #2d2d2d;");
            settingsPane.setStyle("-fx-base: #3f3f3f; -fx-background: #2d2d2d;");
        } else {
            scene.getRoot().setStyle("");
            editorPane.setStyle("");
            welcomePane.setStyle("");
            aboutPane.setStyle("");
            settingsPane.setStyle("");
        }
    }

    private void loadSettings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SETTINGS_PATH))) {
            darkMode = (boolean) ois.readObject();
        } catch (Exception e) {
            // Use default settings if file doesn't exist
            darkMode = false;
        }
    }

    private void saveSettings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SETTINGS_PATH))) {
            oos.writeObject(darkMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRecentFiles() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RECENT_FILES_PATH))) {
            List<DocumentInfo> loadedFiles = (List<DocumentInfo>) ois.readObject();
            recentDocuments.setAll(loadedFiles);
            filteredDocuments = new FilteredList<>(recentDocuments);
        } catch (Exception e) {
            // File doesn't exist or couldn't be read - start with empty list
            recentDocuments.clear();
            filteredDocuments = new FilteredList<>(recentDocuments);
        }
    }

    private void saveRecentFiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RECENT_FILES_PATH))) {
            oos.writeObject(new ArrayList<>(recentDocuments));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAboutPage() {
        aboutPane.setAlignment(Pos.CENTER);
        aboutPane.setPadding(new Insets(40));

        ImageView logo = new ImageView(new Image("file:text-editor-icon.png", 100, 100, true, true));
        Label title = new Label("Modern Text Editor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label version = new Label("Version 1.0");
        version.setStyle("-fx-font-size: 16px;");

        Label developersTitle = new Label("Development Team:");
        developersTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        // Developer table
        TableView<DeveloperInfo> developersTable = new TableView<>();
        developersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DeveloperInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<DeveloperInfo, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        developersTable.getColumns().addAll(nameCol, idCol);

        // Sample developer data
        ObservableList<DeveloperInfo> developers = FXCollections.observableArrayList(
                new DeveloperInfo("Abel Ayele", "DDU1500785"),
                new DeveloperInfo("Hebron Solomon", "DDU1502273"),
                new DeveloperInfo("Nebiyu Ermiyas", "DDU1501530"),
                new DeveloperInfo("Trhas Abrha", "DDU1502253"),
                new DeveloperInfo("Yabsira Dejene", "DDU1501750"),
                new DeveloperInfo("Yenus Kindu", "DDU1501779")
        );

        developersTable.setItems(developers);
        developersTable.setPrefHeight(200);

        Label copyright = new Label("© 2025 Modern Text Editor Team");
        copyright.setStyle("-fx-font-size: 14px; -fx-padding: 20 0 0 0;");

        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> switchToWelcome());

        aboutPane.getChildren().addAll(
                logo, title, version,
                new Separator(),
                developersTitle, developersTable,
                new Separator(),
                copyright,
                backButton
        );
    }

    public static class DeveloperInfo {

        private final String name;
        private final String id;

        public DeveloperInfo(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }

    private void createWelcomeScreen() {
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setPadding(new Insets(40));

        ImageView logo = new ImageView(new Image("file:text-editor-icon.png", 100, 100, true, true));
        Label title = new Label("Modern Text Editor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox header = new VBox(10, logo, title);
        header.setAlignment(Pos.CENTER);

        TableView<DocumentInfo> documentsTable = createDocumentsTable();

        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search documents...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredDocuments.setPredicate(doc
                    -> newVal == null || newVal.isEmpty()
                    || doc.getFileName().toLowerCase().contains(newVal.toLowerCase())
                    || doc.getPath().toLowerCase().contains(newVal.toLowerCase()));
        });

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "Today", "This Week", "This Month");
        filterCombo.setValue("All");
        filterCombo.setOnAction(e -> applyTimeFilter(filterCombo.getValue()));

        searchBox.getChildren().addAll(
                new Label("Search:"), searchField,
                new Label("Filter:"), filterCombo
        );
        searchBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(20);
        Button newDocBtn = new Button("New Document");
        newDocBtn.setOnAction(e -> {
            switchToEditor();
            createNewTab();
        });

        Button openBtn = new Button("Open File...");
        openBtn.setOnAction(e -> {
            openFile();
            if (!tabPane.getTabs().isEmpty()) {
                switchToEditor();
            }
        });

        Button aboutBtn = new Button("About");
        aboutBtn.setOnAction(e -> switchToAbout());

        Button settingsBtn = new Button("Settings");
        settingsBtn.setOnAction(e -> switchToSettings());

        buttonBox.getChildren().addAll(newDocBtn, openBtn, aboutBtn, settingsBtn);
        buttonBox.setAlignment(Pos.CENTER);

        welcomePane.getChildren().addAll(header, searchBox, documentsTable, buttonBox);
    }

    private TableView<DocumentInfo> createDocumentsTable() {
        TableView<DocumentInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(300);

        TableColumn<DocumentInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<DocumentInfo, String> pathCol = new TableColumn<>("Location");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));

        TableColumn<DocumentInfo, String> modifiedCol = new TableColumn<>("Last Modified");
        modifiedCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));

        table.getColumns().addAll(nameCol, pathCol, modifiedCol);
        table.setItems(filteredDocuments);

        // Double click to open
        table.setRowFactory(tv -> {
            TableRow<DocumentInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    DocumentInfo doc = row.getItem();
                    File file = new File(doc.getPath());
                    if (file.exists()) {
                        openFile(file);
                        switchToEditor();
                    } else {
                        showAlert("File Not Found", "The file could not be found at the specified location.");
                        recentDocuments.remove(doc);
                    }
                }
            });
            return row;
        });

        return table;
    }

    private void applyTimeFilter(String filter) {
        LocalDateTime now = LocalDateTime.now();

        filteredDocuments.setPredicate(doc -> {
            if (filter.equals("All")) {
                return true;
            }

            try {
                File file = new File(doc.getPath());
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                LocalDateTime fileDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()),
                        ZoneId.systemDefault());

                if (filter.equals("Today")) {
                    return fileDate.toLocalDate().equals(now.toLocalDate());
                } else if (filter.equals("This Week")) {
                    return fileDate.isAfter(now.minusWeeks(1));
                } else if (filter.equals("This Month")) {
                    return fileDate.isAfter(now.minusMonths(1));
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        });
    }

    private void switchToAbout() {
        if (rootPane.getChildren().contains(welcomePane)) {
            rootPane.getChildren().remove(welcomePane);
        }
        if (rootPane.getChildren().contains(editorPane)) {
            rootPane.getChildren().remove(editorPane);
        }
        if (rootPane.getChildren().contains(settingsPane)) {
            rootPane.getChildren().remove(settingsPane);
        }
        rootPane.getChildren().add(aboutPane);
    }

    private void switchToSettings() {
        if (rootPane.getChildren().contains(welcomePane)) {
            rootPane.getChildren().remove(welcomePane);
        }
        if (rootPane.getChildren().contains(editorPane)) {
            rootPane.getChildren().remove(editorPane);
        }
        if (rootPane.getChildren().contains(aboutPane)) {
            rootPane.getChildren().remove(aboutPane);
        }
        rootPane.getChildren().add(settingsPane);
    }

    private void setupEditorUI() {
        createMenuBar();
        ToolBar toolbar = createToolBar();

        editorPane.setTop(new VBox(menuBar, toolbar));
        editorPane.setCenter(tabPane);
    }

    private void setupFileChooser() {
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Java Files", "*.java"),
                new FileChooser.ExtensionFilter("HTML Files", "*.html", "*.htm"),
                new FileChooser.ExtensionFilter("CSS Files", "*.css"),
                new FileChooser.ExtensionFilter("JavaScript Files", "*.js"),
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("Markdown Files", "*.md"),
                new FileChooser.ExtensionFilter("RTF Files", "*.rtf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
    }

    private void createMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newItem.setOnAction(e -> createNewTab());

        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(e -> openFile());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(e -> saveFile());

        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S,
                KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        saveAsItem.setOnAction(e -> saveFileAs());

        MenuItem closeItem = new MenuItem("Close Tab");
        closeItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
        closeItem.setOnAction(e -> closeCurrentTab());

        MenuItem homeItem = new MenuItem("Home");
        homeItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
        homeItem.setOnAction(e -> switchToWelcome());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            saveRecentFiles();
            saveSettings();
            System.exit(0);
        });

        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem,
                new SeparatorMenuItem(), closeItem, homeItem,
                new SeparatorMenuItem(), exitItem);

        Menu editMenu = new Menu("Edit");
        MenuItem findItem = new MenuItem("Find and Replace...");
        findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        findItem.setOnAction(e -> showAdvancedFindReplaceDialog());

        editMenu.getItems().addAll(findItem);

        Menu formatMenu = new Menu("Format");
        MenuItem fontItem = new MenuItem("Font...");
        fontItem.setOnAction(e -> showFontDialog());

        formatMenu.getItems().addAll(fontItem);

        Menu viewMenu = new Menu("View");
        CheckMenuItem darkModeItem = new CheckMenuItem("Dark Mode");
        darkModeItem.setSelected(darkMode);
        darkModeItem.setOnAction(e -> {
            darkMode = darkModeItem.isSelected();
            applyCurrentTheme(rootPane.getScene());
        });
        viewMenu.getItems().addAll(darkModeItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> switchToAbout());

        helpMenu.getItems().addAll(aboutItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, formatMenu, viewMenu, helpMenu);
    }

    private ToolBar createToolBar() {
        ToolBar toolbar = new ToolBar();

        ComboBox<String> fontFamilyCombo = new ComboBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontFamilyCombo.setValue("Segoe UI");
        fontFamilyCombo.setPrefWidth(150);

        ComboBox<String> fontSizeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"));
        fontSizeCombo.setValue("12");

        Button boldBtn = new Button("B");
        boldBtn.setStyle("-fx-font-weight: bold;");
        Button italicBtn = new Button("I");
        italicBtn.setStyle("-fx-font-style: italic;");
        Button underlineBtn = new Button("U");
        underlineBtn.setStyle("-fx-underline: true;");

        ColorPicker textColorPicker = new ColorPicker(Color.BLACK);
        ColorPicker bgColorPicker = new ColorPicker(Color.WHITE);

        Button homeBtn = new Button("Home");
        homeBtn.setOnAction(e -> switchToWelcome());

        toolbar.getItems().addAll(
                homeBtn, new Separator(),
                new Label("Font:"), fontFamilyCombo,
                new Label("Size:"), fontSizeCombo,
                boldBtn, italicBtn, underlineBtn,
                new Label("Text:"), textColorPicker,
                new Label("BG:"), bgColorPicker
        );

        fontFamilyCombo.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                textArea.setFont(Font.font(fontFamilyCombo.getValue(), textArea.getFont().getSize()));
            }
        });

        fontSizeCombo.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                textArea.setFont(Font.font(textArea.getFont().getFamily(),
                        Double.parseDouble(fontSizeCombo.getValue())));
            }
        });

        boldBtn.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                Font currentFont = textArea.getFont();
                boolean isBold = currentFont.getStyle().toLowerCase().contains("bold");
                textArea.setFont(Font.font(
                        currentFont.getFamily(),
                        isBold ? FontWeight.NORMAL : FontWeight.BOLD,
                        currentFont.getSize()
                ));
            }
        });

        italicBtn.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                Font currentFont = textArea.getFont();
                boolean isItalic = currentFont.getStyle().toLowerCase().contains("italic");
                textArea.setFont(Font.font(
                        currentFont.getFamily(),
                        currentFont.getStyle().toLowerCase().contains("bold") ? FontWeight.BOLD : FontWeight.NORMAL,
                        isItalic ? FontPosture.REGULAR : FontPosture.ITALIC,
                        currentFont.getSize()
                ));
            }
        });

        underlineBtn.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                if (textArea.getStyle().contains("-fx-underline: true")) {
                    textArea.setStyle(textArea.getStyle().replace("-fx-underline: true", ""));
                } else {
                    textArea.setStyle(textArea.getStyle() + "-fx-underline: true;");
                }
            }
        });

        textColorPicker.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                textArea.setStyle(textArea.getStyle() + "-fx-text-fill: " + toHexString(textColorPicker.getValue()) + ";");
            }
        });

        bgColorPicker.setOnAction(e -> {
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                textArea.setStyle(textArea.getStyle() + "-fx-control-inner-background: " + toHexString(bgColorPicker.getValue()) + ";");
            }
        });

        return toolbar;
    }

    private void switchToEditor() {
        if (rootPane.getChildren().contains(welcomePane)) {
            rootPane.getChildren().remove(welcomePane);
        }
        if (rootPane.getChildren().contains(aboutPane)) {
            rootPane.getChildren().remove(aboutPane);
        }
        if (rootPane.getChildren().contains(settingsPane)) {
            rootPane.getChildren().remove(settingsPane);
        }
        rootPane.getChildren().add(editorPane);
    }

    private void switchToWelcome() {
        if (rootPane.getChildren().contains(editorPane)) {
            rootPane.getChildren().remove(editorPane);
        }
        if (rootPane.getChildren().contains(aboutPane)) {
            rootPane.getChildren().remove(aboutPane);
        }
        if (rootPane.getChildren().contains(settingsPane)) {
            rootPane.getChildren().remove(settingsPane);
        }
        rootPane.getChildren().add(welcomePane);
    }

    private void createNewTab() {
        TextArea textArea = new TextArea();
        textArea.setFont(Font.font("Segoe UI", 12));

        Tab tab = new Tab("Untitled " + untitledCount++, textArea);
        tab.setUserData(null);

        tab.setOnCloseRequest(e -> {
            if (!promptToSave(tab)) {
                e.consume();
            }
        });

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        textArea.requestFocus();
    }

    private void openFile() {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));

            for (Tab tab : tabPane.getTabs()) {
                if (file.equals(tab.getUserData())) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            TextArea textArea = new TextArea(content);
            textArea.setFont(Font.font("Segoe UI", 12));

            Tab tab = new Tab(file.getName(), textArea);
            tab.setUserData(file);

            tab.setOnCloseRequest(e -> {
                if (!promptToSave(tab)) {
                    e.consume();
                }
            });

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            textArea.requestFocus();

            addToRecentDocuments(file);
        } catch (IOException e) {
            showAlert("Error", "Could not open file: " + e.getMessage());
        }
    }

    private void saveFile() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            File file = (File) currentTab.getUserData();
            if (file != null) {
                saveToFile(currentTab, file);
            } else {
                saveFileAs();
            }
        }
    }

    private void saveFileAs() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                saveToFile(currentTab, file);
                currentTab.setUserData(file);
                currentTab.setText(file.getName());
                addToRecentDocuments(file);
            }
        }
    }

    private void saveToFile(Tab tab, File file) {
        try {
            TextArea textArea = (TextArea) tab.getContent();
            Files.write(file.toPath(), textArea.getText().getBytes());
        } catch (IOException e) {
            showAlert("Error", "Could not save file: " + e.getMessage());
        }
    }

    private boolean promptToSave(Tab tab) {
        TextArea textArea = (TextArea) tab.getContent();
        if (textArea.getText().isEmpty()) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Changes");
        alert.setHeaderText("Save changes to " + tab.getText() + "?");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == saveButton) {
            File file = (File) tab.getUserData();
            if (file != null) {
                saveToFile(tab, file);
                return true;
            } else {
                File saveFile = fileChooser.showSaveDialog(null);
                if (saveFile != null) {
                    saveToFile(tab, saveFile);
                    tab.setUserData(saveFile);
                    tab.setText(saveFile.getName());
                    return true;
                }
                return false;
            }
        } else if (result.isPresent() && result.get() == dontSaveButton) {
            return true;
        } else {
            return false;
        }
    }

    private void closeCurrentTab() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            if (promptToSave(currentTab)) {
                tabPane.getTabs().remove(currentTab);
                if (tabPane.getTabs().isEmpty()) {
                    switchToWelcome();
                }
            }
        }
    }

    private void showAdvancedFindReplaceDialog() {
        TextArea currentTextArea = getCurrentTextArea();
        if (currentTextArea == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Find and Replace");
        dialog.setHeaderText("Search and modify text");

        TabPane findReplaceTabs = new TabPane();

        Tab findTab = new Tab("Find");
        GridPane findGrid = new GridPane();
        findGrid.setHgap(10);
        findGrid.setVgap(10);
        findGrid.setPadding(new Insets(10));

        TextField findField = new TextField();
        CheckBox matchCase = new CheckBox("Match case");
        CheckBox wholeWord = new CheckBox("Whole word only");
        Button findNextBtn = new Button("Find Next");

        findGrid.add(new Label("Find:"), 0, 0);
        findGrid.add(findField, 1, 0);
        findGrid.add(matchCase, 1, 1);
        findGrid.add(wholeWord, 1, 2);
        findGrid.add(findNextBtn, 1, 3);

        findTab.setContent(findGrid);

        Tab replaceTab = new Tab("Replace");
        GridPane replaceGrid = new GridPane();
        replaceGrid.setHgap(10);
        replaceGrid.setVgap(10);
        replaceGrid.setPadding(new Insets(10));

        TextField replaceFindField = new TextField();
        TextField replaceWithField = new TextField();
        CheckBox replaceMatchCase = new CheckBox("Match case");
        Button replaceBtn = new Button("Replace");
        Button replaceAllBtn = new Button("Replace All");

        replaceGrid.add(new Label("Find:"), 0, 0);
        replaceGrid.add(replaceFindField, 1, 0);
        replaceGrid.add(new Label("Replace with:"), 0, 1);
        replaceGrid.add(replaceWithField, 1, 1);
        replaceGrid.add(replaceMatchCase, 1, 2);
        replaceGrid.add(replaceBtn, 0, 3);
        replaceGrid.add(replaceAllBtn, 1, 3);

        replaceTab.setContent(replaceGrid);

        findReplaceTabs.getTabs().addAll(findTab, replaceTab);
        dialog.getDialogPane().setContent(findReplaceTabs);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        findNextBtn.setOnAction(e -> {
            String textToFind = findField.getText();
            if (!textToFind.isEmpty()) {
                findText(currentTextArea, textToFind,
                        matchCase.isSelected(), wholeWord.isSelected());
            }
        });

        replaceBtn.setOnAction(e -> {
            String textToFind = replaceFindField.getText();
            String replacement = replaceWithField.getText();
            if (!textToFind.isEmpty()) {
                replaceText(currentTextArea, textToFind, replacement,
                        replaceMatchCase.isSelected());
            }
        });

        replaceAllBtn.setOnAction(e -> {
            String textToFind = replaceFindField.getText();
            String replacement = replaceWithField.getText();
            if (!textToFind.isEmpty()) {
                replaceAllText(currentTextArea, textToFind, replacement,
                        replaceMatchCase.isSelected());
            }
        });

        dialog.showAndWait();
    }

    private void findText(TextArea textArea, String textToFind, boolean matchCase, boolean wholeWord) {
        String content = matchCase ? textArea.getText() : textArea.getText().toLowerCase();
        String searchText = matchCase ? textToFind : textToFind.toLowerCase();

        if (wholeWord) {
            searchText = "\\b" + searchText + "\\b";
        }

        int index = content.indexOf(searchText);
        if (index >= 0) {
            textArea.selectRange(index, index + textToFind.length());
        } else {
            showAlert("Not Found", "Text not found.");
        }
    }

    private void replaceText(TextArea textArea, String textToFind, String replacement, boolean matchCase) {
        String selectedText = textArea.getSelectedText();
        if (selectedText != null
                && ((matchCase && selectedText.equals(textToFind))
                || (!matchCase && selectedText.equalsIgnoreCase(textToFind)))) {
            textArea.replaceSelection(replacement);
        } else {
            findText(textArea, textToFind, matchCase, false);
        }
    }

    private void replaceAllText(TextArea textArea, String textToFind, String replacement, boolean matchCase) {
        String content = textArea.getText();
        if (matchCase) {
            textArea.setText(content.replace(textToFind, replacement));
        } else {
            textArea.setText(content.replaceAll("(?i)" + textToFind, replacement));
        }
    }

    private void showFontDialog() {
        TextArea currentTextArea = getCurrentTextArea();
        if (currentTextArea == null) {
            return;
        }

        Font currentFont = currentTextArea.getFont();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Font Settings");

        ComboBox<String> fontFamilyCombo = new ComboBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontFamilyCombo.setValue(currentFont.getFamily());

        ComboBox<Double> fontSizeCombo = new ComboBox<>(FXCollections.observableArrayList(
                8.0, 9.0, 10.0, 11.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0, 26.0, 28.0, 36.0, 48.0, 72.0
        ));
        fontSizeCombo.setValue(currentFont.getSize());

        CheckBox boldCheck = new CheckBox("Bold");
        boldCheck.setSelected(currentFont.getStyle().toLowerCase().contains("bold"));

        CheckBox italicCheck = new CheckBox("Italic");
        italicCheck.setSelected(currentFont.getStyle().toLowerCase().contains("italic"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Font:"), 0, 0);
        grid.add(fontFamilyCombo, 1, 0);
        grid.add(new Label("Size:"), 0, 1);
        grid.add(fontSizeCombo, 1, 1);
        grid.add(new Label("Style:"), 0, 2);
        grid.add(boldCheck, 1, 2);
        grid.add(italicCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButton) {
            FontWeight weight = boldCheck.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = italicCheck.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;

            currentTextArea.setFont(Font.font(
                    fontFamilyCombo.getValue(),
                    weight,
                    posture,
                    fontSizeCombo.getValue()
            ));
        }
    }

    private TextArea getCurrentTextArea() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null && currentTab.getContent() instanceof TextArea) {
            return (TextArea) currentTab.getContent();
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addToRecentDocuments(File file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            String lastModified = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()),
                    ZoneId.systemDefault()).format(formatter);

            DocumentInfo doc = new DocumentInfo(
                    file.getName(),
                    file.getAbsolutePath(),
                    lastModified);

            recentDocuments.removeIf(d -> d.getPath().equals(doc.getPath()));
            recentDocuments.add(0, doc);

            if (recentDocuments.size() > 20) {
                recentDocuments.remove(recentDocuments.size() - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public static class DocumentInfo implements Serializable {

        private final String fileName;
        private final String path;
        private final String lastModified;

        public DocumentInfo(String fileName, String path, String lastModified) {
            this.fileName = fileName;
            this.path = path;
            this.lastModified = lastModified;
        }

        public String getFileName() {
            return fileName;
        }

        public String getPath() {
            return path;
        }

        public String getLastModified() {
            return lastModified;
        }
    }
}
