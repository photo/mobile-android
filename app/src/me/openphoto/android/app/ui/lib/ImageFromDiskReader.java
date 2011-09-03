
package me.openphoto.android.app.ui.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

class ImageFromDiskReader {
    private Handler mHandler;
    private Queue<DiskTask> mQueue = new ConcurrentLinkedQueue<DiskTask>();
    private Map<View, DiskTask> mMap = new HashMap<View, DiskTask>();
    private ExecutorThread mExector;

    public ImageFromDiskReader() {
        mHandler = new Handler();
    }

    public synchronized void displayImage(String path, ImageView view) {
        DiskTask task = new DiskTask();
        task.path = path;
        task.view = view;
        task.stopped = false;

        mQueue.add(task);
        mMap.put(view, task);
        if (mExector == null || !mExector.isAlive()) {
            mExector = new ExecutorThread();
            mExector.start();
        }
    }

    public Bitmap getBitmap(String path) {
        return BitmapFactory.decodeFile(path);
    }

    public synchronized void stop(ImageView view) {
        DiskTask task = mMap.get(view);
        if (task != null) {
            task.stopped = true;
        }
    }

    private synchronized DiskTask getNext() {
        return mQueue.poll();
    }

    private class ExecutorThread extends Thread {

        @Override
        public void run() {

            DiskTask task;
            while ((task = getNext()) != null) {
                mMap.remove(task.path);
                if (!task.stopped) {
                    setFromSdCardImage(task);
                }
            }
        }

        /**
         * Sets the ImageView with the image from sd card.
         * 
         * @param imageView the ImageView in which the image should be
         *            displayed.
         * @param path the path to the image
         */
        private void setFromSdCardImage(final DiskTask task) {
            final Bitmap bitmap = getBitmap(task.path);
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!task.stopped) {
                        task.view.setImageBitmap(bitmap);
                        task.view.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    private class DiskTask {
        public String path;
        public ImageView view;
        public boolean stopped;
    }

}
