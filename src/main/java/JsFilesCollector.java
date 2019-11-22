package main.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


class ExtJSFile {

    static Charset charset = StandardCharsets.UTF_8;

    String name;
    String path;
    String allContent;
    String contentWithoutRequires;
    String requires = "";
    List<String> requiresList = new ArrayList<>();
    String extended = "";

    boolean alreadyWritten = false;

    private static String getFileName(String absolutePath) {
        Path pathRelative = Paths.get(JsFilesCollector.root).relativize(Paths.get(absolutePath));
        String pathRelativeString = pathRelative.toString();
        pathRelativeString = pathRelativeString.replace("/", ".");
        pathRelativeString = pathRelativeString.replace(".js", "");
        String fileName = JsFilesCollector.appName + pathRelativeString;

        return fileName;
    }

    private static String getAllContent(File file) {

        try {

            byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
            return new String(encoded, charset);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setRequiresAndContentWithoutRequires() throws Exception {

        //allContent = "1234567890\nrequires\n:[\nsomething]\n,\n1234567890";

        int reqStart = allContent.indexOf("requires");

        if (reqStart >= 0) {

            String begin = allContent.substring(0, reqStart);
            String afterRequires = allContent.substring(reqStart + 8);

            int columnStart = afterRequires.indexOf(":");

            if (columnStart < 0) {
                throw new Exception("[JsFilesCollector] ERROR: missing character ':'");
            }

            String afterColumn = afterRequires.substring(columnStart + 1);

            int openBreakStart = afterColumn.indexOf("[");

            if (openBreakStart < 0) {
                throw new Exception("[JsFilesCollector] ERROR: missing character '['");
            }

            String afterOpenBreak = afterColumn.substring(openBreakStart + 1);

            int closeBreakStart = afterOpenBreak.indexOf("]");

            if (closeBreakStart < 0) {
                throw new Exception("[JsFilesCollector] ERROR: missing character ']'");
            }

            String afterCloseBreak = afterOpenBreak.substring(closeBreakStart + 1);

            requires = afterOpenBreak.substring(0, closeBreakStart);

            int commaStart = afterCloseBreak.indexOf(",");
            if (commaStart < 0) {

                contentWithoutRequires = begin + afterCloseBreak;
            }

            String afterComma = afterCloseBreak.substring(commaStart + 1);

            contentWithoutRequires = begin + afterComma;

        } else {

            contentWithoutRequires = allContent;
        }

        if (JsFilesCollector.useSandboxData) {
            contentWithoutRequires = contentWithoutRequires.replaceAll("getExt3WindowFromExt6Window", "DESKTOP.applyWindowToDesktop");
        }

    }

    private void setExtended() throws Exception {

        int extendStart = allContent.indexOf("extend");

        if (extendStart >= 0) {

            String afterExtend = allContent.substring(extendStart + 6);

            int columnStart = afterExtend.indexOf(":");

            if (columnStart < 0) {
                throw new Exception("[JsFilesCollector] ERROR: missing character ':'");
            }

            String afterColumn = afterExtend.substring(columnStart + 1);

            int commaStart = afterColumn.indexOf(",");

            if (commaStart < 0) {
                throw new Exception("[JsFilesCollector] ERROR: missing character ','");
            }

            extended = afterColumn.substring(0, commaStart);
            extended = extended.replace("'", "");
            extended = extended.replace("\"", "");
            extended = extended.trim();

            if (!extended.substring(0, 3).equals("CRM")) {
                extended = "";
            } else {
                //System.out.println("[JsFilesCollector] Find extended: [" + extended + "] for [" +  name + "]");
            }
        } else {
            //System.out.println("[JsFilesCollector] No extended: [" +  name + "]");
        }
    }

    private String deleteComments(String requires) {

        requires = requires.trim();

        String[] split = requires.split("\\r?\\n");

        requires = "";
        for (String str : split) {

            str = str.trim();

            int index = str.indexOf("//");

            if (index >= 0) {
                str = str.substring(0, index);
                str = str.trim();
            }

            if (!str.equals("")) {
                requires += str + "\n";
            }
        }

        return requires;
    }

    private void setRequiresList() {

        if (requires != "") {

            requires = deleteComments(requires);

            String[] split = requires.split(",");

            for (String str : split) {
                str = str.trim();
                str = str.replace("\"", "");
                str = str.replace("'", "");

                if (!str.equals("")) {
                    requiresList.add(str);
                    //System.out.println("[JsFilesCollector] Find requires: [" + str + "] for [" +  name + "]");
                }
            }
        }
    }

    public ExtJSFile(File file) throws Exception {

        path = JsFilesCollector.useSandboxData
                ? file.getPath().replace("extjs6-sandbox", "CRM")
                : file.getPath();

        name = ExtJSFile.getFileName(path);

        allContent = JsFilesCollector.useSandboxData
                ? ExtJSFile.getAllContent(file).replaceAll("Ext6\\.", "Ext.")
                : ExtJSFile.getAllContent(file);

        this.setRequiresAndContentWithoutRequires();
        this.setRequiresList();
        this.setExtended();
    }
}

public class JsFilesCollector {

    static PrintWriter writer;
    static String root;
    static String outFile;
    static Integer fileCount;
    static Map<String, ExtJSFile> fileMap = new HashMap<>();
    static String appName;
    static boolean useSandboxData;

    private static void fetchFiles(File dir) throws Exception {

        if (dir.isDirectory()) {

            for (File file : dir.listFiles()) {
                fetchFiles(file);
            }

        } else {

            ExtJSFile extJSFile = new ExtJSFile(dir);

            fileMap.put(extJSFile.name, extJSFile);
            fileCount++;
        }
    }

    private static void addSandboxFiles(String baseDir) throws Exception {
        File sandboxAppDir = new File(baseDir + "/src/main/webapp/resources/extjs6-sandbox/app");
        for (File file : sandboxAppDir.listFiles()) {
            if (file.isDirectory()) {
                fetchFiles(file);
            }
        }
    }

    private static void iterateOverAllFiles(String baseDir) throws Exception {
        File file = new File(root);
        fetchFiles(file);
        if (useSandboxData) {
            addSandboxFiles(baseDir);
        }
        //System.out.println("[JsFilesCollector] Total number of files " + fileCount);
    }

    private static void writeFile(String fileName) {

        ExtJSFile extJSFile = fileMap.get(fileName);

        //System.out.println("[JsFilesCollector] Write file: [" + fileName + "]");

        if (extJSFile != null) {

            if (!extJSFile.extended.equals("")) {
                //System.out.println("[JsFilesCollector] Would write extended file: " + extJSFile.extended);
                writeFile(extJSFile.extended);
            }

            for (String key : extJSFile.requiresList) {
                //System.out.println("[JsFilesCollector] Would write requires file: " + key);
                writeFile(key);
            }

            if (!extJSFile.alreadyWritten) {
                writer.println(extJSFile.contentWithoutRequires);
                extJSFile.alreadyWritten = true;
            }

        } else {
            //System.out.println("[JsFilesCollector] No file for requires: " + fileName);
        }
    }

    private static void generateCollectiveFile() throws FileNotFoundException, UnsupportedEncodingException {

        File file = new File(outFile);

        if (file.delete()) {
            System.out.println("File deleted successfully");
        } else {
            System.out.println("Failed to delete the file");
        }

        writer = new PrintWriter(outFile, "UTF-8");
        writeFile(appName + "Application");
        writer.close();
    }

    private static void lookForUnusedFiles() {

        int count = 0;
        for (String key : fileMap.keySet()) {

            ExtJSFile extJSFile = fileMap.get(key);

            if (!extJSFile.alreadyWritten) {
                //System.out.println("[JsFilesCollector] Unused file: " + key);
                count++;
            }
        }
        //System.out.println("[JsFilesCollector] Total number of unused file: " + count);
    }

    private static void utilRun(String rootParam, String outFileParam, String basedir) throws Exception {

        root = rootParam;
        outFile = outFileParam;
        fileMap.clear();
        fileCount = 0;
        appName = getAppName();


        iterateOverAllFiles(basedir);
        generateCollectiveFile();
        lookForUnusedFiles();
    }

    private static String getAppName() throws Exception {
        if (root.contains("extjs6-sandbox")) {
            useSandboxData = false;
            return "CRM.";
        }
        if (root.contains("CRM")) {
            useSandboxData = true;
            return "CRM.";
        }
        if (root.contains("admin-dashboard")) {
            useSandboxData = false;
            return "AdminDashboard.";
        } else {
            throw new Exception("[JsFilesCollector] ERROR: WRONG ROOT. NO APPLICATION NAME DEFINED.");
        }
    }

    public static void main(String basedir) throws Exception {

        String[] args = new String[6];

//        String basedir = "/media/user/af816374-1bd4-488d-869d-af76f772ad10/CRM/crm13";

        args[0] = basedir + "/src/main/webapp/resources/extjs6-sandbox/app";
        args[1] = basedir + "/target/CRM-1.0.0-BUILD-SNAPSHOT/resources/extjs6-sandbox/appCollective.js";
        args[2] = basedir + "/src/main/webapp/resources/admin-dashboard/app";
        args[3] = basedir + "/target/CRM-1.0.0-BUILD-SNAPSHOT/resources/admin-dashboard/appDashboardCollective.js";
        args[4] = basedir + "/src/main/webapp/resources/CRM/app";
        args[5] = basedir + "/target/CRM-1.0.0-BUILD-SNAPSHOT/resources/CRM/appCRMCollective.js";


//        Arrays.stream(args).forEach(s -> System.out.println(s));

        if (args.length != 6) {
            throw new Exception("[JsFilesCollector] ERROR: WRONG PARAMETERS");
        }

        Date start = new Date();

        //System.out.println("[JsFilesCollector] START COLLECT OLD EXT6");
        utilRun(args[0], args[1], basedir);
        //System.out.println("[JsFilesCollector] START COLLECT DASHBOARD");
        utilRun(args[2], args[3], basedir);
        //System.out.println("[JsFilesCollector] START COLLECT NEW EXT6");
        utilRun(args[4], args[5], basedir);
        Date finish = new Date();
        //System.out.println("[JsFilesCollector] Execution time: " + (finish.getTime() - start.getTime()) + " ms");
    }

}
