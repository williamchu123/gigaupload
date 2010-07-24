package webgloo;

import com.sun.javafx.functions.Function0;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import javafx.async.RunnableFuture;

/**
 *
 * @author  rajeev jha (jha.rajeev@gmail.com)
 *
 * UploadRunnable java task that does all the heavy lifting and
 * is called from javaFx task. We need a separate Task to update
 * javaFx GUI in async manner (on non EDT thread)
 *
 * javaFx main window => UploadTask => UploadRunnable
 * 
 * @see also http://blogs.sun.com/baechul/entry/javafx_1_2_async
 * This class is based on code by  baechul in above sample.
 * 
 *
 */
public class UploadRunnable implements RunnableFuture {

    IProgressable task;
    java.io.File file;
    String uploadURI;
    int postMax;

    public UploadRunnable(IProgressable pi) {

        task = pi;
        this.file = pi.getFile();
        this.postMax = pi.getPostMax().intValue();
        this.uploadURI = pi.getUploadURI();

    }

    @Override
    public void run() throws Exception {
        
        try {
            
            //sanity checks
            if (postMax < 1024000) {
                throw new Exception("Upload chunk size is less than 1 MB");
            }

            
            if (uploadURI == null || uploadURI.equals("")) {
                throw new Exception("Invalid Upload location ");
            }
            
            long totalBytes = this.file.length();

            //number of chunks
            int chunkCount = 1;
            int bytesWritten = 1;
            int totalChunks = 1;

            if (totalBytes % postMax == 0) {
                totalChunks = (int) (totalBytes / postMax);

            } else {
                totalChunks = (int) ((totalBytes / postMax) + 1);
            }

            String message = String.format("Total %d parts to transfer for %d bytes", totalChunks, totalBytes);
            setMessage(message);

            GigaFileUploader.FileMetaData fileMetaData = GigaFileUploader.init(this.file);
            
            FileInputStream is = new FileInputStream(this.file);
            FileChannel fc = is.getChannel();


            java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(this.postMax);

            String response = null;

            while (fc.read(bb) >= 0) {
                bb.flip();
                int limit = bb.limit();
                byte[] bytes = GigaFileUploader.getBufferBytes(bb.array(), limit, this.postMax);
                GigaFileUploader.Content content =
                        GigaFileUploader.createPostContent(fileMetaData, bytes, chunkCount, totalChunks);

                response = GigaFileUploader.upload(this.uploadURI, content);
                //System.out.println(response);
                //@todo parse response for possible troubles
                //@todo parse reponse for S3 Key on end 
                bytesWritten = bytesWritten + limit;

                //calculate upload progress in percentage
                float progress = ((1.0F * bytesWritten) / totalBytes);
                setProgress(progress);

                message = String.format("Transferred (%d / %d) part, total bytes %d ", chunkCount, totalChunks, bytesWritten);
                setMessage(message);

                bb.clear();

                chunkCount++;
                java.lang.Thread.sleep(2000);

            }

            //Done
            message = String.format(" Upload success, %d bytes written", bytesWritten);
            setMessage(message);

        } catch (Exception ex) {
            // No logging here
            // @todo format exception message to write in 50 char lines
            setMessage("Error: " + ex.getMessage());
        }

    }

    void setProgress(final float progress) {
        javafx.lang.FX.deferAction(new Function0<Void>() {

            @Override
            public Void invoke() {
                task.setUploadProgress(progress);
                return null;
            }
        });
    }

    void setMessage(final String message) {
        javafx.lang.FX.deferAction(new Function0<Void>() {

            @Override
            public Void invoke() {
                task.setUploadMessage(message);
                return null;
            }
        });
    }
}
