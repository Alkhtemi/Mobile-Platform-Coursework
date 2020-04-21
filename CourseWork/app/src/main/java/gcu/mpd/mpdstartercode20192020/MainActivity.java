package gcu.mpd.mpdstartercode20192020;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    String[] mobileArray = {};
    private EditText inputSearch;
    private String result;
    Spinner spinner;
    private Button startButton;
    private Button mapbutton;
    private String urlSource = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private SimpleAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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



        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        mapbutton = (Button) findViewById(R.id.mapbutton);
        mapbutton.setOnClickListener(this);
        ArrayAdapter adapter2 = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter2);

    }
    void SetAdpet(){
        ArrayAdapter adapter2 = new ArrayAdapter<String>(this,
                R.layout.activity_listview);
        adapter2.add("hi");
        adapter2.add("sssss");
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter2);

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
//                rawDataDisplay.setText(tmp.item(0).getTextContent() + "\n");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;

    }

    private Object getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }
    @Override
    public void onClick(View aview) {
//        startProgress();
//        SetAdpet();
        Log.e("URLsource","Action ");
        if (spinner.getSelectedItemPosition() == 1) {
            urlSource = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
        } else if (spinner.getSelectedItemPosition() == 2) {
            urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
        } else {
            urlSource = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
        }
        Log.e("Traffic URL",urlSource);

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
        Log.e("parsing", "Data");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(urlSource);
        Element element = doc.getDocumentElement();
        element.normalize();
        NodeList tmp = doc.getElementsByTagName("item");
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview);

        for (int i = 0; i < tmp.getLength(); i++) {
            Node node = tmp.item(i);
            Log.e("Output", i + "");
            String TextContent = "";
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element2 = (Element) node;

                Log.e("Reading=", i + "  OutPuts");
                TextContent += "\nRoad Name:- " + getValue("title", element2) + "\n";
                TextContent += "\nDescription:- \n" + getValue("description", element2) + "\n";
                TextContent += "\nPubDate:- \n" + getValue("pubDate", element2) + "\n";
                TextContent += "\nLink:- \n" + getValue("link", element2);
                adapter.add(TextContent);
            }
            ;
        }

        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MainActivity.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}