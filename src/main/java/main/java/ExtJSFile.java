package main.java;


import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class ExtJSFile {
    String name;
    String path;
    String allContent;
    String contentWithoutRequires;
    String requires = "";
    List<String> requiresList = new ArrayList<>();
    String extended = "";

    boolean alreadyWritten = false;

    public ExtJSFile(File file) throws Exception {

        path = JsFilesCollector.useSandboxData
                ? file.getPath().replace("extjs6-sandbox", "CRM")
                : file.getPath();

        name = ExtJSFile.getFileName(path);

        allContent = JsFilesCollector.useSandboxData
                ? ExtJSFile.getAllContent(file).replaceAll("Ext6\\.", "Ext.")
                : ExtJSFile.getAllContent(file);

        setRequiresAndContentWithoutRequires();
        setRequiresList();
        setExtended();
    }

    private static String getFileName(String absolutePath) {
        Path pathRelative = Paths.get(JsFilesCollector.root).relativize(Paths.get(absolutePath));
        String pathRelativeString = pathRelative.toString();
        pathRelativeString = pathRelativeString.replace("/", ".");
        pathRelativeString = pathRelativeString.replace(".js", "");
        return JsFilesCollector.appName + pathRelativeString;
    }

    private static String getAllContent(File file) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
            return new String(encoded, StandardCharsets.UTF_8);
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
                throw new Exception("ERROR: missing character ':'");
            }

            String afterColumn = afterRequires.substring(columnStart + 1);

            int openBreakStart = afterColumn.indexOf("[");

            if (openBreakStart < 0) {
                throw new Exception("ERROR: missing character '['");
            }

            String afterOpenBreak = afterColumn.substring(openBreakStart + 1);

            int closeBreakStart = afterOpenBreak.indexOf("]");

            if (closeBreakStart < 0) {
                throw new Exception("ERROR: missing character ']'");
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
            contentWithoutRequires = contentWithoutRequires.replaceAll(
                    "getExt3WindowFromExt6Window", "DESKTOP.applyWindowToDesktop"
            );
        }
    }

    private void setExtended() throws Exception {

        int extendStart = allContent.indexOf("extend");

        if (extendStart >= 0) {

            String afterExtend = allContent.substring(extendStart + 6);

            int columnStart = afterExtend.indexOf(":");

            if (columnStart < 0) {
                throw new Exception("ERROR: missing character ':'");
            }

            String afterColumn = afterExtend.substring(columnStart + 1);

            int commaStart = afterColumn.indexOf(",");

            if (commaStart < 0) {
                throw new Exception("ERROR: missing character ','");
            }

            extended = afterColumn.substring(0, commaStart);
            extended = extended.replace("'", "");
            extended = extended.replace("\"", "");
            extended = extended.trim();

            if (!extended.substring(0, 3).equals("CRM")) {
                extended = "";
            } else {
                //System.out.println("Find extended: [" + extended + "] for [" +  name + "]");
            }
        } else {
            //System.out.println("No extended: [" +  name + "]");
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
                    //System.out.println("Find requires: [" + str + "] for [" +  name + "]");
                }
            }
        }
    }
}