import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.InputStreamReader;  
import java.io.OutputStream;  
import java.io.OutputStreamWriter;  
import java.net.HttpURLConnection;  
import java.net.URL;  
File logFileToUpload = null; 
int fileProcessingIndicator = 0;  
String endPointSIOPI = (String)wfc.getWFContent("endPointSIOPI");  
String proposalNumber = (String)wfc.getWFContent("numero-proposta-online");    
String ssoToken = (String)wfc.getWFContent("sso_token");   
String fileName = (String)wfc.getWFContent("fileName");
String antivirus = (String)wfc.getWFContent("antivirus"); 
String hashMD5 = (String)wfc.getWFContent("hashMD5"); 
String documentNumber = (String)wfc.getWFContent("numeroDocumento");
URL serverUrl = new URL(endPointSIOPI+proposalNumber+"/documentos/"+documentNumber);   
HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();           
String boundaryString = "----888607089620547269893505";       
String fileUrl = "/B2B_MOBILIDADE/"+fileName; 
if (antivirus == "OK"){        
    logFileToUpload = new File(fileUrl);         
}
urlConnection.setDoOutput(true);     
urlConnection.setRequestMethod("POST");     
urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);    
urlConnection.addRequestProperty("Authorization","Bearer " + ssoToken);     
urlConnection.addRequestProperty("Accept","application/json");    
OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();    
BufferedWriter httpRequestBodyWriter =     new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));     
httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");   
httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"hashMD5\"");    
httpRequestBodyWriter.write("\n\n");    
httpRequestBodyWriter.write(hashMD5);    
httpRequestBodyWriter.write("\n--" + boundaryString + "\n");    
httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"numeroDocumento\"");    
httpRequestBodyWriter.write("\n\n");    
httpRequestBodyWriter.write(documentNumber);    
httpRequestBodyWriter.write("\n--" + boundaryString + "\n");  
httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"numeroPropostaOnline\"");    
httpRequestBodyWriter.write("\n\n");    
httpRequestBodyWriter.write(proposalNumber);    
httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
if (antivirus == "OK"){
   fileProcessingIndicator = 1;
}else{
   fileProcessingIndicator = 2;
}
httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"processamentoArquivo\"");    
httpRequestBodyWriter.write("\n\n");    
httpRequestBodyWriter.write(fileProcessingIndicator);    
httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
if (antivirus == "OK"){ 
   httpRequestBodyWriter.write("Content-Disposition: form-data;"     + "name=\"arquivo\";"     + "filename=\""+ logFileToUpload.getName() +"\""     + "\nContent-Type: application/octet-stream\n\n");     
   httpRequestBodyWriter.flush(); 
}else{
   httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"codigoMensagemProcessamento\"");       
   httpRequestBodyWriter.write("\n\n");       
   httpRequestBodyWriter.write("2 - Falha no processamento do arquivo");
   httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
   httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"mensagemProcessamento\"");       
   httpRequestBodyWriter.write("\n\n");       
   httpRequestBodyWriter.write(antivirus);
}  
httpRequestBodyWriter.write("\n--" + boundaryString + "\n");   
if (antivirus == "OK"){  
   FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);     
   int bytesRead;   byte[] dataBuffer = new byte[1024];    
   while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {     
     outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);    
   }     
   outputStreamToRequestBody.flush();      
   httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");     
   httpRequestBodyWriter.flush();      
   outputStreamToRequestBody.close();
}else{    
   httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");         
   httpRequestBodyWriter.flush();    
}       
httpRequestBodyWriter.close();       
StringBuffer response = new StringBuffer();   
BufferedReader in = null;   
try {      
  if(urlConnection.getResponseCode() != 200) {  
     in = new BufferedReader(  new InputStreamReader(urlConnection.getErrorStream())); 
  }else{  
     in = new BufferedReader(  new InputStreamReader(urlConnection.getInputStream())); 
  }
String inputLine;         
while ((inputLine = in.readLine()) != null) {       
  response.append(inputLine);      
}      
in.close();      
log.log("response:"+response.toString());     
}catch(Exception ex){      
  log.log(ex.getMessage());      
}        
wfc.setAdvancedStatus("POST");        
wfc.setBasicStatus(000);        
wfc.addWFContent("HTTPResponse",response.toString());  
String responseCode = Integer.toString(urlConnection.getResponseCode()); 
wfc.addWFContent("ResponseCode",responseCode);  
return "000";