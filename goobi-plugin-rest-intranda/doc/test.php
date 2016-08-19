<?php
 echo "Hello, world!";
 
 
 $goobiUrl = 'http://demo03.intranda.com/goobi/';
 $goobiToken = 'test';
 $pageUrl = 'https://adminre.intranda.com/?page_id=388';
 

 	$url = $goobiUrl . 'api/process/check/title/' . '3_01' . '?token=' . $goobiToken;
 	$json = file_get_contents($url);
 	$data =  json_decode($json);
 
 	// --------------------------------------------------------------------------------------------------------------------------------------
 	
 	
if (count($data)) {
 		echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-success alert-shadow"><span class="alert-icon"><i class="fa fa-lg fa-check"></i></span>' . count($data) . ' jobs found for the given time range from ' . $_POST['goobi-startdate'] . ' til today.</div>';
 		echo "\r\n";
 		
 		foreach ($data as $process) {
 			
 			echo '<hr class="goobi-hr"/>';
 			echo '<div class="row"><div class="col-sm-5"><h2>Job title:<b> ' . $process->title . '</b></h2></div><div class="col-sm-2 pull-right"><h2 style="text-align:right">Job ID: <b>' . $process->id . '</b></h2></div><div class="col-sm-5"><h2 style="text-align:left">Job date: <b>' . $process->creationDate . '</b></h2></div></div>';
 			echo "\r\n";
 			
 			if (count($process->step)) {
 				$done = 0;
 				$stepTable = "";
 				$stepTable = $stepTable . '<div class="table-1"><table class="table table-bordered goobi-table">';
 				foreach ($process->step as $step) {
 					echo "\r\n";
 					$stepTable = $stepTable . "<tr>";
 					$stepTable = $stepTable . "<td>$step->order</td>";
 					$stepTable = $stepTable . "<td>$step->title</td>";
 					$stepTable = $stepTable . "<td>$step->user</td>";
 					$stepTable = $stepTable . '<td><i class="fa fontawesome-icon ';
 					if ($step->status=='Completed'){
 						$stepTable = $stepTable . 'goobi-table-fa-green fa-check';
 						$done++;
 					}else if ($step->status=='Locked'){
 						$stepTable = $stepTable . 'goobi-table-fa-red fa-lock';
 					}else{
 						$stepTable = $stepTable . 'goobi-table-fa-orange fa-pencil';
 					}
 			
 					$stepTable = $stepTable . ' goobi-table-fa circle-yes"></i>' . $step->status . '</td>';
 					$stepTable = $stepTable . "</tr>";
 				}
 				$stepTable = $stepTable . "</table></div>";
 				$percent = $done * 100 / count($process->step);
 				echo "\r\n";
 				echo "\r\n";
 				echo round($percent);
 				echo "JETZT KOMMT ES" . $stepTable;
 			}
 			
 		}
 		
 	}else{
 		echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow"><span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>No jobs found for the given time range from ' . $_POST['goobi-startdate'] . ' til today.</div>';
 	}
 

 	// --------------------------------------------------------------------------------------------------------------------------------------
?>
