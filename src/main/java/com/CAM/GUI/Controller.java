package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.AddonManagement.AddonRequest;
import com.CAM.AddonManagement.UpdateProgressListener;
import com.CAM.DataCollection.*;
import com.CAM.HelperTools.*;
import com.CAM.Settings.SessionOnlySettings;
import com.CAM.Starter;
import com.CAM.Updating.SelfUpdater;
import com.CAM.Updating.VersionInfo;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    //================================================================================
    // General - Fields
    //================================================================================
    private static Controller controller;
    final ObservableList<String> listItems = FXCollections.observableArrayList();
    private AddonManager addonManager;

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
    //endregion

    //region Text
    @FXML
    private Text textManagedLabel;

    @FXML
    public Text textOutputLogLabel;

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
    //endregion

    //region Checkboxes
    @FXML
    private CheckBox checkboxReleases;

    //endregion

    //region ListViews
    @FXML
    private ListView<String> listViewAddons;
    //endregion

    //region Text Input/Output
    @FXML
    private TextField textFieldURL;

    @FXML
    private TextArea textAreaOutputLog;

    @FXML
    private TextField filterAddonsTextField;
    //endregion

    //region Loading Images
    @FXML
    private ImageView imageViewAdd;

    @FXML
    public ImageView imageViewUpdate;
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
        UserInput userInput = new GUIUserInput("Please provide path to WoW Classic Installation");
        String installLocation = AddonManager.specifyInstallLocation(userInput);
        if (installLocation == null) {
            return;
        }
        addonManager.setInstallLocation(installLocation);
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
                    throw new ScrapeException(null, "URL does not point to a valid addon! Please double check the URL and try again!");
                }

                switch (urlInfo.addonSource) {
                    case CURSEFORGE:
                        checkIfProceedClassic(origin);
                        break;
                    case GITHUB:
                        handleGithubAdd(origin);
                        break;
                    case WOWINTERFACE:
                        startAddonAddThread(origin);
                        break;
                    case TUKUI:
                        startAddonAddThread(origin);
                        break;
                    case WOWACE:
                        checkIfProceedClassic(origin);
                        break;
                }
            } catch (ScrapeException e) {
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
    private void updateAction() {
        filterAddonsTextField.setText(null);
        Thread updateThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                buttonUpdate.setVisible(false);
                progressBarDownload.setVisible(true);
            });

            UpdateProgressListener progressListener = new GUIUpdateProgressListener(this);

            ArrayList<Exception> exceptions = addonManager.updateAddons(progressListener);
            for (Exception e : exceptions) {
                if (e.getClass() == ScrapeException.class) {
                    handleUpdateScrapeException((ScrapeException) e);
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
    private void removeAction() {
        Thread removeThread = new Thread(() -> {
            Platform.runLater(() -> disableAll());
            int selectedIndex = getNonFilteredIndex();
            if (selectedIndex != -1) {
                addonManager.removeAddon(selectedIndex);
            }
            Platform.runLater(() -> {
                updateListView();
                enableAll();
            });
        });
        removeThread.start();
    }

    @FXML
    public void editAction() {
        int selectedIndex = getNonFilteredIndex();
        if (selectedIndex == -1) {
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("editAddon.fxml"));
        Parent parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditAddonController dialogController = fxmlLoader.getController();
        Addon selectedAddon = addonManager.getManagedAddons().get(selectedIndex);
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
            addonManager.saveToFile();
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
    private void toggleForceUpdatesAction() {
        SessionOnlySettings.toggleForceUpdates();
    }
    //endregion

    //region Import/Export
    @FXML
    private void exportAction() {
        Thread exportThread = new Thread(() -> {
            ArrayList<Addon> addons = new ArrayList<>();
            for (Addon addon : addonManager.getManagedAddons()) {
                addons.add(addon.export());
            }
            Gson gson = new Gson();
            String exportString = gson.toJson(addons);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exported Addon List");
                alert.setHeaderText("Send this to you friend!");
                alert.setContentText(null);

                TextArea textArea = new TextArea();
                textArea.setText(exportString);
                alert.getDialogPane().setContent(textArea);

                alert.showAndWait();
            });
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
            ArrayList<Addon> imported = null;
            try {
                imported = gson.fromJson(textArea.getText(), new TypeToken<ArrayList<Addon>>() {
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
            addonManager.importAddonList(imported);
            Platform.runLater(() -> updateListView());
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
            } catch (ScrapeException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
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
        imageViewAdd.setImage(new Image(this.getClass().getClassLoader().getResource("adding.gif").toExternalForm()));
        listViewAddons.setCellFactory(param -> new AddonListCell<>());
        setFilterList();
        Log.listen(new GUILogListener(textAreaOutputLog));
        progressBarListen();
        setupOutputLogContextMenu();
        if(Starter.showWhatsNew){
            showWhatsNew();
        }
        showEmergencyBroadcast();
    }

    //================================================================================
    // Error Handling
    //================================================================================
    //region General
    private Alert createExceptionAlert(String title, String header, String content, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (e.getClass().equals(ScrapeException.class)) {
            ScrapeException exception = (ScrapeException) e;
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

    private void showInvalidUrlAlert(ScrapeException e) {
        Platform.runLater(() -> {
            Alert invalidAlert = new Alert(Alert.AlertType.ERROR);
            invalidAlert.setTitle("Invalid URL");
            invalidAlert.setHeaderText("URL does not point to an addon!");
            invalidAlert.setContentText(e.getMessage());
            invalidAlert.showAndWait();
        });
        return;
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
    private void handleAddScrapeException(ScrapeException e) {
        e.printStackTrace();
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
                        //TODO: Alert informing that request is bad, maybe addon moved to a new URL? (especially the case with wowinterface)
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

            if (e.getType().equals(ScrapeException.class)) {
                showInvalidUrlAlert(e);
                return;
            }
        });
    }
    //endregion

    //region Updates
    private void handleUpdateScrapeException(ScrapeException e) {
        Log.log("Classic Addon Manager encountered an issue and is stopping!");
        e.printStackTrace();
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

        if (e.getType().equals(ScrapeException.class)) {
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
        e.printStackTrace();
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
                    cleanUpAfterAddAction();
                    return;
                }

                addonManager.addNewAddon(request);
            } catch (ScrapeException e) {
                handleAddScrapeException(e);
            } catch (Exception e) {
                handleUnknownException(e);
            } finally {
                cleanUpAfterAddAction();
            }
        });
        addAddonThread.start();
    }

    private void checkIfProceedClassic(String origin) throws ScrapeException {
        AddonSource addonSource = UrlInfo.getAddonSource(origin);
        String trimmedOrigin = UrlInfo.trimString(origin, addonSource);
        TwitchSite scraper = null;

        if(addonSource == AddonSource.CURSEFORGE){
            scraper = CurseForgeScraper.getOfficialScraper(trimmedOrigin, false);
        } else if(addonSource == AddonSource.WOWACE){
            scraper = WowAceScraper.getOfficialScraper(trimmedOrigin, false);
        }

        if (scraper.isClassicSupported()) {
            startAddonAddThread(null);
            return;
        }
        Scraper scraperConvert = (Scraper) scraper;

        String name = scraperConvert.getName();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Non-Classic addon");
            alert.setHeaderText("This addon does not seem to have an official classic release!");
            alert.setContentText(name + " does not seem to have an official classic release. " +
                    "Do you wish to add it anyway? This will result in downloading non-classic updates until classic ones are released.\n" +
                    "NOTE: There is no guarantee this addon will work with classic!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                cleanUpAfterAddAction();
                return;
            }
            startAddonAddThread(null);
        });
    }

    private void handleGithubAdd(String origin) throws ScrapeException {
        String trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
        if (checkboxReleases.isSelected()) {
            startAddonAddThread(null);
            return;
        }
        determineBranch(trimmedOrigin);
    }

    private void determineBranch(String origin) throws ScrapeException {
        String trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
        ArrayList<String> names = GitHubScraper.getBranches(trimmedOrigin);
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
        if (!request.branch.equals("")) {
            return true;
        }
        return false;
    }

    //================================================================================
    // Updating Program
    //================================================================================
    public void checkForUpdate() {
        try {
            SelfUpdater.selfUpdate(this);
        } catch (ScrapeException e) {
            Platform.runLater(() -> handleUnknownException(e));
        }
    }

    @FXML
    public void showWhatsNew(){
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
    public void showEmergencyBroadcast(){
        Thread whatsNewThread = new Thread(() -> {
            String broadcast = null;
            try {
                broadcast = getBroadCastMessage();
            } catch (IOException e) {
                return;
            }

            String finalBroadcast = broadcast;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    private String getChangeLogAsString(){
        String fileName = "system/CHANGELOG.txt";
        String line = null;
        StringBuilder sb = new StringBuilder();

        try {
            FileReader fileReader = new FileReader(fileName);

            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        textFieldURL.setDisable(true);
        checkboxReleases.setDisable(true);
        filterAddonsTextField.setDisable(true);
    }

    public void enableAll() {
        buttonRemove.setDisable(false);
        buttonAdd.setDisable(false);
        buttonUpdate.setDisable(false);
        buttonEdit.setDisable(false);
        textFieldURL.setDisable(false);
        checkboxReleases.setDisable(false);
        filterAddonsTextField.setDisable(false);
    }

    public void cleanUpAfterAddAction() {
        Platform.runLater(() -> {
            updateListView();
            enableAll();
            imageViewAdd.setVisible(false);
        });
    }

    public void hideForUpdate() {
        buttonUpdate.setDisable(true);
        buttonRemove.setDisable(true);
        buttonEdit.setDisable(true);
        buttonAdd.setDisable(true);
        textAreaOutputLog.setDisable(true);
        textFieldURL.setDisable(true);
        checkboxReleases.setDisable(true);
        menuBar.setDisable(true);
        listViewAddons.setDisable(true);
        textManagedLabel.setDisable(true);
        textManagedLabel.setVisible(false);
        textOutputLogLabel.setDisable(true);
        textOutputLogLabel.setVisible(false);
        filterAddonsTextField.setDisable(true);

        updatingVersionLabel.setDisable(false);
        updatingVersionLabel.setVisible(true);


        imageViewUpdate.setImage(new Image(this.getClass().getClassLoader().getResource("gears_load.gif").toExternalForm()));
        imageViewUpdate.setDisable(false);
        progressBarUpdate.setVisible(true);
        progressBarUpdateTotal.setVisible(true);
    }
    //endregion

    public void updateListView() {
        ArrayList<String> addonNames = new ArrayList<>();
        for (Addon addon : addonManager.getManagedAddons()) {
            addonNames.add(addon.getName());
        }
        listItems.setAll(addonNames);
        updateListViewLabel();
    }

    private void updateListViewLabel() {
        int managedCount = addonManager.getManagedAddons().size();
        String textSuffix = "addons";
        if (managedCount == 1) {
            textSuffix = "addon";
        }
        textManagedLabel.setText("Managing " + managedCount + " " + textSuffix);
    }

    private void progressBarListen() {
        DownloadListener downloadListenerAddon = new GUIDownloadListener(progressBarDownload);
        FileDownloader.listen(downloadListenerAddon);

        DownloadListener downloadListenerUpdate = new GUIDownloadListener(progressBarUpdate, progressBarUpdateTotal);
        FileDownloader.listen(downloadListenerUpdate);
    }

    //region Filtering List View

    private void setFilterList(){
        listViewAddons.setItems(listItems);
        filterAddonsTextField.textProperty().addListener(obs->{
            FilteredList<String> filteredData = new FilteredList<>(listItems, s -> true);
            String filter = filterAddonsTextField.getText();
            if(filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            }
            else {
                filteredData.setPredicate(s -> s.toLowerCase().contains(filter.toLowerCase()));
            }
            listViewAddons.setItems(filteredData);
        });

        listItems.addListener((ListChangeListener<String>) c -> filterAddonsTextField.setText(null));
    }

    private int getNonFilteredIndex(){
        ObservableList<Integer> selected = listViewAddons.getSelectionModel().getSelectedIndices();
        if (selected.size() == 0) {
            return -1;
        }
        int selectedIndex = selected.get(0);
        // Not filtered
        if(listViewAddons.getItems().size() == listItems.size()){
            return selectedIndex;
        }
        // Filtered
        List<Addon> addons =  getAddonManager().getManagedAddons();
        String[] selectedName = listViewAddons.getItems().get(selectedIndex).split(":");
        String selectedNameStripped = selectedName[selectedName.length - 1];
        int trueIndex = 0;
        for(Addon addon : addons){
            if(addon.getName().equals(selectedNameStripped)){
                return trueIndex;
            }
            trueIndex++;
        }
        // should never reach this, will only happen if the names are different in the displayed list and the actual storage
        return -1;
    }




    //endregion

    //================================================================================
    // Misc
    //================================================================================
    public AddonManager getAddonManager() {
        return addonManager;
    }

    public void setAddonManager(AddonManager addonManager) {
        this.addonManager = addonManager;
    }

    public static Controller getInstance() {
        return controller;
    }


}
