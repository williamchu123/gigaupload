package webgloo;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

/**
 * @author  rajeev jha (jha.rajeev@gmail.com)
 * JavaFx GUI for uploading files.
 * @see also http://blogs.sun.com/rakeshmenonp/entry/javafx_upload_file
 * This GUI is a copy of above code with suitable modifications.
 *
 *
 * 
 */

 //server URL where we are posting this file
var postURI: java.lang.String = "http://localhost/giga-upload/receiver.php";
//chunk size
var chunkSize: java.lang.Integer = new java.lang.Integer(2048000);

// method to call when upload button is pressed
// this method starts an async task to upload
// selected file.

function uploadFile(inputFile: java.io.File) {
    println("Main.fx upload file ...");
    task = new UploadTask();
    task.setFile(inputFile);
    task.setUploadURI(postURI);
    task.setPostMax(chunkSize);
    task.start();
}

var task: UploadTask;
def jFileChooser = new javax.swing.JFileChooser();

jFileChooser.setApproveButtonText("Upload");
//upload button 
var button = Button {
            text: "Upload"
            layoutInfo: LayoutInfo { width: 100 height: 30 }
            action: function () {
                var outputFile = jFileChooser.showOpenDialog(null);
                if (outputFile == javax.swing.JFileChooser.APPROVE_OPTION) {
                    uploadFile(jFileChooser.getSelectedFile());
                }
            }
        }
//progressBar
var progressBar = ProgressBar {
            progress: bind {
                task.uploadProgress
            } as Number
            layoutInfo: LayoutInfo { width: 240 height: 30 }
        }
//label to show message
var label = Label {
            text: bind {
                task.uploadMessage
            } as String
        }
var hBox1 = HBox {
            spacing: 10
            content: [progressBar, button]
        }
var hBox2 = HBox {
            spacing: 10
            content: [label]
        }
var vBox = VBox {
            spacing: 10
            content: [hBox1, hBox2]
            layoutX: 10
            layoutY: 10
        }

Stage {
    title: "Upload File"
    width: 380
    height: 120
    scene: Scene {
        content: [vBox]
        fill: Color.WHITE
    }
    resizable: false
}

