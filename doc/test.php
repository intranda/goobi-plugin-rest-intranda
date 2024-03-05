<?php

 $goobiUrl = 'http://demo03.intranda.com/goobi/';
 $goobiToken = 'test';
 $pageUrl = 'https://adminre.intranda.com/?page_id=388';
 
 
 
 $processTitle = 'auctsaofb_steffen1';
 //$processTitle = '3_02';
 //$processTitle = 'auctsaofb_steffen1';

 	$url = $goobiUrl . 'api/process/check/title/' . $processTitle . '?token=' . $goobiToken;
 	$result = file_get_contents($url);
 	
 	// --------------------------------------------------------------------------------------------------------------------------------------
 	
 	switch ($result) {
 		case "Process ok.":
 			echo 'Start downloading';
 			
 			$url = $goobiUrl . 'api/process/download/title/' . $processTitle . '?token=' . $goobiToken;
 			
 			$opts = array(
 					'http'=>array(
 							'method'=>"GET",
 							'header'=>"Accept-language: en\r\n" .
 							"Cookie: foo=bar\r\n"
 					)
 			);
 			
 			$context = stream_context_create($opts);
 			
 			/* Sends an http request to www.example.com
 			 with additional headers shown above */
 			$fp = fopen($url, 'rb', false, $context);
 			echo $fp;
 			fpassthru($fp);
 			fclose($fp);
 			
 			
 			break;
 		case "Process has no images.":
 			echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow"><span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>The job with title ' . $processTitle . ' exists but still has no content to download.</div>';
 			break;
 		default:
 			echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow"><span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>No job with title ' . $processTitle . ' could be found.</div>';
 			break;
 			
 	}
 	
 	echo 'Finished';
 	
 	// --------------------------------------------------------------------------------------------------------------------------------------
?>
