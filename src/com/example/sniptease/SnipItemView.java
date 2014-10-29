package com.example.sniptease;

import java.io.IOException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class SnipItemView extends ImageView {
	Bitmap modifierBit;
	Context context;
	String captionText;
	Paint style;
	Boolean isIncoming, isRead, hasQuestion, isCorrect;
	String withUser, snipUrl, question, correctAnswer;
	int snipId;
	RectF captionBackRect;

	public SnipItemView(Context context, boolean isIncoming, boolean isRead,
			boolean hasQuestion, boolean isCorrect, String withUser,
			String snipUrl, String question, String correctAnswer, int snipId) {
		super(context);
		this.context = context;
		this.isIncoming = isIncoming;
		this.isRead = isRead;
		this.hasQuestion = hasQuestion;
		this.isCorrect = isCorrect;
		this.withUser = withUser;
		this.snipUrl = snipUrl;
		this.question = question;
		this.correctAnswer = correctAnswer;
		this.snipId = snipId;
		captionText = getCaptionText();
		modifierBit = getStatusIconBitmap();
		style = getCaptionStyle();
		GetSnipBitmapTask getSnipBitmap = new GetSnipBitmapTask();
		getSnipBitmap.execute();

	}

	Bitmap finalOverlayBitmap(Bitmap baseBit) {
		Bitmap mutableBitmap = baseBit.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(mutableBitmap);

		if (modifierBit != null) {
			canvas.drawBitmap(modifierBit, 0, 0, null);
		}
		captionBackRect = new RectF(0, Float.valueOf(baseBit.getHeight()
				- (baseBit.getHeight()) / 8),
				Float.valueOf(baseBit.getWidth()), Float.valueOf(baseBit
						.getHeight()));
		Paint rectPaint = new Paint();
		if (isIncoming) {
			rectPaint.setColor(Color.WHITE);
		} else {
			rectPaint.setColor(Color.BLACK);
		}
		canvas.drawRoundRect(captionBackRect, 5, 5, rectPaint);

		canvas.drawText(captionText, baseBit.getWidth() / 10,
				mutableBitmap.getHeight() - 5, style);

		return mutableBitmap;

	}

	Paint getCaptionStyle() {
		Paint paint = new Paint();
		if (isIncoming) {

			paint.setColor(Color.BLACK);

		} else {

			paint.setColor(Color.WHITE);

		}
		if (!isRead) {
			paint.setTypeface(Typeface.DEFAULT_BOLD);

		}
		paint.setStyle(Style.FILL);
		paint.setTextSize(20);
		return paint;
	}

	Bitmap getStatusIconBitmap() {
		if (!isRead && !isIncoming) {
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.pending_icon);
		} else if (isRead && isCorrect) {
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.correct_icon);
		} else if (isRead && !isCorrect) {
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.wrong_icon);
		} else {
			return null;
		}
	}

	String getCaptionText() {
		String captionString;
		TextView thisTextView = new TextView(context);
		thisTextView.setPadding(40, 0, 40, 0);
		if (isIncoming) {

			captionString = "Snip from " + withUser;
			thisTextView.setText(captionString);
			thisTextView.setTextColor(0xFFFFFF);
			thisTextView.setBackgroundColor(0x000000);

		} else {
			captionString = "Snip to " + withUser;
			thisTextView.setText(captionString);
			thisTextView.setTextColor(0x000000);
			thisTextView.setBackgroundColor(0xFFFFFF);
		}
		if (!isRead) {
			thisTextView.setTypeface(null, Typeface.BOLD);
		}
		thisTextView.setDrawingCacheEnabled(true);

		return captionString;
	}

	public class GetSnipBitmapTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap snipImageBaseBitmap;

			try {
				String urlString = "http://23.253.210.151/uploads/snipicons/"
						+ snipUrl;
				if (urlString.contains(".mov")) {
					Log.d("snip url string before", urlString);
					urlString = urlString.replace(".mov", ".jpg");
					Log.d("snip url string after", urlString);
				}

				
				if (!isRead && hasQuestion && isIncoming) {

					// If is not read && is a question
					snipImageBaseBitmap = BitmapFactory.decodeResource(
							getContext().getResources(),
							R.drawable.question_mark_icon);
				} else {
					URL url = new URL(urlString);
					snipImageBaseBitmap = BitmapFactory.decodeStream(url
							.openConnection().getInputStream());
				}

			} catch (IOException e) {
				snipImageBaseBitmap = null;
				Log.e("ERROR", e.toString());
				e.printStackTrace();

			}

			snipImageBaseBitmap = Bitmap.createScaledBitmap(
					snipImageBaseBitmap, 200, 200, false);

			return snipImageBaseBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bp) {
			setPadding(5, 5, 5, 5);

			setCropToPadding(true);
			setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			if (isIncoming) {
				setBackgroundColor(Color.WHITE);
			} else {
				setBackgroundColor(Color.BLACK);
			}
			setClickable(true);
			try{
				JSONObject snipInfoObject = new JSONObject();
				snipInfoObject.put("isIncoming", isIncoming);
				snipInfoObject.put("hasQuestion", hasQuestion);
				snipInfoObject.put("isRead", isRead);
				if(hasQuestion){
					snipInfoObject.put("question", question);
					snipInfoObject.put("correctAnswer",correctAnswer);
				}

				snipInfoObject.put("snip_id", snipId);
				setTag(snipInfoObject);
				}
				catch(JSONException e){
					e.printStackTrace();
				}
			
			setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent;

					if (isIncoming && hasQuestion && !isRead) {
						intent = new Intent(context,
								QuestionViewerActivity.class);
						Log.d("value of has question", hasQuestion.toString());
					} else {
						intent = new Intent(context, SnipViewerActivity.class);
						Log.d("value of has question", hasQuestion.toString());
					}
					if (hasQuestion) {
						Log.d("value of has question", hasQuestion.toString());
						intent.putExtra("question", question);
						intent.putExtra("correctAnswer", correctAnswer);
					} else {
						Log.d("value of has question", hasQuestion.toString());
					}
					intent.putExtra("snip_id", snipId);
					context.startActivity(intent);
					
				}
			});
			setImageBitmap(finalOverlayBitmap(bp));
		}

	}
}
