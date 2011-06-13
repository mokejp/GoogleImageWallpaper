package jp.mokejp.gilw;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class GoogleImageWallpaper extends WallpaperService {

	public static final String SHARED_PREFS_NAME = "gilwsettings";

	@Override
	public Engine onCreateEngine() {
		return new GoogleImageWallpaperEngine();
	}

	class GoogleImageWallpaperEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private static final int RETRY_INTERVAL = 1000;
		private final Paint mPaint = new Paint();
		private final GoogleImage mImage = new GoogleImage();
		private SharedPreferences mPref;
		private ArrayList<GoogleImageResult> mImageList;
		private Bitmap mBitmap;
		private Handler mHandler = new Handler();
		private Runnable mRunnable;
		private Thread mThread;

		private int mWidth;
		private int mHeight;
		private int mDrawWidth;
		private int mDrawHeight;
		private String mKeyword;
		private String mSafe;
		private int mRefreshInterval;
		private float mImageOffset = 0;
		private String mMessage;

		GoogleImageWallpaperEngine() {
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(24);
			mPaint.setColor(Color.LTGRAY);
		}

		/**
		 * 設定情報を更新します。
		 */
		private void updateConfigure() {
			// 検索キーワード
			mKeyword = mPref.getString(
					GoogleImageWallpaperSettings.SETTING_KEYWORD, "");
			if ("".equals(mKeyword)) {
				mKeyword = getText(R.string.gilw_settings_default_keyword)
						.toString();
			}
			// セーフサーチ
			mSafe = mPref.getString(GoogleImageWallpaperSettings.SETTING_SAFE,
					"");
			// 更新頻度
			String interval = mPref.getString(GoogleImageWallpaperSettings.SETTING_REFRESHINTERVAL, "");
			if ("".equals(interval)) {
				mRefreshInterval = 0;
			} else {
				mRefreshInterval = Integer.parseInt(interval);
			}
			// 画像の幅
			String width = mPref.getString(
					GoogleImageWallpaperSettings.SETTING_WIDTH, "");
			if ("".equals(width)) {
				mWidth = this.getDesiredMinimumWidth();
			} else {
				mWidth = Integer.parseInt(width);
			}
			// 画像の高さ
			String height = mPref.getString(
					GoogleImageWallpaperSettings.SETTING_HEIGHT, "");
			if ("".equals(height)) {
				mHeight = this.getDesiredMinimumHeight();
			} else {
				mHeight = Integer.parseInt(height);
			}
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			// android.os.Debug.waitForDebugger();

			mPref = getSharedPreferences(SHARED_PREFS_NAME,
					android.content.Context.MODE_WORLD_READABLE);
			mPref.registerOnSharedPreferenceChangeListener(this);

			updateConfigure();

			mRunnable = new Runnable() {
				@Override
				public void run() {
					updateImage(mBitmap);
				}
			};
			beginUpdateThread();
		}

		private void beginUpdateThread() {
			// 画像更新スレッド
			if (mThread != null && mThread.isAlive()) {
				// kill thread
				mThread.stop();
			}
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					mImageList = null;
					mBitmap = null;
					mMessage = getText(R.string.gilw_message_loading).toString();
					mHandler.removeCallbacks(mRunnable);
					mHandler.post(mRunnable);
					try {
						mImageList = mImage.requestImageSearch(mKeyword,
								mWidth, mHeight, mSafe, 0);
						if (mImageList.size() == 0) {
							mMessage = getText(R.string.gilw_message_notfound).toString();
						} else {
							mMessage = null;
						}
						mBitmap = getImage();
						if (mBitmap == null) {
							mMessage = getText(R.string.gilw_message_notfound).toString();
						} else {
							mMessage = null;
						}
						// 画像更新
						mHandler.removeCallbacks(mRunnable);
						mHandler.post(mRunnable);
						if (mRefreshInterval != 0) {
							mHandler.removeCallbacks(this);
							mHandler.postDelayed(this, 60000 * mRefreshInterval);
						}
					} catch (GoogleImageException ex) {
						mBitmap = null;
						mImageList = null;
						mHandler.removeCallbacks(this);
						mHandler.postDelayed(this, RETRY_INTERVAL);
					}
				}
			});
			mThread.start();
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			// if (mImageList != null && mImageList.size() > 0) {
			// changeImage(holder);
			// }
			mDrawWidth = width;
			mDrawHeight = height;
		}

		private Bitmap getImage() {
			double rand = Math.random();
			rand *= 1000.0d;
			if (mImageList.size() != 0) {
				return mImage.getImage((String) mImageList.get(
						(int) rand % mImageList.size()).getUrl());
			} else {
				return null;
			}
		}

		private void updateImage(Bitmap bitmap) {

			SurfaceHolder holder = this.getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					c.drawColor(android.graphics.Color.BLACK);

					if (bitmap != null) {
						if (mDrawWidth < bitmap.getWidth()) {
							// スクロールあり
							int x = getDesiredMinimumWidth() / 2
									- bitmap.getWidth() / 2;
							int y = getDesiredMinimumHeight() / 2
									- bitmap.getHeight() / 2;

							Matrix matrix = new Matrix();
							matrix.postScale(1, 1);

							matrix.setTranslate(x + mImageOffset, y);
							c.drawBitmap(bitmap, matrix, mPaint);
						} else {
							// スクロールなし
							int x = mDrawWidth / 2 - bitmap.getWidth() / 2;
							int y = mDrawHeight / 2 - bitmap.getHeight() / 2;
							c.drawBitmap(bitmap, x, y, mPaint);
						}
					} 
					if (mMessage != null) {
						float textWidth = mPaint.measureText(mMessage);

						int x = mDrawWidth / 2 - (int) textWidth / 2;
						int y = mDrawHeight / 2;
						c.drawText(mMessage, 0, mMessage.length(), x, y,
								mPaint);
					}
					c.restore();
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
		}


		@Override
		public void onDestroy() {
			super.onDestroy();
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
			mImageOffset = xPixels;
			updateImage(mBitmap);
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			updateConfigure();

			if (mHandler != null && mRunnable != null) {
				beginUpdateThread();
			}
		}

	}
}