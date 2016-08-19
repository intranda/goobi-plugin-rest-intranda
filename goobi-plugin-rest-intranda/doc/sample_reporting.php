[insert_php]

$goobiUrl = 'http://demo03.intranda.com/goobi/';
$goobiToken = 'test';
$pageUrl = 'https://adminre.intranda.com/?page_id=315';

echo '<form action="' . $pageUrl . '" method="post">';
echo '<div class="row"><div class="col-sm-3 goobi-label">Start date:</div><div class="col-sm-6"><input type="text" name="goobi-startdate" />';
echo '</div><div class="col-sm-3"><input class="goobi-button fusion-button button-small" type="submit" value="Request status" /></div></div>';
echo'</form>';

if( isset($_POST['goobi-startdate']) && $_POST['goobi-startdate'] !='' ){
    $url = $goobiUrl . 'api/process/report/' . $_POST['goobi-startdate'] . '?token=' . $goobiToken;
    $json = file_get_contents($url);
    $data =  json_decode($json);

    // --------------------------------------------------------------------------------------------------------------------------------------
    
    
	if (count($data)) {
 		echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-success alert-shadow"><span class="alert-icon"><i class="fa fa-lg fa-check"></i></span>' . count($data) . ' jobs found for the given time range from ' . $_POST['goobi-startdate'] . ' til today.</div>';
 		
		echo '<div class="row" style="font-size:13px; margin-bottom:10px">';
		echo '<div class="col-sm-4"><b>Job title</b></div>';
		echo '<div class="col-sm-3"><b>Progress</b></div>';
		echo '<div class="col-sm-2"><b>Job ID</b></div>';
		echo '<div class="col-sm-3"><b>Job date</b></div>';
		echo '</div>';
		
 		foreach ($data as $process) {
 			$done = 0;
 			$stepTable = "";
 			
 			if (count($process->step)) {
 				
 				$stepTable = $stepTable . '<div id="' . $process->title . '" style="display:none; margin:20px; padding:5px; background-color:#eee" class="table-1"><table class="table table-bordered goobi-table" style="margin-top:0px">';
 				foreach ($process->step as $step) {
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
 				
 			}
 			
 			$percent = $done * 100 / count($process->step);
			
			// echo '<hr class="goobi-hr"/>';
 			
 			echo '<div class="row" style="font-size:13px;">';
 			echo '<div class="col-sm-4" style="cursor:pointer;" onclick="toggle_visibility(\\'' . $process->title . '\\');"><span id="chevron_' . $process->title . '" class="fa fa-chevron-right" style="margin-right:5px;color:#eee"></span><div title="' . $process->title . '" style="overflow:hidden;text-overflow: ellipsis;max-width:170px;display:inline-block;vertical-align:bottom;">' . $process->title . '</div></div>';
 			
 			echo '<div class="col-sm-3">';
 			echo '<div class="fusion-progressbar fusion-progressbar-text-on-bar"><div class="fusion-progressbar-bar progress-bar" style="background-color:#f6f6f6;height:17px;"><div aria-valuenow="' . round($percent) . '" aria-valuemax="100" aria-valuemin="0" role="progressbar" style="width: ' . round($percent) . '%; background-color: green; border: 0px solid;" class="progress progress-bar-content"/></div></div></div>';
			echo '</div>';
			
 			echo '<div class="col-sm-2">' . $process->id . '</div>';
 			echo '<div class="col-sm-3">' . $process->creationDate . '</div>';
 			
 			echo '</div>';
 			echo $stepTable;
 			
 		}
 		
 	}else{
 		echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow"><span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>No jobs found for the given time range from ' . $_POST['goobi-startdate'] . ' til today.</div>';
 	}
    

    // --------------------------------------------------------------------------------------------------------------------------------------
}

[/insert_php]

	<script type="text/javascript">
	
	<!--
	    function toggle_visibility(id) {
			var e = document.getElementById(id);
		    if(e.style.display == 'block'){
		       e.style.display = 'none';
		    }else{
		       e.style.display = 'block';
		    }
	
		    var e2 = document.getElementById('chevron_' + id);
		    if(e2.className == 'fa fa-chevron-right'){
		    	e2.className = 'fa fa-chevron-down';
		    }else{
		    	e2.className = 'fa fa-chevron-right';
		    }
	    }
	//-->
	</script>