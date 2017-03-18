package net.qsoft.docview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

public class Util {
	public static String TAG = Util.class.getSimpleName();
	
	public static String REMOTE_BASE_URL = "http://192.168.0.100:8088/bracbd/";
	public static String LOCAL_BASE_URL="http://localhost:8080/";
	public static String ld=Environment.getExternalStorageDirectory().getAbsolutePath().toString();
	public static String DIR_WWW=ld+ "/WWW/app_html/";
	public static String FILE_LIST_NAME_PREV="json.txt";
	public static String FILE_LIST_SERVICE="json.php";
	
	public Util() {
		// TODO Auto-generated constructor stub
	}

	public static String localPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/WWW/";
	}
	
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}

	public static String getStringFromFile (String filePath) throws Exception {
	    File fl = new File(filePath);
	    FileInputStream fin = new FileInputStream(fl);
	    String ret = convertStreamToString(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}	
	
	   public static Date ConvertStringToDate(String dateString, String formatString) {
	    	Date ret=null;
	    	
	    	try {
	    		ret = (new SimpleDateFormat(formatString, Locale.getDefault())).parse(dateString);
//	    		ret = (new SimpleDateFormat(formatString, Locale.ENGLISH)).parse(dateString);
	    	} catch (ParseException e) {
	    		Log.e(TAG, e.toString());
	    	}
	    	return ret;
	    }
	    
	    public static Date ConvertStringToDate(String dateString) {
	    	return ConvertStringToDate(dateString, "yyyy-MM-dd hh:mm:ss");
	    }
	    
	    public static String FormatDate(Date date, String formatString) {
	    	return (new SimpleDateFormat(formatString, Locale.getDefault())).format(date);
	    }
	    
	    public static String PathCombine (String path1, String path2)
	    {
	        File file1 = new File(path1);
	        File file2 = new File(file1, path2);
	        return file2.getPath();
	    }
	    
	 // pad with " " to the right to the given length (n)
	    public static String padRight(String s, int n) {
	      return String.format("%1$-" + n + "s", s);
	    }

	    // pad with " " to the left to the given length (n)
	    public static String padLeft(String s, int n) {
	      return String.format("%1$" + n + "s", s);
	    }
	    
	    public static int daysBetweenDates(Date d1, Date d2) {
	    	long diff = d2.getTime() - d1.getTime();
	    	return (int) (diff / (1000 * 60 * 60 * 24));
	    }
	    
	    //method for pursinng  json from url 
	    public static String getDataFromURL(String myURL) {
	        System.out.println("Requested URL:" + myURL);
	        StringBuilder sb = new StringBuilder();
	        URLConnection urlConn = null;
	        InputStreamReader in = null;
	        try {
	            URL url = new URL(myURL);
	            urlConn = url.openConnection();
	            if (urlConn != null) {
	                urlConn.setReadTimeout(60 * 1000);
	            }
	            if (urlConn != null && urlConn.getInputStream() != null) {
	                in = new InputStreamReader(urlConn.getInputStream(),
	                        Charset.defaultCharset());
	                BufferedReader bufferedReader = new BufferedReader(in);
	                if (bufferedReader != null) {
	                    int cp;
	                    while ((cp = bufferedReader.read()) != -1) {
	                        sb.append((char) cp);
	                    }
	                    bufferedReader.close();
	                }
	            }
	            in.close();
	        } catch (Exception e) {
	            throw new RuntimeException("Exception while calling URL:" + myURL, e);
	        }

	        return sb.toString();
	    }

	    //method for pursing  json from local 
	    public static String getDataFromFile(String myPath) {
	        String jsonData = "";
	        BufferedReader br = null;
	        
	        try {
	            String line;
	            br = new BufferedReader(new FileReader(myPath));
	            while ((line = br.readLine()) != null) {
	                jsonData += line + "\n";
	            }
	        } catch (IOException e) {
	            e.getCause();
	        } finally {
	            try {
	                if (br != null) {
	                    br.close();
	                }
	            } catch (IOException ex) {
	                ex.getCause();
	            }
	        }
	        return jsonData;
	    }

	    public static void createFile(String path, String txt) throws IOException{
	         BufferedWriter output = null;
	        try {
	        	
	            File file = new File(path);
	            if(file.exists()) 
	            	file.delete();

	            output = new BufferedWriter(new FileWriter(file));
	            output.write(txt);
	        } catch ( IOException e ) {
	            Log.d(TAG, e.getMessage());
	        } finally {
	          if ( output != null ) {
	            output.close();
	          }
	        }
	    }
	    
	    public static Map<String, FileDesc> getFileListFromJSON(String lst) throws JSONException {
	    	
	    	Map<String, FileDesc> newLst = new HashMap<String, FileDesc>();
	    	
            JSONArray jr = new JSONArray(lst);
            for(int i = 0 ; i < jr.length(); i++) {
            	JSONObject jo = jr.getJSONObject(i);
            	FileDesc fd = new FileDesc(jo.getString("name"),
            				jo.getString("type"),
            				Util.ConvertStringToDate(jo.getString("date")), (long) 0);
            	newLst.put(fd.get_Name(), fd);
            }
            return newLst;
	    }
	    
	    // Copy file
	    public static void CopyFile(String srcFile, String destFile) {
	    	// Check source exists
	    	InputStream in = null;
	    	OutputStream out = null;

	    	try {
				File sf = new File(srcFile);
				if (sf.exists()) {
					// Source exists. Check destination
					File df = new File(destFile);
					if (df.exists()) {
						df.delete();
					} else {
						// Extract file path
						String destPath = destFile.substring(0,
								destFile.lastIndexOf("/") - 1);
						File dd = new File(destPath);
						if (!dd.exists())
							dd.mkdirs();
					}
					in = new FileInputStream(srcFile);
					out = new FileOutputStream(destFile);

					byte[] buffer = new byte[1024];
					int read;
					while ((read = in.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
					in.close();
					in = null;

					// write the output file (You have now copied the file)
					out.flush();
					out.close();
					out = null;
				}
	        }  catch (FileNotFoundException fnfe1) {
	            Log.e("tag", fnfe1.getMessage());
	        }
	                catch (Exception e) {
	            Log.e("tag", e.getMessage());
	        }
	   }
	    
	    // Recursively delete directory or file
	    public static void deleteRecursive(File fileOrDirectory) {

	    	 if (fileOrDirectory.isDirectory())
	    	    for (File child : fileOrDirectory.listFiles())
	    	        deleteRecursive(child);

	    	    fileOrDirectory.delete();

	    }
}
