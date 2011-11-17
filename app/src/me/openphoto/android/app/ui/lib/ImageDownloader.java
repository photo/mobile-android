
package me.openphoto.android.app.ui.lib;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.openphoto.android.app.ui.lib.ImageStorage.OnImageDisplayedCallback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * ImageDownloader is responsible for downloading images from the internet,
 * saving them on the sd card and showing them in an ImageView. If the ImageView
 * has been reused, than the download can be canceled if not started yet. If the
 * ImageView is reused for another image, the downloaded image will not be
 * displayed. ImageDownloader uses a thread pool to download images.
 */
class ImageDownloader {

    private static final String TAG = ImageDownloader.class.getSimpleName();
    private static final int MAX_RUNNING_TASKS = 5;

    private final ConcurrentLinkedQueue<Task> mQueue;
    private final Dictionary<Integer, Task> mTasks;
    private int mRunningTasks = 0;
    private int mAsyncTaskIdIncrementor = 0;
    private final Object mLock = new Object();
    private final Hashtable<String, List<Task>> mFileLocks;

    public ImageDownloader() {
        mQueue = new ConcurrentLinkedQueue<Task>();
        mTasks = new Hashtable<Integer, Task>();
        mFileLocks = new Hashtable<String, List<Task>>();
    }

    /**
     * Run the download in the queue. Wait if there are others running.
     * 
     * @param imageView the ImageView in which the image should be displayed
     * @param imageFileLocation the location where the image should be stored
     * @param downloadUrl the download url
     * @param listener
     * @param manipulation the manipulation which should be done to the image
     *            before saving and displaying
     */
    public void runInQueue(ImageView imageView, String imageFileLocation, String downloadUrl,
            OnImageDisplayedCallback listener) {
        Task task = new Task();
        task.imageViewReference = new WeakReference<ImageView>(imageView);
        task.saveLocation = imageFileLocation;
        task.imageUrl = downloadUrl;
        task.listener = listener;

        task.id = ++mAsyncTaskIdIncrementor;
        imageView.setTag(task.id);
        mTasks.put(task.id, task);
        mQueue.offer(task);

        if (mRunningTasks < MAX_RUNNING_TASKS) {
            new TaskExecuterThread().start();
        }
    }

    /**
     * Stop tasks for a given ImageView. This method needs to be called, when a
     * ImageView is being reused.
     * 
     * @param imageView The ImageView for which tasks should be canceled.
     */
    public void stopTasksFor(ImageView imageView) {
        Object tag = imageView.getTag();
        if (tag instanceof Integer) {
            Task task = mTasks.get(tag);
            if (task != null) {
                Log.i(TAG, "Cancelling task with id " + tag);
                task.isCanceled = true;
            }
        }
        imageView.setTag(null);
    }

    /**
     * Download image and save it to the given location.
     * 
     * @param url the url
     * @param saveLocation the save location
     * @return the downloaded bitmap
     * @throws MalformedURLException the malformed url exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotFoundException the file not found exception
     */
    public Bitmap downloadImage(String url, String saveLocation) throws MalformedURLException,
            IOException, FileNotFoundException {
        final URL theUrl = new URL(url);

        ImageTools.createDirsOfFile(saveLocation);

        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(theUrl.openStream());
            fout = new FileOutputStream(saveLocation);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }

            return BitmapFactory.decodeFile(saveLocation);
        } finally {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
    }

    /**
     * The Class Task represents a download task
     */
    private static class Task {

        public OnImageDisplayedCallback listener;

        /** The handler. */
        Handler handler;

        /** The url to the image which should be downloaded. */
        String imageUrl;

        /** The save location. */
        String saveLocation;

        /** The image view reference. */
        WeakReference<ImageView> imageViewReference;

        /** The id of the task. */
        int id;

        /** Notes if the task has been canceled. */
        boolean isCanceled;

        /**
         * Instantiates a new task.
         */
        public Task() {
            handler = new Handler();
            isCanceled = false;
        }
    }

    /**
     * The Class TaskExecuterThread is the worker thread for downloading,
     * manipulating and displaying the image.
     */
    private class TaskExecuterThread extends Thread {

        /**
         * Instantiates a new task executer thread.
         */
        public TaskExecuterThread() {
            setPriority(Thread.MIN_PRIORITY);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            synchronized (mLock) {
                ++mRunningTasks;
            }

            Task task;
            while ((task = mQueue.poll()) != null) {
                mTasks.remove(task.id);
                if (ensureFileAccessFor(task)) {
                    try {
                        if (!task.isCanceled) {
                            handleTask(task);
                        }
                    } finally {
                        // to make sure it got removed
                        mFileLocks.remove(task.saveLocation);
                    }
                }
            }

            synchronized (mLock) {
                if (mRunningTasks > 0) {
                    --mRunningTasks;
                }
            }
        }

        private boolean ensureFileAccessFor(Task task) {
            synchronized (mFileLocks) {
                if (mFileLocks.containsKey(task.saveLocation)) {
                    mFileLocks.get(task.saveLocation).add(task);
                    return false;
                } else {
                    mFileLocks.put(task.saveLocation, new ArrayList<ImageDownloader.Task>());
                    return true;
                }
            }
        }

        /**
         * Handle task.
         * 
         * @param task the task
         */
        private void handleTask(final Task task) {
            Log.i(TAG, "[" + task.id + "]Task started");
            try {
                final Bitmap bitmap = downloadImage(task.imageUrl, task.saveLocation);
                displayBitmap(task, bitmap);
                setForDeniedDownloads(task, bitmap);
            } catch (MalformedURLException e) {
                Log.w(TAG, "MalformedURLException for " + task.saveLocation + "/" + task.imageUrl
                        + ": " + e.getMessage());
            } catch (IOException e) {
                Log.w(TAG,
                        "IOException for " + task.saveLocation + "/" + task.imageUrl + ": "
                                + e.getMessage());
            }
        }

        private void setForDeniedDownloads(Task task, Bitmap bitmap) {
            synchronized (mFileLocks) {
                List<Task> tasks = mFileLocks.remove(task.saveLocation);
                if (tasks != null) {
                    for (Task deniedTask : tasks) {
                        displayBitmap(deniedTask, bitmap);
                    }
                }
            }
        }

        /**
         * Display bitmap in the ImageView provided by the task.
         * 
         * @param task the task
         * @param bitmap the bitmap
         */
        private void displayBitmap(final Task task, final Bitmap bitmap) {
            task.handler.post(new Runnable() {

                @Override
                public void run() {
                    if (task.imageViewReference != null) {
                        final ImageView imageView = task.imageViewReference.get();
                        if (imageView != null && bitmap != null
                                && Integer.valueOf(task.id).equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                            imageView.setVisibility(View.VISIBLE);
                            if (task.listener != null) {
                                task.listener.onImageDisplayed(imageView);
                            }
                        }
                    }
                }
            });
        }
    }
}
