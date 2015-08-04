package alef.uchicago.edu;

//region Imports

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//endregion

public class ThreadedController implements Initializable{

    //region FXMLAnnotatons

    @FXML // fx:id="docxBox"
    private CheckBox docxBox; // Value injected by FXMLLoader

    @FXML // fx:id="pptBox"
    private CheckBox pptBox; // Value injected by FXMLLoader

    @FXML // fx:id="xlsxBox"
    private CheckBox xlsxBox; // Value injected by FXMLLoader

    @FXML // fx:id="pdfBox"
    private CheckBox pdfBox; // Value injected by FXMLLoader

    @FXML // fx:id="txtBox"
    private CheckBox txtBox; // Value injected by FXMLLoader

    @FXML // fx:id="docBox"
    private CheckBox docBox; // Value injected by FXMLLoader

    @FXML // fx:id="csvBox"
    private CheckBox csvBox; // Value injected by FXMLLoader

    @FXML // fx:id="mnuQuit"
    private MenuItem mnuQuit; // Value injected by FXMLLoader

    @FXML // fx:id="lblResult"
    private Label lblResult; // Value injected by FXMLLoader

    @FXML // fx:id="btnGo"
    private Button btnGo; // Value injected by FXMLLoader

    @FXML // fx:id="lstView"
    private ListView<String> lstView; // Value injected by FXMLLoader

    @FXML // fx:id="lblTitle"
    private Label lblTitle; // Value injected by FXMLLoader

    @FXML // fx:id="table"
    private TableView<DownTask> table; // Value injected by FXMLLoader

    @FXML // fx:id="txtSearch"
    private TextField txtSearch; // Value injected by FXMLLoader

    @FXML
    private TextField exactPhrase;

    @FXML
    private TextField notTheseWords;

    @FXML // fx:id="btnSelect"
    private Button btnSelect; // Value injected by FXMLLoader

    //endregion

    private String filePath;
    private String strDirSave;
    private File researchDirectory;
    private int mostRelevantFile;
    private List<String> docTypeForDownload;

    private HashMap<String, Integer> numberOfSearchesByFilePath = new HashMap<>();
    private String containsSomeOfTheseWordsSearch;
    private String containsNoneOfTheseWords;
    private String containsThisExactPhrase;

    @FXML
    void btnGoAction(ActionEvent event) {

    }

    @FXML
    void mnuQuitAction(ActionEvent event) {

        System.exit(0);
    }

    public class GetReviewsTask extends Task<ObservableList<String>> {

        private String convertSpacesToPluses(String strOrig) {
            return strOrig.trim().replace(" ", "+");
        }
        private String convertSpacesToPlusAndMinus(String strOrig) {return strOrig.trim().replace(" ", "+-");}

        @Override
        protected ObservableList<String> call() throws Exception{

            Map<CheckBox, String> typeOfDocument = new HashMap<>();
            typeOfDocument.put(pptBox, "ppt"); typeOfDocument.put(docBox, "doc"); typeOfDocument.put(docxBox, "docx"); typeOfDocument.put(pdfBox, "pdf"); typeOfDocument.put(xlsxBox, "xlsx");
            typeOfDocument.put(txtBox, "txt"); typeOfDocument.put(csvBox, "csv");

            //Ensures the search query will be run on all selected document types
            docTypeForDownload = typeOfDocument.entrySet().stream()
                    .filter(e -> e.getKey().isSelected())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            ArrayList<String> combinedList = new ArrayList<>();
            ArrayList<String> processedTypeList = new ArrayList<>();
            for (String docType: docTypeForDownload)
            {
               combinedList.addAll(runSearchQuery(docType));
            }
            //Ensures our titles will not include any words that we want excluded.
            String[] wordsExcluded = notTheseWords.getText().split(" ");
            Boolean fileDownloadCheck  = true;
            for (String result: combinedList){
                fileDownloadCheck = true;
                for (String wordRemoved : wordsExcluded){
                    System.out.println(wordsExcluded.length);
                    System.out.println(wordRemoved);
                    if (!wordRemoved.isEmpty() && result.toLowerCase().indexOf(wordRemoved.toLowerCase()) > -1){
                        fileDownloadCheck = false;
                    }
                }
                if (fileDownloadCheck){
                    processedTypeList.add(result);
                }
            }
                return FXCollections.observableArrayList(processedTypeList);
        }

        public ArrayList<String> runSearchQuery(String docTypeWanted) {

            ObservableList<String> sales = FXCollections.observableArrayList();
            updateMessage("Finding files...");
            containsSomeOfTheseWordsSearch = txtSearch.getText();
            containsNoneOfTheseWords = "+-" + notTheseWords.getText().trim();
            containsThisExactPhrase =  "+%22" + exactPhrase.getText()+"%22";
            String strUrl = "https://www.google.com/search?q=";

            strUrl += convertSpacesToPluses(containsSomeOfTheseWordsSearch);
            if(!containsNoneOfTheseWords.equals(" -"))
            {
                strUrl += convertSpacesToPlusAndMinus(containsNoneOfTheseWords);
            }
            strUrl += convertSpacesToPluses(containsThisExactPhrase);
            strUrl += "+filetype:" + docTypeWanted;
            System.out.println(strUrl);

            Document doc;
            ArrayList<String> strResults = new ArrayList<>();

            try {
                doc = Jsoup.connect(strUrl).userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
                Elements hrefs = doc.select("a[href]");
                for (Element href : hrefs) {
                    String strRef = href.attr("abs:href");

                    if (strRef.contains("." + docTypeWanted) && strRef.contains("https://www.google.com/url?q=")) {

                        String strFileToDownload = strRef.substring(strRef.indexOf("https://www.google.com/url?q=") + 29, strRef.indexOf("." + docTypeWanted) + 4);
                        strResults.add(strFileToDownload);
                    }
                }

                if (strResults == null || strResults.size() == 0) {
                    updateMessage("No data found! womp womp");
                } else {
                    updateMessage("files found");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return strResults;

        }


    }

    //making file titles meet project specs
    private String countingRelevantFiles(){

        mostRelevantFile++;
        return String.format("%04d", mostRelevantFile);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        strDirSave = null;

        TableColumn<ThreadedController.DownTask, String> statusCol = new TableColumn("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<ThreadedController.DownTask, String>("message"));
        statusCol.setPrefWidth(100);

        TableColumn<ThreadedController.DownTask, Double> progressCol = new TableColumn("Progess");
        progressCol.setCellValueFactory(new PropertyValueFactory<ThreadedController.DownTask, Double>("progress"));
        progressCol.setPrefWidth(125);

        progressCol.setCellFactory(ProgressBarTableCell.<ThreadedController.DownTask>forTableColumn());

        TableColumn<ThreadedController.DownTask, String> fileCol = new TableColumn("File");
        fileCol.setCellValueFactory(new PropertyValueFactory<ThreadedController.DownTask, String>("title"));
        fileCol.setPrefWidth(375);

        table.getColumns().addAll(statusCol, progressCol, fileCol);

        //ensures filepath made correctly and search entered correctly
        btnGo.setOnAction(event -> {
               filePath = txtSearch.getText();
               mostRelevantFile = 1;

               if (!notTheseWords.getText().isEmpty()){
                   filePath += '-';
                   filePath += notTheseWords.getCharacters();
               }

               if (!exactPhrase.getText().isEmpty()){
                   filePath += '+';
                   filePath += exactPhrase.getCharacters();
               }
                   researchDirectory = new File("c:/path/selectedDir/" + filePath);

                   if (!researchDirectory.exists()) {
                       System.out.println("making your new directory! " + filePath);
                       numberOfSearchesByFilePath.put(filePath, 1);
                       boolean made = false;
                       try {
                           researchDirectory.mkdirs();
                           made = true;
                       } catch (SecurityException se) {
                           se.printStackTrace();
                           System.out.println("didnt make for some reason");
                       }
                       if (made) {
                           System.out.println("Created!");
                       }
                   }
                  else{
                       Integer numSearches = numberOfSearchesByFilePath.get(filePath);
                       if(numSearches == null){
                           numSearches = 0;
                       }

                        numberOfSearchesByFilePath.put(filePath, numSearches +1);
                   }

                   strDirSave = researchDirectory.getAbsolutePath();

               final Task<ObservableList<String>> getReviewsTask = new GetReviewsTask();

               if (strDirSave == null) {
                   System.err.println("You dont have an output dir!");
                   return;
               }

               table.getItems().clear();
               btnGo.disableProperty().bind(getReviewsTask.runningProperty());
               lstView.itemsProperty().bind(getReviewsTask.valueProperty());

               getReviewsTask.setOnSucceeded(event1 -> {

                   ObservableList<String> observableList = getReviewsTask.getValue();
                   if (observableList == null || observableList.size() == 0) {
                       return;
                   }

                   //adding our tasks to the table
                   for (String str : getReviewsTask.getValue()) {
                       table.getItems().add(new DownTask(str, strDirSave, countingRelevantFiles()));
                   }

                   //use executor service with only a limited number of threads
                   ExecutorService executor = Executors.newFixedThreadPool(6, r -> {
                       Thread thread = new Thread(r);
                       thread.setDaemon(true);
                       return thread;
                   });

                   for (DownTask pbarTask : table.getItems()) {
                       executor.execute(pbarTask);
                   }
               });

              new Thread(getReviewsTask).start();
           });
    }

    class DownTask extends Task<Void> {

        private String fileFrom, fileTo;
        private String relevantNum;

        public DownTask(String fileFrom, String fileTo, String relevantNum){
            this.fileFrom = fileFrom;
            this.fileTo = fileTo;
            this.relevantNum = relevantNum;
        }

        @Override
        protected Void call() throws Exception {

            HttpDownUtil util = new HttpDownUtil();
            try {

                this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
                this.updateMessage("Waiting...");
                util.downloadFile(this.fileFrom);

                InputStream inputStream = util.getInputStream();
                int searchNum = numberOfSearchesByFilePath.get(filePath);
                FileOutputStream outputStream = new FileOutputStream(fileTo + "/" + searchNum + "_" + relevantNum +"_" + "ggl" + "_" + util.getFileName());

                this.updateTitle(fileTo + "/" + util.getFileName());

                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                long totalBytesRead = 0;
                int percentCompleted = 0;
                long fileSize = util.getContentLength();

                while ((bytesRead = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    percentCompleted = (int) (totalBytesRead * 100 / fileSize);
                    updateProgress(percentCompleted, 100);
                }
                updateMessage("done");
                outputStream.close();
                util.disconnect();

            } catch (Exception ex) {
                ex.printStackTrace();
                this.cancel(true);
                table.getItems().remove(this);
            }
            return null;
        }
    }
}

