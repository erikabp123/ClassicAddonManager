package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.AddonManagement.AddonRequest;
import com.CAM.AddonManagement.UpdateProgressListener;
import com.CAM.DataCollection.*;
import com.CAM.DataCollection.Github.GitHubAPI;
import com.CAM.DataCollection.Tukui.TukuiAPISearcher;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseForgeAPISearcher;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseForgeScraper;
import com.CAM.DataCollection.TwitchOwned.TwitchSite;
import com.CAM.DataCollection.TwitchOwned.WowAce.WowAceScraper;
import com.CAM.DataCollection.WowInterface.WowInterfaceAPISearcher;
import com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse.WowInterfaceAddonResponse;
import com.CAM.HelperTools.CompressionUtil;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.IO.DownloadListener;
import com.CAM.HelperTools.Logging.Log;
import com.CAM.HelperTools.TableViewStatus;
import com.CAM.HelperTools.UrlInfo;
import com.CAM.Settings.AddonNameCache;
import com.CAM.Settings.Preferences;
import com.CAM.Settings.SessionOnlySettings;
import com.CAM.Starter;
import com.CAM.Updating.SelfUpdater;
import com.CAM.Updating.VersionInfo;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    //================================================================================
    // General - Fields
    //================================================================================
    private static Controller controller;
    final ObservableList<Addon> listItems = FXCollections.observableArrayList();
    final ObservableList<Addon> shownItems = FXCollections.observableArrayList();
    private AddonManagerControl addonManagerControl;
    private final AtomicReference<String> lastSearchQuery = new AtomicReference<>("");
    private final AtomicReference<Boolean> lastSearchQueryCheckbox = new AtomicReference<>(false);
    private final AtomicReference<HashMap<Addon, TableViewStatus>> updateTableViewMap = new AtomicReference<>(null);
    private final AtomicLong updateTableViewMapTimeStamp = new AtomicLong(0);
    private final ObservableList<SearchedAddonRequest> addSearchResults = FXCollections.observableArrayList();

    //================================================================================
    // FXML - Fields
    //================================================================================
    //region General UI
    @FXML
    public VBox vboxMainUI;
    //endregion

    //region Menus
    @FXML
    public MenuBar menuBar;

    @FXML
    private MenuItem menuAboutVersion;

    @FXML
    public MenuItem menuToggleGithub;

    @FXML
    public MenuItem menuAboutUpdates;

    @FXML
    public MenuItem menuPreferences;
    //endregion

    //region Text
    @FXML
    private Text textManagedLabel;

    @FXML
    private Label gameVersionLabel;

    @FXML
    private Tab managedTab;

    @FXML
    public Label textOutputLogLabel;

    @FXML
    public Text updatingVersionLabel;
    //endregion

    //region Buttons
    @FXML
    private Button buttonAdd;

    @FXML
    private Button buttonUpdate;

    @FXML
    private Button buttonRemove;

    @FXML
    public Button buttonEdit;

    @FXML
    public Button buttonRefresh;

    @FXML
    public Button buttonClearSelection;

    @FXML
    public Button buttonSearchAdd;
    //endregion

    //region Checkboxes
    @FXML
    private CheckBox checkboxReleases;

    @FXML
    private CheckBox checkboxGameVersionSearch;

    //endregion

    //region ChoiceBoxes

    @FXML
    private ChoiceBox managedVersionChoiceBox;

    //endregion

    @FXML
    private ImageView updateAllSpinner;

    //region ListViews
    @FXML
    private ListView<String> listViewAddons;
    //endregion

    @FXML
    private TableView installedAddonsTableView;

    @FXML
    private TableView searchedTableView;

    @FXML
    private TableColumn<Addon, Addon> managedTableColumnSource;

    @FXML
    private TableColumn<Addon, String> managedTableColumnAddon;

    @FXML
    private TableColumn<Addon, String> managedTableColumnAuthor;

    @FXML
    private TableColumn<Addon, Addon> managedTableColumnUpdated;

    @FXML
    private TableColumn<Addon, Addon> managedTableColumnStatus;

    @FXML
    private TableColumn<Addon, String> managedTableColumnFlavor;

    @FXML
    private TableColumn<SearchedAddonRequest, SearchedAddonRequest> searchSourceTableColumn;

    @FXML
    private TableColumn<SearchedAddonRequest, String> searchAddonTableColumn;

    @FXML
    private TableColumn<SearchedAddonRequest, String> searchAuthorTableColumn;

    @FXML
    private TableColumn<SearchedAddonRequest, SearchedAddonRequest> searchGameVersionTableColumn;

    @FXML
    private TableColumn<SearchedAddonRequest, Double> searchRelevanceTableColumn;

    @FXML
    private TextArea selectedSearchedAddonTextArea;

    @FXML
    private TextField searchAllSourcesTextField;

    @FXML
    private Tab searchTab;

    @FXML
    private Tab debugTab;

    @FXML
    private TabPane categoriesTabPane;

    //region Text Input/Output
    @FXML
    private TextField textFieldURL;

    @FXML
    private TextArea textAreaOutputLog;

    @FXML
    private TextField filterAddonsTextField;

    @FXML
    private Text textConverting;

    @FXML
    private Text textConvertingProgress;
    //endregion

    //region ComboBoxes
    @FXML
    private ComboBox comboBoxSearch;
    //endregion

    //region Tabs
    @FXML
    private Tab tabSearch;

    @FXML
    private Tab tabManual;
    //endregion

    //region Loading Images
    @FXML
    private ImageView imageViewAdd;

    @FXML
    private ImageView imageViewAddSearch;

    @FXML
    public ImageView imageViewUpdate;
    //endregion

    //region choice boxes
    @FXML
    public ChoiceBox choiceBoxSource;
    //endregion

    //region Progress Bars
    @FXML
    public ProgressBar progressBarDownload;

    @FXML
    public ProgressBar progressBarUpdate;

    @FXML
    public ProgressBar progressBarUpdateTotal;
    //endregion

    //================================================================================
    // FXML - Functions
    //================================================================================
    //region General Functionality
    @FXML
    private void setupAction() {
        AddonManagerControl.selectInstallations((HashMap<GameVersion, AddonManager>) null);
    }

    @FXML
    private void addAction() {
        Thread precheckThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                imageViewAdd.setVisible(true);
            });

            try {
                String origin = textFieldURL.getText();

                UrlInfo urlInfo = UrlInfo.examineAddonUrl(origin);
                if (!urlInfo.isValid) {
                    throw new DataCollectionException(null, "URL does not point to a valid addon! Please double check the URL and try again!");
                }

                if (urlInfo.addonSource == AddonSource.GITHUB) {
                    handleGithubAdd(origin);
                } else {
                    throw new DataCollectionException(null, "Invalid website!");
                }
            } catch (DataCollectionException e) {
                handleAddScrapeException(e);
                cleanUpAfterAddAction();
            } catch (Exception e) {
                handleUnknownException(e);
                cleanUpAfterAddAction();
            }
        });
        precheckThread.start();
    }

    @FXML
    private void addSearchedAction() {
        Thread precheckThread = new Thread(() -> {
            Platform.runLater(this::disableAll);

            Object addon = searchedTableView.getSelectionModel().getSelectedItem();
            if(addon == null) {
                Platform.runLater(this::disableAll);
                return;
            }
            Class addonSource = addon.getClass();

            try {
                if (addonSource.equals(CurseAddonResponse.class)) {
                    CurseAddonResponse response = (CurseAddonResponse) addon;
                    checkIfProceedGameVersionSearch(response);
                }
                else if (addonSource.equals(TukuiAddonResponse.class)) {
                    startAddonAddSearchedThread((TukuiAddonResponse) addon);
                }
                else if (addonSource.equals(WowInterfaceAddonResponse.class)){
                    startAddonAddSearchedThread((WowInterfaceAddonResponse) addon);
                }

            } catch (DataCollectionException e) {
                handleAddScrapeException(e);
                cleanUpAfterAddAction();
            } catch (Exception e) {
                handleUnknownException(e);
                cleanUpAfterAddAction();
            }
        });
        precheckThread.start();
    }

    @FXML
    private void clearSearchSelectionAction() {
        buttonClearSelection.setVisible(false);
        buttonClearSelection.setDisable(true);
        buttonClearSelection.setFocusTraversable(false);
        comboBoxSearch.valueProperty().set(null);
        comboBoxSearch.getEditor().clear();
        comboBoxSearch.setDisable(false);
        comboBoxSearch.setEditable(true);
        comboBoxSearch.setFocusTraversable(true);
        choiceBoxSource.setDisable(false);
        choiceBoxSource.setFocusTraversable(true);
        choiceBoxSource.requestFocus();
        comboBoxSearch.requestFocus();
        buttonSearchAdd.setDisable(true);
        buttonSearchAdd.setFocusTraversable(false);
        comboBoxSearch.getStyleClass().remove("combo-search-disable");
        choiceBoxSource.getStyleClass().remove("combo-search-disable");
    }

    @FXML
    private void convertAddonsAction(){
        Thread updateAddonFormatThread = new Thread(this::updateManagedListToLatestFormat);
        updateAddonFormatThread.start();
    }

    private void searchEnterAction() {
        Thread searchThread = new Thread(() -> {
            try {
                String userQuery = searchAllSourcesTextField.getText();
                String lastQuery = lastSearchQuery.get();

                if (userQuery.length() < 1 || (userQuery.equals(lastQuery))) {
                    return;
                }

                searchAll(userQuery);

                boolean success = false;
                while (!success) {
                    success = lastSearchQuery.compareAndSet(lastSearchQuery.get(), userQuery);
                }
            } catch (DataCollectionException e) {
                handleUnknownException(e);
            }
        });
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void searchAll(String userQuery) throws DataCollectionException {
        CurseForgeAPISearcher curseForgeAPISearcher = new CurseForgeAPISearcher();
        TukuiAPISearcher tukuiAPISearcher = new TukuiAPISearcher(getAddonManager().getGameVersion());
        WowInterfaceAPISearcher wowInterfaceAPISearcher = new WowInterfaceAPISearcher();

        ArrayList<SearchedAddonRequest> results = curseForgeAPISearcher.search(userQuery);
        results.addAll(tukuiAPISearcher.search(userQuery));
        results.addAll(wowInterfaceAPISearcher.search(userQuery));

        Collections.sort(results);

        Set<String> names = results.stream()
                .map(SearchedAddonRequest::getName)
                .collect(Collectors.toSet());
        AddonNameCache.getInstance().addAddonNamesToCache(names);

        Platform.runLater(() -> {
            addSearchResults.setAll(results);
            searchedTableView.setItems(addSearchResults);
        });
    }

    private void tukuiSearch(String userQuery) throws DataCollectionException {
        TukuiAPISearcher apiSearcher = new TukuiAPISearcher(getAddonManager().getGameVersion());
        ArrayList<SearchedAddonRequest> results = apiSearcher.search(userQuery);
        ObservableList<SearchedAddonRequest> observableList = FXCollections.observableList(results);
        Platform.runLater(() -> {
            comboBoxSearch.show();
            comboBoxSearch.setItems(observableList);
        });
    }

    private void curseSearch(String userQuery) throws DataCollectionException {
        CurseForgeAPISearcher apiSearcher = new CurseForgeAPISearcher();
        ArrayList<SearchedAddonRequest> results = apiSearcher.search(userQuery);
        ArrayList<SearchedAddonRequest> gameVersionResults = new ArrayList<>();
        for(SearchedAddonRequest response : results){
            CurseAddonResponse castResponse = (CurseAddonResponse) response;
            if(castResponse.isGameVersionSupported(getAddonManager().getGameVersion())){
                gameVersionResults.add(response);
            }
        }

        ObservableList<SearchedAddonRequest> observableList;
        observableList = checkboxGameVersionSearch.isSelected()
                ? FXCollections.observableList(gameVersionResults)
                : FXCollections.observableList(results);
        Platform.runLater(() -> {
            comboBoxSearch.show();
            comboBoxSearch.setItems(observableList);
        });
    }

    private void wowInterfaceSearch(String userQuery) throws DataCollectionException {
        WowInterfaceAPISearcher apiSearcher = new WowInterfaceAPISearcher();
        ArrayList<SearchedAddonRequest> results = apiSearcher.search(userQuery);
        ObservableList<SearchedAddonRequest> observableList = FXCollections.observableList(results);
        Platform.runLater(() -> {
            comboBoxSearch.show();
            comboBoxSearch.setItems(observableList);
        });
    }

    private void selectSearchedAddon(Object object) {
        comboBoxSearch.setEditable(false);
        comboBoxSearch.setValue(object);
        comboBoxSearch.setDisable(true);
        comboBoxSearch.setFocusTraversable(false);
        buttonClearSelection.setDisable(false);
        buttonClearSelection.setVisible(true);
        buttonClearSelection.setFocusTraversable(true);
        buttonSearchAdd.setDisable(false);
        buttonSearchAdd.setFocusTraversable(true);
        comboBoxSearch.getStyleClass().add("combo-search-disable");
        choiceBoxSource.getStyleClass().add("combo-search-disable");
        choiceBoxSource.setDisable(true);
        choiceBoxSource.setFocusTraversable(false);
    }

    private void updateAllAction() {
        filterAddonsTextField.setText(null);
        Thread updateThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                buttonUpdate.setVisible(false);
                progressBarDownload.setVisible(true);
            });

            UpdateProgressListener progressListener = new GUIUpdateProgressListener(this);

            ArrayList<Exception> exceptions = getAddonManager().updateAddons(progressListener);
            for (Exception e : exceptions) {
                if (e.getClass() == DataCollectionException.class) {
                    ((DataCollectionException) e).getException().printStackTrace();
                    handleUpdateScrapeException((DataCollectionException) e);
                } else {
                    handleUnknownException(e);
                }
            }
            Platform.runLater(() -> {
                enableAll();
                buttonUpdate.setVisible(true);
                progressBarDownload.setVisible(false);
            });

        });
        updateThread.start();
    }

    @FXML
    private void updateAction() {
        filterAddonsTextField.setText(null);
        Platform.runLater(() -> {
            if(Preferences.getInstance().isScrollToBottomOnUpdate()){
                int lastIndex = installedAddonsTableView.getItems().size() - 1;
                installedAddonsTableView.scrollTo(lastIndex);
            }

            disableAll();
            searchTab.setDisable(true);
            debugTab.setDisable(true);
            updateAllSpinner.setImage(new Image(this.getClass().getClassLoader().getResource("processingAddon.gif").toExternalForm()));
            updateAllSpinner.setVisible(true);
            updateAllSpinner.setDisable(false);
        });
        Thread updateThread = new Thread(() -> {
            checkForAddonUpdates();
            for(Addon addon: updateTableViewMap.get().keySet()){
                updateTableViewMap.get().get(addon).setQueuedForUpdate(true);
            }
            getAddonManager().updateAllAddons(updateTableViewMap.get());

            Platform.runLater(() -> {
                installedAddonsTableView.refresh();
                enableAll();
                searchTab.setDisable(false);
                debugTab.setDisable(false);
                updateAllSpinner.setVisible(false);
                updateAllSpinner.setDisable(true);
            });

        });
        updateThread.start();
    }

    public void updateManagedListToLatestFormat(){
        try {
            Platform.runLater(() -> {
                    disableAll();
                    imageViewUpdate.setImage(new Image(this.getClass().getClassLoader().getResource("gears_load.gif").toExternalForm()));
                    imageViewUpdate.setVisible(true);
                    textConverting.setVisible(true);
                    textConverting.setDisable(false);
                    textConvertingProgress.setVisible(true);
                    textConvertingProgress.setDisable(false);
            });
            getAddonManager().updateToLatestFormat(progress -> Platform.runLater(() ->
                    textConvertingProgress.setText(progress + "/" + getAddonManager().getManagedAddons().size())));
        } catch (DataCollectionException e) {
            Log.printStackTrace(e);
        } finally {
            Platform.runLater(() -> {
                imageViewUpdate.setImage(null);
                textConverting.setVisible(false);
                textConverting.setDisable(true);
                textConvertingProgress.setVisible(false);
                textConvertingProgress.setDisable(true);
                imageViewUpdate.setVisible(false);
                enableAll();
            });

        }
    }

    @FXML
    private void removeAction() {
        Thread removeThread = new Thread(() -> {
            Platform.runLater(this::disableAll);
            Addon selectedAddon = getSelectedAddon();
            ArrayList<Addon> asList = new ArrayList<>();
            if(selectedAddon != null){
                getAddonManager().removeAddon(selectedAddon);
                asList.add(selectedAddon);
                searchedTableView.refresh();
            }
            Platform.runLater(() -> {
                removeFromTableView(asList);
                enableAll();
            });
        });
        removeThread.start();
    }

    @FXML
    public void editAction() {
        Addon selectedAddon = getSelectedAddon();
        if(selectedAddon == null) return;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("editAddon.fxml"));
        Parent parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            Log.printStackTrace(e);
        }
        EditAddonController dialogController = fxmlLoader.getController();
        dialogController.createDialog(selectedAddon);

        Scene scene = new Scene(parent);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("bootstrap3.css").toExternalForm());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("Edit Managed Addon");
        stage.getIcons().add(new Image(getClass().getClassLoader().getResource("program_icon.png").toExternalForm()));
        stage.showAndWait();
        if (dialogController.buttonPress == EditAddonController.BUTTON_SAVE) {
            getAddonManager().saveToFile();
        }
    }
    //endregion

    //region Toggleable Settings
    @FXML
    private void toggleDebugAction() {
        SessionOnlySettings.toggleLogging();
    }

    @FXML
    private void toggleGithubDownloadsAction() {
        SessionOnlySettings.toggleGithubDownloads();
    }

    @FXML
    private void toggleForceUpdateCheckingAction() {
        SessionOnlySettings.toggleForceUpdateChecking();
    }

    @FXML
    private void toggleForceReDownloadsAction() {
        SessionOnlySettings.toggleForceReDownloads();
    }

    @FXML
    public void preferencesAction() {
        Window prefWindow = new Window("editPreferences.fxml", "Edit Preferences");
        prefWindow.initDialog(null);
        prefWindow.showAndWait();
    }

    //endregion

    //region Import/Export
    @FXML
    private void exportAction() {
        Thread exportThread = new Thread(() -> {
            ArrayList<Addon> addons = new ArrayList<>();
            for (Addon addon : getAddonManager().getManagedAddons()) {
                addons.add(addon.export());
            }
            Gson gson = new Gson();
            try {
                String exportString = CompressionUtil.compress(gson.toJson(addons));

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Exported Addon List");
                    alert.setHeaderText("Send this to you friend!");
                    alert.setContentText(null);

                    SplitPane splitPane = new SplitPane();
                    alert.getDialogPane().setContent(splitPane);

                    TextArea textArea = new TextArea();
                    textArea.setText(exportString);

                    splitPane.setOrientation(Orientation.VERTICAL);
                    splitPane.getItems().addAll(textArea);

                    alert.showAndWait();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
                    exceptionAlert.setTitle("An error occurred");
                    exceptionAlert.setHeaderText("Couldn't convert your addons to a list");
                    exceptionAlert.showAndWait();
                });
            }
        });
        exportThread.start();
    }

    @FXML
    private void importAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Import Addon List");
        alert.setHeaderText("Paste Addon Export Here");
        alert.setContentText(null);

        TextArea textArea = new TextArea();
        alert.getDialogPane().setContent(textArea);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }

        Thread importThread = new Thread(() -> {
            Gson gson = new Gson();
            ArrayList<Addon> imported;
            try {
                imported = gson.fromJson(CompressionUtil.decompress(textArea.getText()), new TypeToken<ArrayList<Addon>>() {
                }.getType());
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alertIncorrect = new Alert(Alert.AlertType.INFORMATION);
                    alertIncorrect.setTitle("Invalid Import");
                    alertIncorrect.setHeaderText("Import String was invalid!");
                    alertIncorrect.setContentText("Double check the string and try again!");
                    alertIncorrect.showAndWait();
                });
                return;
            }
            getAddonManager().importAddonList(imported);
            ArrayList<Addon> finalImported = imported;
            Platform.runLater(() -> addToTableView(finalImported));
        });
        importThread.start();
    }
    //endregion

    //region Version and Updating
    @FXML
    public void checkForUpdatesAction() {
        Thread updateThread = new Thread(() -> {
            try {
                SelfUpdater.selfUpdate(Controller.controller);
            } catch (DataCollectionException e) {
                handleUnknownException(e);
            }
        });
        updateThread.start();
    }
    //endregion

    //region Redirection
    @FXML
    private void githubRedirectAction() {
        String github = "https://github.com/erikabp123/ClassicAddonManager";
        openUrl(github);
    }

    @FXML
    private void discordRedirectAction() {
        String discord = "https://discord.gg/StX3gbw";
        openUrl(discord);
    }

    @FXML
    private void patreonRedirectAction() {
        String patreon = "https://www.patreon.com/ClassicAddonManager";
        openUrl(patreon);
    }

    @FXML
    private void curseRedirectAction() {
        String curse = "https://www.curseforge.com/wow/addons?filter-game-version=1738749986%3A67408";
        openUrl(curse);
    }

    @FXML
    private void githubExploreRedirectAction() {
        String githubExplore = "https://github.com/search?q=classic+wow+addon";
        openUrl(githubExplore);
    }

    @FXML
    private void tukuiRedirectAction() {
        String tukui = "https://www.tukui.org/classic-addons.php";
        openUrl(tukui);
    }

    @FXML
    private void wowAceRedirectAction() {
        String wowAce = "https://www.wowace.com/addons?filter-game-version=1738749986%3A67408";
        openUrl(wowAce);
    }

    @FXML
    private void wowInterfaceRedirectAction() {
        String wowInterface = "https://www.wowinterface.com/downloads/index.php?cid=160";
        openUrl(wowInterface);
    }

    private void openUrl(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                Log.printStackTrace(e);
            }
        }
    }
    //endregion

    //================================================================================
    // General - Constructor
    //================================================================================
    public Controller() {
        controller = this;
    }

    //================================================================================
    // JavaFX - Setup
    //================================================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuAboutVersion.setText("Version " + VersionInfo.CAM_VERSION);
        Image addingImage = new Image(this.getClass().getClassLoader().getResource("adding.gif").toExternalForm());
        System.out.println(addingImage);
        imageViewAdd.setImage(addingImage);
        //imageViewAddSearch.setImage(addingImage);
        //listViewAddons.setCellFactory(param -> new AddonListCell<>());
        setFilterList();
        installedAddonsTableView.setPlaceholder(new Label("No addons installed"));
        searchAllSourcesTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                Thread searchThread = new Thread(() -> {
                    try {
                        String userQuery = searchAllSourcesTextField.getText();
                        searchAll(userQuery);
                    } catch (DataCollectionException e) {
                        handleAddSearchedScrapeException(e);
                    }
                });
                searchThread.setDaemon(true);
                searchThread.start();
            }
        });
        Log.listen(new GUILogListener(textAreaOutputLog));
        progressBarListen();
        setupOutputLogContextMenu();
        //setupSearchSourcesList();
        if (Starter.showWhatsNew) {
            showWhatsNew();
        }
        showEmergencyBroadcast();

    }

    private void setupSearchSourcesList() {
        ObservableList<AddonSource> sources = FXCollections.observableArrayList();
        for(AddonSource source: AddonSource.values()){
            if(source.isSearchable()) sources.add(source);
        }
        choiceBoxSource.setItems(sources);
        choiceBoxSource.getSelectionModel().select(0);
        choiceBoxSource.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    clearSearchSelectionAction();
                    comboBoxSearch.setItems(null);
                    resetLastSearchQuery();
                });
    }

    private void resetLastSearchQuery(){
        boolean success = false;
        while(!success){
            String prev = lastSearchQuery.get();
            success = lastSearchQuery.compareAndSet(prev, "");
        }
    }

    //================================================================================
    // Error Handling
    //================================================================================
    //region General
    public Alert createExceptionAlert(String title, String header, String content, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (e.getClass().equals(DataCollectionException.class)) {
            DataCollectionException exception = (DataCollectionException) e;
            if (exception.getAddon() != null) {
                pw.append("Addon: " + exception.getAddon().getName() + "\n");
                pw.append("Author: " + exception.getAddon().getAuthor() + "\n");
                pw.append("URL: " + exception.getAddon().getOrigin() + "\n");
                pw.append("Branch: " + exception.getAddon().getBranch());
                pw.append("Releases: " + exception.getAddon().isReleases());
                pw.append("Last Updated: " + exception.getAddon().getLastUpdated() + "\n");
                pw.append("Last File Name: " + exception.getAddon().getLastFileName() + "\n");
            }
            exception.getException().printStackTrace(pw);
        }
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Send this to the author:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        return alert;
    }

    private void showInvalidUrlAlert(DataCollectionException e) {
        Platform.runLater(() -> {
            Alert invalidAlert = new Alert(Alert.AlertType.ERROR);
            invalidAlert.setTitle("Invalid URL");
            invalidAlert.setHeaderText("URL does not point to an addon!");
            invalidAlert.setContentText(e.getMessage());
            invalidAlert.showAndWait();
        });
    }

    private void showGithubLimitAlert() {
        Platform.runLater(() -> {
            Alert limitAlert = new Alert(Alert.AlertType.ERROR);
            limitAlert.setTitle("Github Limit");
            limitAlert.setHeaderText("Github request limit hit!");
            limitAlert.setContentText("Github unfortunately has a 60 requests per hour limit for non-authenticated users!\n"
                    + "Please wait 1 hour for the limit to reset or login with your github account (not yet available in this version). \n"
                    + "You can toggle github downloads off under the 'File' menu if you wish to continue updating non-github addons.");
            limitAlert.showAndWait();
        });

    }

    private void showBadRequestAlert() {
        Platform.runLater(() -> {
            Alert badAlert = new Alert(Alert.AlertType.ERROR);
            badAlert.setTitle("400 - Bad request");
            badAlert.setHeaderText("Website responded with 400 - Bad Request!");
            badAlert.setContentText("Double check the provided URL is correct! It may not be the correct URL!");
            badAlert.showAndWait();
        });
    }

    private void showForbiddenRequestAlert() {
        Platform.runLater(() -> {
            Alert forbiddenAlert = new Alert(Alert.AlertType.ERROR);
            forbiddenAlert.setTitle("403 - Forbidden");
            forbiddenAlert.setHeaderText("Request was denied - 403!");
            forbiddenAlert.setContentText("The provided URL was blocked by the website (403 - forbidden). \n" +
                    "Please wait a little and try again later or skip this addon!");
            forbiddenAlert.showAndWait();
        });
    }
    //endregion

    //region Adding
    private void handleAddScrapeException(DataCollectionException e) {
        Log.printStackTrace(e);
        Platform.runLater(() -> {
            if (e.getType().equals(FailingHttpStatusCodeException.class)) {
                FailingHttpStatusCodeException exception = (FailingHttpStatusCodeException) e.getException();

                switch (exception.getStatusCode()) {
                    case 404:
                        e.setMessage("The URL did not point to an addon! \n" +
                                "This may be due to a typo, please double check the URL and try again!");
                        showInvalidUrlAlert(e);
                        return;
                    case 403:
                        if (e.getSource() == AddonSource.GITHUB) {
                            showGithubLimitAlert();
                        } else {
                            showForbiddenRequestAlert();
                        }
                        return;
                    case 400:
                        showBadRequestAlert();
                        return;
                    case 503:
                        e.setMessage("The URL provided responded with an internal server error! \n" +
                                "This is likely due to the website being down or experiencing issues. Try waiting 15-20 min!");
                        showInvalidUrlAlert(e);
                        return;
                    default:
                        handleUnknownException(e);
                        return;
                }
            }

            if (e.getType().equals(DataCollectionException.class)) {
                showInvalidUrlAlert(e);
            }
        });
    }

    private void handleAddSearchedScrapeException(DataCollectionException e) {
        Log.printStackTrace(e);
        Platform.runLater(() -> {
            if (e.getType().equals(FailingHttpStatusCodeException.class)) {
                FailingHttpStatusCodeException exception = (FailingHttpStatusCodeException) e.getException();

                switch (exception.getStatusCode()) {
                    case 503:
                        e.setMessage("The API responded with an internal server error! \n" +
                                "This is likely due to the API being down or experiencing issues. Try waiting 15-20 min!");
                        showInvalidUrlAlert(e);
                        return;
                    default:
                        handleUnknownException(e);
                        return;
                }
            }

            if (e.getType().equals(DataCollectionException.class)) {
                showInvalidUrlAlert(e);
            }
        });
    }
    //endregion

    //region Updates
    void handleUpdateScrapeException(DataCollectionException e) {
        Log.log("Classic Addon Manager encountered an issue and is stopping!");
        Log.printStackTrace(e);
        if (e.getType().equals(FailingHttpStatusCodeException.class)) {
            FailingHttpStatusCodeException exception = (FailingHttpStatusCodeException) e.getException();

            if (exception.getStatusCode() == 404) {
                Platform.runLater(() -> {
                    Alert invalidUrlAlert = new Alert(Alert.AlertType.ERROR);
                    invalidUrlAlert.setTitle("Url Is No Longer Valid!");
                    invalidUrlAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is no longer valid!");
                    invalidUrlAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a 404 (page not found)!\n" +
                            "Double check the URL; perhaps the addon has been moved?");
                    invalidUrlAlert.showAndWait();
                });
                return;
            }

            if (exception.getStatusCode() == 503) {
                Platform.runLater(() -> {
                    Alert invalidUrlAlert = new Alert(Alert.AlertType.ERROR);
                    invalidUrlAlert.setTitle("Website is encountering problems!");
                    invalidUrlAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is currently experiencing problems!");
                    invalidUrlAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a 503 (internal server error)!\n" +
                            "This is likely " + e.getAddon().getAddonSource() + " being down or having temporary problems. Try waiting sometime!");
                    invalidUrlAlert.showAndWait();
                });
                return;
            }

            if (e.getSource() == AddonSource.GITHUB) {
                if (exception.getStatusCode() == 403) {
                    showGithubLimitAlert();
                    return;
                }
            }

            //TODO: Add any other causes of FailingHttpStatusCodeExepection here
        }

        if (e.getType().equals(DataCollectionException.class)) {
            showInvalidUrlAlert(e);
        }

        if (e.getType().equals(NullPointerException.class)) {
            if (e.getSource() == AddonSource.GITHUB) {
                Platform.runLater(() -> {
                    Alert invalidUrlAlert = new Alert(Alert.AlertType.ERROR);
                    invalidUrlAlert.setTitle("Url Is No Longer Valid!");
                    invalidUrlAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is no longer valid!");
                    invalidUrlAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a problem!\n" +
                            "Double check the URL and Branch/Release; perhaps the addon has been moved or the branch/release has been changed?");
                    invalidUrlAlert.showAndWait();
                });
                return;
            }
            if (e.getSource() == AddonSource.TUKUI) {
                Platform.runLater(() -> {
                    Alert invalidUrlAlert = new Alert(Alert.AlertType.ERROR);
                    invalidUrlAlert.setTitle("Url Is No Longer Valid!");
                    invalidUrlAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is no longer valid!");
                    invalidUrlAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a problem!\n" +
                            "Double check the URL; perhaps the addon has been moved?");
                    invalidUrlAlert.showAndWait();
                });
                return;
            }
        }

        if (e.getType().equals(IndexOutOfBoundsException.class)) {
            if (e.getSource() == AddonSource.GITHUB) {
                e.setMessage("Could not find download link! Perhaps the addon has moved?");
                Platform.runLater(() -> {
                    Alert releasesUrlAlert = new Alert(Alert.AlertType.ERROR);
                    releasesUrlAlert.setTitle("Url Is No Longer Valid!");
                    releasesUrlAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is no longer valid!");
                    releasesUrlAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a problem!\n" +
                            "The addon does not appear to have any releases.");
                    releasesUrlAlert.showAndWait();
                });
                return;
            }
            if (e.getSource() == AddonSource.WOWINTERFACE) {
                Platform.runLater(() -> {
                    Alert invalidUrlAlert = new Alert(Alert.AlertType.ERROR);
                    invalidUrlAlert.setTitle("Url Is No Longer Valid!");
                    invalidUrlAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is no longer valid!");
                    invalidUrlAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a problem!\n" +
                            "Double check the URL; perhaps the addon has been moved?");
                    invalidUrlAlert.showAndWait();
                });
                return;
            }
        }

        if (e.getType().equals(MalformedURLException.class)) {
            Platform.runLater(() -> {
                Alert malformedAlert = new Alert(Alert.AlertType.ERROR);
                malformedAlert.setTitle("Invalid URL format!");
                malformedAlert.setHeaderText("The URL provided for " + e.getAddon().getName() + " is not a valid format URL!");
                malformedAlert.setContentText("While attempting to update " + e.getAddon().getName() + " the program encountered a problem!\n" +
                        "The URL provided for the addon is not in the correct format, it must start with http or https and be of the format 'https://www.WEBSITE.com/...'!");
                malformedAlert.showAndWait();
            });
        }

        handleUnknownException(e);
    }
    //endregion

    //region Unknown Errors
    private void handleUnknownException(Exception e) {
        Log.log("Classic Addon Manager encountered an issue and is stopping!");
        Log.printStackTrace(e);
        Platform.runLater(() -> {
            String title = "Exception Dialog";
            String header = "Something went wrong!";
            String content = "Classic Addon Manager encountered an unexpected error, please report this to the author on either Discord or Github (see the 'Help' menu).";
            Alert alert = createExceptionAlert(title, header, content, e);
            alert.showAndWait();
        });
    }
    //endregion

    //================================================================================
    // Adding addons
    //================================================================================
    private void startAddonAddThread(String branch) {
        Thread addAddonThread = new Thread(() -> {
            try {
                String origin = textFieldURL.getText();
                boolean releases = checkboxReleases.isSelected();

                AddonRequest request = new AddonRequest();
                request.origin = origin;
                request.branch = branch;
                request.releases = releases;

                if (!isValidRequest(request)) {
                    return;
                }

                Addon newAddon = getAddonManager().addNewAddon(request);
                ArrayList<Addon> asList = new ArrayList<>();
                if(newAddon != null){
                    asList.add(newAddon);
                }
                addToTableView(asList);
            } catch (DataCollectionException e) {
                handleAddScrapeException(e);
            } catch (Exception e) {
                handleUnknownException(e);
            } finally {
                cleanUpAfterAddAction();
            }
        });
        addAddonThread.start();
    }

    private void startAddonAddSearchedThread(SearchedAddonRequest request) {
        Thread addAddonThread = new Thread(() -> {
            try {
                Addon newAddon = getAddonManager().addNewSearchedAddon(request);
                searchedTableView.getSelectionModel().clearSelection();
                searchedTableView.refresh();
                ArrayList<Addon> asList = new ArrayList<>();
                if(newAddon != null){
                    asList.add(newAddon);
                }
                addToTableView(asList);
            } catch (DataCollectionException e) {
                handleAddSearchedScrapeException(e);
            } catch (Exception e) {
                handleUnknownException(e);
            } finally {
                cleanUpAfterAddSearchAction();
            }
        });
        addAddonThread.start();
    }



    private void checkIfProceedGameVersion(String origin) throws DataCollectionException {
        AddonSource addonSource = UrlInfo.getAddonSource(origin);
        String trimmedOrigin = UrlInfo.trimString(origin, addonSource);
        TwitchSite scraper = null;

        if (addonSource == AddonSource.CURSEFORGE) {
            scraper = CurseForgeScraper.getOfficialScraper(trimmedOrigin, false);
        } else if (addonSource == AddonSource.WOWACE) {
            scraper = WowAceScraper.getOfficialScraper(trimmedOrigin, false);
        }

        if (scraper.isGameVersionSupported()) {
            startAddonAddThread(null);
            return;
        }
        Scraper scraperConvert = (Scraper) scraper;

        String name = scraperConvert.getName();
        Platform.runLater(() -> {
            String gameVersionString = "\"" + addonManagerControl.getActiveManager().getGameVersion() + "\"";
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Non-" + gameVersionString + " addon");
            alert.setHeaderText("This addon does not seem to have an official " + gameVersionString + " release!");
            alert.setContentText(name + " does not seem to have an official " + gameVersionString + " release. " +
                    "Do you wish to add it anyway? This will result in downloading non-" + gameVersionString + " updates until proper ones are released.\n" +
                    "NOTE: There is no guarantee this addon will work with your installation!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                cleanUpAfterAddAction();
                return;
            }
            startAddonAddThread(null);
        });
    }

    private void checkIfProceedGameVersionSearch(CurseAddonResponse response) throws DataCollectionException {

        if (response.isGameVersionSupported(getAddonManager().getGameVersion())) {
            startAddonAddSearchedThread(response);
            return;
        }

        String name = response.name;
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Non-Classic addon");
            alert.setHeaderText("This addon does not seem to have an official classic release!");
            alert.setContentText(name + " does not seem to have an official classic release. " +
                    "Do you wish to add it anyway? This will result in downloading non-classic updates until classic ones are released.\n" +
                    "NOTE: There is no guarantee this addon will work with classic!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                cleanUpAfterAddSearchAction();
                return;
            }
            startAddonAddSearchedThread(response);
        });
    }

    private void handleGithubAdd(String origin) throws DataCollectionException {
        String trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
        if (checkboxReleases.isSelected()) {
            startAddonAddThread(null);
            return;
        }
        determineBranch(trimmedOrigin);
    }

    private void determineBranch(String origin) throws DataCollectionException {
        String trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
        ArrayList<String> names = GitHubAPI.getBranches(trimmedOrigin);
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.getItems().addAll(names);

            dialog.setTitle("Github Branch");
            dialog.setHeaderText("The addon you're adding has the following branches:");
            dialog.setContentText("Branch:");
            if (names.contains("master")) {
                dialog.setSelectedItem("master");
            } else {
                dialog.setSelectedItem(names.get(0));
            }

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                cleanUpAfterAddAction();
                return;
            }
            startAddonAddThread(result.get());
        });
    }

    private boolean isValidRequest(AddonRequest request) {
        if (request.origin.contains("curseforge.com") || request.origin.contains("wowinterface.com")
                || request.origin.contains("tukui.org") || request.origin.contains("wowace.com")) {
            return true;
        }
        if (request.releases) {
            return true;
        }
        return !request.branch.equals("");
    }

    //================================================================================
    // Updating Program
    //================================================================================
    public void checkForUpdate() {
        try {
            SelfUpdater.selfUpdate(this);
        } catch (DataCollectionException e) {
            Platform.runLater(() -> handleUnknownException(e));
        }
    }

    @FXML
    public void showWhatsNew() {
        Thread whatsNewThread = new Thread(() -> {
            String changeLog = getChangeLogAsString();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("What's New?");
                alert.setHeaderText("Changelog for the new update");
                alert.setContentText(null);

                TextArea textArea = new TextArea();
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setText(changeLog);
                textArea.setPrefWidth(500);
                textArea.setPrefHeight(300);
                alert.getDialogPane().setContent(textArea);
                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

                alert.showAndWait();
            });
        });

        whatsNewThread.start();
    }

    @FXML
    public void showEmergencyBroadcast() {
        Thread whatsNewThread = new Thread(() -> {
            String broadcast = null;
            try {
                broadcast = getBroadCastMessage();
            } catch (IOException e) {
                return;
            }

            if(broadcast == null || broadcast.isBlank()){
                return;
            }


            String[] messageParts = broadcast.split("messageId: ");

            Preferences preferences = Preferences.getInstance();

            int messageId = Integer.parseInt(messageParts[1]);
            boolean broadcastIgnored = preferences.isBroadcastIgnored(messageId);
            if(broadcastIgnored) return;

            String finalBroadcast = messageParts[0];

            Platform.runLater(() -> {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                // Need to force the alert to layout in order to grab the graphic,
                // as we are replacing the dialog pane with a custom pane
                alert.getDialogPane().applyCss();
                Node graphic = alert.getDialogPane().getGraphic();
                // Create a new dialog pane that has a checkbox instead of the hide/show details button
                // Use the supplied callback for the action of the checkbox
                alert.setDialogPane(new DialogPane() {
                    @Override
                    protected Node createDetailsButton() {
                        CheckBox optOut = new CheckBox();
                        optOut.setText("Don't show this message again");
                        optOut.setOnAction(e -> {
                            if(optOut.isSelected()) preferences.ignoreBroadcast(messageId);
                            else preferences.unIgnoreBroadcast(messageId);
                            Preferences.savePreferencesFile();
                        });
                        return optOut;
                    }
                });
                alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                // Fool the dialog into thinking there is some expandable content
                // a Group won't take up any space if it has no children
                alert.getDialogPane().setExpandableContent(new Group());
                alert.getDialogPane().setExpanded(true);
                // Reset the dialog graphic using the default style
                alert.getDialogPane().setGraphic(graphic);
                alert.setTitle("Status Update");
                alert.setHeaderText("IMPORTANT INFORMATION!");
                alert.setContentText(null);

                TextArea textArea = new TextArea();
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setText(finalBroadcast);
                textArea.setPrefWidth(500);
                textArea.setPrefHeight(300);
                alert.getDialogPane().setContent(textArea);
                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

                alert.showAndWait();
            });
        });

        whatsNewThread.start();
    }


    private String getBroadCastMessage() throws IOException {
        BroadcastFetcher fetcher = new BroadcastFetcher();
        return fetcher.fetchBroadcastMessage();
    }

    private String getChangeLogAsString() {
        String fileName = "system/CHANGELOG.txt";
        String line = null;
        StringBuilder sb = new StringBuilder();

        try {
            FileReader fileReader = new FileReader(fileName);

            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            bufferedReader.close();
        } catch (IOException e) {
            Log.printStackTrace(e);
        }

        return sb.toString();
    }

    //================================================================================
    // Output Log
    //================================================================================
    //region Output Log Functionality
    private void setupOutputLogContextMenu() {
        ContextMenu contextMenuOutputLog = new ContextMenu();
        MenuItem clearLog = new MenuItem("Clear log");
        MenuItem selectAll = new MenuItem("Select all");
        MenuItem copy = new MenuItem("Copy");
        MenuItem separator1 = new SeparatorMenuItem();
        MenuItem separator2 = new SeparatorMenuItem();

        clearLog.setOnAction(event -> textAreaOutputLog.clear());
        selectAll.setOnAction(event -> textAreaOutputLog.selectAll());
        copy.setOnAction(event -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String selected = textAreaOutputLog.getSelectedText();
            if (selected.equals("")) {
                return;
            }
            clipboard.setContents(new StringSelection(selected), null);
        });

        contextMenuOutputLog.getItems().addAll(selectAll, separator1, copy, separator2, clearLog);
        textAreaOutputLog.setContextMenu(contextMenuOutputLog);
    }
    //endregion

    //================================================================================
    // Visibility/Interaction Toggling
    //================================================================================
    //region Updating/Adding Visibility
    public void disableAll() {
        buttonRemove.setDisable(true);
        buttonAdd.setDisable(true);
        buttonUpdate.setDisable(true);
        buttonEdit.setDisable(true);
        buttonRefresh.setDisable(true);
        textFieldURL.setDisable(true);
        checkboxReleases.setDisable(true);
        filterAddonsTextField.setDisable(true);
        tabManual.setDisable(true);
        tabSearch.setDisable(true);
        managedVersionChoiceBox.setDisable(true);
    }

    public void enableAll() {
        buttonRemove.setDisable(false);
        buttonAdd.setDisable(false);
        buttonUpdate.setDisable(false);
        buttonEdit.setDisable(false);
        buttonRefresh.setDisable(false);
        textFieldURL.setDisable(false);
        checkboxReleases.setDisable(false);
        filterAddonsTextField.setDisable(false);
        tabManual.setDisable(false);
        tabSearch.setDisable(false);
        managedVersionChoiceBox.setDisable(false);
    }

    public void cleanUpAfterAddAction() {
        Platform.runLater(() -> {
            enableAll();
            imageViewAdd.setVisible(false);
        });
    }

    public void cleanUpAfterAddSearchAction() {
        Platform.runLater(() -> {
            enableAll();
        });
    }

    public void hideForUpdate() {
        disableAll();
        buttonUpdate.setDisable(true);
        buttonRemove.setDisable(true);
        buttonEdit.setDisable(true);
        buttonAdd.setDisable(true);
        textAreaOutputLog.setDisable(true);
        textFieldURL.setDisable(true);
        checkboxReleases.setDisable(true);
        menuBar.setDisable(true);
        installedAddonsTableView.setDisable(true);
        textManagedLabel.setDisable(true);
        textManagedLabel.setVisible(false);
        textOutputLogLabel.setDisable(true);
        textOutputLogLabel.setVisible(false);
        filterAddonsTextField.setDisable(true);

        updatingVersionLabel.setDisable(false);
        updatingVersionLabel.setVisible(true);

        categoriesTabPane.getSelectionModel().select(0);

        debugTab.setDisable(true);
        managedTab.setDisable(true);
        gameVersionLabel.setDisable(true);


        imageViewUpdate.setImage(new Image(this.getClass().getClassLoader().getResource("gears_load.gif").toExternalForm()));
        imageViewUpdate.setDisable(false);
        imageViewUpdate.setVisible(true);
        progressBarUpdate.setVisible(true);
        progressBarUpdate.setDisable(false);
        progressBarUpdateTotal.setVisible(true);
        progressBarUpdateTotal.setDisable(false);
    }
    //endregion

    public void refreshAction(){
        Platform.runLater(() -> {
            disableAll();
            searchTab.setDisable(true);
            debugTab.setDisable(true);
        });
        Thread refreshThread = new Thread(() -> {
            checkForAddonUpdates();
            Platform.runLater(() -> {
                enableAll();
                searchTab.setDisable(false);
                debugTab.setDisable(false);
            });
        });
        refreshThread.start();
    }

    public void checkForAddonUpdates(){
        Platform.runLater(() -> managedTableColumnStatus.setCellFactory(param -> {
            final ImageView imageview = new ImageView();
            imageview.setFitHeight(50);
            imageview.setFitWidth(50);
            TableCell<Addon, Addon> cell = new TableCell<>() {
                @Override
                public void updateItem(Addon item, boolean empty) {
                    if (item != null) {
                        Image loadingImage = new Image(this.getClass().getClassLoader().getResource("processingAddon.gif").toExternalForm());
                        imageview.setImage(loadingImage);
                    } else {
                        imageview.setImage(null);
                    }
                }
            };

            cell.setGraphic(imageview);
            return cell;
        }));

        if(System.currentTimeMillis() - updateTableViewMapTimeStamp.get() >= Preferences.getInstance().getMaxCacheDuration()){
            ArrayList<Exception> exceptions = new ArrayList<>();
            ArrayList<Addon> updatesAvailable = getAddonManager().checkForUpdates(exceptions);
            for (Exception e : exceptions) {
                if (e.getClass() == DataCollectionException.class) {
                    ((DataCollectionException) e).getException().printStackTrace();
                    handleUpdateScrapeException((DataCollectionException) e);
                } else {
                    handleUnknownException(e);
                }
            }
            //ArrayList<Addon> updatesAvailable = new ArrayList<Addon>();
            //updatesAvailable.add(getAddonManager().getManagedAddons().get(0));
            //updatesAvailable.add(getAddonManager().getManagedAddons().get(1));

            HashMap<Addon, TableViewStatus> updateHashmap = new HashMap<>();
            for(Addon addon: updatesAvailable){
                updateHashmap.put(addon, new TableViewStatus());
            }
            updateTableViewMap.set(updateHashmap);
            updateTableViewMapTimeStamp.set(System.currentTimeMillis());
        }

        Platform.runLater(() -> managedTableColumnStatus.setCellFactory(param -> {
            Button updateButton = new Button("Update");
            updateButton.setDisable(true);
            updateButton.setVisible(false);

            TableCell<Addon, Addon> cell = new UpdateAddonTableCell(updateTableViewMap);
            return cell;
        }));
    }

    public void setupTableView(){
        List<Addon> addons = getAddonManager().getManagedAddons();

        managedTableColumnAddon.setCellValueFactory(new PropertyValueFactory<>("name"));
        managedTableColumnAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        managedTableColumnFlavor.setCellValueFactory(new PropertyValueFactory<>("flavor"));

        managedTableColumnUpdated.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        managedTableColumnUpdated.setCellFactory(param -> {
            TableCell<Addon, Addon> cell = new TableCell<>() {
                @Override
                public void updateItem(Addon item, boolean empty) {
                    if (item == null) {
                        setText(null);
                        return;
                    }
                    if(item.getLastUpdated() == null) setText("Not installed!");
                    else setText(item.getLastUpdated().toString());
                }
            };
            return cell;
        });

        managedTableColumnSource.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        managedTableColumnSource.setCellFactory(param -> {
            final ImageView imageview = new ImageView();
            imageview.setFitHeight(75);
            imageview.setFitWidth(150);
            TableCell<Addon, Addon> cell = new TableCell<>() {
                @Override
                public void updateItem(Addon item, boolean empty) {
                    if (item != null) {
                        imageview.setImage(item.getWebsiteIcon());
                        setOnMouseEntered(event -> setCursor(Cursor.HAND));
                        setOnMouseExited(event -> setCursor(Cursor.DEFAULT));
                        setOnMouseClicked(event -> openUrl(item.getOrigin()));
                    } else {
                        imageview.setImage(null);
                    }
                }
            };

            cell.setGraphic(imageview);
            return cell;
        });

        managedTableColumnStatus.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        managedTableColumnStatus.setCellFactory(param -> {
            TableCell<Addon, Addon> cell = new TableCell<>() {
                @Override
                public void updateItem(Addon item, boolean empty) {
                    if (item != null) {
                        Date lastUpdateCheck = item.getLastUpdateCheck();
                        String text = "Not installed!";
                        if(lastUpdateCheck != null) text = "Not checked";
                        setText(text);
                    } else {
                        setText(null);
                    }
                }
            };
            return cell;
        });

        managedTableColumnSource.setComparator(Comparator.comparing(o -> o.getAddonSource().name()));
        managedTableColumnStatus.setComparator(Comparator.comparing(Addon::getLastUpdateCheck));
        managedTableColumnUpdated.setComparator((o1, o2) -> {
            if (o1.getLastUpdated() == null) return -1;
            if (o2.getLastUpdated() == null) return 1;
            return o1.getLastUpdated().compareTo(o2.getLastUpdated());
        });

        listItems.setAll(addons);
        shownItems.setAll(listItems);
        installedAddonsTableView.setItems(shownItems);
        updateListViewLabel();
        if (Preferences.getInstance().isCheckForUpdatesOnLaunch()) refreshAction();
    }

    public void setupSearchedAddonsTableView(){
    //TODO: Continue working here, add searched results to table view
        searchAddonTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        searchAuthorTableColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        searchRelevanceTableColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        searchSourceTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        searchSourceTableColumn.setCellFactory(param -> {
            final ImageView imageview = new ImageView();
            imageview.setFitHeight(75);
            imageview.setFitWidth(150);
            TableCell<SearchedAddonRequest, SearchedAddonRequest> cell = new TableCell<>() {
                @Override
                public void updateItem(SearchedAddonRequest item, boolean empty) {
                    if (item == null) {
                        imageview.setImage(null);
                        return;
                    }
                    imageview.setImage(item.getAddonSource().getWebsiteIcon());
                    setOnMouseEntered(event -> setCursor(Cursor.HAND));
                    setOnMouseExited(event -> setCursor(Cursor.DEFAULT));
                    setOnMouseClicked(event -> openUrl(item.getOrigin()));

                    for(Addon addon: getAddonManager().getManagedAddons()){
                        if(addon.getOrigin().equals(item.getOrigin())) {
                            getTableRow().setOpacity(0.5);
                            getTableRow().setDisable(true);
                            return;
                        }
                    }
                    getTableRow().setOpacity(1);
                    getTableRow().setDisable(false);
                }
            };

            cell.setGraphic(imageview);
            return cell;
        });

        searchGameVersionTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        searchGameVersionTableColumn.setCellFactory(param -> {
            TableCell<SearchedAddonRequest, SearchedAddonRequest> cell = new TableCell<>() {
                @Override
                public void updateItem(SearchedAddonRequest item, boolean empty) {
                    if (item != null) {
                        StringBuilder text = new StringBuilder();
                        for(String s: item.getSupportedPatches()) text.append(s + "\n");
                        if(text.toString().isBlank()) text.append("Unknown");
                        setText(text.toString());
                    } else {
                        setText(null);
                    }
                }
            };
            return cell;
        });

        searchSourceTableColumn.setComparator(Comparator.comparing(o -> o.getAddonSource().name()));

        searchAllSourcesTextField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                searchEnterAction();
            }
        });

        searchedTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            SearchedAddonRequest searchedAddonRequest = (SearchedAddonRequest) newValue;
            String text = "";
            if (searchedAddonRequest != null) text = searchedAddonRequest.getDescription();
            selectedSearchedAddonTextArea.setText(text);
        });

    }


    public void setupAutoCompletionListener() {
        final int paddingOffsets = 77;

        AddonNameCache addonNameCache = AddonNameCache.getInstance();
        AutoCompletion autoCompletion = AutoCompletion.getInstance();
        Binding binding = autoCompletion.addNewBinding(searchAllSourcesTextField);
        autoCompletion.updateSuggestions(searchAllSourcesTextField, addonNameCache.getCachedAddonNames());

        binding.getTextFieldBinding().setMinWidth(searchAllSourcesTextField.getPrefWidth());
        Scene scene = Stage.getWindows().stream().filter(javafx.stage.Window::isShowing).collect(Collectors.toList()).get(0).getScene();

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            binding.getTextFieldBinding().setMinWidth(newValue.doubleValue() - paddingOffsets); // checking width of textField is cleaner but for some reason doesnt work with window maximizing and shrinking
            binding.getTextFieldBinding().setMaxWidth(newValue.doubleValue() - paddingOffsets);
        });

        addonNameCache.addListener(observable -> {
            AddonNameCache updatedCache = (AddonNameCache) observable;
            autoCompletion.updateSuggestions(searchAllSourcesTextField, updatedCache.getCachedAddonNames());
        });
    }

    public void removeFromTableView(List<Addon> addons) {
        listItems.removeAll(addons);
        shownItems.removeAll(addons);
        installedAddonsTableView.setItems(shownItems);
        updateListViewLabel();
    }

    public void addToTableView(List<Addon> addons) {
        listItems.addAll(addons);
        shownItems.addAll(addons);
        Collections.sort(shownItems);
        installedAddonsTableView.setItems(shownItems);
        updateListViewLabel();
    }


    private void updateListViewLabel() {
        int managedCount = getAddonManager().getManagedAddons().size();
        String textSuffix = "addons";
        if (managedCount == 1) {
            textSuffix = "addon";
        }
        textManagedLabel.setText("Managing " + managedCount + " " + textSuffix);
    }

    private void progressBarListen() {
        DownloadListener downloadListenerUpdate = new GUIDownloadListener(progressBarUpdate, progressBarUpdateTotal);
        FileDownloader.listen(downloadListenerUpdate);
    }

    //region Filtering List View

    private void setFilterList() {
        shownItems.setAll(listItems);
        filterAddonsTextField.textProperty().addListener(obs -> {
            FilteredList<Addon> filteredData = new FilteredList<>(listItems, s -> true);
            String filter = filterAddonsTextField.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                filteredData.setPredicate(s -> s.getName().toLowerCase().contains(filter.toLowerCase()) || s.getAuthor().toLowerCase().contains(filter.toLowerCase()));
            }
            shownItems.setAll(filteredData);
            Addon selectedAddon = getSelectedAddon();
            if(selectedAddon == null || !shownItems.contains(selectedAddon)){
                installedAddonsTableView.getSelectionModel().clearSelection();
            }
            installedAddonsTableView.setItems(shownItems);
        });

        //listItems.addListener((ListChangeListener<Addon>) c -> filterAddonsTextField.setText(null));
    }

    private Addon getSelectedAddon(){
        return (Addon) installedAddonsTableView.getSelectionModel().getSelectedItem();
    }


    //endregion

    //================================================================================
    // Misc
    //================================================================================
    public AddonManager getAddonManager() {
        return addonManagerControl.getActiveManager();
    }

    public AddonManagerControl getAddonManagerControl() {
        return addonManagerControl;
    }

    public void setAddonManagerControl(AddonManagerControl addonManagerControl){
        this.addonManagerControl = addonManagerControl;
        updateManagedVersionChoiceBox();
        AddonManager activeManager = addonManagerControl.getActiveManager();
        if(activeManager != null ) managedVersionChoiceBox.getSelectionModel().select(activeManager.getGameVersion());
        else managedVersionChoiceBox.getSelectionModel().select(0);
    }

    public void updateSelectedManagedVersionChoiceBox(){
        AddonManager activeManager = addonManagerControl.getActiveManager();
        managedVersionChoiceBox.getSelectionModel().select(activeManager.getGameVersion());
    }

    public void updateManagedVersionChoiceBox(){
        ObservableList<GameVersion> managed = FXCollections.observableArrayList(addonManagerControl.getManagedGames());
        managedVersionChoiceBox.setItems(managed);
    }

    public void updateActiveManager(GameVersion gameVersion){
        Platform.runLater(() -> {
            addonManagerControl.setActiveManager(gameVersion);
            setupTableView();
        });

    }

    public void setupListeners(){
        managedVersionChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() == -1) {
                return;
            }
            GameVersion gameVersion = (GameVersion) managedVersionChoiceBox.getItems().get(newValue.intValue());
            Log.verbose("Changing managed version to " +  gameVersion);
            updateActiveManager(gameVersion);
            searchedTableView.refresh();
        });
    }

    public static Controller getInstance() {
        return controller;
    }



}
