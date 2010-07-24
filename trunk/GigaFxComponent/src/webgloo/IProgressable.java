
package webgloo;

/**
 *
 * @author  rajeev jha (jha.rajeev@gmail.com)
 * IProgressable interface for javaFx Async task
 *
 * @see also http://blogs.sun.com/baechul/entry/javafx_1_2_async
 * This interface is based on code by  baechul in above sample
 * 
 */
public interface IProgressable {
    void setUploadProgress(float progress);
    void setUploadMessage(String message);
    
    java.io.File getFile();
    java.lang.String getUploadURI();
    java.lang.Integer getPostMax();
    
}
