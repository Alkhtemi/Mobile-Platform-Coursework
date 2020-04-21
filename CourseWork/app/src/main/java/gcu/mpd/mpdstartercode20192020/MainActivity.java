package gcu.mpd.mpdstartercode20192020;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private TextView rawDataDisplay;
    //    private TextView description;
//    private TextView pubdate;
//    private TextView link;
    private String result;
    Spinner spinner;
    private Button startButton;
    private Button mapbutton;
    // Traffic Scotland URLs
    //private String urlSource = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String urlSource = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    //private String urlSource = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private EditText inputSearch;
    private ListView lv;
    private ArrayList<HashMap<String, String>> RSSList = new ArrayList<>();
    private HashMap<String,String> rss = new HashMap<>();
    private SimpleAdapter adapter;
   // private Handler handler= new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        final String[] select_qualification = {
                "Current Incidents", "Roadworks", "Planned Roadworks"};
        spinner = (Spinner) findViewById(R.id.List_Links);

        ArrayList<String> listVOs = new ArrayList<>();

        for (int i = 0; i < select_qualification.length; i++) {

            listVOs.add(select_qualification[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, select_qualification);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        rawDataDisplay = (TextView) findViewById(R.id.rawDataDisplay);
//        description = (TextView) findViewById(R.id.description);
//        pubdate = (TextView) pubdate.findViewById(R.id.pubdate);
//        link = (TextView) link.findViewById(R.id.link);
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        mapbutton = (Button) findViewById(R.id.mapbutton);
        mapbutton.setOnClickListener(this);


    }

    Document ConvertString2XML(String xmlString) {

        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlString)));
            NodeList errNodes = doc.getElementsByTagName("error");
            if (errNodes.getLength() > 0) {
                Element err = (Element) errNodes.item(0);
                System.out.println(err.getElementsByTagName("errorMessage")
                        .item(0)
                        .getTextContent());
            } else {
                // success
                NodeList tmp = doc.getElementsByTagName("description");
                rawDataDisplay.setText(tmp.item(0).getTextContent() + "\n");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;

    }
public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
    return false;
}
    private Object getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }

    public void onClick(View aview) {
//        startProgress();
        if (spinner.getSelectedItemPosition() == 1) {
            urlSource = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
        } else if (spinner.getSelectedItemPosition() == 2) {
            urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
        } else {
            urlSource = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
        }

        new Thread(new Runnable() {
            public void run() {
                final String XMLString = getTextFromWeb(urlSource); // format your URL
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Output", XMLString);
//                        rawDataDisplay.setText(XMLString);
                        Document doc = ConvertString2XML(XMLString);
                        try {
                            displayAcc(doc);
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
        if (aview == mapbutton) {
            Intent i = new Intent(MainActivity.this,MapsActivity.class);
            startActivity(i);
        }
    }

    public String getTextFromWeb(String urlString) {
        URLConnection feedUrl;
        String OutputXML = "";

        try {
            feedUrl = new URL(urlString).openConnection();
            InputStream is = feedUrl.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;

            while ((line = reader.readLine()) != null) // read line by line
            {
                OutputXML += (line); // add line to list
//                Log.e("MOhamm",line);
            }
            is.close(); // close input stream

            return OutputXML; // return whatever you need
        } catch (Exception e) {
            e.printStackTrace();
        }

        return OutputXML;
    }

    void displayAcc(Document doc) throws ParserConfigurationException, IOException, SAXException {
               DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(urlSource);
        Element element = doc.getDocumentElement();
        element.normalize();
        NodeList tmp = doc.getElementsByTagName("item");
        for (int i=0; i <tmp.getLength(); i++){
            Node node = tmp.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element2 = (Element) node;

                rawDataDisplay.setText(rawDataDisplay.getText() + "\nRoad Name:- " + getValue("title", element2) + "\n");
                rawDataDisplay.setText(rawDataDisplay.getText() + "\nDescription:- \n" + getValue("description", element2) + "\n");
                rawDataDisplay.setText(rawDataDisplay.getText() + "\nPubDate:- \n" + getValue("pubDate", element2) + "\n");
                rawDataDisplay.setText(rawDataDisplay.getText() + "\nLink:- \n" + getValue("link", element2));
                rawDataDisplay.setText(rawDataDisplay.getText() + "\n-----------------------------------------------------------");
            };
        }

    }

}







