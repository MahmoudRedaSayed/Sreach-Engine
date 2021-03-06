package HelpersPackages.Helpers;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperClass {


    // get the path of the inverted Files
    public static String invertedFilePath(String fileName)
    {
        String filePath = Paths.get("").normalize().toAbsolutePath().toString();
        filePath += File.separator + "InvertedFiles_V3" + File.separator + fileName + ".txt";
        return filePath;
    }

    // get the path of the inverted Files_V2
    public static String invertedFilePath_V2(String fileName)
    {
        String filePath = Paths.get("").normalize().toAbsolutePath().toString();
        filePath += File.separator + "InvertedFiles_V3" + File.separator + fileName + ".txt";
        return filePath;
    }

    // get the path of the inverted Files_V3
    public static String invertedFilePath_V3(String fileName)
    {
//        String filePath = Paths.get("").normalize().toAbsolutePath().toString();
        String filePath = "D:\\Study\\Second Year\\Second Sem\\APT\\New folder (2)\\New folder (2)\\Sreach-Engine";
        //filePath = filePath.substring(0, filePath.lastIndexOf("\\"));
        filePath += File.separator + "InvertedFiles_V3" + File.separator + fileName + ".txt";
        return filePath;
    }

    // get the path of the inverted Files_V3 folder
    public static String invertedFilePathDirectoryPath()
    {
        String filePath = "D:\\Study\\Second Year\\Second Sem\\APT\\New folder (2)\\New folder (2)\\Sreach-Engine";
        // filePath = filePath.substring(0, filePath.lastIndexOf("\\"));
        filePath += File.separator + "InvertedFiles_V3";
        return filePath;
    }

    // get the path of the content files
    public static String contentFilesPath()
    {
        String filePath = "D:\\Study\\Second Year\\Second Sem\\APT\\New folder (2)\\New folder (2)\\Sreach-Engine";
        // filePath = filePath.substring(0, filePath.lastIndexOf("\\"));
        filePath += File.separator + "ContentFiles";
        return filePath;
    }

    public static String populairtyFilesPath()
    {
        String filePath = "D:\\Study\\Second Year\\Second Sem\\APT\\New folder (2)\\New folder (2)\\Sreach-Engine";
        // filePath = filePath.substring(0, filePath.lastIndexOf("\\"));
        filePath += File.separator + "PopularityFiles";
        return filePath;
    }

    // get the path of the description files
    public static String descriptionFilesPath()
    {
        String filePath = "D:\\Study\\Second Year\\Second Sem\\APT\\New folder (2)\\New folder (2)\\Sreach-Engine";
        // filePath = filePath.substring(0, filePath.lastIndexOf("\\"));
        filePath += File.separator + "descriptionFiles";
        return filePath;
    }

    // get the content which stored in a file
    public static String readContent(int fileName)
    {
        Path filePath = Path.of(contentFilesPath() + File.separator + fileName + ".txt");
        String content = null;
        try {
            content = Files.readString(filePath);
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    // get the description which stored in a file
    public static String readDescription(int fileName)
    {
        Path filePath = Path.of(descriptionFilesPath() + File.separator + fileName + ".txt");
        String content = null;
        try {
            content = Files.readString(filePath);
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    // get the content which stored in a file
    public static String readdescription(int fileName)
    {
        Path filePath = Path.of(contentFilesPath() + File.separator + fileName + ".txt");
        String content = null;
        try {
            content = Files.readString(filePath);
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    // get the path of the content length files
    public static String contentLengthFiles(String fileName)
    {
        String filePath = Paths.get("").normalize().toAbsolutePath().toString();
        filePath += File.separator + "ContentLength" + File.separator + fileName + ".txt";
        return filePath;
    }

    // check if a given word is existing in a given inverted file or not
    // returns the whole line that contains this word
    public synchronized static String isExistingInFile(String word, File myFile) throws IOException {
        Scanner read = new Scanner(myFile);
        String tempInput;
        String wordInFile = null;
        boolean found = false;
        while(read.hasNextLine() && !found)
        {
            tempInput = read.nextLine();
            if (tempInput.equals(""))
                continue;

            // check if this line is for a word or just an extension for the previous line
            if (tempInput.charAt(0) == '<')
            // compare to check if this word = ourWord ?
            {
//                // get the word
                if (tempInput.indexOf('|') < 0)
                    return "";
                wordInFile = tempInput.substring(1, tempInput.indexOf('|'));
                int wordSize = word.length();
//                char ch = tempInput.charAt(1);      // just initialization
//                boolean matchingFlag = true;
//
//                int i;
//                for (i = 0; i < wordSize; i++)
//                    if(tempInput.charAt(i+1) != word.charAt(i))
//                        break;
//
//                if(i == wordSize)
//                    return tempInput;
            if (wordInFile.equals(word))
                return tempInput;
            }
        }
        return "";      // if not found, return empty
    }

    // this function replaces a line in a given inverted file
    public synchronized static void replaceLineInFile(Path path, String oldLine, String newLine) throws IOException {
        List<String>fileContents = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        int ContentSize = fileContents.size();

        for (int i = 0; i < ContentSize; i++)
        {
            if(fileContents.get(i).equals(oldLine)) {
                fileContents.set(i, newLine);
                break;
            }
        }
        Files.write(path, fileContents, StandardCharsets.UTF_8);
    }

    // stem the word using Porter Stemmer Lib
    public static String stemTheWord(String word)
    {
        PorterStemmer stemObject = new PorterStemmer();
        stemObject.setCurrent(word);
        stemObject.stem();
        return stemObject.getCurrent();
    }

    // check if the word is arabic
    public static boolean isProbablyArabic(String s) {
        for (int i = 0; i < s.length();) {
            int c = s.codePointAt(i);
            if (c >= 0x0600 && c <= 0x06E0)
                return true;
            i += Character.charCount(c);
        }
        return false;
    }

    // this function checks if the info is already exist or not,
    // and if exists, just increment the counter of occurrences
    public synchronized static String updateInfoOfWord(String line, String oldInfo) {

        // substring the line to get the needed information
        int separationIndex = line.indexOf('|');
        String allInfo = line.substring(separationIndex + 1);

        // explode the info
        List<String> infoList = new ArrayList<>(List.of(allInfo.split(";", 0)));
        String theNewInfo;

        for (String info : infoList) {

            // split the frequency counter from the info of the word
            List<String> tempList = new ArrayList<>(List.of(info.split("::", 0)));

            // check if the same info is existing or not
            if (tempList.get(0).equals(oldInfo)) {
                String frequency = tempList.get(1);
                int integerFrequency = Integer.parseInt(frequency);
                theNewInfo = tempList.get(0) + "::" + String.valueOf(integerFrequency + 1); /* convert the ( int freq + 1 ) to string here */
                oldInfo = oldInfo + "::" + frequency;
                line = line.replace(oldInfo , theNewInfo);
                return line;
            }
        }

        // if not returned, then the info is not exist
        theNewInfo = oldInfo + "::1";
        line += theNewInfo + ';';
        return line;

    }

    // this function gets the snippets of the result links based on the query that the user entered
    // this function gets the snippets of the result links based on the query that the user entered
    public static Map<String, String> getSnippet(String[] queryWords, ArrayList<String> resultLinks, Map<String, String> linkParagraphs)
    {
        Map<String, String> result = new HashMap<>();
        int wordsSize = queryWords.length,
                linksSize = resultLinks.size();

        String currentParagraph = null,
                snippetParagraph = null,
                fullParagraphs   = null;

        for (int i = 0; i < wordsSize; i++)
        {
            for (int j = 0; j < linksSize; j++)
            {
                String currentLink = resultLinks.get(j);
                if (result.containsKey(currentLink)) // we need just one snippet for each link, so if we already get a snippet ,then continue
                    continue;

                fullParagraphs = linkParagraphs.get(currentLink);
                // split paragraphs
                if (fullParagraphs != null)
                {
                    String[] separatedParagraphs = fullParagraphs.split("\\S.&\\S");

                    int size = separatedParagraphs.length;

                    for (int k = 0; k < size; k++)
                    {
                        currentParagraph = separatedParagraphs[k];
//                        if (currentParagraph.contains(queryWords[i]))
                        if (isContain(currentParagraph, queryWords[i]))
                        {
                            if(currentParagraph.charAt(0) == '[')
                                currentParagraph = currentParagraph.substring(1);

                            else if (currentParagraph.charAt(0)== '.' && currentParagraph.charAt(1)== '&')
                                currentParagraph = currentParagraph.substring(3);

                            result.put(currentLink, splitTo30Words(currentParagraph));
                            break;      // because i need just one snippet, if found don't continue to the other paragraphs in this link
                        }
                    }

                }
            }
        }
        return result;
    }

    // this functions get only 30 words from the text
    public static String splitTo30Words(String str)
    {
        String[] arr = str.split(" ");

        if (arr.length <= 30)
            return str;

        String result = arr[0];
        for (int i = 1; i < 30; i++)
            result += " " + arr[i];

        return result;
    }

    public static boolean isContain(String source, String subItem){
        String pattern = "\\b"+subItem+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(source);
        return m.find();
    }

    public static Map<Integer, String> getAllContent(Map<String, Integer>IDs)
    {
        Map<Integer, String> result = new HashMap<>();
        String content;
        Integer id;
        for (Iterator<Map.Entry<String, Integer>> it = IDs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            id=IDs.get(entry.getKey());
            content = readContent(id);
            result.put(id, content);
        }
        return result;
    }

    public static Map<Integer, Double> getAllPopularity(Map<String, Integer>IDs)
    {
        Map<Integer,Double> result = new HashMap<>();
        String content;
        Integer id;
        for (Iterator<Map.Entry<String, Integer>> it = IDs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            id=IDs.get(entry.getKey());
            content = readPopularity(id);
            if ( content.equals("")  || content.equals(" ") || content.equals(null)   )
            {
                continue;
            }
            result.put(id, Double.parseDouble(content));
        }
        return result;
    }

    // function to read popularity
    public static String readPopularity(int fileName)
    {
        Path filePath = Path.of(populairtyFilesPath() + File.separator + fileName + ".txt");
        if(!filePath.toFile().exists())
        {
            return " ";
        }
        String content = null;
        try {
            content = Files.readString(filePath);
            return content;
        } catch (IOException e) {
            return null;
        }
    }

}


