gigaupload project would help you to upload very large files, like 1 GB (giga bytes) file using a javafx client and a server side PHP script. PHP does not have nice support for request stream and file upload size is limited by web server and php.ini settings (typically 2MB - 8 MB)

Giga Upload allows you to overcome that limit by providing a client that can chunk big files on client side and then send these chunks one by one to PHP receiver script. One file chunk is sent as a POST to server (well within the POST\_MAX limits) PHP receiver script will then assemble these chunks and create the original file on server.

This project is aimed at developers and released under a very liberal license.