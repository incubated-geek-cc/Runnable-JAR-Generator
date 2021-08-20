package executablejargenerator;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

public class MainPanel extends JPanel {

    private static final JFrame APP_FRAME = new JFrame("Create Runnable JAR from NetBeans IDE :: v1.0");
    private static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
    
    // OUTPUT LOGS
    private static final JTextArea LOG_TEXT_AREA = new JTextArea();
    private static JScrollPane jScrollPanelOutputFileLogs;
    
    private String nameOfMainClass;
    private String nameOfManifestDir;
    private String nameOfExternalJARsDir;
    private String nameOfManifestFile;
    
    private JButton jButtonCreateRunnable;
    private JButton jButtonSelectJARFile;

    private JLabel jLabelSelectJARFile;
    private JLabel jLabelSelectedJARFileName;
    private JTextField jTextFieldMainClassName;

    private JLabel jLabelMainClassName;
    private JLabel jLabelSelectedJARs;
    private JButton jButtonSelectJARs;
    private JLabel jLabelInputFilesSelected;
        
    private File inputJARFile = null;

    // input files selected
    private JButton jButtonRemoveSelectedFiles;
    private DefaultListModel jListInputFilesSelectedModel = new DefaultListModel<>();
    private JList<String> jListInputFilesSelected;
    private JScrollPane jScrollPane1FileListItems;
    
    private JButton jButtonClearAll;
    
    // LIST OF FILE ITEMS - INPUT FILES TO EXTRACT
    private final ArrayList<File> INPUT_FILES = new ArrayList<File>();

    public MainPanel() {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(LOG_TEXT_AREA)));
        
        LOGGER.info(() -> "Generate a Runnable JAR from your NetBeans built output. \n");
        
        // OUTPUT LOGS
        LOG_TEXT_AREA.setEditable(false);
        LOG_TEXT_AREA.setWrapStyleWord(true);
        jScrollPanelOutputFileLogs = new JScrollPane(LOG_TEXT_AREA);
        updateLogs();
        jScrollPanelOutputFileLogs.setHorizontalScrollBar(null);
        
        DefaultCaret caret = (DefaultCaret) LOG_TEXT_AREA.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        //construct components
        jLabelSelectJARFile = new JLabel("Step 1: Select Compiled JAR (in dist folder)*");
        jButtonSelectJARFile = new JButton("Choose File...");
        jLabelSelectedJARFileName = new JLabel("(No File Selected)");

        jLabelMainClassName = new JLabel("Step 2: Specifiy name of Main Class*");
        jTextFieldMainClassName = new JTextField("gui.Main");

        // INPUT FILES SELECTED
        jLabelSelectedJARs = new JLabel("Step 3: Upload External JAR(s) (in lib folder)");
        jButtonSelectJARs = new JButton("Choose File(s)...");
        jButtonRemoveSelectedFiles = new JButton("Remove File");
        jLabelInputFilesSelected = new JLabel("List of JAR(s) selected:");
        jListInputFilesSelected = new JList<>(jListInputFilesSelectedModel);
        jScrollPane1FileListItems = new JScrollPane(jListInputFilesSelected);

        jButtonCreateRunnable = new JButton("Create Runnable JAR >>");
        jButtonClearAll = new JButton("Clear All");
        
        //adjust size and set layout
        int frameHeight = 715;
        int frameWidth = 575;
        setPreferredSize(new Dimension(frameHeight, frameWidth));
        setLayout(null);

        //add components
        add(jLabelSelectJARFile);
        add(jButtonSelectJARFile);
        add(jLabelSelectedJARFileName);

        add(jLabelMainClassName);
        add(jTextFieldMainClassName);

        add(jButtonSelectJARs);
        add(jLabelSelectedJARs);
        add(jButtonRemoveSelectedFiles);
        add(jLabelInputFilesSelected);
        add(jScrollPane1FileListItems);
        add(jButtonCreateRunnable);
        add(jScrollPanelOutputFileLogs);
        add(jButtonClearAll);
        
        jButtonCreateRunnable.setEnabled(false);
        
        //set component bounds (only needed by Absolute Positioning)
        int leftHorizontalMargin = 15;
        int widthOfLabel = 260;
        int widthOfButton = 125;
        int heightOfLabelsButtons = 30;
        int componentSpacing = 5;

        int verticalMargin = 50;

        jLabelSelectJARFile.setBounds(
                leftHorizontalMargin,
                15,
                widthOfLabel,
                heightOfLabelsButtons);
        int fileButtonHorizontalMargin = leftHorizontalMargin
                + widthOfLabel
                + componentSpacing;
        jButtonSelectJARFile.setBounds(
                fileButtonHorizontalMargin,
                15,
                widthOfButton,
                heightOfLabelsButtons);

        int widthOfFileName = frameWidth - (fileButtonHorizontalMargin
                + widthOfButton
                + componentSpacing
                + leftHorizontalMargin);
        jLabelSelectedJARFileName.setBounds(
                fileButtonHorizontalMargin
                + widthOfButton
                + componentSpacing,
                15,
                widthOfFileName,
                heightOfLabelsButtons);

        jLabelMainClassName.setBounds(
                leftHorizontalMargin,
                15 + verticalMargin,
                widthOfLabel,
                heightOfLabelsButtons);
        jTextFieldMainClassName.setBounds(
                fileButtonHorizontalMargin,
                15 + verticalMargin,
                frameWidth
                - fileButtonHorizontalMargin
                + widthOfButton,
                heightOfLabelsButtons);

        jLabelSelectedJARs.setBounds(
                leftHorizontalMargin,
                15 + verticalMargin + verticalMargin,
                widthOfLabel,
                heightOfLabelsButtons);
        jButtonSelectJARs.setBounds(
                fileButtonHorizontalMargin,
                15 + verticalMargin + verticalMargin,
                widthOfButton,
                heightOfLabelsButtons);

        int widthOfSelectExternalJarsTitle = frameWidth
                - (fileButtonHorizontalMargin + leftHorizontalMargin);
        jLabelInputFilesSelected.setBounds(
                fileButtonHorizontalMargin,
                15 + verticalMargin + verticalMargin + verticalMargin,
                widthOfSelectExternalJarsTitle,
                heightOfLabelsButtons);
        jButtonRemoveSelectedFiles.setBounds(
                widthOfSelectExternalJarsTitle + fileButtonHorizontalMargin + leftHorizontalMargin,
                15 + verticalMargin + verticalMargin + verticalMargin,
                widthOfButton,
                heightOfLabelsButtons);

        int yCoordOfFileListItems = 15
                + verticalMargin + verticalMargin + verticalMargin
                + heightOfLabelsButtons + componentSpacing;
        jScrollPane1FileListItems.setBounds(
                fileButtonHorizontalMargin,
                yCoordOfFileListItems,
                widthOfSelectExternalJarsTitle 
                + widthOfButton
                + leftHorizontalMargin,
                125);

        jButtonCreateRunnable.setBounds(
                leftHorizontalMargin,
                yCoordOfFileListItems + 125 + componentSpacing,
                185,
                heightOfLabelsButtons
        );
        
        int yCoordOfStatus = yCoordOfFileListItems + 125 + componentSpacing 
            + heightOfLabelsButtons + componentSpacing;
        int widthOfLogs=widthOfSelectExternalJarsTitle 
            + widthOfLabel
            + widthOfButton
            + leftHorizontalMargin
            + componentSpacing;
        jScrollPanelOutputFileLogs.setBounds(
            leftHorizontalMargin,
            yCoordOfStatus,
            widthOfLogs,
            165
        );
        
        jButtonClearAll.setBounds(
            widthOfSelectExternalJarsTitle + fileButtonHorizontalMargin + leftHorizontalMargin,
            yCoordOfStatus + 165 + componentSpacing,
            widthOfButton,
            heightOfLabelsButtons
        );
        
        // Add actions performed by each selectable component
        jButtonSelectJARFile.addActionListener((java.awt.event.ActionEvent evt) -> {
            selectJARFileAction(evt);
            jButtonCreateRunnable.setEnabled(true);
        });

        jButtonRemoveSelectedFiles.addActionListener((java.awt.event.ActionEvent evt) -> {
            removeListItemAction(evt);
        });

        jButtonSelectJARs.addActionListener((java.awt.event.ActionEvent evt) -> {
            selectInputFilesAction(evt);
        });

        jButtonCreateRunnable.addActionListener((java.awt.event.ActionEvent evt) -> {
            try {
                createRunnableJARAction(evt);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        jButtonClearAll.addActionListener((java.awt.event.ActionEvent evt) -> {
            clearAllSelections(evt);
        });
    }
    
    private void clearAllSelections(ActionEvent e) {
        jLabelSelectedJARFileName.setText("(No File Selected)");
        jTextFieldMainClassName.setText("gui.Main");
        INPUT_FILES.clear();
        jListInputFilesSelectedModel.clear();
        jListInputFilesSelected = new JList<>(jListInputFilesSelectedModel);
        jButtonCreateRunnable.setEnabled(false);
        LOG_TEXT_AREA.setText("");
        
        LOGGER.info(() -> "Generate a Runnable JAR from your NetBeans built output. \n");
    }
    
    private void selectInputFilesAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Input File(s)");

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR File (.jar)", "jar");
        fileChooser.addChoosableFileFilter(filter);

        int option = fileChooser.showOpenDialog(APP_FRAME);
        if (option == JFileChooser.APPROVE_OPTION) {
            jListInputFilesSelectedModel = (DefaultListModel) jListInputFilesSelected.getModel();
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File selectedFile : selectedFiles) { // FOR-EACH FILE
                String selectedFileName = selectedFile.getName();
                jListInputFilesSelectedModel.addElement(selectedFileName);
                INPUT_FILES.add(selectedFile);
            }
        }
    }

    private void removeListItemAction(ActionEvent e) {
        jListInputFilesSelectedModel = (DefaultListModel) jListInputFilesSelected.getModel();
        int[] selectedInputFiles = jListInputFilesSelected.getSelectedIndices();

        for (int i : selectedInputFiles) {
            jListInputFilesSelectedModel.remove(i);
            INPUT_FILES.remove(i);
        }
    }

    private void createRunnableJARAction(ActionEvent e) throws IOException {
        nameOfExternalJARsDir = "EXTERNAL_JARS";
        nameOfManifestDir = "META-INF";
        nameOfManifestFile = "MANIFEST.MF";
        nameOfMainClass = jTextFieldMainClassName.getText().trim();

        // create temp folder to work in
        String workingDir = System.getProperty("user.dir");
        String tempFolderName = "extracted_" + getCurrentTimeStamp();
        File tempWorkingDir = new File(workingDir, tempFolderName);
        tempWorkingDir.mkdir();
        File externalJarFolder = new File(tempWorkingDir, nameOfExternalJARsDir);
        externalJarFolder.mkdir();
        String externalJarFolderAbsPath = externalJarFolder.getAbsolutePath();
        String jarFileName = inputJARFile.getName();
        
        File copiedJarFile = new File(tempWorkingDir, jarFileName);
        if (!copiedJarFile.exists()) {
            copiedJarFile.createNewFile();
        }
        copy(inputJARFile, copiedJarFile);
        
        System.out.println("External JARs are being extracted");
        outputConsoleLogsBreakline(LOGGER, "External JARs are being extracted");
        updateLogs();
        extractJARFileContents(INPUT_FILES, externalJarFolder);
        
        System.out.println("Contents from application JAR are being extracted");
        outputConsoleLogsBreakline(LOGGER, "Contents from application JAR are being extracted");
        updateLogs();
        ArrayList<File> arrList = new ArrayList<File>();
        arrList.add(copiedJarFile);
        extractJARFileContents(arrList, externalJarFolder);
        
        // META-INF + Manifest File
        System.out.println("Configuring " + nameOfManifestFile + " file");
        outputConsoleLogsBreakline(LOGGER, "Configuring " + nameOfManifestFile + " file");
        updateLogs();
        File manifestDir = new File(externalJarFolderAbsPath, nameOfManifestDir);
        boolean deletedOriginalManifestDir = deleteDirectory(manifestDir);
        if (deletedOriginalManifestDir) {
            // recreate manifestDir
            manifestDir = new File(externalJarFolderAbsPath, nameOfManifestDir);
            boolean createdManifestDir = manifestDir.mkdir();
            if (createdManifestDir) {
                File manifestFile = new File(manifestDir.getAbsolutePath(), nameOfManifestFile);
                FileWriter myWriter = new FileWriter(manifestFile);
                // writing the Manifest File
                myWriter.write("Manifest-Version: 1.0" + "\n");
                myWriter.write("Class-Path: ." + "\n");
                myWriter.write("Main-Class: " + nameOfMainClass + "\n");
                myWriter.close();
            }
        }
        
        System.out.println("Creating Runnable JAR file");
        outputConsoleLogsBreakline(LOGGER, "Creating Runnable JAR file");
        updateLogs();
        File[] filesToAddToJar=externalJarFolder.listFiles();
        File destJarFile = new File(externalJarFolderAbsPath, jarFileName);
        createJarFile(filesToAddToJar, destJarFile.getAbsolutePath());
        
        System.out.println("Cleaning up");
        outputConsoleLogsBreakline(LOGGER, "Cleaning up");
        updateLogs();
        File[] tempFiles = tempWorkingDir.listFiles();
        for (File tempFile : tempFiles) {
            String tempFileName = tempFile.getName();
            if (!tempFileName.equalsIgnoreCase(externalJarFolder.getName())) {
                tempFile.delete();
            }
        }
        tempFiles = externalJarFolder.listFiles();
        for (File tempFile : tempFiles) {
            String tempFileName = tempFile.getName();
            if(tempFile.isDirectory()) {
                deleteDirectory(tempFile);
            } else if (!tempFileName.equalsIgnoreCase(jarFileName)) {
                tempFile.delete();
            }
        }
        
        // move the RUNNABLE JAR to outside the JAR folder
        // renaming the file and moving it to a new location
        if(destJarFile.renameTo(new File(tempWorkingDir, jarFileName))){
            boolean deletedExternalJarsDir = deleteDirectory(externalJarFolder);
            if(deletedExternalJarsDir) {
                System.out.println("File moved successfully");
                JOptionPane.showMessageDialog(
                    APP_FRAME,
                    "Runnable JAR file has been successfully generated.",
                    "Alert",
                    JOptionPane.WARNING_MESSAGE
                );
                Desktop.getDesktop().open(tempWorkingDir);
            }
        } else {
            System.out.println("Failed to move the file");
            LOGGER.info(() -> "Failed to move the file.\n");
        }
    }

    private void selectJARFileAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Input File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR File (.jar)", "jar");
        fileChooser.addChoosableFileFilter(filter);

        int option = fileChooser.showOpenDialog(APP_FRAME);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                inputJARFile = selectedFile;
                jLabelSelectedJARFileName.setText(inputJARFile.getName());
                jTextFieldMainClassName.setEnabled(true);
            }
        }
    }
    
    private static void updateLogs() {
        jScrollPanelOutputFileLogs.getVerticalScrollBar().setValue(jScrollPanelOutputFileLogs.getVerticalScrollBar().getMaximum());
        jScrollPanelOutputFileLogs.getVerticalScrollBar().paint(jScrollPanelOutputFileLogs.getVerticalScrollBar().getGraphics());
        LOG_TEXT_AREA.scrollRectToVisible(LOG_TEXT_AREA.getVisibleRect());
        LOG_TEXT_AREA.paint(LOG_TEXT_AREA.getGraphics());
    }
    
    private static void addTextToOutputLogs(Logger LOGGER, String logString) {
        LOGGER.info(() -> logString);
    }

    private static void outputConsoleLogsBreakline(Logger LOGGER, String consoleCaption) {
        String logString = "";

        int charLimit = 180;
        if (consoleCaption.length() > charLimit) {
            logString = consoleCaption.substring(0, charLimit - 4) + " ...";
        } else {
            String result = "";

            if (consoleCaption.isEmpty()) {
                for (int i = 0; i < charLimit; i++) {
                    result += "=";
                }
                logString = result;
            } else {
                charLimit = (charLimit - consoleCaption.length() - 1);
                for (int i = 0; i < charLimit; i++) {
                    result += "-";
                }
                logString = consoleCaption + " " + result;
            }
        }
        logString = logString + "\n";
        addTextToOutputLogs(LOGGER, logString);
    }
    
    private void extractJARFileContents(ArrayList<File> jarFiles, File destDir) throws IOException {
        for (File jarFile : jarFiles) {
            FileInputStream jarFileInputStream = new FileInputStream(jarFile);
            byte[] buffer = new byte[1024];
            ZipInputStream jarInputStream = new ZipInputStream(jarFileInputStream);
            ZipEntry jarEntry = jarInputStream.getNextEntry();
            while (jarEntry != null) {
                File newJarContent = newJarContent(destDir, jarEntry);
                if (jarEntry.isDirectory()) {
                    if (!newJarContent.isDirectory() && !newJarContent.mkdirs()) {
                        throw new IOException("Failed to create directory " + newJarContent);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newJarContent.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    // write file content
                    if (!newJarContent.getName().equalsIgnoreCase("META-INF")) {
                        FileOutputStream fos = new FileOutputStream(newJarContent);
                        int len;
                        while ((len = jarInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                }
                jarEntry = jarInputStream.getNextEntry();
            }
            jarInputStream.closeEntry();
            jarInputStream.close();
            jarFileInputStream.close();
        }
    }

    private File newJarContent(File destinationDir, ZipEntry jarEntry) throws IOException {
        File destFile = new File(destinationDir, jarEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + jarEntry.getName());
        }
        return destFile;
    }

    private void copy(File src, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            byte[] buf = new byte[1024]; // buffer size 1K
            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    
    private String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MMM_yyyy_hhmmaa");
        Date date = new Date();
        String timestamp = sdf.format(date);

        return timestamp;
    }
    
    private void createJarFile(File[] listFiles, String destJarFile) throws FileNotFoundException, IOException {
        ZipOutputStream jarOS = new ZipOutputStream(new FileOutputStream(destJarFile));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                addDirToJar(file, file.getName(), jarOS);
            } else {
                addFileToJar(file, jarOS);
            }
        }
        jarOS.flush();
        jarOS.close();
    }
    
    private void addDirToJar(File folder, String parentFolder, ZipOutputStream jarOS) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addDirToJar(file, parentFolder + "/" + file.getName(), jarOS);
                continue;
            }
            jarOS.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[4096];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                jarOS.write(bytesIn, 0, read);
                bytesRead += read;
            }
            jarOS.closeEntry();
        }
    }
    
    private void addFileToJar(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
    }
    
    public static void main(String[] args) {
        APP_FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        APP_FRAME.getContentPane().add(new MainPanel());
        APP_FRAME.pack();

        // set to center and middle of screen
        APP_FRAME.setLocationRelativeTo(null);
        APP_FRAME.setVisible(true);
    }
}
