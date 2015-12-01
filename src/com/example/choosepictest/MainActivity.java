package com.example.choosepictest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.example.choospictest.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	public static final int TAKE_PHOTO = 1;
	public static final int CROP_PHOTO_ONE = 2;
	public static final int CROP_PHOTO_TWO = 3;
	public static final int SET_IMAGE_VIEW = 4;
	private Button takePhoto;
	private Button chooseFromAlbum;
	private ImageView picture;
	private Uri imageUri;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		picture = (ImageView) findViewById (R.id.picture);
		takePhoto = (Button) findViewById (R.id.take_photo);
		takePhoto.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				/* 创建一个File对象，用来存储摄像头拍下的照片 File类构造方法接收两个参数 ①SD卡路径 通过Environment对象的getExternalStorageDirectory()方法获取sd的根目录
				 * ② 文件名称*/
				File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
				try {
					if (outputImage.exists()) {//判断文件是否存在，弱存在就删除。
						outputImage.delete();
					}
					outputImage.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
				/* 调用Uri的fromFile()方法将File对象转化成Uri对象。 */
				imageUri = Uri.fromFile(outputImage);
				Intent intent = new Intent ("android.media.action.IMAGE_CAPTURE");//设置隐形Intent启动的匹配条件,IMAGE_CAPTURE表示启动相机。
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//调用Intent的putExtra()方法设置拍下照片储存的路径，跟File储存的路径一样。
				startActivityForResult(intent, TAKE_PHOTO);//startActivityForResult方式启动活动。即启动相机 参数1传入Intent对象，参数2传入一个用来返回的自定义值。在onActivityResult中做判断
			}
		});
		chooseFromAlbum = (Button) findViewById (R.id.choose_from_album);
		chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
				try {
					if (outputImage.exists()){
						outputImage.delete();
					} 
						outputImage.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//该方法4.4以上已经废除，获取的Uir不是绝对路径了。
				/*imageUri = Uri.fromFile(outputImage);
				Intent intent = new Intent("android.intent.action.GET_CONTENT");
				intent.setType("image/*");//mime类型 从所有图片中选取
				intent.putExtra("crop", true);//可裁剪
				intent.putExtra("scale", true);//允许缩放
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, CROP_PHOTO);*/
				        Intent intent = new Intent("android.intent.action.GET_CONTENT"); 
				        //intent.addCategory(Intent.CATEGORY_OPENABLE);//不知道干嘛的 --！
				        
				        intent.setType("image/*");    
				        startActivityForResult(intent, CROP_PHOTO_TWO);  
			}
		});
	}
			
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.d("MainActivity", "requestCode: " + requestCode);
		/* stratActivityForResult方式启动的活动拍完照后会有个结果返回到onActivityResult方法中。*/
		/* 裁剪完后再次返回到onActivityResult()方法中此时*/
		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {//如果拍照成功
				Intent intent = new Intent("com.android.camera.action.CROP");//构建一个隐式Intent将拍好的照片调用裁剪器裁剪。
				intent.setDataAndType(imageUri, "image/*");//即要设置路径又要设置MIME类型 用setDataAndType
				intent.putExtra ("scale", true);
				intent.putExtra (MediaStore.EXTRA_OUTPUT, imageUri);//设置裁剪完照片的存放路径。与拍完照片的路径一样。
				startActivityForResult (intent, CROP_PHOTO_ONE);//启动裁剪程序，同样是startActivityForResult方式启动。 
			}
			break;
		case CROP_PHOTO_ONE:		
			//Log.d("MainActivity", "requestCode: " + resultCode);
			if (resultCode == RESULT_OK) {//判断 返回的自定义值，如果裁剪成功。就将照片在ImageView中显示。
				try {
					 /*相机调用是自己创建的新照片知道真实路径。从相册选取不能用这个方法，因为获取的路径不是真实路径而是相对路径（4.4改动的）。用BitmapFactory中的decodeStream()方法将裁剪后的照片解析成Bitmap对象。*/
					Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
					picture.setImageBitmap(bitmap);//裁剪好的照片显示出来。
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
					
			}
			break;
		case CROP_PHOTO_TWO:
			Log.d("MainActivity", "选图后的结果: " + resultCode);
			if (resultCode == RESULT_OK) {
				String imagePath = getPath(MainActivity.this, data.getData()); //4.4以后的方法。调用自定义方法获取真实路径。
				Uri imageUri = Uri.fromFile(new File(imagePath));
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imageUri, "image/*");
				intent.putExtra ("crop" , true);
				intent.putExtra ("scale", true);
				intent.putExtra("return-data", false);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "output_image.jpg")));
				startActivityForResult (intent, SET_IMAGE_VIEW);
			}
			break;
		case SET_IMAGE_VIEW:
			Log.d("MainActivity", "裁剪后的结果: " + resultCode);
			if (resultCode == RESULT_OK) {
				try {
					Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "output_image.jpg"))));
					picture.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
	}
	/**
	 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
	 * @param activity
	 * @param imageUri
	 * @author yaoxing
	 * @date 2014-10-12
	 */
	@TargetApi(19)
	public static String getPath(Activity context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} // MediaStore (and general)
		else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(imageUri))
				return imageUri.getLastPathSegment();
			return getDataColumn(context, imageUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
}
