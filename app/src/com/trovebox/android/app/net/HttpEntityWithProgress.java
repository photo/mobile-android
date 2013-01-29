
package com.trovebox.android.app.net;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

public class HttpEntityWithProgress extends HttpEntityWrapper {

    private final ProgressListener mListener;
    private final long mTotalLength;

    public HttpEntityWithProgress(final HttpEntity entity, final ProgressListener listener) {
        super(entity);
        mListener = listener;
        mTotalLength = entity.getContentLength();
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        this.wrappedEntity.writeTo(out instanceof OutputStreamWithProgress ? out
                : new OutputStreamWithProgress(out, this.mListener, mTotalLength));
    }

    static class OutputStreamWithProgress extends FilterOutputStream {

        private final ProgressListener mListener;
        private long mTransferred;
        private final long mTotalLength;

        OutputStreamWithProgress(final OutputStream out, final ProgressListener listener,
                long totalLength) {
            super(out);
            this.mListener = listener;
            this.mTransferred = 0;
            this.mTotalLength = totalLength;
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            // // NO, double-counting, as super.write(byte[], int, int)
            // delegates to write(int).
            // super.write(b, off, len);
            out.write(b, off, len);
            this.mTransferred += len;
            this.mListener.transferred(this.mTransferred, this.mTotalLength);
        }

        @Override
        public void write(final int b) throws IOException {
            out.write(b);
            this.mTransferred++;
            this.mListener.transferred(this.mTransferred, this.mTotalLength);
        }
    }

    public static interface ProgressListener {
        void transferred(long transferedBytes, long totalBytes);
    }
}
