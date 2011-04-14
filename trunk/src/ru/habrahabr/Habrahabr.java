package ru.habrahabr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

public class Habrahabr extends Activity {
    
	WebView mResultView = null;
	URLClient urlClient = null;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Log.d("onCreate", "SharedPref");
        
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences.Editor preferencesEditor = preferences.edit();
        
        urlClient = new URLClient();
        
        Log.d("onCreate", "mResultView");
        
        mResultView = (WebView) findViewById(R.id.result);
        if(mResultView == null) Log.e("onCreate", "mResultView == null");
        
        Log.d("onCreate", "Settings");
        mResultView.getSettings().setAllowFileAccess(true);
        mResultView.getSettings().setJavaScriptEnabled(true);
        Log.d("onCreate", "Load temp data");
        mResultView.loadData("Loading...", "text/html", "utf-8");
        
        /*String out = "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><link href=\"general.css\" rel=\"stylesheet\" media=\"all\"/></head>";
        String data = urlClient.getURL("http://habrahabr.ru/?fl=all");
        
        HabraTopicParser parser = new HabraTopicParser(data);
        HabraTopic topic = null;
        while((topic = parser.parseTopicFromList()) != null)
        {
        	Log.d("while", topic.title);
        	out += RemoveImage.remove(topic.getTopicDataAsHTML());
        }*/
        
        Log.d("onCreate", "Load data");
        
        String out = "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><link href=\"general.css\" rel=\"stylesheet\" media=\"all\"/></head>";
        String data = urlClient.getURL("http://habrahabr.ru/blogs/java/117386/");
        
        HabraTopicParser parser = new HabraTopicParser(data);
        HabraTopic topic = parser.parseFullTopic();
        
        if(topic == null) Log.w("onCreate", "topic == null");
        
        out += topic.getTopicDataAsHTML();
        out += "<hr>";
        
        HabraComment comment = null;
        HabraCommentParser comParser = new HabraCommentParser(data);
        while((comment = comParser.parseComment()) != null)
        {
        	out += "<div style='margin-left:"+(comment.padding*10)+"px;border:1px solid blue;'>"+comment.getCommentAsHTML()+"</div>";
        }
          
        mResultView.loadDataWithBaseURL("file:///android_asset/", out, "text/html", "utf-8", null);
        
        final Button go = (Button) findViewById(R.id.go);
        final EditText code = (EditText) findViewById(R.id.code);
        final EditText login = (EditText) findViewById(R.id.login);
        final EditText pass = (EditText) findViewById(R.id.password);
        
        go.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(code.getText().length() < 6)
				{
					byte[] bb = urlClient.getURLAsBytes("http://habrahabr.ru/core/captcha/");
			        
			        String imgCa = getFilesDir().getAbsolutePath() + "/code.png";
			        Log.i("imgCa", imgCa);
			        File f = new File(imgCa);
			        try {
			        	if(f.exists()) f.delete();
						f.createNewFile();
						new FileOutputStream(f).write(bb);
					} catch (IOException e) {
						Log.e("IOException", e.getMessage());
					}
					
					/*String data = "<img src=\"code.png\"><br>";
					String[] ff = getFilesDir().list();
					for(int i = 0; i < ff.length; i++) data += ff[i]+"<br>";
					mResultView.loadDataWithBaseURL(getFilesDir().getAbsolutePath(), data, "text/html", "utf-8", null);*/
					
					mResultView.loadUrl("file://" + imgCa);
				}
				else
				{
					String[][] post = new String[][]{{"act","login"}, {"redirect_url","http://habrahabr.ru/"},
			        		{"login",login.getText().toString()},{"password",pass.getText().toString()},
			        		{"captcha",code.getText().toString()},{"","true"}};
			        
			        String data = urlClient.postURL("http://habrahabr.ru/ajax/auth/", post, "http://habrahabr.ru/login/");
			        
			        mResultView.loadDataWithBaseURL("http://habrahabr.ru/", data, "text/html", "utf-8", null);
				}
			}
		});
        
    }
    
    public void onBackPressed()
    {
    
    }
    
    /**
     * Копирует файл из assets в ~/files/
     * @param file Файл для копирования
     * @return путь до файла в папке files
     */
    public String copyInFilesDir(String file)
    {
    	String filePath = getFilesDir().getAbsolutePath() + "/" + file;
    	Log.d("copyFile", filePath);
    	
    	File fileDesc = new File(filePath);
    	
    	if(fileDesc.exists()) return filePath;
    	
		try {
			InputStream inputStream = getAssets().open(file);
			FileOutputStream outputStream = new FileOutputStream(fileDesc);
			
	    	byte buf[] = new byte[1024];
	    	
	    	int len = 0;
    		while ((len = inputStream.read(buf)) != -1) 
			{
				outputStream.write(buf, 0, len);
			}
			
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
			return null;
	    }
    
    	return filePath;
    }
}