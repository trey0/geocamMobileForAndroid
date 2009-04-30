package gov.nasa.arc.geocam;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class CameraPreviewActivity extends Activity {

	private static final int DIALOG_ANNOTATE_PHOTO = 1;
	private static final int DIALOG_DELETE_PHOTO = 2;

	private Uri mImageUri;
	private JSONObject mImageData;
	private String mImageNote;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Window and view properties
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_preview);

		// Load bitmap from intent data and display in imageview
		mImageUri = getIntent().getData();
		try {
			mImageData = new JSONObject(getIntent().getExtras().getString("data"));
		} catch (JSONException e1) {
			Log.d(GeoCamMobile.DEBUG_ID, "Error unserializing JSON data from intent");
			mImageData = new JSONObject();
		}
		Bitmap bitmap;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
			ImageView imageView = (ImageView)findViewById(R.id.camera_preview_imageview);
			imageView.setAdjustViewBounds(true);
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			imageView.setImageBitmap(bitmap);

		} catch (FileNotFoundException e) {
			Log.d(GeoCamMobile.DEBUG_ID, "Error loading bitmap in CameraPreviewActivity");
		} catch (IOException e) {
			Log.d(GeoCamMobile.DEBUG_ID, "Error loading bitmap in CameraPreviewActivity");
		}

		// Buttons
		final ImageButton annotateButton = (ImageButton)findViewById(R.id.camera_preview_annotate_button);
		annotateButton.setImageDrawable(getResources().getDrawable(R.drawable.annotate));
		annotateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CameraPreviewActivity.this.showDialog(DIALOG_ANNOTATE_PHOTO);
			}			
		});
		
		final ImageButton deleteButton = (ImageButton)findViewById(R.id.camera_preview_delete_button);
		deleteButton.setImageDrawable(getResources().getDrawable(R.drawable.delete));
		deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CameraPreviewActivity.this.showDialog(DIALOG_DELETE_PHOTO);
			}			
		});
		
		final ImageButton saveButton = (ImageButton)findViewById(R.id.camera_preview_save_button);
		saveButton.setImageDrawable(getResources().getDrawable(R.drawable.save));
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CameraPreviewActivity.this.finish();
			}			
		});

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();		
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_ANNOTATE_PHOTO:
	        LayoutInflater factory = LayoutInflater.from(this);
	        final View textEntryView = factory.inflate(R.layout.camera_preview_annotate, null);
			return new AlertDialog.Builder(this)
	        .setTitle(R.string.camera_preview_annotate_dialog_title)
	        .setView(textEntryView)
	        .setPositiveButton(R.string.camera_dialog_ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	mImageNote = ((EditText)textEntryView.findViewById(R.id.camera_preview_annotate_edittext)).getText().toString();
	            	Log.d(GeoCamMobile.DEBUG_ID, "Setting image note to: " + mImageNote);
	            	annotatePhoto();
	            }
	        })
	        .setNegativeButton(R.string.camera_dialog_cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        })			
			.create();
			
		case DIALOG_DELETE_PHOTO:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.camera_delete_dialog_title)
			.setPositiveButton(R.string.camera_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
	            	deletePhoto();
	            	CameraPreviewActivity.this.finish();
				}
			})
			.setNegativeButton(R.string.camera_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.create();
			
			default:
				break;
		}
		return null;
	}

	private void annotatePhoto() {
		try {
			mImageData.put("note", mImageNote);
			Log.d(GeoCamMobile.DEBUG_ID, "Saving image with data: " + mImageData.toString());
		}
		catch (JSONException e) {
			Log.d(GeoCamMobile.DEBUG_ID, "Error while adding annotation to image");
		}

		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DESCRIPTION, mImageData.toString());
		getContentResolver().update(mImageUri, values, null, null);
		Log.d(GeoCamMobile.DEBUG_ID, "Updating " + mImageUri.toString() + " with values " + values.toString());
		
		this.finish();
	}
	
	private void deletePhoto() {
		Log.d(GeoCamMobile.DEBUG_ID, "Deleting photo with Uri: " + mImageUri.toString());
		getContentResolver().delete(mImageUri, null, null);
	}
}
