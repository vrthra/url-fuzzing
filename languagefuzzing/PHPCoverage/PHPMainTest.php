<?php
require "./vendor/league/uri/src/UriString.php";
require "./vendor/league/uri-interfaces/src/Contracts/UriException.php";



foreach (glob("./vendor/league/uri-interfaces/src/Exceptions/*.php") as $filename)
{
    require $filename;
}




use PHPUnit\Framework\TestCase;
use League\Uri\UriString;
use League\Uri\Exceptions\SyntaxError;

class PHPMainTest extends TestCase {
public function test_urls(){
$file = fopen("../urls/plainURLs","r");
$exceptions="";

while(! feof($file))
  {
  $url= fgets($file);
  //parse_url($url);
  $url=substr($url, 0, -1);
  list($base, $rel)=explode("<", $url);
  
  
try {
	if(!empty($rel)){
	    $b=Uri::createFromString($base);
	    $r=Uri::createFromString($rel);
	    $res=UriResolver::resolve($r, $b);
	}else{
	    UriString::parse($url);
	}
    
} catch (Exception $e) {
   $exceptions.="\n{\"url\":\"".$url."\", \"exception\":\"".$e->getMessage()."\"}";
}
  }

fclose($file);
file_put_contents('PHPExceptions.txt', $exceptions); 
$this->assertTrue(True);
}
}
?>
