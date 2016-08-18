[insert_php]

$goobiUrl = 'http://demo03.intranda.com/goobi/';
$goobiToken = 'test';
$pageUrl = 'https://adminre.intranda.com/?page_id=388';

echo '<form action="' . $pageUrl . '" method="post">';
echo '<div class="row"><div class="col-sm-3 goobi-label">Start date:</div><div class="col-sm-6"><input type="text" name="goobi-startdate" />';
echo '</div><div class="col-sm-3"><input class="goobi-button fusion-button button-small" type="submit" value="Request status" /></div></div>';
echo'</form>';

if( isset($_POST['goobi-startdate']) && $_POST['goobi-startdate'] !='' ){
    $url = $goobiUrl . 'api/process/report/' . $_POST['goobi-startdate'] . '?token=' . $goobiToken;
    $json = file_get_contents($url);
    $data =  json_decode($json);

    if (count($data)) {
    	echo 'Hits found: ' . count($data);
    }else{
		echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow">  <span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>No jobs found for the given time range from ' . $_POST['goobi-startdate'] . ' til today.</div>';
    }
    
}

[/insert_php]
