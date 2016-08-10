$goobiUrl = 'http://192.168.178.133:8080/Goobi/';
$goobiToken = 'test';
$pageUrl = 'http://vagrantpress.dev/?p=188';

if( isset($_POST['goobi-id']) && $_POST['goobi-id'] !='' ){
	$url = $goobiUrl . 'api/process-status/json/' . $_POST['goobi-id'] . '?token=' . $goobiToken;
	$json = file_get_contents($url);
	$data =  json_decode($json);
	
	if ($data->result !='success') {
		echo '<br/>' . $data->result . '<br/><br/>';
	}else{
		echo "<h1>$data->title</h1>";
		echo "<h2>$data->id</h2>";
		echo "<h2>$data->creationDate</h2>";

		if (count($data->step)) {
        	echo "<table>";
        	foreach ($data->step as $idx => $step) {
            	echo "<tr>";
            	echo "<td>$step->order</td>";
            	echo "<td>$step->title</td>";
				echo "<td>$step->user</td>";
            	echo "<td>$step->status</td>";
            	echo "</tr>";
			}
			echo "</table>";
		}
	}
}

echo '<form action="' . $pageUrl . '" method="post">';
echo '<p>ID: <input type="text" name="goobi-id" /></p>';
echo '<p><input type="submit" /></p>';
echo'</form>';