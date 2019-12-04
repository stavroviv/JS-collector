package main.java;

import java.io.*;
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
                throw new Exception("[main.java.JsFilesCollector] ERROR: missing character ':'");
            }

            String afterColumn = afterRequires.substring(columnStart + 1);

            int openBreakStart = afterColumn.indexOf("[");

            if (openBreakStart < 0) {
                throw new Exception("[main.java.JsFilesCollector] ERROR: missing character '['");
            }

            String afterOpenBreak = afterColumn.substring(openBreakStart + 1);

            int closeBreakStart = afterOpenBreak.indexOf("]");

            if (closeBreakStart < 0) {
                throw new Exception("[main.java.JsFilesCollector] ERROR: missing character ']'");
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
                throw new Exception("[main.java.JsFilesCollector] ERROR: missing character ':'");
            }

            String afterColumn = afterExtend.substring(columnStart + 1);

            int commaStart = afterColumn.indexOf(",");

            if (commaStart < 0) {
                throw new Exception("[main.java.JsFilesCollector] ERROR: missing character ','");
            }

            extended = afterColumn.substring(0, commaStart);
            extended = extended.replace("'", "");
            extended = extended.replace("\"", "");
            extended = extended.trim();

            if (!extended.substring(0, 3).equals("CRM")) {
                extended = "";
            } else {
                //System.out.println("[main.java.JsFilesCollector] Find extended: [" + extended + "] for [" +  name + "]");
            }
        } else {
            //System.out.println("[main.java.JsFilesCollector] No extended: [" +  name + "]");
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
                    //System.out.println("[main.java.JsFilesCollector] Find requires: [" + str + "] for [" +  name + "]");
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

    static StringBuilder sb;
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
        //System.out.println("[main.java.JsFilesCollector] Total number of files " + fileCount);
    }

    private static void writeFile(String fileName) {

        ExtJSFile extJSFile = fileMap.get(fileName);

        //System.out.println("[main.java.JsFilesCollector] Write file: [" + fileName + "]");

        if (extJSFile != null) {

            if (!extJSFile.extended.equals("")) {
                //System.out.println("[main.java.JsFilesCollector] Would write extended file: " + extJSFile.extended);
                writeFile(extJSFile.extended);
            }

            for (String key : extJSFile.requiresList) {
                //System.out.println("[main.java.JsFilesCollector] Would write requires file: " + key);
                writeFile(key);
            }

            if (!extJSFile.alreadyWritten) {
                sb.append(extJSFile.contentWithoutRequires).append("\n");
                extJSFile.alreadyWritten = true;
            }

        } else {
            //System.out.println("[main.java.JsFilesCollector] No file for requires: " + fileName);
        }
    }

    private static StringBuilder getStringBuilderByFile(String filePath) {

        File tempFile = new File(filePath);
        boolean exists = tempFile.exists();

        if (!exists) {
            return new StringBuilder("-1");
        }

        try {

            StringBuilder contentBuilder = new StringBuilder();

            BufferedReader br = new BufferedReader(new FileReader(tempFile));

            String st;
            while ((st = br.readLine()) != null) {
                contentBuilder.append(st).append("\n");
            }

            return contentBuilder;

        } catch (IOException e) {
            e.printStackTrace();
            return new StringBuilder("-1");
        }
    }

    private static boolean generateCollectiveFile() throws FileNotFoundException, UnsupportedEncodingException {

        sb = new StringBuilder("");
        writeFile(appName + "Application");

        StringBuilder sbOld = getStringBuilderByFile(outFile);

        int hash1 = sb.toString().hashCode();
        int hash2 = sbOld.toString().hashCode();

//        System.out.println("new " + hash1);
//        System.out.println("old " + hash2);

        boolean bool = (hash1 == hash2);

        if (!bool) {
            PrintWriter writer = new PrintWriter(outFile, "UTF-8");
            writer.print(sb.toString());
            writer.close();
        }

        return bool;
    }

    private static boolean utilRun(String rootParam, String outFileParam, String basedir) throws Exception {

        root = rootParam;
        outFile = outFileParam;
        fileMap.clear();
        fileCount = 0;
        appName = getAppName();

        iterateOverAllFiles(basedir);
        return generateCollectiveFile();
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
            throw new Exception("[main.java.JsFilesCollector] ERROR: WRONG ROOT. NO APPLICATION NAME DEFINED.");
        }
    }

    public static boolean main(String basedir, String targetDirectoryName) throws Exception {

        String[] args = new String[6];

//        String basedir = "/media/user/af816374-1bd4-488d-869d-af76f772ad10/CRM/crm13";

        args[0] = basedir + "/src/main/webapp/resources/extjs6-sandbox/app";
        args[1] = basedir + "/target/" + targetDirectoryName + "/resources/extjs6-sandbox/appCollective.js";
        args[2] = basedir + "/src/main/webapp/resources/admin-dashboard/app";
        args[3] = basedir + "/target/" + targetDirectoryName + "/resources/admin-dashboard/appDashboardCollective.js";
        args[4] = basedir + "/src/main/webapp/resources/CRM/app";
        args[5] = basedir + "/target/" + targetDirectoryName + "/resources/CRM/appCRMCollective.js";


//        Arrays.stream(args).forEach(s -> System.out.println(s));

        if (args.length != 6) {
            throw new Exception("[main.java.JsFilesCollector] ERROR: WRONG PARAMETERS");
        }

        Date start = new Date();

        //System.out.println("[main.java.JsFilesCollector] START COLLECT OLD EXT6");
        boolean bool1 = utilRun(args[0], args[1], basedir);
        //System.out.println("[main.java.JsFilesCollector] START COLLECT DASHBOARD");
        boolean bool2 = utilRun(args[2], args[3], basedir);
        //System.out.println("[main.java.JsFilesCollector] START COLLECT NEW EXT6");
        boolean bool3 = utilRun(args[4], args[5], basedir);
        Date finish = new Date();
        //System.out.println("[main.java.JsFilesCollector] Execution time: " + (finish.getTime() - start.getTime()) + " ms");

        return (bool1 && bool2 && bool3);
//        return bool1;
    }

}
