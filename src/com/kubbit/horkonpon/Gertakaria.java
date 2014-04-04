package com.kubbit.horkonpon;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;

import com.kubbit.utils.Utils;

public class Gertakaria
{
	final int HORKONPON_API_MESSAGE_VERSION = 1;

	private String argazkia = "";
	private String fitxategiIzena = "";
	private double latitudea = 0;
	private double longitudea = 0;
	private double zehaztasuna = Float.MAX_VALUE;
	private String herria = "";
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

		json.put("bertsioa", HORKONPON_API_MESSAGE_VERSION);

		JSONObject app = new JSONObject();
		app.put("os", Utils.getOSVersion());
		app.put("version", Utils.getVersion(context));
		json.put("app", app);

		if (this.argazkia != null && !this.argazkia.equals(""))
		{
			JSONObject jsArgazkia = new JSONObject();
			jsArgazkia.put("izena", this.fitxategiIzena);
			jsArgazkia.put("edukia", this.argazkia);
			json.put("argazkia", jsArgazkia);
		}

		if (this.latitudea != 0 || this.longitudea != 0)
		{
			JSONObject gps = new JSONObject();
			gps.put("latitudea", this.latitudea);
			gps.put("longitudea", this.longitudea);
			gps.put("zehaztasuna", this.zehaztasuna);
			json.put("gps", gps);
		}

		if (this.herria != null && !this.herria.equals(""))
			json.put("herria", this.herria);
		if (this.izena != null && !this.izena.equals(""))
			json.put("izena", this.izena);
		if (this.telefonoa != null && !this.telefonoa.equals(""))
			json.put("telefonoa", this.telefonoa);
		if (this.posta != null && !this.posta.equals(""))
			json.put("posta", this.posta);
		if (this.ohartarazi)
			json.put("ohartarazi", this.ohartarazi);
		if (this.hizkuntza != null && !this.hizkuntza.equals(""))
			json.put("hizkuntza", this.hizkuntza);
		if (this.oharrak != null && !this.oharrak.equals(""))
			json.put("oharrak", this.oharrak);

		return json.toString();
	}
}