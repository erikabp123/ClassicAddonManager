package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.AddonManagement.AddonRequest;
import com.CAM.DataCollection.CurseForgeScraper;
import com.CAM.DataCollection.FileDownloader;
import com.CAM.DataCollection.GitHubScraper;
import com.CAM.HelperTools.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    @FXML
    private ListView<String> listViewAddons;

    @FXML
    private Button buttonRemove;

    @FXML
    private TextField textFieldURL;

    @FXML
    private Text textManagedLabel;

    @FXML
    private TextArea textAreaOutputLog;

    @FXML
    private ImageView imageViewAdd;

    @FXML
    private CheckBox checkboxReleases;

    @FXML
    public VBox vboxMainUI;

    @FXML
    public ImageView imageViewUpdate;

    @FXML
    public ProgressBar progressBarDownload;

    @FXML
    private Button buttonUpdate;

    @FXML
    private Button buttonAdd;

    @FXML
    private MenuItem menuAboutVersion;

    @FXML
    public ProgressBar progressBarUpdate;

    @FXML
    public MenuBar menuBar;

    @FXML
    public Text textOutputLogLabel;

    @FXML
    public Text updatingVersionLabel;

    @FXML
    private void exportAction(){
        Thread exportThread = new Thread(() -> {
            ArrayList<Addon> addons = new ArrayList<>();
            for(Addon addon : addonManager.getManagedAddons()){
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
    private void importAction(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Import Addon List");
        alert.setHeaderText("Paste Addon Export Here");
        alert.setContentText(null);

        TextArea textArea = new TextArea();
        alert.getDialogPane().setContent(textArea);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK){
            return;
        }

        Thread importThread = new Thread(() -> {
            Gson gson = new Gson();
            ArrayList<Addon> imported = null;
            try {
                imported = gson.fromJson(textArea.getText(), new TypeToken<ArrayList<Addon>>(){}.getType());
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

    final ObservableList<String> listItems = FXCollections.observableArrayList();

    private AddonManager addonManager;

    private ContextMenu contextMenuOutputLog;

    @FXML
    private void setupAction(){
        UserInput userInput = new GUIUserInput("Please provide path to WoW Classic Installation");
        String installLocation = AddonManager.specifyInstallLocation(userInput);
        if(installLocation == null){
            return;
        }
        addonManager.setInstallLocation(installLocation);
    }

    @FXML
    private void toggleDebugAction(){
        Log.toggleLogging();
    }

    @FXML
    private void removeAction(){
        Thread removeThread = new Thread(() -> {
            Platform.runLater(() -> disableAll());
            ObservableList<Integer> selected = listViewAddons.getSelectionModel().getSelectedIndices();
            if(selected.size() < 1){
                return;
            }
            addonManager.removeAddon(selected.get(0));
            Platform.runLater(() -> {
                updateListView();
                enableAll();
            });
        });
        removeThread.start();
    }

    @FXML
    private void updateAction(){
        Thread updateThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                buttonUpdate.setVisible(false);
                progressBarDownload.setVisible(true);
            });
            addonManager.updateAddons();
            Platform.runLater(() -> {
                enableAll();
                buttonUpdate.setVisible(true);
                progressBarDownload.setVisible(false);
            });
        });
        updateThread.start();
    }

    private void determineBranch(String origin){
        String trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
        ArrayList<String> names = GitHubScraper.getBranches(trimmedOrigin);
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.getItems().addAll(names);

            dialog.setTitle("Github Branch");
            dialog.setHeaderText("The addon you're adding has the following branches:");
            dialog.setContentText("Branch:");
            if(names.contains("master")){
                dialog.setSelectedItem("master");
            } else {
                dialog.setSelectedItem(names.get(0));
            }

            Optional<String> result = dialog.showAndWait();

            if(!result.isPresent()){
                cleanUpAfterAddAction();
                return;
            }
            startAddonAddThread(result.get());
        });
    }

    @FXML
    private void addAction(){
        Thread precheckThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                imageViewAdd.setVisible(true);
            });

            String origin = textFieldURL.getText();

            UrlInfo urlInfo = UrlInfo.examineAddonUrl(origin);
            if (!urlInfo.isValid) {
                cleanUpAfterAddAction();
                return;
            }

            if(origin.contains("curseforge.com")){
                checkIfProceedClassic(origin);
                return;
            }
            if(origin.contains("github.com")){
                String trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
                GitHubScraper scraper = new GitHubScraper(trimmedOrigin, null, true);
                if(!checkboxReleases.isSelected()){
                    scraper.setReleases(false);
                    if(!scraper.isValidLink()){
                        cleanUpAfterAddAction();
                        return;
                    }
                    determineBranch(origin);
                    return;
                }
                if(!scraper.isValidLink()){
                    cleanUpAfterAddAction();
                    return;
                }
                startAddonAddThread(null);
                return;
            }
            startAddonAddThread(null);
            return;
        });
        precheckThread.start();
    }

    private void startAddonAddThread(String branch){
        Thread addAddonThread = new Thread(() -> {
            String origin = textFieldURL.getText();
            boolean releases = checkboxReleases.isSelected();

            AddonRequest request = new AddonRequest();
            request.origin = origin;
            request.branch = branch;
            request.releases = releases;

            if(!isValidRequest(request)){
                cleanUpAfterAddAction();
                return;
            }

            addonManager.addNewAddon(request);
            Platform.runLater(() -> {
                updateListView();
                enableAll();
                imageViewAdd.setVisible(false);
            });
        });
        addAddonThread.start();
    }

    private void checkIfProceedClassic(String origin){
        String trimmedOrigin = UrlInfo.trimCurseForgeUrl(origin);
        CurseForgeScraper scraper = CurseForgeScraper.getOfficialScraper(trimmedOrigin);
        if(!scraper.isValidLink()){
            Log.log("Link does not point to an addon!");
            cleanUpAfterAddAction();
            return;
        }
        if(scraper.isClassicSupported()){
            startAddonAddThread(null);
            return;
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Non-Classic addon");
            alert.setHeaderText("This addon does not seem to have an official classic release!");
            alert.setContentText(scraper.getName() + " does not seem to have an official classic release. " +
                    "Do you wish to add it anyway? This will result in downloading non-classic updates until classic ones are released.\n" +
                    "NOTE: There is no guarantee this addon will work with classic!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK){
                cleanUpAfterAddAction();
                return;
            }
            startAddonAddThread(null);
        });
    }

    private void cleanUpAfterAddAction(){
        Platform.runLater(() -> {
            enableAll();
            imageViewAdd.setVisible(false);
        });
    }

    private void progressBarListen(){
        DownloadListener downloadListener = new GUIDownloadListener(progressBarDownload);
        FileDownloader.listen(downloadListener);

        DownloadListener downloadListener1 = new GUIDownloadListener(progressBarUpdate);
        FileDownloader.listen(downloadListener1);
    }

    public void hideForUpdate(){
        buttonUpdate.setDisable(true);
        buttonRemove.setDisable(true);
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
        updatingVersionLabel.setDisable(false);
        updatingVersionLabel.setVisible(true);

        imageViewUpdate.setImage(new Image(this.getClass().getClassLoader().getResource("gears_load.gif").toExternalForm()));
        imageViewUpdate.setDisable(false);
        progressBarUpdate.setVisible(true);
    }

    private boolean isValidRequest(AddonRequest request){
        if(request.origin.contains("curseforge.com") || request.origin.contains("wowinterface.com")){
            return true;
        }
        if(request.releases){
            return true;
        }
        if(!request.branch.equals("")){
            return true;
        }
        return false;
    }

    @FXML
    private void githubRedirectAction(){
        String github = "https://github.com/erikabp123/ClassicAddonManager";
        openUrl(github);
    }

    @FXML
    private void discordRedirectAction(){
        String discord = "https://discord.gg/StX3gbw";
        openUrl(discord);
    }

    private void openUrl(String url){
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

    public void updateListView(){
        ArrayList<String> addonNames = new ArrayList<>();
        for(Addon addon : addonManager.getManagedAddons()){
            addonNames.add(addon.getName());
        }
        listItems.setAll(addonNames);
        updateListViewLabel();
    }

    private void updateListViewLabel(){
        int managedCount = addonManager.getManagedAddons().size();
        String textSuffix = "addons";
        if(managedCount == 1){
            textSuffix = "addon";
        }
        textManagedLabel.setText("Managing " + managedCount +" " + textSuffix);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuAboutVersion.setText("Version " + SelfUpdater.VERSION);
        imageViewAdd.setImage(new Image(this.getClass().getClassLoader().getResource("adding.gif").toExternalForm()));
        listViewAddons.setItems(listItems);
        Log.listen(new GUILogListener(textAreaOutputLog));
        progressBarListen();
        setupOutputLogContextMenu();
    }

    private void setupOutputLogContextMenu(){
        contextMenuOutputLog = new ContextMenu();
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
            if(selected.equals("")){
                return;
            }
            clipboard.setContents(new StringSelection(selected), null);
        });

        contextMenuOutputLog.getItems().addAll(selectAll, separator1, copy, separator2, clearLog);
        textAreaOutputLog.setContextMenu(contextMenuOutputLog);
    }

    public void checkForUpdate(){
        SelfUpdater.selfUpdate(this);
    }

    public AddonManager getAddonManager(){
        return addonManager;
    }

    public void setAddonManager(AddonManager addonManager){
        this.addonManager = addonManager;
    }

    private void disableAll(){
        buttonRemove.setDisable(true);
        buttonAdd.setDisable(true);
        buttonUpdate.setDisable(true);
        textFieldURL.setDisable(true);
        checkboxReleases.setDisable(true);
    }

    private void enableAll(){
        buttonRemove.setDisable(false);
        buttonAdd.setDisable(false);
        buttonUpdate.setDisable(false);
        textFieldURL.setDisable(false);
        checkboxReleases.setDisable(false);
    }


}
