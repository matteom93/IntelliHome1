package com.auh.opencomune;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class App extends FragmentActivity {

	private static final String url = "jdbc:mysql://db4free.net:3306/intellihomedb";
	private static final String user = "intellihomegroup";
	private static final String pass = "intellihomepassword";
	
	public int numeronotizie = 0;
	public int num_acqua = 0; //numero elementi memorizzati - consumo acqua
	public int num_giorno=0; //numero campioni nel giorno scelto
	public float acqua_gior=0; //consumo giornaliero di acqua
	public float prev_acqua=0; //previsione settimanale consumo acqua
	public String giorno="2015-07-24"; //giorno in cui fare l'analisi - DA ACQUISIRE MEDIANTE CALENDARIO!!! O DA TEXT
	public ArrayList <String> notizie;
	
	public int[] consumo_acqua = new int[num_acqua]; //contiene consumo dell'acqua espresso il lt/ora
	public Date [] date_acqua = new Date[num_acqua]; //contiene la data relativa al consumo
	public Float [] orari_acqua =new Float[num_acqua]; //contiene gli orari espressi in ore
	
	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	
	private int currentColor = 0xFFF4842D;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_principale);
		
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (ViewPager) findViewById(R.id.pager);
		
		adapter = new MyPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources()
				.getDisplayMetrics());
		pager.setPageMargin(pageMargin);
		tabs.setViewPager(pager);
		changeColor(currentColor);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.principale, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		}
		return super.onOptionsItemSelected(item);
	}
	
	//connessione alla tabella ACQUA del database e ricezione consumi
	private class ConnessioneDB_consumi extends AsyncTask<String, Void, int[]> {
		private ProgressDialog dialog;
		protected void onPreExecute() {
			dialog = new ProgressDialog(App.this);
			dialog.setIndeterminate(true);
			dialog.setMessage("In connessione");
			dialog.setTitle("Attendi per piacere");
			dialog.show();
		}
	    @Override
	    protected int[] doInBackground(String... urls) {
	       int[] appoggio = new int[1]; //variabile di appoggio usata in caso di errore lettura database
	       appoggio[0]=0;
	       try {
	          Class.forName("com.mysql.jdbc.Driver");
	          Connection con = DriverManager.getConnection(url, user, pass);
	          
	          Statement statement = con.createStatement();
	          ResultSet resultset = statement.executeQuery("select * from ACQUA");
	          
	          num_acqua=0;
	          while(resultset.next()) num_acqua++; //numero di elementi presenti nel database (numero righe)
	          int[] cons_acqua = new int[num_acqua]; //vengono memorizzati i consumi di acqua
	          int i=0;
	          ResultSet rs = statement.executeQuery("select * from ACQUA");
	          while(rs.next()) {
	        	  cons_acqua[i]=rs.getInt(4); //4 -> numero colonna "consumi"
	              i++;
	          }
	          dialog.dismiss();
	  		  return cons_acqua;     
	      }
	      catch(Exception e) {
	          e.printStackTrace();
	      }
	      return appoggio;	    
	    }
	}
	
	//connessione alla tabella ACQUA del database e ricezione data
	private class ConnessioneDB_data extends AsyncTask<String, Void, Date[]> {
		private ProgressDialog dialog;
		protected void onPreExecute() {
			dialog = new ProgressDialog(App.this);
			dialog.setIndeterminate(true);
			dialog.setMessage("In connessione");
			dialog.setTitle("Attendi per piacere");
			dialog.show();
		}
	   @Override
	   protected Date[] doInBackground(String... urls) {
         String appoggio_data=""; //stringa che contiene le date
         Date [] dates = new Date[num_acqua];
         SimpleDateFormat sdt = new SimpleDateFormat("yyyy-mm-dd");
         try {
		    Class.forName("com.mysql.jdbc.Driver");
		    Connection con = DriverManager.getConnection(url, user, pass);
		 		          
		    Statement statement = con.createStatement();
		    ResultSet rs = statement.executeQuery("select * from ACQUA");
		    int i=0;
		    while(rs.next()){
		       appoggio_data=rs.getString(2); //2 -> numero colonna "data"
		       dates[i]=sdt.parse(appoggio_data);//conversione stringa in formato data
		       i++;
		    }		    
		    dialog.dismiss();   
		  }
		  catch(Exception e) {
		       e.printStackTrace();
		  }
		  return dates;
		}
     }
	
	//connessione alla tabella ACQUA del database e ricezione orario
		private class ConnessioneDB_orario extends AsyncTask<String, Void, Float[]> {
			private ProgressDialog dialog;
			protected void onPreExecute() {
				dialog = new ProgressDialog(App.this);
				dialog.setIndeterminate(true);
				dialog.setMessage("In connessione");
				dialog.setTitle("Attendi per piacere");
				dialog.show();
			}
		   @Override
		   protected Float[] doInBackground(String... urls) {
	         String appoggio_orario=""; //stringa che contiene gli orari
	         char car1,car2,car3,car4,car5,car6; //variabili di appoggio caratteri
	         int num1,num2,num3,num4,num5,num6; //variabili di appoggio numeri
	         int ore,min,sec,tempo_sec;
	         Float [] times = new Float[num_acqua]; //tempo espresso in ore
	         try {
			    Class.forName("com.mysql.jdbc.Driver");
			    Connection con = DriverManager.getConnection(url, user, pass);
			          
			    Statement statement = con.createStatement();
			    ResultSet rs = statement.executeQuery("select * from ACQUA");
			    int j=0;
			    while(rs.next()){
			        appoggio_orario=rs.getString(3); //3 -> numero colonna "ora"
			        car1=appoggio_orario.charAt(0);
			        car2=appoggio_orario.charAt(1);
			        car3=appoggio_orario.charAt(3);
			        car4=appoggio_orario.charAt(4);
			        car5=appoggio_orario.charAt(6);
			        car6=appoggio_orario.charAt(7);
			        num1=Character.getNumericValue(car1);
			        num2=Character.getNumericValue(car2);
			        num3=Character.getNumericValue(car3);
			        num4=Character.getNumericValue(car4);
			        num5=Character.getNumericValue(car5);
			        num6=Character.getNumericValue(car6);
			        ore=num1*10+num2;
			        min=num3*10+num4;
			        sec=num5*10+num6;
			        tempo_sec=ore*3600+min*60+sec; //tempo in secondi 
			        times[j]=(float)tempo_sec/3600;
			        j++;
			    }
			    dialog.dismiss();   
			    }
			    catch(Exception e) {
			       e.printStackTrace();
			    }
			    return times;
			 }
	     }
	
	public void analisi_consumi()
	{
		SimpleDateFormat sdt = new SimpleDateFormat("yyyy-mm-dd");
		try {
			Toast.makeText(App.this, "Attendi qualche istante", Toast.LENGTH_SHORT).show();
			AsyncTask<String,Void,int[]> task = new ConnessioneDB_consumi();
			consumo_acqua = task.execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		try {
			AsyncTask<String,Void,Date[]> task = new ConnessioneDB_data();
			date_acqua = task.execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		try {
			AsyncTask<String,Void,Float[]> task = new ConnessioneDB_orario();
			orari_acqua = task.execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		//individuazione del giorno corrente e calcolo del consumo giornaliero (integrale con metodo del trapezio)
		int i=0;
		num_giorno=0; //numero campionamenti nel giorno scelto
		for (i=0;i<num_acqua;i++)
			if (sdt.format(date_acqua[i]).equals(giorno)) //confronto se la data acquisita da db corrisponde con il giorno selezionato
				num_giorno++;
		int[] indici_giorno = new int[num_giorno]; //indici dei vettori di consumi, ora e data relativi al giorno scelto (chiave primaria)
		int k=0;
		for (i=0;i<num_acqua;i++) //scorro il vettore e memorizzo gli indici corrispondenti
			if (sdt.format(date_acqua[i]).equals(giorno)){
				indici_giorno[k]=i;
				k++;
			}
		//Calcolo dell'integrale con il metodo del trapezio
		for(k=0;k<(num_giorno-1);k++)
			acqua_gior=acqua_gior+((consumo_acqua[indici_giorno[k]]+consumo_acqua[indici_giorno[k+1]])*(orari_acqua[indici_giorno[k+1]]-orari_acqua[indici_giorno[k]])/2);
		prev_acqua=acqua_gior*7;
		//Visualizzazione risultato su TextView
		TextView acqua_sett,acqua_giorno;
		acqua_giorno = (TextView) findViewById(R.id.tv_acqua_giorno);
		acqua_sett = (TextView) findViewById(R.id.tv_acqua_sett);
		acqua_giorno.setText(Float.toString(acqua_gior));
		acqua_sett.setText(Float.toString(prev_acqua));
		
		//CONTINUARE!!!!
		// 1) Scrivere su casella di testo acqua_gior, fare *7 e scrivere previsione settimanale
		// 2) Introdurre possibilit� di scelta giorno da agenda
		// 3) Disegnare il grafico
		// 4) Cambiare icona 
		
		grafico_acqua(indici_giorno);
	}
	
	public void grafico_acqua(int[] indici_giorno){ //costruzione del grafico sul consumo di acqua
		LinearLayout layout = (LinearLayout)findViewById(R.id.chartcontainer);	
		XYSeries series = new XYSeries("Consumo giornaliero di acqua");
		XYMultipleSeriesDataset mySeries= new XYMultipleSeriesDataset();
		for (int i=0;i<num_giorno;i++) //popolamento della serie per grafico
			series.add(orari_acqua[indici_giorno[i]],consumo_acqua[indici_giorno[i]]);
		mySeries.addSeries(series);
		//Paramentri costruttivi del grafico
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(2);
		renderer.setColor(Color.GREEN);
		renderer.setDisplayBoundingPoints(true);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setPointStrokeWidth(3);
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
		//mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); //margini trasparenti
		mRenderer.setPanEnabled(false, false); //no assi cartesiani
		mRenderer.setShowGrid(true); // mostra griglia
		Log.d("Grafico","Grafico pronto da inserire");
		
		GraphicalView grafico = ChartFactory.getLineChartView(getBaseContext(), mySeries, mRenderer);
		//layout.addView(grafico);
		AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);
		mbuilder.setView(grafico);
		mbuilder.create().show();
	}
	
	public ArrayList <String> ricevinotizie()
	{
		ArrayList <String> risultato = new ArrayList<String>();
		Connection conn = null;
		Statement mess = null;
		ResultSet ris = null;
		
		String url = "jdbc:mysql://db4free.net:3306/";
        String nomedb = "auhprojectdb";
        String driver = "com.mysql.jdbc.Driver";
        String username = "auhcoders";
        String password = "auh2013";
        String query = "SELECT notizie FROM Notizie;";       
        
        try {
          Class.forName(driver).newInstance();
          conn = DriverManager.getConnection(url+nomedb,username,password);
          mess = conn.createStatement();
          ris = mess.executeQuery(query);
          
          int i =0;
          while (ris.next()) 
          {
        	  risultato.add(ris.getString(1));
        	  i++;
          }
          
          numeronotizie=i;
          ris.close();
          mess.close();
          conn.close();
          
          Toast.makeText(this, "Connessione riuscita, "+numeronotizie+" elementi", Toast.LENGTH_SHORT).show();
          
        } catch (Exception e) {
        	e.printStackTrace();
        	Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return risultato;
	}
	
	public int inviadati(String dest,String nome, String cognome, String testo, String ora, String data, String luogo)
	{
		Connection conn = null;
		Statement mess = null;
		int aggiorna = 0;
		
		String url = "jdbc:mysql://db4free.net:3306/";
        String nomedb = "auhprojectdb";
        String driver = "com.mysql.jdbc.Driver";
        String username = "auhcoders";
        String password = "auh2013";
        String query = "INSERT INTO Segnalazioni (nome,cognome,destinazione,segnalazione,data,ora,luogo) VALUES ('"+nome+"','"+cognome+"','"+dest+"','"+testo+"','"+data+"','"+ora+"','"+luogo+"');";       
        
        try {
          Class.forName(driver).newInstance();
          conn = DriverManager.getConnection(url+nomedb,username,password);
          Toast.makeText(this, "Connessione riuscita", Toast.LENGTH_SHORT).show();
          mess=conn.createStatement();
          aggiorna = mess.executeUpdate(query);
          if (aggiorna != 0) {
        	  Toast.makeText(this, "Invio effettuato", Toast.LENGTH_SHORT).show();
        	  mess.close();
        	  conn.close();
        	  }
        } catch (Exception e) {
        	e.printStackTrace();
        	Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
          return 1;
        }
      
		return 0;
	}
	
	public void onClick (View v){
		switch (v.getId())
		{
		case R.id.segnalainvia:
		{
			EditText casellanome = (EditText) findViewById(R.id.txtnome);
			EditText casellacognome = (EditText) findViewById(R.id.txtcognome);
			EditText casellatesto = (EditText) findViewById(R.id.txttesto);
			EditText caselladata = (EditText) findViewById(R.id.txtdata);
			EditText casellaora = (EditText) findViewById(R.id.txtora);
			EditText casellaluogo = (EditText) findViewById(R.id.txtluogo);
			if (casellanome.getText().toString().matches("") || casellacognome.getText().toString().matches("") || casellatesto.getText().toString().matches("") || casellaora.getText().toString().matches("")|| caselladata.getText().toString().matches("")|| casellaluogo.getText().toString().matches("")) 
			{
				Toast.makeText(this, "Riempi tutti i campi", Toast.LENGTH_SHORT).show();
				
			}
			else
			{
				Spinner scelta = (Spinner) findViewById(R.id.sceltasegnala);
				inviadati(scelta.getSelectedItem().toString(),casellanome.getText().toString(),casellacognome.getText().toString(),casellatesto.getText().toString(),caselladata.getText().toString(),casellaora.getText().toString(),casellaluogo.getText().toString());
				casellanome.setText("");
				casellacognome.setText("");
				casellatesto.setText("");
				casellaora.setText("");
				caselladata.setText("");
				casellaluogo.setText("");
			}
			break;
		}
		case R.id.notifica:
		{	
			notizie = ricevinotizie();
			LinearLayout listanotizie = (LinearLayout) findViewById(R.id.notiziecont);
			listanotizie.removeAllViewsInLayout();
			if (numeronotizie!=0)
			{
				for (int i=0; i<numeronotizie;i++)
				{
					TextView casella = (TextView) getLayoutInflater().inflate(R.layout.notizia, null);
					casella.setText(notizie.get(i));
					listanotizie.addView(casella);
					View divider = (View) getLayoutInflater().inflate(R.layout.divisore, null);
					listanotizie.addView(divider);
				}
			}
			break;
		}
		case R.id.richiedimoduli:
		{
			Toast.makeText(App.this, "Attendi qualche istante", Toast.LENGTH_SHORT).show();
			analisi_consumi();
			break;
		}
		}
	}
	
	private void changeColor(int newColor) {

		tabs.setIndicatorColor(newColor);
		currentColor = newColor;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentColor", currentColor);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentColor = savedInstanceState.getInt("currentColor");
		changeColor(currentColor);
	}
    
	public class MyPagerAdapter extends FragmentPagerAdapter {

		private final String[] TITLES = { "Notizie", "Segnala", "Consumo acqua", "Info" };

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			return CardFragment.newInstance(position);
		}

	}		
}