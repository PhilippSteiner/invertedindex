
import models.Tuple;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.BreakIterator;

public class Main {

    private static String directory = "/Users/Philipp/Google Drive/FH/Master/Semester 2/Information Retrieval/IR_Aufgabe_1/reuters21578/";
    private static String dictionary_directory = "/Users/Philipp/Google Drive/FH/Master/Semester 2/Information Retrieval/IR_Aufgabe_1/";

    private static HashMap<String, List<Tuple>> index = new HashMap<String, List<Tuple>>();
    private static long time;
    private static ArrayList<File> files = new ArrayList<File>();

    /*
     * Im folgenden Program können 2 verschiedene Varianten verwendetwerden um
      * einen invertierten Index aus einem Directory an Dateien zu erstellen.
      *
      *  Variante 1:
      *
      *  1. Terme werden aus allen Dateien extrahiert
      *  2. Terme werden sortiert
      *  3. Duplikate Terme werden entfernt
      *  4. Position Index wird für jede Datei angelegt
      *
      *  Variante 2:
      *
      *  1. Terme werden pro Datei mit Position Index indiziert
      *  2. Bestehender Index wird mit jeder weiteren Datei gemerged
     *
    * */

    public static void main(String[] args) {

        System.out.print("Bitte wählen Sie eine Variante zum erstellen des Index aus (1/2): ");
        Scanner sc = new Scanner(System.in);
        int variante = sc.nextInt();

        System.out.println("Start Time: 00:00:00");
        time = System.currentTimeMillis();
        files = getFiles(directory);

        switch (variante) {
            case 1:
                variante1();
                break;
            case 2:
                variante2();
                break;
            default:
                System.out.println("Falsche Eingabe - Program Ende");
                break;
        }

        System.out.print("Nach welchem Term soll gesucht werden: ");
        sc = new Scanner(System.in);
        String searchTerm = sc.nextLine();
        searchTerm(searchTerm);

    }

    private static void variante1() {
        printTime("Generating List...");
        List<String> sortedWordList = getSortedWordsList();
        createDictionaryFile(sortedWordList);
        printTime("Finished...Size: " + sortedWordList.size());
        printTime("Generating Posting List...");
        generatePostingLists(sortedWordList);
        printTime("Finished!");
    }

    private static List<String> getSortedWordsList() {
        List<String> wordList = new ArrayList<String>();
        for (File file : files) {
            wordList.addAll(getWordsFromFile(file));
        }
        /* Wörter sortieren */
        Collections.sort(wordList);
        /* Dublikate entfernen */
        return new ArrayList<String>(new LinkedHashSet<String>(wordList));
    }

    private static void generatePostingLists(List<String> sortedWordList) {
        for (File file : files) {
            printTime("Processing File " + files.indexOf(file));
            indexFile(file, sortedWordList);
            /* Für Test Zwecke kann hier nach einem Term gesucht werden */
            searchTerm("do");
        }
    }

    private static void indexFile(File file, List<String> sortedWordList) {
        int position = 0;
        List<String> wordsFromFile = getWordsFromFile(file);
        printTime("Number words: " + wordsFromFile.size());
        for (String word : wordsFromFile) {
            if (sortedWordList.contains(word)) {
                List<Tuple> positionList = index.get(word);
                if (positionList == null) {
                    positionList = new LinkedList<Tuple>();
                    index.put(word, positionList);
                }
                positionList.add(new Tuple(files.indexOf(file), position));
            }
            position++;
        }
    }

    private static void searchTerm(String term) {
        if (index.containsKey(term)) {
            printTime("Term: " + term);
            for (Tuple tuple : index.get(term)) {
                System.out.println("Document Number: " + tuple.getNumber() + " Position: " + tuple.getPosition());
            }
        } else {
            printTime("Term is not in any file!");
        }
    }

    private static void variante2() {
        createTermsFromFiles();
        printTime("Finished!");
    }

    private static ArrayList<File> getFiles(String directory) {
        File dir = new File(directory);
        return new ArrayList<File>(Arrays.asList(dir.listFiles()));
    }

    private static void createTermsFromFiles() {
        for (File file : files) {
            printTime("Processing File " + files.indexOf(file));
            index = mergeTerms(index, getWordsFromFile(file), files.indexOf(file));
            printTime("Index Size:" + index.size());
        }
    }

    private static HashMap<String, List<Tuple>> mergeTerms(HashMap<String, List<Tuple>> terms, List<String> words, int fileNumber) {
        int position = 0;
        for (String word : words) {
            position++;
            if (terms.containsKey(word)) {
                terms.get(word).add(new Tuple(fileNumber, position));
            } else {
                terms.put(word, new ArrayList<Tuple>());
                terms.get(word).add(new Tuple(fileNumber, position));
            }
        }
        return terms;
    }

    private static List<String> getFileNames(String directory) {
        List<String> textFiles = new ArrayList<String>();
        File dir = new File(directory);
        for (File file : dir.listFiles()) {
            textFiles.add(file.getName());
        }
        return textFiles;
    }

    private static List<String> getWordsFromFile(File file) {
        return getWordsFromString(getStringFromFile(file));
    }

    private static String getStringFromFile(File file) {
        String text = "";
        try {
            text = FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    private static List<String> getWordsFromString(String text) {
        List<String> words = new ArrayList<String>();
        BreakIterator breakIterator = BreakIterator.getWordInstance();
        breakIterator.setText(text);
        int lastIndex = breakIterator.first();
        while (BreakIterator.DONE != lastIndex) {
            int firstIndex = lastIndex;
            lastIndex = breakIterator.next();
            if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(text.charAt(firstIndex))) {
                words.add(text.substring(firstIndex, lastIndex).toLowerCase());
            }
        }
        return words;
    }

    private static void printTime(String text) {
        Date date = new Date(System.currentTimeMillis() - time);
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss:SSS");
        System.out.println("Time: " + formatter.format(date) + " " + text);
    }

    private static void createDictionaryFile(List<String> sortedList) {
        File newFile = new File(dictionary_directory, "dictionary_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".txt");
        try {
            newFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            for (String word : sortedList) {
                writer.write(word);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
