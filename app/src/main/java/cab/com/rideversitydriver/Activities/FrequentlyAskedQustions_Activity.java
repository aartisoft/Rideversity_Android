package cab.com.rideversitydriver.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cab.com.rideversitydriver.Interfaces.AsyncTaskInterface;
import cab.com.rideversitydriver.R;
import cab.com.rideversitydriver.Utils.CommonAsynTask;
import cab.com.rideversitydriver.Utils.Constants;
import cab.com.rideversitydriver.Utils.MyApplication;
import cab.com.rideversitydriver.Utils.Utilities;

/**
 * Created by Kalidoss on 01/08/16.
 */

public class FrequentlyAskedQustions_Activity extends AppCompatActivity implements View.OnClickListener {

    MyApplication application = null;
    TextView txtTermofService;
    static TextView txtFaq;
    RelativeLayout imgBack;
    static Context context;
    static RelativeLayout layoutFull;
    static Snackbar snackbar;
    private WebView webViewFAQ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frequently_asked_questions);
        context = this;
        application = (MyApplication) getApplication();
        layoutFull = (RelativeLayout) findViewById(R.id.layout_faq);
        txtFaq = (TextView) findViewById(R.id.textView_faq);
        webViewFAQ = (WebView) findViewById(R.id.webView);
        webViewFAQ.setOnClickListener(this);
        imgBack = (RelativeLayout) findViewById(R.id.imageView_backarrow);
        imgBack.setOnClickListener(this);

        if (Utilities.isOnline(context)) {
            Faq();
        } else {
            Utilities.snackbarNoInternet(context, layoutFull);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_backarrow:
                finish();
                break;
            default:
                break;
        }
    }

    //changed from Static to non static
    public void Faq() {
        CommonAsynTask faqAsyncTask = new CommonAsynTask(context, Constants.FAQ_URL, "",
                Constants.REQUEST_TYPE_GET, new AsyncTaskInterface() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {

                if (jsonObject != null) {
                    try {
                        if (jsonObject.getString(Constants.RESULT).equals(Constants.SUCCESS)) {
                            JSONObject userObj = jsonObject.getJSONObject(Constants.DATA);
                            webViewFAQ.loadData(userObj.getString(Constants.DESCRIPTION), "text/html; charset=utf-8", "UTF-8");
                        } else if (jsonObject.getString(Constants.RESULT).equals(Constants.ERROR)) {
                            snackbar = Snackbar.make(layoutFull, "" + jsonObject.getString(Constants.MESSAGE), Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setMaxLines(15);
                            sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_background));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
                            snackbar.show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(context, "Backend server problem", Toast.LENGTH_LONG).show();
                }

            }
        });
        faqAsyncTask.execute();
    }
}