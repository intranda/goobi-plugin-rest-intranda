$goobiUrl = 'http://demo03.intranda.com/goobi/';
$goobiToken = 'test';
$pageUrl = 'https://adminre.intranda.com/?page_id=313';

echo '<form action="' . $pageUrl . '" method="post">';
echo '<div class="row"><div class="col-sm-3 goobi-label">Job title:</div><div class="col-sm-6"><input type="text" name="goobi-title" />';
echo '</div><div class="col-sm-3"><input class="goobi-button fusion-button button-small" type="submit" value="Request status" /></div></div>';
echo'</form>';

if( isset($_POST['goobi-title']) && $_POST['goobi-title'] !='' ){
    $url = $goobiUrl . 'api/process/details/title/' . $_POST['goobi-title'] . '?token=' . $goobiToken;
    // echo $url;
    $json = file_get_contents($url);
    $data =  json_decode($json);

    //echo '<br/>' . $json . '<br/><br/>';
    
    if ($data->result !='ok') {
        echo '<div class="goobi-alert fusion-alert alert error alert-dismissable alert-danger alert-shadow">  <span class="alert-icon"><i class="fa fa-lg fa-exclamation-triangle"></i></span>' . $data->result . '</div>';
        
    }else{
        echo '<hr class="goobi-hr"/>';
        echo '<div class="row"><div class="col-sm-5"><h2>Job title:<b> ' . $data->title . '</b></h2></div><div class="col-sm-2 pull-right"><h2 style="text-align:right">Job ID: <b>' . $data->id . '</b></h2></div><div class="col-sm-5"><h2 style="text-align:left">Job date: <b>' . $data->creationDate . '</b></h2></div></div>';
       
        if (count($data->step)) {
            echo '<div class="table-1"><table class="table table-bordered goobi-table">';
            foreach ($data->step as $idx => $step) {
                echo "<tr>";
                echo "<td>$step->order</td>";
                echo "<td>$step->title</td>";
                echo "<td>$step->user</td>";
                echo '<td><i class="fa fontawesome-icon ';
                if ($step->status=='Completed'){
                    echo 'goobi-table-fa-green fa-check';
                }else if ($step->status=='Locked'){
                    echo 'goobi-table-fa-red fa-lock';
                }else{
                    echo 'goobi-table-fa-orange fa-pencil';
                }
                      
                echo ' goobi-table-fa circle-yes"></i>' . $step->status . '</td>';
                echo "</tr>";
            }
            echo "</table></div>";
        }
    }
}
