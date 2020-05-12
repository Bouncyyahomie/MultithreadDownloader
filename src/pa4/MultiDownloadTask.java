package pa4;

import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;

/*
 * For download with multi thread task
 * @author Jakkrathorn Srisawad
 * */
public class MultiDownloadTask extends Controller {
    private Label label;
    private long getLength;
    public DownloadTask[] tasks = new DownloadTask[threadsWalking];


    public void multiTaskDownload(ProgressBar[] walker, ProgressBar progressBar, URL url, long length, File selectDirectory) {
        getLength = length;
        GetFileName getter = new GetFileName();
        long sizeOfKB = 4096; //The part size is multiples of 4KB
        long chunk = (long) Math.ceil((length / (sizeOfKB * 5))); //Create chunk
        //Size of each thread worker multiplied by 5 because use 5 threads to separate and the 6th threads do the fraction.
        long sizeOfEach = ((chunk / threadsWalking) * (sizeOfKB * 5));
        executor = Executors.newFixedThreadPool(6); //Create executor for get number of thread
        for (int i = 0; i < threadsWalking; i++) {
            walker[i].setVisible(true); //Show in ui
            //For thread 1-5
            tasks[i] = new DownloadTask(url, selectDirectory,
                    sizeOfEach * i, sizeOfEach);
            //For last threads that has fraction
            if (i == threadsWalking - 1) {
                tasks[i] = new DownloadTask(url,
                        selectDirectory,
                        sizeOfEach * i, length - (sizeOfEach * i));
            }
            //add progress tasks to listener
            tasks[i].valueProperty().addListener(this::getBytes);
            //execute tasks
            executor.execute(tasks[i]); //For start each thread
            //show progress in each progress bar
            walker[i].progressProperty().bind(tasks[i].progressProperty()); //Show progress in ui
        }
        //Set main progress bar will combind with all thread progressbar
        progressBar.progressProperty().bind(tasks[0].progressProperty().multiply(1.0 / 6.0).
                add(tasks[1].progressProperty().multiply(1.0 / 6.0)
                        .add(tasks[2].progressProperty().multiply(1.0 / 6.0)
                                .add(tasks[3].progressProperty().multiply(1.0 / 6.0)
                                        .add(tasks[4].progressProperty().multiply(0.2)    //)))));
                                                .add(tasks[5].progressProperty().multiply(1.0 / 6.0)))))));
        executor.shutdown();
    }

    /*
    * Get Label from controller
    * */
    public void labelGetter(Label getLabel) {
        label = getLabel;
    }

    /*
     * Method for Listener with observer
     *
     * */
    public void getBytes(Observable observable, Long oldValue, long newValue) {
        if (oldValue == null) {
            upValue = newValue;
            updateText += upValue;
        } else {
            upValue = newValue - oldValue;
            updateText += upValue;
        }
        if (updateText == getLength) {
            label.setText("STATUS : FINISHED");
        } else {
            label.setText(String.format("%d / %d", updateText, getLength));
        }
    }

    /*
     * For cancel all tasks
     * */
    public void cancelTasks() {
        for (DownloadTask task : tasks) {
            task.cancel();
        }
        label.setText("STATUS : CANCELLED");
    }

    public void clearTask(){
        Arrays.fill(tasks,null);
        label.setText("STATUS : CLEAR ");
    }

}
