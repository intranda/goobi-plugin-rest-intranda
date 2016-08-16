$goobiUrl = 'http://demo03.intranda.com/goobi/';
$goobiToken = 'test';
$goobiWorkflowId = '343';
$catalogue = 'GBV_MARC';
$pageUrl = 'https://adminre.intranda.com/?page_id=311';

echo '<form action="' . $pageUrl . '" method="post">';
echo '<div class="row"><div class="col-sm-3 goobi-label">Catalogue identifier:</div><div class="col-sm-6"><input type="text" name="catalogue-id" />';
echo '</div><div class="col-sm-3"><input class="goobi-button fusion-button button-small" type="submit" value="Order job" /></div></div>';
echo'</form>';

if( isset($_POST['catalogue-id']) && $_POST['catalogue-id'] !='' ){

    $url = $goobiUrl . 'api/process/create/' . $goobiWorkflowId . '/' . $catalogue . '/' . $_POST['catalogue-id'] . '?token=' . $goobiToken;

    //echo $url;
    $json = file_get_contents($url);
    $data =  json_decode($json);

    if ($data->result !='success') {
        $message = 'Error while creating the Goobi process.<br/><br/>' . $data->errorText;
        if ($data->processName != ''){
            $message += '(Process: ' . $data->processName . ')';
        }
        if ($data->processId != '0'){
            $message += '(ID: ' . $data->processId . ')';
        }
        
        echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow">  <span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>' . $message . '</div>';
                
    }else{
        
        echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-success alert-shadow">  <span class="alert-icon"><i class="fa fa-lg fa-check"></i></span>Goobi process created successfully for ID ' . $data->processName . '<br/><br/>Your internal Job ID is ' . $data->processId . '</div><br/>';
    }
}