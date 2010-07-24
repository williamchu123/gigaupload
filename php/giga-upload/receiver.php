<?php 
  
    // =====================================
    // user variables 
    // =====================================
    $uploadLocation = 'F:\\' ;
    //upload location on unix box
    // $uploadLocation = '/var/www/htdocs/big/'

     $logPath = $uploadLocation."giga-upload.log";
     $logfp = fopen($logPath, 'a');


    function errorInUpload($message) {
        //raise error
        header('HTTP/1.1 500 Internal Server Error');
        echo $message ;
        exit(1);

    }
    
    function logError($file, $line,$errorstr){
        //open log in write  mode
        logMessage($errorstr);
       
    }

    function logMessage($message){
         global  $logfp;
         fwrite($logfp,$message);
         
    }
    
    // we need a custom error handler for file upload because the usual
    // html error messages are not suitable for Java Applet.
    // Also PHP parsing and core errors will not be caught by user error handler
    // To deal with such cases we reply on UPSTATUS OK flag. Upload script response
    // should be parsed on client side to detect trouble.
    
    function upload_error_handler($errorno,$errorstr,$file,$line) {
        logError($file,$line,$errorstr);
        errorInUpload('Upload receiver script execution error');
    }
    
    set_error_handler('upload_error_handler');
    ob_start();

    //what javafx component sent

    $uuid = $_POST['uuid'];
    $chunk = $_POST['chunk'];
    $totalChunks = $_POST['total_chunks'];


    $fileData = $_FILES["f"];
    $ftmp = $fileData['tmp_name'];
    $fname = $fileData['name'];
    $mime = $fileData['type'] ;


    $oTempFile = fopen($ftmp, "rb");
    $size = filesize($ftmp);
    //file BLOB
    $sBlobData = fread($oTempFile, $size);
    
    
    if($size <= 0 ) {
        
        errorInUpload("No data uploaded!");
    }

    //Based on received input - Act now
    //sanity checking
    //chunk should be <= total_chunks

    //create fname that we can use
    //save extension first

    $extension = NULL ;
    $storeName = NULL ;

    $pos = strrpos($fname, '.');

    if($pos != false ) {
        //separate filename and extension
        $extension = substr($fname,$pos+1);
        $fname = substr($fname,0,$pos);
    }
    
    
    //create a digest with fname + uuid
    // This remains the same for all chunks

    $digest = md5($fname.$uuid);
    //throw away half of it
    $digest = substr($digest,0,16);
    
    if(empty($extension)){
        $storeName = $digest ;
    } else {
        $storeName = $digest.'.'.$extension ;
    }

    logMessage(" store chunk = $chunk of file  $storeName \n");
    
    $fp = NULL ;
    $path = $uploadLocation.$storeName ;
    
    if($chunk > $totalChunks) {
        errorInUpload("Trying to upload a chunk past file length!");
       
    }

    //error checks
    if( $chunk == 1 && file_exists($path)) {
         errorInUpload("This file upload is already in progress!");

    }

    if( $chunk > 1 && !file_exists($path)) {
       errorInUpload("Earlier file chunks have been deleted on server!");

    }
    
    //open file in append mode
    $fp = fopen($path, 'a');
    fwrite($fp, $sBlobData);
    //close file pointer
    fclose($fp);
    fclose($logfp);
    
    
    $message = "UPSTATUS::OK|UPCHUNK::".$chunk ;

    if($chunk == $totalChunks) {
        //file upload complete
        // do any post processing
        
    }
    
    header("HTTP/1.0 200 OK");
    echo $message ;
       

?>
