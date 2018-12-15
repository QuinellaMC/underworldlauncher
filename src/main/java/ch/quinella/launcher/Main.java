package ch.quinella.launcher;

import ch.quinella.launcher.launcher.LauncherFrame;
import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ch.quinella.launcher.launcher.Launcher.DIR;

public class Main implements ProgressListener {

    public static boolean isLoading;

    private static final String DIRECTORY = DIR.getAbsolutePath() + File.separator;
    private static String ACCESS_TOKEN = "d_h8FG-QAaAAAAAAAAAAVPWI25FJJeCK_K-M19doFuIiwncWIKJeYUykx0cGfHbX";
    private static List<File> filesList;

    private static long totalBytes = 0; //SI BUG CHANGER ET REMPLACER PAR 1 (DIVISION PAR ZERO)

    private static long bytesDownloaded = 0;

    private static LauncherFrame instance;

    private static List<Metadata> fileToDownload = new ArrayList<>();


    public static void main(String[] args) {

        display(DIR.toString());
        if (!DIR.exists()) {
            DIR.mkdirs();
        }

        // Astuce pour avoir le style visuel du systeme hôte
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        instance = new LauncherFrame();

        filesList = listFilesForFolder(DIR);

        DbxClientV2 client = new DbxClientV2(DbxRequestConfig.newBuilder("dropbox/java-tutorial").build(), ACCESS_TOKEN);

        isLoading = true;

        checkForFilesToDownload("", client);

        startDownload(client);

        checkMods(client);

        instance.getLauncherPanel().setInfoLabel("Téléchargement terminé, le jeu est prêt à être lancé");

        instance.getLauncherPanel().setProgressBarVisible(false);

        isLoading = false;

    }

    /**
     * Cette méthode permet de vérifier si tous les fichiers sont bien là. Elle ajoute à la liste <code>fileToDownload</code>
     * tous les fichiers qu'il faudrait télécharger. Elle ajoute aussi à chaque fois les tailles des metadatas à télécharger.
     * Cela permet d'avoir une barre de progressement globale.
     * @param pathName
     * @param client
     */
    public static void checkForFilesToDownload(String pathName, DbxClientV2 client) {

        try {
            ListFolderResult result = client.files().listFolder(pathName);

            while (true) {

                for (Metadata metadata : result.getEntries()) {//Parcours de tous les fichiers dans la dropbox

                    List<String> resultFiles = new ArrayList<>(); //Revérifie la liste à chaque fois pour n'ajouter que les
                    for (File file : filesList) {
                        resultFiles.add(file.getName());
                    }
                    if (metadata instanceof FolderMetadata) {
                        checkForFilesToDownload(metadata.getPathLower(), client);
                    }
                    else if (!resultFiles.contains(metadata.getName()) &&
                            !resultFiles.contains(metadata.getName().substring(0, metadata.getName().length() - 4))) {

                        fileToDownload.add(metadata);//Si pas de doublon téléchargement
                        display(metadata.toString());

                        int var1 = metadata.toString().indexOf("size") + 6;

                        int var2 = metadata.toString().indexOf(",", var1);

                        String ch = metadata.toString().substring(var1, var2);

                        totalBytes = Long.parseLong(ch);

                        //totalBytes += Long.parseLong(metadata.toStringMultiline().substring(metadata.toStringMultiline().indexOf("size") + 8, metadata.toStringMultiline().indexOf(",\n", metadata.toStringMultiline().indexOf("size") + 8))); //Énorme ligne permettant de trouver la taille de la metadata

                    } else display("Le fichier " + metadata.getName() + " existe déjà."); //Sinon preuve
                }
                if (!result.getHasMore()) {
                    break;
                }
                for (File file : filesList){
                    display(file.getName());
                }
                result = client.files().listFolderContinue(result.getCursor());
            }
        } catch (ListFolderContinueErrorException e) {
            e.printStackTrace();
        } catch (ListFolderErrorException e) {
            e.printStackTrace();
        } catch (DbxApiException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }

    }


    /**
     * Cette méthode permet de vérifier, dans les deux sens, si l'utilisateur a le bon nombre de mods. Si ce n'est pas le cas,
     * soit la méthode télécharge le mod manquant, soit elle supprime le/s mod/s en trop.
     * @param client
     */
    public static void checkMods(DbxClientV2 client){
        int mods = 0;
        File modDIR = new File(DIRECTORY + "mods" + File.separator);
        List<File> resultFiles = new ArrayList<>();
        for(File file : modDIR.listFiles()){
            resultFiles.add(file);
        }
        try {
            ListFolderResult result = client.files().listFolder( "/mods");
            while (true) {
                for (Metadata metadata : result.getEntries()) {//Parcours de tous les fichiers dans la dropbox
                    mods++;
                    display(metadata.getPathLower());
                }
                if (!result.getHasMore()) {
                    break;
                }
                result = client.files().listFolderContinue(result.getCursor());
            }
            if(mods < resultFiles.size()){
                display("Vous possédez des mods en trop… supression de ces mods en cours…");
                List<String> list = new ArrayList<>();
                while (true) {
                    for (Metadata metadata : result.getEntries()) {//Parcours de tous les fichiers dans la dropbox
                        list.add(metadata.getName());
                    }
                    if (!result.getHasMore()) {
                        break;
                    }
                    result = client.files().listFolderContinue(result.getCursor());
                }
                for(File file : resultFiles){
                    if(!list.contains(file.getName())){
                        file.delete();
                    }
                }
            }

            else if(mods > resultFiles.size()){
                display("Vous ne possédez pas assez de mods en trop… téléchargement de ces mods en cours…");
                while (true) {
                    List<String> resultNameFiles = new ArrayList<>();
                    for(File file : resultFiles){
                        resultNameFiles.add(file.getName());
                    }
                    for (Metadata metadata : result.getEntries()) {//Parcours de tous les fichiers dans la dropbox
                        if(!resultNameFiles.contains(metadata.getName())){
                            fileToDownload.add(metadata);

                            int var1 = metadata.toString().indexOf("size") + 6;

                            int var2 = metadata.toString().indexOf(",", var1);

                            String ch = metadata.toString().substring(var1, var2);

                            totalBytes = Long.parseLong(ch);

                        }
                    }
                    if (!result.getHasMore()) {
                        break;
                    }
                    result = client.files().listFolderContinue(result.getCursor());
                }
                startDownload(client);
            }

            else { return; }

            List<String> nameFilesList = new ArrayList<>();
            for(File file : resultFiles){ nameFilesList.add(file.getName()); }
            if(nameFilesList.contains("1.10.2")){
                new File(DIRECTORY + "mods" + File.separator + "1.10.2" + File.separator).delete();
            }

        } catch (ListFolderErrorException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }

    }

    /** Méthode permettant de lancer le téléchargement. Elle se lance dans la méthode main, après avoir vérifier les fichiers
     * avec <code>checkForFilesToDownload</code>, qui a ajouté les fichiers manquant à la list <code>fileToDownload</code>.
     * @param client
     */
    public static void startDownload(DbxClientV2 client) {

        display("Bytes à télécharger : " + totalBytes + " (" + (double)totalBytes/1000d + "Mo)");

        for (Metadata metadata : fileToDownload) {

            downloadFile(metadata, client);

        }

    }

    /**
     * Méthode ajouté par Swing. Elle renvoie à chaque fois qu'il y a une update au niveau du téléchargement, le nombre en pourcentage
     * de bytes téléchargés.
     * @param uploaded
     * @param totalCurrentSize
     */
    private static void printProgress(long uploaded, long totalCurrentSize) {

        if (uploaded == totalCurrentSize) {
            bytesDownloaded += uploaded;
        } else
            instance.getLauncherPanel().setProgress((int) (100 * ((double) (bytesDownloaded + uploaded) / (double) totalBytes)) + 1);
    }


    /**
     * Cette méthode permet de télécharger la metadata que l'on donne en argument. Elle permet aussi de modifier, avec la méthode
     * <code>printProgress</code> d'afficher en direct sur la JFrame l'avancement du téléchargement. Elle télécharge aussi les
     * dossiers en .zip, qu'il faut après unzip et extraire.
     * @param metadata
     * @param client
     */
    private static void downloadFile(Metadata metadata, DbxClientV2 client) {

        String slash = metadata.getPathLower();
        int nSlash = 0;
        for(int i = 0; i < metadata.getPathLower().length(); i++){
            if(slash.charAt(i) == '/'){

                nSlash++;
            }
        }
        if(nSlash >= 2){
            display(metadata.getName() + " provient d'un dossier");
            File fileOut = new File(DIRECTORY + metadata.getPathLower() + "/");
            fileOut.getParentFile().mkdirs();
        }

        int var1 = metadata.toString().indexOf("size") + 6;

        int var2 = metadata.toString().indexOf(",", var1);

        String ch = metadata.toString().substring(var1, var2);

        long size = Long.parseLong(ch);

        instance.getLauncherPanel().setProgressBarVisible(true);

        FileOutputStream out = null;
        try {
            if (metadata instanceof FileMetadata) {

                out = new FileOutputStream(new File(DIRECTORY + metadata.getPathLower()));
                display("Téléchargement : " + metadata.getName());
                DbxDownloader<FileMetadata> downloader = client.files().download(metadata.getPathLower());

                ProgressListener progressListener = bytesWritten -> printProgress(bytesWritten, size);
                instance.getLauncherPanel().setInfoLabel(metadata.getName());
                downloader.download(out, progressListener);

                out.close();
                if (metadata.getName().substring(metadata.getName().length() - 4, metadata.getName().length()).equals(".zip")) {
                    unzip(DIRECTORY + File.separator + metadata.getName(), DIRECTORY + File.separator);
                }
            } else if (metadata instanceof FolderMetadata) {
                out = new FileOutputStream(new File(DIRECTORY + metadata.getName()) + ".zip");
                display("Téléchargement : " + metadata.getName());
                DbxDownloader<DownloadZipResult> downloader = client.files().downloadZip(metadata.getPathLower());

                ProgressListener progressListener = bytesWritten -> printProgress(bytesWritten, size);
                instance.getLauncherPanel().setInfoLabel(metadata.getName());
                downloader.download(out, progressListener);

                out.close();
                unzip(DIRECTORY + metadata.getName() + ".zip", DIRECTORY);
            }
            display("Flux fermé avec succès");

            instance.getLauncherPanel().setProgressBarVisible(true);

            instance.getLauncherPanel().setInfoLabel("Chargement…");
            listFilesForFolder(DIR);
        } catch (FileNotFoundException e) {
            display("Fichier non trouvé : ");
            e.printStackTrace();
            System.exit(1);
        } catch (DbxException e) {
            display("Dropbox a rencontré une erreur fatale en téléchargant le fichier : " + metadata.getName());
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            display("Erreur de lecture du fichier : " + metadata.getName());
            e.printStackTrace();
            System.exit(1);
        }

    }


    /**
     * Méthode basique d'unzip
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else if (entry.isDirectory()) {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        new File(zipFilePath).delete();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /** Ajoute dans une liste tous les dossiers et fichiers d'un répertoire. Elle "rentre" aussi dans les dossiers pour en prendre leur contenu
     * @param folder
     * @return
     */
    public static ArrayList<File> listFilesForFolder(File folder) {
        List<File> resultFiles = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                resultFiles.add(fileEntry);
                List<File> second = listFilesForFolder(fileEntry);
                for(int i = 0; i < second.size(); i++){ //Petite astuce pour combiner deux listes facilement
                    resultFiles.add(second.get(i));
                }
            } else {
                if (!fileEntry.getName().equals(".DS_Store")) resultFiles.add(fileEntry); //Empêcher les bugs macOS :(
                else {
                    fileEntry.delete();
                }
            }
        }
        return new ArrayList<>(resultFiles);
    }

    public static LauncherFrame getInstance() {
        return instance;
    }

    @Override
    public void onProgress(long bytesWritten) {

    }

    /**
     * Méthode simple d'affichage, qui met l'heure la minute et la seconde à laquelle elle a été appelée. Inspiré par Mojang.
     * Elle est appelée à la place de <code>System.out.println("");</code>
     * @param text
     */
    public static void display(String text){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss:ms");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[" + dtf.format(now) + "] [Underworld Launcher] " + text);

    }


}