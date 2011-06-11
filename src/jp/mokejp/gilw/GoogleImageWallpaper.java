package jp.mokejp.gilw;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class GoogleImageWallpaper extends WallpaperService {
	
	public static final String SHARED_PREFS_NAME = "gilwsettings";
	
	@Override
	public Engine onCreateEngine() {
		return new GoogleImageWallpaperEngine();
	}

	class GoogleImageWallpaperEngine extends Engine
		implements SharedPreferences.OnSharedPreferenceChangeListener {

		private final Paint mPaint = new Paint();
		private final GoogleImage mImage = new GoogleImage();
		private SharedPreferences mPref;
		private ArrayList<GoogleImageResult> mImageList;
		private Bitmap mBitmap;
		private Handler mHandler = new Handler();
		private Runnable mRunnable;
		
		private int mWidth;
		private int mHeight;
		private int mDrawWidth;
		private int mDrawHeight;
		private String mKeyword;
		private String mSafe;
		private float mImageOffset = 0;
		
		GoogleImageWallpaperEngine() {

		}

		/**
		 * 設定情報を更新します。
		 */
		private void updateConfigure() {
			// 検索キーワード
			mKeyword = mPref.getString(GoogleImageWallpaperSettings.SETTING_KEYWORD, "");
			// セーフサーチ
			mSafe = mPref.getString(GoogleImageWallpaperSettings.SETTING_SAFE, "");
			// 画像の幅
			String width = mPref.getString(GoogleImageWallpaperSettings.SETTING_WIDTH, "");
			if ("".equals(width)) {
				mWidth = this.getDesiredMinimumWidth();
			} else {
				mWidth = Integer.parseInt(width);
			}
			// 画像の高さ
			String height = mPref.getString(GoogleImageWallpaperSettings.SETTING_HEIGHT, "");
			if ("".equals(height)) {
				mHeight = this.getDesiredMinimumHeight();
			} else {
				mHeight = Integer.parseInt(height);
			}
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
	        android.os.Debug.waitForDebugger();
			
	        mPref = getSharedPreferences(SHARED_PREFS_NAME, android.content.Context.MODE_WORLD_READABLE);
	        mPref.registerOnSharedPreferenceChangeListener(this);
	        
	        updateConfigure();
			
			mRunnable = new Runnable() {
				@Override
				public void run() {
					if (mImageList != null && mImageList.size() == 0) {
						Toast.makeText(getApplicationContext(), R.string.gilw_message_isempty, android.widget.Toast.LENGTH_LONG);
					} else {
						updateImage(mBitmap);
					}
				}
			};
			
			beginUpdateThread();
		}
		
		private void beginUpdateThread() {
			// 画像更新スレッド
			new Thread(new Runnable() {

				@Override
				public void run() {
					mBitmap = null;
					mHandler.post(mRunnable);
					mImageList = mImage.requestImageSearch(mKeyword, mWidth, mHeight, mSafe, 0);
					mBitmap = getImage();
					// 画像更新
					mHandler.post(mRunnable);
				}
				
			}).start();
		}
		
		@Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
			//if (mImageList != null && mImageList.size() > 0) {
			//	changeImage(holder);
			//}
            mDrawWidth = width;
            mDrawHeight = height;
        }
		
		private Bitmap getImage() {
			double rand = Math.random();
			rand *= 1000.0d;
			if (mImageList.size() != 0) {
				return mImage.getImage((String)mImageList.get((int)rand % mImageList.size()).getUrl());
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
                    		int x = getDesiredMinimumWidth() / 2 - bitmap.getWidth() / 2;
                    		int y = getDesiredMinimumHeight() / 2 - bitmap.getHeight() / 2;
                    		
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
                    c.restore();
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
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