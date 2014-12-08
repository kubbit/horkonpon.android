package com.kubbit.horkonpon;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.kubbit.utils.Utils;

public class Gertakaria
{
	final int HORKONPON_API_MESSAGE_VERSION = 2;
	final static String HORKONPON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private String argazkia = "";
	private String fitxategiIzena = "";
	private double latitudea = 0;
	private double longitudea = 0;
	private double zehaztasuna = Float.MAX_VALUE;
	private String herria = "";
	private Boolean anonimoa = false;
	private String izena = "";
	private String telefonoa = "";
	private String posta = "";
	private Boolean ohartarazi = false;
	private String oharrak = "";
	private String hizkuntza = "";

	public String getArgazkia()
	{
		return this.argazkia;
	}
	private byte[] readFileToByteArray(File argazkia) throws Exception
	{
		FileInputStream fi = new FileInputStream(argazkia);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int bytesRead;

		while ((bytesRead = fi.read(b)) != -1)
		{
			os.write(b, 0, bytesRead);
		}

		return os.toByteArray();
	}
	public void setArgazkia(File argazkia) throws Exception
	{
		byte[] b = this.readFileToByteArray(argazkia);

		if (android.os.Build.VERSION.SDK_INT < 8)
			this.argazkia = android.util.compatibility.Base64.encodeToString(b, android.util.compatibility.Base64.DEFAULT);
		else
			this.argazkia = Base64.encodeToString(b, Base64.DEFAULT);

		this.fitxategiIzena = argazkia.getName();
	}
	public void setArgazkia(byte[] argazkia, String izena)
	{
		if (android.os.Build.VERSION.SDK_INT < 8)
			this.argazkia = android.util.compatibility.Base64.encodeToString(argazkia, android.util.compatibility.Base64.DEFAULT);
		else
			this.argazkia = Base64.encodeToString(argazkia, Base64.DEFAULT);

		this.fitxategiIzena = izena;
	}
	public double getLatitudea()
	{
		return this.latitudea;
	}
	public void setLatitudea(double latitudea)
	{
		this.latitudea = latitudea;
	}
	public double getLongitudea()
	{
		return this.longitudea;
	}
	public void setLongitudea(double longitudea)
	{
		this.longitudea = longitudea;
	}
	public double getZehaztasuna()
	{
		return this.zehaztasuna;
	}
	public void setZehaztasuna(double zehaztasuna)
	{
		this.zehaztasuna = zehaztasuna;
	}
	public String getHerria()
	{
		return this.herria;
	}
	public void setHerria(String herria)
	{
		this.herria = herria;
	}
	public Boolean getAnonimoa()
	{
		return this.anonimoa;
	}
	public void setAnonimoa(Boolean anonimoa)
	{
		this.anonimoa = anonimoa;
	}
	public String getIzena()
	{
		return izena;
	}
	public void setIzena(String izena)
	{
		this.izena = izena;
	}
	public String getTelefonoa()
	{
		return telefonoa;
	}
	public void setTelefonoa(String telefonoa)
	{
		this.telefonoa = telefonoa;
	}
	public String getPosta()
	{
		return this.posta;
	}
	public void setPosta(String posta)
	{
		this.posta = posta;
	}
	public Boolean getOhartarazi()
	{
		return this.ohartarazi;
	}
	public void setOhartarazi(Boolean ohartarazi)
	{
		this.ohartarazi = ohartarazi;
	}
	public String getOharrak()
	{
		return this.oharrak;
	}
	public void setOharrak(String oharrak)
	{
		this.oharrak = oharrak;
	}
	public String getHizkuntza()
	{
		return this.hizkuntza;
	}
	public void setHizkuntza(String hizkuntza)
	{
		this.hizkuntza = hizkuntza;
	}

	public void clear()
	{
		this.argazkia = "";
		this.fitxategiIzena = "";
		this.latitudea = 0;
		this.longitudea = 0;
		this.zehaztasuna = Float.MAX_VALUE;
		this.herria = "";
		this.izena = "";
		this.telefonoa = "";
		this.posta = "";
		this.ohartarazi = false;
		this.oharrak = "";
	}

	public Boolean validate(Context context)
	{
		if (this.argazkia.equals("") && this.oharrak.equals(""))
		{
			Toast.makeText(context, R.string.validate_gertakaria_no_data, Toast.LENGTH_LONG).show();
			return false;
		}

		if (this.latitudea == 0 && this.longitudea == 0)
		{
			Toast.makeText(context, R.string.validate_gertakaria_no_gps, Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public String asJSON(Context context) throws Exception
	{
		JSONObject json = new JSONObject();

		json.put("version", HORKONPON_API_MESSAGE_VERSION);
		json.put("date", new SimpleDateFormat(HORKONPON_DATE_FORMAT).format(new Date()));

		JSONObject app = new JSONObject();
		app.put("os", Utils.getOSVersion());
		app.put("version", Utils.getVersion(context));
		json.put("app", app);

		if (this.argazkia != null && !this.argazkia.equals(""))
		{
			JSONObject jsFitxategia = new JSONObject();
			jsFitxategia.put("filename", this.fitxategiIzena);
			jsFitxategia.put("content", this.argazkia);
			json.put("file", jsFitxategia);
		}

		if (this.latitudea != 0 || this.longitudea != 0)
		{
			JSONObject gps = new JSONObject();
			gps.put("latitude", this.latitudea);
			gps.put("longitude", this.longitudea);
			gps.put("accuracy", this.zehaztasuna);
			json.put("gps", gps);
		}

		if (this.herria != null && !this.herria.equals(""))
		{
			JSONObject helbidea = new JSONObject();
			helbidea.put("locality", this.herria);
			json.put("address", helbidea);
		}

		JSONObject erabiltzailea = new JSONObject();
		if (!this.anonimoa)
		{
			if (this.izena != null && !this.izena.equals(""))
				erabiltzailea.put("fullname", this.izena);
			if (this.telefonoa != null && !this.telefonoa.equals(""))
				erabiltzailea.put("phone", this.telefonoa);
			if (this.posta != null && !this.posta.equals(""))
				erabiltzailea.put("mail", this.posta);

			erabiltzailea.put("notify", this.ohartarazi);
		}
		else
			erabiltzailea.put("anonymous", true);
		if (this.hizkuntza != null && !this.hizkuntza.equals(""))
			erabiltzailea.put("language", this.hizkuntza);
		json.put("user", erabiltzailea);

		if (this.oharrak != null && !this.oharrak.equals(""))
			json.put("comments", this.oharrak);

		return json.toString();
	}
}
