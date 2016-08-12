$goobiUrl = 'http://192.168.178.133:8080/Goobi/';
$goobiToken = 'test';
$goobiWorkflowId = '108';
$pageUrl = 'http://vagrantpress.dev/?p=188';

echo '<form action="' . $pageUrl . '" method="post">';
echo '<p>Catalogue-ID: <input type="text" name="catalogue-id" /></p>';
echo '<p><input type="submit" /></p>';
echo'</form>';

if( isset($_POST['catalogue-id']) && $_POST['catalogue-id'] !='' ){
	
	$url = $goobiUrl . 'api/process-create/' . $goobiWorkflowId . '/' . $_POST['catalogue-id'] . '?token=' . $goobiToken;
	
	$json = file_get_contents($url);
	$data =  json_decode($json);
	
	if ($data->result !='success') {
		echo 'Error while creating the Goobi process:';
		echo '<br/>Error message:' . $data->errorText;
		
		if ($data->processName != ''){
			echo '<br/>Process:' . $data->processName;
		}
		if ($data->processId != '0'){
			echo '<br/>ID: ' . $data->processId;
		}
	}else{
		echo 'Goobi process created successfully:';
		echo '<br/>Process:' . $data->processName;
		echo '<br/>ID: ' . $data->processId;
	}
	
}