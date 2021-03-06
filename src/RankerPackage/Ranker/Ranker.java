package RankerPackage.Ranker;

import DataBasePackages.DataBase.DataBase;
import ServletsPackages.ServletPackage.QuerySearch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import RankerPackage.Ranker.*;
import HelpersPackages.Helpers.*;
import java.lang.reflect.Array;
import java.util.*;
import org.json.*;
import java.io.*;
import java.lang.Math.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.sql.*;
import java.sql.ResultSet;

public class Ranker
{
    private DataBase connect = new DataBase();
    JSONArray dividedQuery = new JSONArray();
    Map<String,Long> wordsCount;
    Map<String,Integer> IDs;
    String[] completedLinks;
    Map<String, Double> popularityResult;
    ArrayList<Integer>completedLinksIds=new ArrayList<Integer>();
    int completeCount;

    public Ranker()
    {
        System.out.printf("From ranker constructor");
//        wordsCount = connect.getWordsCountAsMap();
//        completedLinks = connect.getAllUrls();
        completeCount=connect.getCompleteCount();
//        IDs = connect.getIDsAsMap();
        completedLinksIds = connect.getIds();
        calculatePopularity(completeCount);
    }

    public Integer [] removeDuplicateElements(Integer arr[], int n){
        Integer[] temp = new Integer[n];
        if (n==0 || n==1){
            return temp;
        }
        int j = 0;
        for (int i=0; i<n-1; i++){
            if (!Objects.equals(arr[i], arr[i + 1])){
                temp[j++] = arr[i];
            }
        }
        temp[j++] = arr[n-1];
        // Changing original array
        for (int i=0; i<j; i++){
            arr[i] = temp[i];
        }
        return temp;
    }


    public void calculatePopularity(int totalNodes)             //Popularity
    {
        //pagesRank1 ==> store the final results of popularity ( each page and its popularity )
        Map<Integer, Double> pagesRank1 = new HashMap<Integer, Double>();

        //TempPageRank is used to store values of pagesRank1 in it temporarily
        Map<Integer, Double> TempPageRank = new HashMap<Integer, Double>();

        int countEmptyParents=0;

        //calculate the initial value of popularity for all pages
        double InitialPageRank = 1.0 / totalNodes;
        //ITERATION_STEP is used to iterate twice following PageRank Algorithm steps
        // 1/5000
        int ITERATION_STEP = 1;
        while (ITERATION_STEP <= 2) {

            // Store the PageRank for All Nodes in Temporary Map
            for (int k = 0; k < totalNodes; k++) {
                if(ITERATION_STEP==1)
                {
                    TempPageRank.put(completedLinksIds.get(k), InitialPageRank);
                    pagesRank1.put(completedLinksIds.get(k), 0.0);
                }
                else {
                    TempPageRank.put(completedLinksIds.get(k), pagesRank1.get(completedLinksIds.get(k)));
                    pagesRank1.put(completedLinksIds.get(k), 0.0);
                }
            }

            double tempSum = 0;

            for (int currentPage = 0; currentPage < totalNodes; currentPage++)
            {
                double temp=0.0;
                //get all links that lead to the current page
                String LinksOfCurrentPage = connect.getLinknumParent(completedLinksIds.get(currentPage));

                //remove the first ,
                int index =LinksOfCurrentPage.indexOf(",");
                LinksOfCurrentPage = LinksOfCurrentPage.substring(index+1);

                //split the links that lead to the current page
                String[] allLinksOfCurrentPage = LinksOfCurrentPage.split(",");
                if ( allLinksOfCurrentPage[0].equals("") || allLinksOfCurrentPage[0].equals(" "))
                {
                    //Links that does not have a parent links
                    countEmptyParents++;
                    pagesRank1.put(completedLinksIds.get(currentPage), -1.0);
                    continue;
                }

                //convert to int array
                Integer[] IdsAfterRemovingDuplicates = Arrays.stream(allLinksOfCurrentPage)
                        .map(Integer::parseInt)
                        .toArray(Integer[]::new);
                Integer[] IdsAfterRemovingDuplicatesFinal;
                if ( allLinksOfCurrentPage.length == 1 )
                {
                    IdsAfterRemovingDuplicates[0]=Integer.parseInt(allLinksOfCurrentPage[0]);
                }

                //remove duplicates from the array
                IdsAfterRemovingDuplicatesFinal = removeDuplicateElements(IdsAfterRemovingDuplicates,IdsAfterRemovingDuplicates.length);
                if ( IdsAfterRemovingDuplicatesFinal.length == 0 )
                {
                    //Links that does not have a parent links
                    countEmptyParents++;
                    pagesRank1.put(completedLinksIds.get(currentPage), -1.0);
                    continue;
                }
                int k;

                for (k = 0; k < IdsAfterRemovingDuplicatesFinal.length; k++) {
                    //I will send child link and I must get Number of OutgoingLinks of the parent
                    double OutGoingLinks = connect.getOutGoingLinksNum(IdsAfterRemovingDuplicatesFinal[k]);         //Get it from From ==> (Reda) to recieve the number of outgoing links from parent link
                    if (OutGoingLinks == -1 || OutGoingLinks == 0)
                    {
                        //Links that does not have a parent links
                        countEmptyParents++;
                        pagesRank1.put(completedLinksIds.get(currentPage), -1.0);
                        continue;
                    }
                    //System.out.println((TempPageRank.get(IdsAfterRemovingDuplicatesFinal[k])));
                    if (TempPageRank.get(IdsAfterRemovingDuplicatesFinal[k])==null)
                    {
                        //Links that does not have a parent links
                        countEmptyParents++;
                        pagesRank1.put(completedLinksIds.get(currentPage), -1.0);
                        continue;
                    }
                    temp = temp + (TempPageRank.get(IdsAfterRemovingDuplicatesFinal[k])) * (1.0 / OutGoingLinks) ;
                }

                pagesRank1.put(completedLinksIds.get(k), temp);
                tempSum += pagesRank1.get(completedLinksIds.get(k));
            }

            //Special handling for the first page only as there is no outgoing links to it

            double temp = 1 - tempSum;
            countEmptyParents = countEmptyParents/2;
            double slice = temp / countEmptyParents;
            for ( int i=0 ; i< totalNodes ; i++ )
            {
                if ( pagesRank1.get(completedLinksIds.get(i)) == -1 )
                {
                    pagesRank1.put(completedLinksIds.get(i),slice );
                }
            }

            ITERATION_STEP++;
        }

        // Add the Damping Factor to PageRank
        double DampingFactor = 0.75;
        double temp = 0;
        for (int k = 0; k < totalNodes; k++) {
            temp = (1 - DampingFactor) + DampingFactor * pagesRank1.get(completedLinksIds.get(k));
            temp = temp * 1000;
            //System.out.println("temp : " + temp);
        }


        for (int k = 0; k < totalNodes; k++) {
            temp = (1 - DampingFactor) + DampingFactor * pagesRank1.get(completedLinksIds.get(k));
            WorkingFiles.addPopularityToFile(completedLinksIds.get(k),temp);
        }
    }

    public String calculateRelevance(ArrayList<String> tempLines , Map<String, Integer> allLinks , boolean isPhraseSearching) throws FileNotFoundException, JSONException {

        JSONArray finalJsonFile = new JSONArray();   //For final results
        Map<String, Double> pagesRanks = new HashMap<String, Double>();
        double tf = 0.0,
                idf = 0.0,
                tf_idf = 0.0,
                numOfOccerrencesInCurrentDocument = 0.0, // this value will be weighted
                numOfOccerrencesInAllDocuments = 0.0;
        int counterForWords = 0;
        //tempMap is used to store each link and its tf-idf value
        Map<String, Double> allLinksTf_Idf = new HashMap<String, Double>();
        ArrayList<String> uniqueLinks = new ArrayList<String>();


        //

        for (int i = 0; i < tempLines.size(); i++) {
            Map<String, Double> Links_numOfOccurrences = new HashMap<String, Double>();
            //to make priority between title,header,paragraph
            double coeff = 0.0;

            int startIndex = tempLines.get(i).indexOf('|');
            String lineWithoutTheWord = tempLines.get(i).substring(startIndex + 1);
            String[] linksWithWordPosition = lineWithoutTheWord.split(";");

            //array to store all links of the current query
            ArrayList<String> arr = new ArrayList<String>();
            int counter =0;
            //iterate over the links of each word in the query
            for (int j = 0; j < linksWithWordPosition.length; j++) {


                //to get id of current page
                int bracePosition = linksWithWordPosition[j].indexOf('[');
                String linkOfCurrentPage = linksWithWordPosition[j].substring(bracePosition + 1, linksWithWordPosition[j].indexOf(','));

                if(isPhraseSearching && allLinks.containsKey(linkOfCurrentPage) || !isPhraseSearching)
                {
                    //get the length of the page
                    Long lengthOfPage = wordsCount.get(linkOfCurrentPage);

                    //to get the type of the word ==> paragraph or title or strong or header
                    int separetorPosition = linksWithWordPosition[j].indexOf(',');
                    char wordType = linksWithWordPosition[j].charAt(separetorPosition + 1);

                    if (wordType == 't')                                   //title
                        coeff = 1.0 / 2.0;
                    else if (wordType == 'h' || wordType == 's')         //header or strong
                        coeff = 1.0 / 4.0;
                    else                                                    //paragraph
                        coeff = 1.0 / 8.0;

                    //to get number of occurrences of each word
                    int countSeperator = linksWithWordPosition[j].indexOf("]::");
                    String wordCount = linksWithWordPosition[j].substring(countSeperator + 3);
                    numOfOccerrencesInCurrentDocument = coeff * Integer.parseInt(wordCount);
                    numOfOccerrencesInAllDocuments += coeff * Integer.parseInt(wordCount);

                    if (lengthOfPage != null && lengthOfPage != 0) {
                        tf = Double.valueOf(numOfOccerrencesInCurrentDocument) / lengthOfPage;
                        if (Links_numOfOccurrences.containsKey(linkOfCurrentPage)) {
                            double tempTf = Links_numOfOccurrences.get(linkOfCurrentPage).doubleValue();
                            tf += tempTf;
                        } else {
                            arr.add(linkOfCurrentPage);
                        }
                        Links_numOfOccurrences.put(linkOfCurrentPage, tf);
                    }
                    tf = 0;
                }
                //
            }

            counterForWords++;

            //calculate the idf value of the page
            idf = completeCount / Double.valueOf(numOfOccerrencesInAllDocuments);                                      // 5100 ==> number of indexed web pages

            // the map will contain the link with its tf_idf
            for (int h = 0; h < arr.size(); h++) {
                tf_idf=0;
                if(allLinksTf_Idf.containsKey(arr.get(h)))
                {
                    tf_idf+=allLinksTf_Idf.get(arr.get(h));
                }
                else
                {
                    uniqueLinks.add(arr.get(h));
                }

                tf_idf += idf * Links_numOfOccurrences.get(arr.get(h));
                allLinksTf_Idf.put(arr.get(h), tf_idf);
            }
            // to the new word
            numOfOccerrencesInAllDocuments=0;

        }
        System.out.println("the map "+ allLinksTf_Idf+"\n \n \n");


        for(int i=0;i<uniqueLinks.size();i++)
        {
            allLinksTf_Idf.put(uniqueLinks.get(i),(0.7*allLinksTf_Idf.get(uniqueLinks.get(i))+0.3*popularityResult.get(uniqueLinks.get(i))));
        }
        // will need to use the popularty here
        allLinksTf_Idf.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> allLinksTf_Idf.put(x.getKey(), x.getValue()));
        System.out.println("the map "+ allLinksTf_Idf+"\n \n \n");


        for (Iterator<Map.Entry<String, Double>> iter = allLinksTf_Idf.entrySet().iterator(); iter.hasNext(); ) {


            Map.Entry<String, Double> LinkEntry = iter.next();


            String link = LinkEntry.getKey();

            // Get description and populate Json Array
            String description;
            JSONObject Jo = new JSONObject();
            if(IDs.containsKey(link)) {
                int currentLinkID = IDs.get(link);
                description = HelperClass.readDescription(currentLinkID);
                Jo.put("Link", link);
                Jo.put("Description", description);
                finalJsonFile.put(Jo);
            }
        }


        return finalJsonFile.toString();
    }



}