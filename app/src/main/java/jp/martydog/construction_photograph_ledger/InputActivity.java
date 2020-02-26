package jp.martydog.construction_photograph_ledger;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int CHOOSER_REQUEST_CODE = 100;
    private EditText editText;
    private ImageView imageView;
    private Button button;
    private Uri mPictureUri;
    private Picture picture;
    private int picId;
    private Realm realm;
    private RealmManager realmManager = new RealmManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        editText = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);
        Intent intent = getIntent();
        picId = intent.getIntExtra(MainActivity.EXTRA_PIC, -1);
        /*
        RealmConfiguration config2 = new RealmConfiguration.Builder()
                .name("default1.realm")
                .schemaVersion(2)
                .migration(new Migration())
                .build();
        */
        realm = realm.getDefaultInstance();
        picture = realm.where(Picture.class).equalTo("id", picId).findFirst();
        realm.close();

        if (picId != -1) {
            editText.setText(picture.getText());
            Bitmap bmp = null;
            byte[] bytes = picture.getBitmapArray();
            if (bytes != null) {
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
            imageView.setImageBitmap(bmp);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // パーミッションの許可状態を確認する
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // 許可されている
                        showChooser();
                    } else {
                        // 許可されていないので許可ダイアログを表示する
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

                        return;
                    }
                } else {
                    showChooser();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                addPicture();
                if (picture.getBitmapArray() == null) {
                    Toast toast = Toast.makeText(getBaseContext(), "写真を選択してください", Toast.LENGTH_SHORT);
                    toast.show();
                    showChooser();
                } else {
                    finish();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != RESULT_OK) {
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            // 画像を取得
            Uri uri = (data == null || data.getData() == null) ? mPictureUri : data.getData();

            // URIからBitmapを取得する
            Bitmap image;
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                return;
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight); // (1)

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);

            // BitmapをImageViewに設定する
            imageView.setImageBitmap(resizedImage);

            mPictureUri = null;
        }
    }
/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Toast toast = Toast.makeText(getBaseContext(), "写真を選択してください", Toast.LENGTH_SHORT);
            toast.show();
            showChooser();

            finish();
            return false;


        }
        return super.onKeyDown(keyCode, event);
    };
*/

    @Override
    public void onBackPressed() {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

        if (drawable == null || picId != -1) {
            finish();

        } else {
            Toast toast = Toast.makeText(getBaseContext(), "入力完了を押してください", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void addPicture() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        if (picture == null) {
            picture = new Picture();
            RealmResults<Picture> realmResults = realm.where(Picture.class).findAll();
            int identifier;
            if (realmResults.max("id") != null) {
                identifier = realmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            picture.setId(identifier);
        }


        // 添付画像を取得する
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            byte[] bytes;
            if (bitmapString != null) {
                bytes = Base64.decode(bitmapString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            picture.setBitmapArray(bytes);
        }

        if (drawable == null) {
            realm.commitTransaction();
            realm.close();
            return;
        } else {
            String text = editText.getText().toString();
            picture.setText(text);
        }

        realm.copyToRealmOrUpdate(picture);
        realm.commitTransaction();

        realm.close();

    }

    private void showChooser() {
        // ギャラリーから選択するIntent
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // カメラで撮影するIntent
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPictureUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
    }
}
