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
				/* ����һ��File���������洢����ͷ���µ���Ƭ File�๹�췽�������������� ��SD��·�� ͨ��Environment�����getExternalStorageDirectory()������ȡsd�ĸ�Ŀ¼
				 * �� �ļ�����*/
				File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
				try {
					if (outputImage.exists()) {//�ж��ļ��Ƿ���ڣ������ھ�ɾ����
						outputImage.delete();
					}
					outputImage.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
				/* ����Uri��fromFile()������File����ת����Uri���� */
				imageUri = Uri.fromFile(outputImage);
				Intent intent = new Intent ("android.media.action.IMAGE_CAPTURE");//��������Intent������ƥ������,IMAGE_CAPTURE��ʾ���������
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//����Intent��putExtra()��������������Ƭ�����·������File�����·��һ����
				startActivityForResult(intent, TAKE_PHOTO);//startActivityForResult��ʽ���������������� ����1����Intent���󣬲���2����һ���������ص��Զ���ֵ����onActivityResult�����ж�
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
				//�÷���4.4�����Ѿ��ϳ�����ȡ��Uir���Ǿ���·���ˡ�
				/*imageUri = Uri.fromFile(outputImage);
				Intent intent = new Intent("android.intent.action.GET_CONTENT");
				intent.setType("image/*");//mime���� ������ͼƬ��ѡȡ
				intent.putExtra("crop", true);//�ɲü�
				intent.putExtra("scale", true);//��������
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, CROP_PHOTO);*/
				        Intent intent = new Intent("android.intent.action.GET_CONTENT"); 
				        //intent.addCategory(Intent.CATEGORY_OPENABLE);//��֪������� --��
				        
				        intent.setType("image/*");    
				        startActivityForResult(intent, CROP_PHOTO_TWO);  
			}
		});
	}
			
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.d("MainActivity", "requestCode: " + requestCode);
		/* stratActivityForResult��ʽ�����Ļ�����պ���и�������ص�onActivityResult�����С�*/
		/* �ü�����ٴη��ص�onActivityResult()�����д�ʱ*/
		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {//������ճɹ�
				Intent intent = new Intent("com.android.camera.action.CROP");//����һ����ʽIntent���ĺõ���Ƭ���òü����ü���
				intent.setDataAndType(imageUri, "image/*");//��Ҫ����·����Ҫ����MIME���� ��setDataAndType
				intent.putExtra ("scale", true);
				intent.putExtra (MediaStore.EXTRA_OUTPUT, imageUri);//���òü�����Ƭ�Ĵ��·������������Ƭ��·��һ����
				startActivityForResult (intent, CROP_PHOTO_ONE);//�����ü�����ͬ����startActivityForResult��ʽ������ 
			}
			break;
		case CROP_PHOTO_ONE:		
			//Log.d("MainActivity", "requestCode: " + resultCode);
			if (resultCode == RESULT_OK) {//�ж� ���ص��Զ���ֵ������ü��ɹ����ͽ���Ƭ��ImageView����ʾ��
				try {
					 /*����������Լ�����������Ƭ֪����ʵ·���������ѡȡ�����������������Ϊ��ȡ��·��������ʵ·���������·����4.4�Ķ��ģ�����BitmapFactory�е�decodeStream()�������ü������Ƭ������Bitmap����*/
					Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
					picture.setImageBitmap(bitmap);//�ü��õ���Ƭ��ʾ������
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
					
			}
			break;
		case CROP_PHOTO_TWO:
			Log.d("MainActivity", "ѡͼ��Ľ��: " + resultCode);
			if (resultCode == RESULT_OK) {
				String imagePath = getPath(MainActivity.this, data.getData()); //4.4�Ժ�ķ����������Զ��巽����ȡ��ʵ·����
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
			Log.d("MainActivity", "�ü���Ľ��: " + resultCode);
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
	 * ����Uri��ȡͼƬ����·�������Android4.4���ϰ汾Uriת��
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
