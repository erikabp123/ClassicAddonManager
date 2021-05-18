package com.CAM.Patches;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.GUI.AddonManagerControl;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.IO.FileOperations;
import com.CAM.HelperTools.Logging.Log;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TbcPatch {

    private final HashMap<GameVersion, AddonManager> managers;
    private final AddonManagerControl amc;

    public TbcPatch(AddonManagerControl amc) {
        this.amc = amc;
        this.managers = amc.getManagers();
    }

    public List<Addon> tbcPrepatchMove() {

        AddonManager classicManager = managers.get(GameVersion.CLASSIC);
        classicManager.setGameVersion(GameVersion.TBC);

        managers.put(GameVersion.TBC, classicManager);
        amc.setActiveManager(GameVersion.TBC);
        managers.remove(GameVersion.CLASSIC);
        FileOperations.renameDirectory("data/CLASSIC", "TBC");

        for (Addon addon : classicManager.getManagedAddons()) {
            addon.setLastUpdateCheck(null);
            addon.setLastUpdated(null);
        }

        List<Addon> tukuiAddons = new ArrayList<>();

        List<Addon> addons = new ArrayList<>(classicManager.getManagedAddons());
        for (Addon addon : addons) {
            if (addon.getAddonSource() != AddonSource.TUKUI) continue;
            tukuiAddons.add(addon);
            classicManager.getManagedAddons().remove(addon);
        }

        classicManager.saveToFile();
        amc.saveToFile();

        return tukuiAddons;
    }

    public List<Addon> tbcPrepatchCopy() {

        AddonManager classicManager = managers.get(GameVersion.CLASSIC);
        String classicInstallLocation = classicManager.getInstallLocation();

        AddonManager tbcManager = new AddonManager(classicInstallLocation, GameVersion.TBC);

        List<Addon> tukuiAddons = new ArrayList<>();

        for (Addon addon : classicManager.getManagedAddons()) {
            if (addon.getAddonSource() == AddonSource.TUKUI) {
                tukuiAddons.add(addon);
                continue;
            }
            tbcManager.forceAddNewAddon(addon.export());
        }

        managers.put(GameVersion.TBC, tbcManager);
        amc.setActiveManager(GameVersion.TBC);

        tbcManager.saveToFile();
        amc.saveToFile();

        return tukuiAddons;
    }


    public void promptUserChoice() {
        List<Addon> incompatibleAddons = tbcPrepatchMove();
        showResult(incompatibleAddons);

        /*
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Convert to TBC?");
        alert.setHeaderText("Choose an option for updating to TBC!");
        alert.setContentText("ClassicAddonManager has a couple options for preparing for TBC. \n" +
                "A.) Create a copy of your current classic wow addon list and mirror it TBC.\n" +
                "B.) Move your current addon list permanently to TBC, removing your classic list.\n" +
                "C.) Skip setting up TBC and leave your current classic list as it is.\n\n" +
                "Please Select an option to continue!");

        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType copyButton = new ButtonType("Copy", ButtonBar.ButtonData.YES);
        ButtonType moveButton = new ButtonType("Move", ButtonBar.ButtonData.NO);
        ButtonType skipButton = new ButtonType("Skip", ButtonBar.ButtonData.OTHER);
        alert.getButtonTypes().setAll(copyButton, moveButton, skipButton);

        alert.showAndWait().ifPresent(type -> {
            if (type.getButtonData() == ButtonType.YES.getButtonData()) {
                List<Addon> incompatibleAddons = tbcPrepatchCopy();
                showResult(incompatibleAddons);
            } else if (type.getButtonData() == ButtonType.NO.getButtonData()){
                List<Addon> incompatibleAddons = tbcPrepatchMove();
                showResult(incompatibleAddons);
            } else showResult(null);
        });
        */
    }

    private void displayNoIssues() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Addons Converted");
        alert.setHeaderText("Your CLASSIC folder has automatically been converted to TBC!");
        alert.setContentText("You will need to update your addons again. " +
                "Also keep in mind that some addons may not be compatible with TBC " +
                "or may have specific versions for CLASSIC versus TBC.");

        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    private void displayIncompatibleAddons(List<Addon> incompatibleAddons) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Addon list converted to TBC");

        SplitPane splitPane = new SplitPane();
        alert.getDialogPane().setContent(splitPane);

        Text disclaimerText = new Text("Your addon setup has automatically been converted to TBC. Your addons will need to be updated again.\n " +
                "Note some addons may not be compatible with TBC or may have TBC variants which will need to be installed instead.\n" +
                "Tukui addons were not possible to convert to TBC, please re-install these through the search functionality.\n\n " +
                "The following addons were affected, do you wish to uninstall them or keep them?\n " +
                "NOTE: the manager will not be able to manage or display these addons but they will remain installed in your wow folder.");

        TextArea textArea = new TextArea();
        String text = "";
        for (Addon addon : incompatibleAddons) text += addon.toString() + "\n";
        textArea.setText(text);

        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().add(disclaimerText);
        splitPane.getItems().addAll(textArea);


        ButtonType uninstallButton = new ButtonType("Uninstall", ButtonBar.ButtonData.YES);
        ButtonType keepButton = new ButtonType("Keep", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(uninstallButton, keepButton);
        alert.showAndWait().ifPresent(type -> {
            if (type.getButtonData() == ButtonType.YES.getButtonData()) {
                for (Addon addon : incompatibleAddons) {
                    amc.getActiveManager().forceRemoveAddon(addon);
                }
            }
        });
    }

    public void showResult(List<Addon> incompatibleAddons) {
        if (incompatibleAddons != null && incompatibleAddons.isEmpty()) displayNoIssues();
        else if (incompatibleAddons != null) displayIncompatibleAddons(incompatibleAddons);

        File file = new File("system/patches/PATCHED_TbcPatch");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.printStackTrace(e);
        }
    }
}
