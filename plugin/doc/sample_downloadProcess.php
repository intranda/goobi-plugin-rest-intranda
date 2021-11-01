$goobiUrl = 'http://demo03.intranda.com/goobi/';
$goobiToken = 'test';
$pageUrl = 'https://adminre.intranda.com/?page_id=317';

echo '<form action="' . $pageUrl . '" method="post">';
echo '<div class="row"><div class="col-sm-3 goobi-label">Job title:</div><div class="col-sm-6"><input type="text" name="goobi-title" />';
echo '</div><div class="col-sm-3"><input class="goobi-button fusion-button button-small" type="submit" value="Request status" /></div></div>';
echo'</form>';

if( isset($_POST['goobi-title']) && $_POST['goobi-title'] !='' ){
    $url = $goobiUrl . 'api/process/check/title/' . $_POST['goobi-title'] . '?token=' . $goobiToken;
    echo $url;
}
