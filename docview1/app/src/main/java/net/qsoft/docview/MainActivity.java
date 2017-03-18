package net.qsoft.docview;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
	public static String TAG = MainActivity.class.getSimpleName();

	private int dnlCount = 0;
	private DownloadManager dm;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            	
            	long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            	if(validDownload(downloadId)) {
            		
            	}
            	dnlCount--;
            	if(dnlCount==0) {
            		// All download complete. finalize process
            		processFiles();
            	}
            }
        }
    };
    
    private Map<Long, FileDesc> updLst = new HashMap<Long, FileDesc>();
    private Map<String, FileDesc> delLst = new HashMap<String, FileDesc>();
    
    WebView webView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
	//	Util.DIR_WWW = Util.localPath();
		
		webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new MyWebViewClient());

        String url = "http://localhost:8080/app_html/";
		WebSettings webSettings = webView.getSettings();

		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);


        webView.loadUrl(url);
        	        
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        
        new CompareURIs().execute(Util.REMOTE_BASE_URL + Util.FILE_LIST_SERVICE);
        
	}

	
	 /* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));	
	}


	private class MyWebViewClient extends WebViewClient {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return true;
	     }
	 }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
    }
    
	private void processFiles() {
		// Copy files
		for(FileDesc x:updLst.values()) {
			Util.CopyFile(x.get_DnName(), Util.DIR_WWW + x.get_Name());
			dm.remove(x.get_DLId());
		}
		// Process delete file
		for(FileDesc x:delLst.values()) {
			File f = new File(x.get_Name());
			Util.deleteRecursive(f);
		}
		
		// Make current file list as old
		Util.CopyFile(Util.DIR_WWW + "temp.txt", Util.DIR_WWW + Util.FILE_LIST_NAME_PREV);
	}
	
	private boolean validDownload(long downloadId) {

	    Log.d(TAG,"Checking download status for id: " + downloadId);

	    //Verify if download is a success
	    Cursor c= dm.query(new DownloadManager.Query().setFilterById(downloadId));

	    if(c.moveToFirst()){            
	        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

	        if(status == DownloadManager.STATUS_SUCCESSFUL){
	        	FileDesc fd = updLst.get(downloadId);
	        	fd.set_DnName(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)));
	        	
	            return true; //Download is valid, celebrate
	        }else{
	            int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
	            Log.d(TAG, "Download not correct, status [" + status + "] reason [" + reason + "]");            
	            return false;
	        }   
	    }               
	    return false;  
	}
	
    // Async Task Class
    class CompareURIs extends AsyncTask<String, String, String> {

        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Download Music File from Internet
        @Override
        protected String doInBackground(String... f_url) {
            
            try {
                
            	String lst = Util.getDataFromURL(f_url[0]); //Util.getStringFromFile(f_url[0]);
                
            	Log.d(TAG,  lst);
				System.out.println(lst);
            	
            	// Save this file to ...
            	Util.createFile(Util.PathCombine(Util.DIR_WWW, "temp.txt"), lst );
                
            	Map<String, FileDesc> newLst = Util.getFileListFromJSON(lst);

            	lst = Util.getStringFromFile(Util.PathCombine(Util.DIR_WWW, Util.FILE_LIST_NAME_PREV) );
            	Log.d(TAG, "\nSecond list: " + lst);
				System.out.println(lst);
            	
                Map<String, FileDesc> oldLst = Util.getFileListFromJSON(lst);
                
                for(FileDesc x:oldLst.values() ) {
                	if(!newLst.containsKey(x.get_Name()) ) {
                    	delLst.put(x.get_Name(), x);
                	}
                	else {
                		// Process update 
                		FileDesc nfd = newLst.get(x.get_Name());
                		if(nfd.get_Type().equals("file")) {
	                		if(nfd.get_Date().after(x.get_Date())) {
	                			// Newer file, download
	                			Request request = new Request(Uri.parse(Util.REMOTE_BASE_URL + nfd.get_Name()));
	                			long enq = dm.enqueue(request);
	                			nfd.set_DLId(enq);
	                			dnlCount++;
	                			
	                			// put into update list
	                			updLst.put(enq, nfd);
	                		}
                		}
                	}
                }
                
                // Process new files
                for(FileDesc x:newLst.values() ) {
            		if(x.get_Type().equals("file")) {
	                	if(!oldLst.containsKey(x.get_Name()) ) {
	            			Request request = new Request(Uri.parse(Util.REMOTE_BASE_URL + x.get_Name()));
	            			long enq = dm.enqueue(request);
	            			x.set_DLId(enq);
	            			dnlCount++;
	            			// put into update list
	            			updLst.put(enq, x);
	                	}
            		}
                }                
                
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            // Dismiss the dialog after the Music file was downloaded
        
        }
    }
}
