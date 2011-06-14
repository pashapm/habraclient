/**
 * 
 */
package ru.client.habr;

import java.util.Date;

import ru.client.habr.HabraEntry.OnSendFinish;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

/**
 * @author WNeZRoS
 *
 */
public class ActivityCommentEditor extends Activity {	
	
	private WebView mPreView;
	private EditText mText;
	private int mPostID;
	private int mParentID;
	private char mType = 'c';
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_editor);
		
		mPostID = getIntent().getIntExtra("post", 0);
		mParentID = getIntent().getIntExtra("parent", 0);
		mType = getIntent().getCharExtra("type", 'c');
		
		Dialogs.setContext(this);
		
		if(mPostID == 0) {
			Dialogs.showToast(R.string.incorrect_post);
			setResult(RESULT_CANCELED);
			finish(); 
		}
		
		mPreView = (WebView) findViewById(R.id.data);
		mText = (EditText) findViewById(R.id.text);
		
		mPreView.getSettings().setAllowFileAccess(true);
	}
	
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}
	
	public void onClickPreview(View v) {
		if(mText.getText().length() == 0) {
			Dialogs.showToast(R.string.empty_text);
			return;
		}
		
		HabraComment preview = new HabraComment();
		preview.author = HabraLogin.getHabraLogin().getUserName();
		preview.avatar = "http://habrahabr.ru/i/avatars/stub-user-small.gif";
		preview.childs = null;
		preview.content = mText.getText().toString();
		preview.id = 0;
		preview.date = new Date().toLocaleString();
		preview.newReply = false;
		preview.rating = 0;
		
		String data = "<html>\n<head>\n" 
			+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" 
			+ "<link href=\"general.css\" rel=\"stylesheet\"/>\n</head>\n" 
			+ "<body>\n<div class=\"main-content\">\n<div id=\"comments\">\n" 
			+ preview.getDataAsHTML(true) + "\n</div>\n</div>\n</body>\n</html>";
		
		mPreView.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "utf-8", null);
	}
	
	public void onClickSend(View v) {
		if(mText.getText().length() == 0) {
			Dialogs.showToast(R.string.empty_text);
			return;
		}
		
		OnSendFinish onSendFinish = new OnSendFinish() {
			@Override
			public void onFinish(boolean ok, String data) {
				if(ok) {
					setResult(RESULT_OK);
					finish();
				} else {
					Dialogs.showToast(R.string.not_added);
				}
			}
		};
		
		switch(mType) {
		case 'A': 
			HabraAnswer.send(mPostID, mText.getText().toString(), onSendFinish); 
			break;
		case 'U': 
			HabraAnswer.send(mPostID, mParentID, mText.getText().toString(), onSendFinish);
			break;
		case 'C':
			HabraComment.send(mText.getText().toString(), mPostID, mParentID, onSendFinish);
			break;
		}
	}
}
