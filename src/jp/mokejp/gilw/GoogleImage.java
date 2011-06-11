package jp.mokejp.gilw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class GoogleImage {
	private static final String QUERY_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8";
	
	public ArrayList<GoogleImageResult> requestImageSearch(String key, int width, int height, String safe, int index) {
		ArrayList<GoogleImageResult> listData = new ArrayList<GoogleImageResult>();

		String encodeKey = Uri.encode(key);

		HttpClient client = new DefaultHttpClient();

		String req = QUERY_URL + "&safe=" + safe + "&q=" + encodeKey + "+imagesize:" + width + "x" + height + "&start=" + (index * 8);

		HttpUriRequest httpUriReq = new HttpGet(req);

		try {

			HttpResponse res = client.execute(httpUriReq);

			if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException();
			} else {

				String entity = EntityUtils.toString(res.getEntity());

				JSONObject jsonObj = new JSONObject(entity);

				String responseStatus = jsonObj.getString("responseStatus");

				if (responseStatus.equals("200") != true) {
					
				} else {
					JSONObject jsonObjData = jsonObj.getJSONObject("responseData");
				//	JSONObject cursorObjData = jsonObjData.getJSONObject("cursor");
				//	int currentPageIndex = cursorObjData.getInt("currentPageIndex");
				//	JSONArray pagesArray = cursorObjData.getJSONArray("pages");
					JSONArray jsonObjResultArray = jsonObjData
						.getJSONArray("results");
					for (int i = 0; i < jsonObjResultArray.length(); i++) {
						JSONObject jsonObjResult = jsonObjResultArray
								.getJSONObject(i);
	
						GoogleImageResult result = new GoogleImageResult(jsonObjResult.getString("title"), jsonObjResult.getString("unescapedUrl"));
						listData.add(result);
					}
					/*
					if (currentPageIndex == 0) {
						
						for (int i = 1; pagesArray.length() > 1 && i < pagesArray.length(); i++) {
							listData.addAll(requestImageSearch(key, width, height, i));
						}
					}
					*/
				}

			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return listData;
	}

	public Bitmap getImage(String url) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		byte[] buf = new byte[512];
		int bytes;

		try {
			in = new BufferedInputStream(new URL(url).openStream(), 512);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, 512);
			while ((bytes = in.read(buf)) > 0) {
				out.write(buf, 0, bytes);
			}

			out.flush();

			final byte[] data = dataStream.toByteArray();
			BitmapFactory.Options options = new BitmapFactory.Options();

			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					options);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}
}
