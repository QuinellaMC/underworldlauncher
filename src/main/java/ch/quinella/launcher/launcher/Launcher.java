package ch.quinella.launcher.launcher;

import java.io.File;
import java.util.Arrays;

import ch.quinella.launcher.Main;
import fr.theshark34.openauth.AuthPoints;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openauth.Authenticator;
import fr.theshark34.openauth.model.AuthAgent;
import fr.theshark34.openauth.model.response.AuthResponse;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import fr.theshark34.openlauncherlib.minecraft.GameInfos;
import fr.theshark34.openlauncherlib.minecraft.GameTweak;
import fr.theshark34.openlauncherlib.minecraft.GameType;
import fr.theshark34.openlauncherlib.minecraft.GameVersion;
import fr.theshark34.openlauncherlib.minecraft.MinecraftLauncher;
import fr.theshark34.openlauncherlib.util.ProcessLogManager;

public class Launcher {

    public static final GameVersion U_VERSION = new GameVersion("1.10.2", GameType.V1_8_HIGHER);

    public static final GameInfos U_INFOS = new GameInfos("underworld", U_VERSION, new GameTweak[] {GameTweak.FORGE});

    public static final File DIR = U_INFOS.getGameDir();

    private static AuthInfos authInfos;

    public static void auth(String username, String password)throws AuthenticationException{

        Authenticator authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);
        AuthResponse response = authenticator.authenticate(AuthAgent.MINECRAFT, username, password, "");
        authInfos = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());
    }

    static void launch() throws LaunchException {
        ExternalLaunchProfile externalLaunchProfile = MinecraftLauncher.createExternalProfile(U_INFOS, GameFolder.BASIC, authInfos);
        externalLaunchProfile.getVmArgs().addAll(Arrays.asList("-Xms512M", "-Xmx" + (int)Main.getInstance().getLauncherPanel().getRAM() + "M"));
        ExternalLauncher externalLauncher = new ExternalLauncher(externalLaunchProfile);
        externalLaunchProfile.setMacDockName("Underworld");
        Process process = externalLauncher.launch();
        ProcessLogManager processLogManager = new ProcessLogManager(process.getInputStream(), new File(DIR, "logs.txt"));
        processLogManager.start();

        try {
            //LauncherFrame.getInstance().setVisible(false);
            process.waitFor();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.exit(0);
    }
}