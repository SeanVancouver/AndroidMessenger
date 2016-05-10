<?php

require_once("mysql.class.php"); 

$dbHost = "TO FILL";
$dbUsername = "TO FILL"; 
$dbPassword = "TO FILL";
$dbName = "TO FILL"; 

$db = new MySQL($dbHost,$dbUsername,$dbPassword,$dbName);

// if operation is failed by unknown reason
define("FAILED", 0);  

define("SUCCESSFUL", 1);
// when  signing up, if username is already taken, return this error
define("SIGN_UP_USERNAME_CRASHED", 2);  
// when add new friend request, if friend is not found, return this error 
define("ADD_NEW_USERNAME_NOT_FOUND", 2);

// TIME_INTERVAL_FOR_USER_STATUS: if last authentication time of user is older 
// than NOW - TIME_INTERVAL_FOR_USER_STATUS, then user is considered offline
define("TIME_INTERVAL_FOR_USER_STATUS", 60);

define("USER_APPROVED", 1);
define("USER_UNAPPROVED", 0);


$username = (isset($_REQUEST['username']) && count($_REQUEST['username']) > 0) 
							? $_REQUEST['username'] 
							: NULL;
//$password = isset($_REQUEST['password']) ? md5($_REQUEST['password']) : NULL;  md5 deprecated, password not encrypted

$password = $_REQUEST['password']; 

$port = isset($_REQUEST['port']) ? $_REQUEST['port'] : NULL;

$action = isset($_REQUEST['action']) ? $_REQUEST['action'] : NULL;
if ($action == "testWebAPI")
{
	if ($db->testconnection()){
	echo SUCCESSFUL;
	exit;
	}else{
	echo FAILED;
	exit;
	}
}

if ($username == NULL || $password == NULL)	 
{
	echo FAILED;
		echo "was d0";
	exit;
}

$out = NULL;

error_log($action."\r\n", 3, "error.log");
switch($action) 
{
	
	case "authenticateUser":
		
		
		if ($userId = authenticateUser($db, $username, $password)) 
		{			
			$sql = "select u.username, u.age, u.sex, u.bio, u.Id, u.email, u.password, (NOW()-u.authenticationTime) as authenticateTimeDifference  from users u"; 
			
			$sqlmessage = "SELECT m.id, m.fromuid, m.touid, m.sentdt, m.read, m.readdt, m.messagetext, u.username from messages m \n"
    . "left join users u on u.Id = m.fromuid WHERE `touid` = ".$userId." AND `read` = 0 LIMIT 0, 30 ";
			
	
			if ($result = $db->query($sql))		 	
			{
					$out .= "<data>"; 
					$out .= "<user userKey='".$userId."' />";
					
					while ($row = $db->fetchObject($result))
					{				
						$status = "offline";					
						
						if (((int)$row->authenticateTimeDifference) < TIME_INTERVAL_FOR_USER_STATUS)
						{
							$status = "online"; 	  						 
						} 										 				
						
						$out .= "<friend  email = '".$row->email."' age = '".$row->age."'  sex = '".$row->sex."'  bio = '".$row->bio."' Id = '".$row->Id."'  status='".$status."' IP='".$row->IP."' userKey = '".$row->Id."'  username = '".$row->username."'  port='".$row->port."'/>";
																
					}
						if ($resultmessage = $db->query($sqlmessage))			
							{
							while ($rowmessage = $db->fetchObject($resultmessage)) 
								{
								$out .= "<message  from='".$rowmessage->username."'  sendt='".$rowmessage->sentdt."' text='".$rowmessage->messagetext."' />";
								$sqlendmsg = "UPDATE `messages` SET `read` = 1, `readdt` = '".DATE("Y-m-d H:i")."' WHERE `messages`.`id` = ".$rowmessage->id.";";
								$db->query($sqlendmsg); 
								}
							}
					$out .= "</data>"; 
			}
			else
			{ 
				$out = FAILED;    
			}			
		}
		else 
		{
				$out = FAILED; 
		}
		
	
	
	break;
	
	case "signUpUser":
		if (isset($_REQUEST['email']))
		{
			 $email = $_REQUEST['email'];		
			 	
			 $sql = "select Id from  users 
			 				where username = '".$username."' limit 1";
			 				
			 if ($result = $db->query($sql))
			 {
			 		if ($db->numRows($result) == 0) 
			 		{
			 				$sql = "insert into users(username, password, email)
			 					values ('".$username."', '".$password."', '".$email."') ";		 					
						 					
			 					error_log("$sql", 3 , "error_log");
							if ($db->query($sql))	
							{
							 		$out = SUCCESSFUL;
							}				
							else {
									$out = FAILED;
							}				 			
			 		}
			 		else
			 		{
			 			$out = SIGN_UP_USERNAME_CRASHED;
			 		}
			 }				 	 	
		}
		else
		{
			$out = FAILED;
		}	
	break;
	
	case "sendMessage":
	if ($userId = authenticateUser($db, $username, $password)) 
		{	
		if (isset($_REQUEST['to']))
		{
			 $tousername = $_REQUEST['to'];	
			 $message = $_REQUEST['message'];	
				
			 $sqlto = "select Id from  users where username = '".$tousername."' limit 1";		 			 
		
					if ($resultto = $db->query($sqlto))			
					{
						while ($rowto = $db->fetchObject($resultto))
						{
							$uto = $rowto->Id;
						}
						$sql22 = "INSERT INTO `messages` (`fromuid`, `touid`, `sentdt`, `messagetext`) VALUES ('".$userId."', '".$uto."', '".DATE("Y-m-d H:i")."', '".$message."');";						
						 					
			 					error_log("$sql22", 3 , "error_log");
							if ($db->query($sql22))	
							{
							 		$out = SUCCESSFUL;
							}				
							else {
									$out = FAILED;
							}				 		
						$resultto = NULL;
					}	
			 				 	 	
		$sqlto = NULL;
		}
		}
		else
		{
			$out = FAILED;
		}	
	break;
	

case "updateProfile":
		$userId = authenticateUser($db, $username, $password);

					if ($userId != NULL)
			{
											 $age = $_REQUEST['age'];			 $sex = $_REQUEST['sex']; 	
			 $bio = $_REQUEST['bio'];	  

								
							$sql = "UPDATE users SET `age`= '$age', `sex`= '$sex' ,`bio`='$bio' WHERE `Id`= '$userId'";		
									 
																if ($db->query($sql))	
								{
										$out =SUCCESSFUL; 
								}
																				else {	$out = FAILED; }
			} 
	 
	break;
	
	
				case "deleteAccount":
		$userId = authenticateUser($db, $username, $password);

				if ($userId != NULL)
		{ 
										 $currentId = $_REQUEST['currentId'];
													
						$sql = "DELETE u.*, m.* FROM users u LEFT JOIN messages m ON m.fromuid = u.Id OR m.touid = u.Id WHERE u.Id = '$currentId'";
				
															if ($db->query($sql))	
							{
							 		$out = "SUCCESSFUL";
							}
																			else {	$out = "$currentId"; }
		} 
	
	break;
	
	default:
		$out = FAILED;		
		break;	
}

echo $out;



///////////////////////////////////////////////////////////////
function authenticateUser($db, $username, $password)
{
	$sql22 = "select * from users where username = '".$username."' and password = '".$password."' limit 1"; 
	      
	
	$out = NULL;        
	if ($result22 = $db->query($sql22)) 
	{
		if ($row22 = $db->fetchObject($result22))
		{
				$out = $row22->Id; 
				
				$sql22 = "update users set authenticationTime = NOW(), 
																 IP = '".$_SERVER["REMOTE_ADDR"]."' ,
																 port = 15145 
								where Id = ".$row22->Id."
								limit 1";
				
				$db->query($sql22);	
												
		}		
	}
	
	return $out;
}

?>