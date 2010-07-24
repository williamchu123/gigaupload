package webgloo;

import javafx.async.*;

/**
 *
 * @author  rajeev jha (jha.rajeev@gmail.com)
 *
 * JavaFx UploadTask. This task is called from the main java FX window
 * when user starts upload of a file. This task executes a runnable
 * and signals the java FX UI of upload progress,
 *
 * @see also http://blogs.sun.com/baechul/entry/javafx_1_2_async
 * This class is based on code by  baechul in above sample.
 *
 *
 */
public class UploadTask extends JavaTaskBase, IProgressable {

    //default access is script-private 
    var peer: UploadRunnable;
    public var uploadProgress: Number;
    public var uploadMessage: String;
    var postMax: java.lang.Integer;
    var uploadURI: java.lang.String;
    var file: java.io.File;

    override function create(): RunnableFuture {
        peer = new UploadRunnable(this);
    }

    override function setUploadProgress(progress: Float) {
        uploadProgress = progress;
    }

    override function setUploadMessage(message: String) {
        uploadMessage = message;
    }

    public function setFile(inputFile: java.io.File) {
        file = inputFile;
    }

    override public function getFile() {
        return file;
    }

    public function setUploadURI(postURI: String) {
        uploadURI = postURI;
    }

    override public function getUploadURI() {
        return uploadURI;
    }

    override public function getPostMax() {
        return postMax;
    }

    public function setPostMax(chunkSize: java.lang.Integer) {
        //println("post Max size is ");
        //println(chunkSize.intValue());
        postMax = chunkSize;
    }

    override public function start(): Void {
        super.start();
    }

}
