<?php
	
	$name = $_POST["name"];
	$image = $_POST["image"];
	
	//make image from encoded (String) to bitmap for display
	$decodedImage = base64_decode("$image");
	//Store the image in location pictures/name.jpg
	file_put_contents("pictures/" . $name . ".JPG", $decodedImage);

?>