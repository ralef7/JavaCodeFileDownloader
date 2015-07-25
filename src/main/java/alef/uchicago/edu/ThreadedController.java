package alef.uchicago.edu;

/**
 * Sample Skeleton for 'threaded.fxml' Controller Class
 */

import com.sun.deploy.net.HttpDownload;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
//import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import static javafx.concurrent.Worker.State.CANCELLED;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.READY;
import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.concurrent.Worker.State.SCHEDULED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
//import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ThreadedController implements Initializable{

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

    @FXML // fx:id="btnSelect"
    private Button btnSelect; // Value injected by FXMLLoader

    private String strDirSave;


    @FXML
    private void btnSelect_go(ActionEvent event) {

        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.setTitle("This is my file ch");

        //Show open file dialog
        File file = directoryChooser.showDialog(null);

        if (file != null) {

            btnSelect.setText(file.getPath());
            strDirSave = file.getAbsolutePath();

        }

    }
    @FXML
    void btnGoAction(ActionEvent event) {

        lblResult.setText("Result");
    }



    @FXML
    void mnuQuitAction(ActionEvent event) {

        System.exit(0);

    }

    public class GetReviewsTask extends Task<ObservableList<String>> {

        private String convertSpacesToPluses(String strOrig) {
            return strOrig.trim().replace(" ", "+");
        }

    //    @Override
        protected ObservableList<String> call() throws Exception{

            ObservableList<String> sales = FXCollections.observableArrayList();
            updateMessage("Finding files...");

            String strUrl = "https://www.google.com/search?q=";
            strUrl += convertSpacesToPluses(txtSearch.getText());
            strUrl += "+filetype:pdf"; //make this dynamic

            Document doc;
            ArrayList<String> strResults = new ArrayList<String>();

            try {
                doc = Jsoup.connect(strUrl).userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
                Elements hrefs = doc.select("a[href]");
                for (Element href : hrefs){

                    String strRef = href.attr("abs:href");
                    if (strRef.contains(".pdf") && strRef.contains("https://www.google.com/url?q=")){

                        String strFileToDownload = strRef.substring(strRef.indexOf("https://www.google.com/url?q=") + 29, strRef.indexOf(".pdf") + 4);
                        strResults.add(strFileToDownload);
                    }
                }

                if (strResults == null || strResults.size() == 0){
                    updateMessage("No data found! womp womp");
                }
                else{
                    updateMessage("files found");
                }

            }
            catch(IOException e){
                e.printStackTrace();
            }
            return  FXCollections.observableArrayList(strResults);
        }
    }

  //  @Override
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

        //java 8 to more concisely deal with anonymous class?
        btnGo.setOnAction(new EventHandler<ActionEvent>() {
            //@Override
            public void handle(ActionEvent event) {
                if (strDirSave == null) {
                    System.err.println("You need to set an output dir!");
                    return;
                }

                final Task<ObservableList<String>> getReviewsTask = new GetReviewsTask();

                table.getItems().clear();
                btnGo.disableProperty().bind(getReviewsTask.runningProperty());
                lstView.itemsProperty().bind(getReviewsTask.valueProperty());

                getReviewsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                 //   @Override
                    public void handle(WorkerStateEvent event) {

                        ObservableList<String> observableList = getReviewsTask.getValue();
                        if (observableList == null || observableList.size() == 0) {
                            return;
                        }

                        //adding our tasks to the table
                        for (String str : getReviewsTask.getValue()) {
                            table.getItems().add(new DownTask(str, strDirSave));
                        }

                        //use executor service with only a limited number of threads
                        //user java 8 to make more concise and easy to read code here.
                        ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
                          // @Override
                            public Thread newThread(Runnable r) {
                                Thread thread = new Thread(r);
                                thread.setDaemon(true);
                                return thread;
                            }
                        });

                        for (ThreadedController.DownTask pbarTask : table.getItems()) {
                            executor.execute(pbarTask);
                        }

                    }
                });

                new Thread(getReviewsTask).start();
            }
        });
    }

    class DownTask extends Task<Void> {

        private String fileFrom, fileTo;

        public DownTask(String fileFrom, String fileTo){
            this.fileFrom = fileFrom;
            this.fileTo = fileTo;
        }

        @Override
        protected Void call() throws Exception {

            HttpDownUtil util = new HttpDownUtil();
            try {

                this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
                this.updateMessage("Waiting...");
                util.downloadFile(this.fileFrom);

                //using streams here java8??
                InputStream inputStream = util.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(fileTo + "/" + util.getFileName());
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

    public String getJSON (String url, int timeout){
        try {

            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return sb.toString();
            }

        }
        catch (MalformedURLException ex){

        }
        catch (IOException ex){

        }
        return null;
    }


}

